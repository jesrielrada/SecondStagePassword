package com.prometheus_service.midas.secondstagepassword.utils

import android.util.Base64
import android.util.Log
import com.prometheus_service.midas.secondstagepassword.data.PinLockPrefs
import org.json.JSONException
import org.json.JSONObject
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object PinLockUtils {

    private const val CIPHER_KEY_LEN = 16 //128 bits
    const val TRANSFORMATION = "AES/CBC/PKCS5PADDING"

    private fun initKey(key: String): String {
        var tempKey = key
        if (tempKey.length < CIPHER_KEY_LEN) {
            val numPad = CIPHER_KEY_LEN - tempKey.length
            for (i in 0 until numPad) {
                tempKey += "0" //0 pad to len 16 bytes
            }
        } else if (tempKey.length > CIPHER_KEY_LEN) {
            tempKey = tempKey.substring(0, CIPHER_KEY_LEN) //truncate to 16 bytes
        }

        return tempKey
    }

    fun encryptPin(
        key: String,
        value: String?,
        transformation: String,
        clientSecret: String
    ): String? {
        val tempKey = initKey(key)
        try {
            val iv =
                IvParameterSpec(initVector(clientSecret).toByteArray(charset("UTF-8")))
            val skeySpec =
                SecretKeySpec(tempKey.toByteArray(charset("UTF-8")), "AES")
            val cipher =
                Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            val encrypted = cipher.doFinal(value?.toByteArray())
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun decryptPin(
        key: String,
        encryptedPin: String?,
        transformation: String,
        clientSecret: String
    ): String? {
        val tempKey = initKey(key)
        try {
            val iv =
                IvParameterSpec(initVector(clientSecret).toByteArray(charset("UTF-8")))
            val skeySpec =
                SecretKeySpec(tempKey.toByteArray(charset("UTF-8")), "AES")
            val cipher =
                Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val original = cipher.doFinal(Base64.decode(encryptedPin, Base64.DEFAULT))
            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    private fun initVector(clientSecret: String): String {
        return clientSecret.substring(0, 16)
    }

    fun setCmsboPinEnabled(jsonObject: String, prefs: PinLockPrefs) {
        try {
            val dataObj = JSONObject(jsonObject)
            prefs.setPinCmsboEnabled(dataObj.getBoolean("secondStagePassword"))
        } catch (e: JSONException) {
            Log.e(PinLockUtils::class.java.name, e.localizedMessage)
        }
    }

    fun isPinLockActive(isPWAReady: Boolean?, isLoggedIn : Boolean, prefs: PinLockPrefs): Boolean {
        val isPwaReady = isPWAReady ?: false

        val isPinCmsboEnabled = prefs.isPinCmsboEnabled()
        val isPinEnabled = prefs.isPinEnabled()
        val savedPin = prefs.getPin()
        val isPinSaved = (savedPin?.isNotEmpty() ?: false) && (savedPin?.isNotBlank() ?: false)

        return (isPwaReady && isLoggedIn && isPinCmsboEnabled && isPinEnabled && isPinSaved)
    }
}