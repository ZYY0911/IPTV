package com.yingyuzhang.iptv.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtil {
    private const val PREFS_NAME = "tv_prefs"
    const val TV_INDEX_NUM = "tv_index_num"
    const val TV_UPDATE_V6 = "tv_update_v6"
    const val TV_UPDATE_V4 = "tv_update_v4"
    const val FIRST_INIT = "is_first"
    const val TvListUrl6 = "tvListUrl6"
    const val TvFileName6 = "tvFileName6"
    const val TvListUrl4 = "tvListUrl4"
    const val TvFileName4 = "tvFileName4"
    const val IS_IPV6 = "is_ipv6"
    const val IS_AUTO_UPDATE = "is_auto_update"
    const val SHOW_RATIO = "show_ratio"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun checkInitialization() {
        if (!::sharedPreferences.isInitialized) {
            throw IllegalStateException("SharedPreferencesUtil is not initialized. Call init() first.")
        }
    }

    fun putString(key: String, value: String) {
        checkInitialization()
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        checkInitialization()
        return sharedPreferences.getString(key, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        checkInitialization()
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        checkInitialization()
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        checkInitialization()
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun putBooleanAfterFinish(key: String, value: Boolean) {
        checkInitialization()
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        checkInitialization()
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        checkInitialization()
        val editor = sharedPreferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        checkInitialization()
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        checkInitialization()
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        checkInitialization()
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun remove(key: String) {
        checkInitialization()
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun clear() {
        checkInitialization()
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.commit()
    }
}
