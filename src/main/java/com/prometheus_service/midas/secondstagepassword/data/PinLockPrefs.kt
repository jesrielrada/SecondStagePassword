package com.prometheus_service.midas.secondstagepassword.data

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.prometheus_service.midas.secondstagepassword.Constants.Companion.KEY_MEMBER_PIN
import com.prometheus_service.midas.secondstagepassword.Constants.Companion.KEY_PIN_CMSBO_ENABLED
import com.prometheus_service.midas.secondstagepassword.Constants.Companion.KEY_PIN_ENABLED
import com.prometheus_service.midas.secondstagepassword.Constants.Companion.KEY_STORED_CREDENTIALS

@SuppressLint("CommitPrefEdits")
class PinLockPrefs(private val prefs: SharedPreferences) {


    fun setPinCmsboEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_CMSBO_ENABLED, enabled).apply()
    }

    fun isPinCmsboEnabled(): Boolean {
        return prefs.getBoolean(KEY_PIN_CMSBO_ENABLED, false)
    }

    fun setPinEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
    }

    fun isPinEnabled(): Boolean {
        return prefs.getBoolean(KEY_PIN_ENABLED, false)
    }

    fun setPin(pin: String?) {
        prefs.edit().putString(KEY_MEMBER_PIN, pin).apply()
    }

    fun getPin(): String? {
        return prefs.getString(KEY_MEMBER_PIN, "")
    }

    fun deletePin() {
        prefs.edit().remove(KEY_MEMBER_PIN).apply()
    }

    fun setStoredCredentials(data: String?) {
        prefs.edit().putString(KEY_STORED_CREDENTIALS, data).apply()
    }

    fun getStoredCredentials(): String? {
        return prefs.getString(KEY_STORED_CREDENTIALS, "")
    }

    fun deletePreference() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}