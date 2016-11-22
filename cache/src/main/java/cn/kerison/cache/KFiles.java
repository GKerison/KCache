package cn.kerison.cache;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;

/**
 * Created by k on 2016/11/21. 基于ACache做了部分修改
 *
 * @see https://github.com/xufeifandj/Acache
 */

public class KFiles {

    public static final int TIME_HOUR = 60 * 60;
    public static final int TIME_DAY = TIME_HOUR * 24;

    private static final int MAX_SIZE = 1000 * 1000 * 50; // 50 MB
    private static final int MAX_COUNT = Integer.MAX_VALUE; // 不限制存放数据的数量

    private static KCacheManager mCache;

    public static void init(Context ctx, String cacheName) {
        File f = new File(ctx.getFilesDir(), cacheName);
        init(f, MAX_SIZE, MAX_COUNT);
    }

    public static void init(File cacheDir) {
        init(cacheDir, MAX_SIZE, MAX_COUNT);
    }

    public static synchronized void init(File cacheDir, long maxSize, int maxCount) {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new RuntimeException("can't make dirs in "
                    + cacheDir.getAbsolutePath());
        }
        mCache = new KCacheManager(cacheDir, maxSize, maxCount);
    }

    // =======================================
    // ============ 数据 读写 ==============
    // =======================================
    public static void putString(String key, String value) {
        putString(key, value, -1);
    }

    public static void putString(String key, String value, int saveTime) {
        putObject(key, value, saveTime);
    }

    /**
     * 获取字符串缓存
     */
    public static String getString(String key) {
        return (String) mCache.getObject(key);
    }

    public static void putJSONObject(String key, JSONObject value) {
        putString(key, value.toString());
    }

    public static void putJSONObject(String key, JSONObject value, int saveTime) {
        putString(key, value.toString(), saveTime);
    }

    /**
     * 获取JSON缓存
     */
    public static JSONObject getJSONObject(String key) {
        String jsonString = getString(key);
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void putJSONArray(String key, JSONArray value) {
        putString(key, value.toString());
    }

    public static void putJSONArray(String key, JSONArray value, int saveTime) {
        putString(key, value.toString(), saveTime);
    }

    /**
     * 获取JSONArray缓存
     */
    public static JSONArray getJSONArray(String key) {
        String jsonString = getString(key);
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return new JSONArray(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 缓存数据
     */
    public static boolean putObject(String key, Serializable value) {
        return putObject(key, value, -1);
    }

    /**
     * 缓存一段时间的数据
     *
     * @param saveTime 单位：s
     */
    public static boolean putObject(String key, Serializable value, int saveTime) {
        return mCache.saveObject(key, value, saveTime);
    }

    /**
     * 获取数据
     *
     * @return 没有缓存或者缓存过期 返回null
     */
    public static Object getObject(String key) {
        return mCache.getObject(key);
    }

    /**
     * 移除某个缓存
     *
     * @return 是否移除成功
     */
    public static boolean remove(String key) {
        return mCache.remove(key);
    }

    /**
     * 清除所有缓存
     */
    public static void clear() {
        mCache.clear();
    }
}
