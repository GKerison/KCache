package cn.kerison.cache;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by k on 2016/11/21.
 */

public class KPrefs {

    private static SharedPreferences mPrefs;

    public static void init(Context context) {
        init(context, context.getPackageName().replace(".", "_"));
    }

    public static void init(Context context, String fileName) {
        mPrefs = context.getApplicationContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getPrefs() {
        return mPrefs;
    }

    public static SharedPreferences.Editor getEditor() {
        return mPrefs.edit();
    }

    public static boolean getBoolean(String key) {
        return mPrefs.getBoolean(key, false);
    }

    public static long getLong(String key) {
        return mPrefs.getLong(key, 0);
    }

    public static int getInt(String key) {
        return mPrefs.getInt(key, 0);
    }

    public static float getFloat(String key) {
        return mPrefs.getFloat(key, 0);
    }

    public static String getString(String key) {
        return mPrefs.getString(key, null);
    }

    public static Set<String> getStringSet(String key) {
        return mPrefs.getStringSet(key, null);
    }

    public static boolean hasSave(String key) {
        return mPrefs.contains(key);
    }

    public static boolean clear(String key) {
        return mPrefs.edit().remove(key).commit();
    }

    public static boolean clear(){
        return mPrefs.edit().clear().commit();
    }
}
