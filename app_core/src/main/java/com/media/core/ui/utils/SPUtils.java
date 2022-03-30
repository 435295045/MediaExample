package com.media.core.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.alibaba.fastjson.JSON;
import com.web.socket.utils.LoggerUtils;

import java.util.Map;
import java.util.Set;


public class SPUtils {

    private static final String dataName = "sp_media_data";

    /**
     * 获取默认偏好设置编辑器Editor
     *
     * @param context
     * @return
     */
    private static Editor getEditor(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.edit();
    }

    /**
     * 获取偏好设置SharedPreferences
     *
     * @param context
     * @return
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(dataName, Context.MODE_PRIVATE);
    }

    /**
     * 存储偏好设置键值对
     *
     * @param context
     * @param key
     * @param value   默认值
     */
    public static void putInt(Context context, String key, Integer value) {
        Editor editor = getEditor(context);
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * 存储偏好设置键值对
     *
     * @param context
     * @param key
     * @param value   默认值
     */
    public static void putFloat(Context context, String key, Float value) {
        Editor editor = getEditor(context);
        editor.putFloat(key, value);
        editor.commit();
    }

    /**
     * 存储偏好设置键值对
     *
     * @param context
     * @param key
     * @param value   默认值
     */
    public static void putLong(Context context, String key, Long value) {
        Editor editor = getEditor(context);
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * 存储偏好设置键值对
     *
     * @param context
     * @param key
     * @param value   默认值
     */
    public static void putBoolean(Context context, String key, Boolean value) {
        Editor editor = getEditor(context);
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 存储偏好设置键值对
     *
     * @param context
     * @param key
     * @param value   默认值
     */
    public static void putString(Context context, String key, String value) {
        Editor editor = getEditor(context);
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 存储偏好设置键值对集合
     *
     * @param context
     * @param key
     * @param values
     */
    public static void putStringSet(Context context, String key, Set<String> values) {
        Editor editor = getEditor(context);
        editor.putStringSet(key, values);
        editor.commit();
    }


    /**
     * 获取指定偏好设置
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static Integer getInt(Context context, String key, Integer defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * 获取指定偏好设置
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static Float getFloat(Context context, String key, Float defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getFloat(key, defaultValue);
    }

    /**
     * 获取指定偏好设置
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static Long getLong(Context context, String key, Long defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getLong(key, defaultValue);
    }

    /**
     * 获取指定偏好设置
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static Boolean getBoolean(Context context, String key, Boolean defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * 获取指定偏好设置
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * 获取指定偏好设置
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static Set<String> getStringSet(Context context, String key, Set<String> defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getStringSet(key, defaultValue);
    }

    public static <T> void putClazz(Context context, String key, T t) {
        Editor editor = getEditor(context);
        editor.putString(key, JSON.toJSONString(t));
        editor.commit();
    }

    public static <T> T getClazz(Context context, String key, Class<T> clazz) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        T t = null;
        try {
            t = JSON.parseObject(sharedPreferences.getString(key, ""), clazz);
        } catch (Exception e) {
            LoggerUtils.e(e.getMessage(), e);
        }
        return t;
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     */
    public static void put(Context context, String key, Object object) {
        Editor editor = getEditor(context);
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.commit();
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    public static Object get(Context context, String key, Object defaultObject) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (defaultObject instanceof String) {
            return sharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /**
     * 获取所有偏好设置
     *
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Map<String, ?> all = sharedPreferences.getAll();
        return all;
    }

    /**
     * 删除指定偏好设置
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        Editor editor = getEditor(context);
        editor.remove(key);
        editor.commit();
    }

    /**
     * 清空所有偏好设置
     *
     * @param context
     */
    public static void clear(Context context) {
        Editor editor = getEditor(context);
        editor.clear();
        editor.commit();
    }

    /**
     * 判断键名对应值是否存在
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.contains(key);
    }

}
