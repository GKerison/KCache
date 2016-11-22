package cn.kerison.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by k on 2016/11/21.
 */

class KCacheManager {

    private final AtomicLong cacheSize;
    private final AtomicInteger cacheCount;

    private final long sizeLimit;
    private final int countLimit;

    private final Map<File, Long> lastUsageDates = Collections
            .synchronizedMap(new HashMap<File, Long>());
    protected File cacheDir;

    KCacheManager(File cacheDir, long sizeLimit, int countLimit) {
        this.cacheDir = cacheDir;
        this.sizeLimit = sizeLimit;
        this.countLimit = countLimit;
        cacheSize = new AtomicLong();
        cacheCount = new AtomicInteger();
        calculateCacheSizeAndCacheCount();
    }

    /**
     * 计算 cacheSize和cacheCount
     */
    private void calculateCacheSizeAndCacheCount() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = 0;
                int count = 0;
                File[] cachedFiles = cacheDir.listFiles();
                if (cachedFiles != null) {
                    for (File cachedFile : cachedFiles) {
                        size += cachedFile.length();
                        count += 1;
                        lastUsageDates.put(cachedFile,
                                cachedFile.lastModified());
                    }
                    cacheSize.set(size);
                    cacheCount.set(count);
                }
            }
        }).start();
    }

    /**
     * 保存对象
     * @param key
     * @param obj
     * @param duration
     * @return
     */
    boolean saveObject(String key,Serializable obj, long duration){
        File file = newFile(key);
        boolean isSuccess = false;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            if (duration > 0) {
                oos.writeLong(System.currentTimeMillis());
                oos.writeLong(duration);
            }else{
                oos.writeLong(-1);
                oos.writeLong(-1);
            }
            oos.writeObject(obj);
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        } finally {
            try {
                oos.flush();
                oos.close();
            } catch (IOException e) {
                isSuccess = false;
                e.printStackTrace();
            }
        }

        if (isSuccess) {
            put(file);
        }

        return isSuccess;
    }

    /**
     * 获取保存的对象
     * @param key
     * @return
     */
    Object getObject(String key) {
        File file = get(key);
        if (!file.exists())
            return null;

        Object result = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            Long saveTime = ois.readLong();
            Long duration = ois.readLong();
            if (duration == -1
                    || System.currentTimeMillis() < saveTime + duration * 1000) {
                result = ois.readObject();
            }else{
                remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    boolean remove(String key) {
        File file = get(key);
        lastUsageDates.remove(file);
        if (!file.exists()) {
            return true;
        }
        return file.delete();
    }

    void clear() {
        lastUsageDates.clear();
        cacheSize.set(0);
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if(f.exists()){
                    f.delete();
                }
            }
        }
    }

    private void put(File file) {
        int curCacheCount = cacheCount.get();
        while (curCacheCount + 1 > countLimit) {
            long freedSize = releaseCache();
            cacheSize.addAndGet(-freedSize);
//            cacheCount.addAndGet(-1);
        }
//        cacheCount.addAndGet(1);
        long valueSize = file.length();
        long curCacheSize = cacheSize.get();
        while (curCacheSize + valueSize > sizeLimit) {
            long freedSize = releaseCache();
            curCacheSize = cacheSize.addAndGet(-freedSize);
        }
        cacheSize.addAndGet(valueSize);

        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        lastUsageDates.put(file, currentTime);
    }

    private File get(String key) {
        File file = newFile(key);
        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        lastUsageDates.put(file, currentTime);
        return file;
    }

    private File newFile(String key) {
        return new File(cacheDir, key.hashCode() + "");
    }

    /**
     * 可优化移除策略
     *
     * @return
     */
    private long releaseCache() {
        if (lastUsageDates.isEmpty()) {
            return 0;
        }

        Long oldestUsage = null;
        File mostLongUsedFile = null;
        Set<Map.Entry<File, Long>> entries = lastUsageDates.entrySet();
        synchronized (lastUsageDates) {
            for (Map.Entry<File, Long> entry : entries) {
                if (mostLongUsedFile == null) {
                    mostLongUsedFile = entry.getKey();
                    oldestUsage = entry.getValue();
                } else {
                    Long lastValueUsage = entry.getValue();
                    if (lastValueUsage < oldestUsage) {
                        oldestUsage = lastValueUsage;
                        mostLongUsedFile = entry.getKey();
                    }
                }
            }
        }

        long fileSize = mostLongUsedFile.length();
        if (mostLongUsedFile.delete()) {
            lastUsageDates.remove(mostLongUsedFile);
        }
        return fileSize;
    }
}
