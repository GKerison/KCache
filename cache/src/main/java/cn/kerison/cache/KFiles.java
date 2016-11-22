package cn.kerison.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static cn.kerison.cache.KPrefs.getString;

/**
 * Created by k on 2016/11/21.
 * 基于ACache做了部分修改
 * @see https://github.com/xufeifandj/Acache
 */

public class KFiles {

    public static final int TIME_HOUR = 60 * 60;
    public static final int TIME_DAY = TIME_HOUR * 24;

    private static final int MAX_SIZE = 1000 * 1000 * 50; // 50 MB
    private static final int MAX_COUNT = Integer.MAX_VALUE; // 不限制存放数据的数量

    private static KFiles mInstance;
    private KCacheManager mCache;

    public static KFiles get(){
        return mInstance;
    }

    public static KFiles init(Context ctx) {
        return init(ctx, "KCache");
    }

    public static KFiles init(Context ctx, String cacheName) {
        File f = new File(ctx.getFilesDir(), cacheName);
        return init(f, MAX_SIZE, MAX_COUNT);
    }

    public static KFiles init(File cacheDir) {
        return init(cacheDir, MAX_SIZE, MAX_COUNT);
    }

    public static synchronized KFiles init(File cacheDir, long maxSize, int maxCount) {
        if (mInstance == null) {
            mInstance = new KFiles(cacheDir, maxSize, maxCount);
        }
        return mInstance;
    }

    private KFiles(File cacheDir, long maxSize , int maxCount) {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new RuntimeException("can't make dirs in "
                    + cacheDir.getAbsolutePath());
        }
        mCache = new KCacheManager(cacheDir, maxSize, maxCount);
    }

    // =======================================
    // ============ 数据 读写 ==============
    // =======================================
    public void putString(String key, String value) {
        putString(key, value,-1);
    }

    public void putString(String key, String value, int saveTime) {
        putObject(key, value, saveTime);
    }

    /**
     * 获取字符串缓存
     * @param key
     * @return
     */
    public String getString(String key) {
        return (String) mCache.getObject(key);
    }

    public void putJSONObject(String key, JSONObject value) {
        putString(key, value.toString());
    }

    public void putJSONObject(String key, JSONObject value, int saveTime) {
        putString(key, value.toString(), saveTime);
    }

    /**
     * 获取JSON缓存
     * @param key
     * @return
     */
    public JSONObject getJSONObject(String key) {
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

    public void putJSONArray(String key, JSONArray value) {
        putString(key, value.toString());
    }

    public void putJSONArray(String key, JSONArray value, int saveTime) {
        putString(key, value.toString(), saveTime);
    }

    /**
     * 获取JSONArray缓存
     * @param key
     * @return
     */
    public JSONArray getJSONArray(String key) {
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
     * @param key
     * @param value
     * @return
     */
    public boolean put(String key, Serializable value) {
        return putObject(key, value, -1);
    }

    /**
     * 缓存一段时间的数据
     * @param key
     * @param value
     * @param saveTime 单位：s
     * @return
     */
    public boolean putObject(String key, Serializable value, int saveTime) {
       return mCache.saveObject(key, value, saveTime);
    }

    /**
     * 获取数据
     * @param key
     * @return 没有缓存或者缓存过期 返回null
     */
    public Object getObject(String key) {
        return mCache.getObject(key);
    }

    /**
     * 移除某个缓存
     *
     * @param key
     * @return 是否移除成功
     */
    public boolean remove(String key) {
        return mCache.remove(key);
    }

    /**
     * 清除所有缓存
     */
    public void clear() {
        mCache.clear();
    }
}
