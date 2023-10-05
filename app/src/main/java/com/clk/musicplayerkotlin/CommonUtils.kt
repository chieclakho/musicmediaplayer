package com.clk.musicplayerkotlin

import android.content.Context
import android.content.SharedPreferences

class CommonUtils private constructor() {
    companion object {
        private  var instance: CommonUtils? = null
       private const val PREF_FILE = "pref_file"
        fun getInstance(): CommonUtils {
            if (instance == null) {
                instance = CommonUtils()
            }
            return instance!!
        }
    }



    fun savePref(key: String?, value: String?) {
        val pref: SharedPreferences =
            App.instance.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        pref.edit().putString(key, value).apply()
    }

    fun getPref(key: String?): String? {
        val pref: SharedPreferences =
            App.instance.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return pref.getString(key, null)
    }

    fun clearPref(key: String?) {
        val pref: SharedPreferences =
            App.instance.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        pref.edit().remove(key).apply()
    }
}