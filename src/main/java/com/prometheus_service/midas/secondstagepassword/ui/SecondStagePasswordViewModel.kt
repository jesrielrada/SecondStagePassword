package com.prometheus_service.midas.secondstagepassword.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prometheus_service.midas.secondstagepassword.data.PinLockPrefs
import com.prometheus_service.midas.secondstagepassword.PinLockState
import com.prometheus_service.midas.secondstagepassword.utils.PinLockUtils

class SecondStagePasswordViewModel : ViewModel() {

    companion object{
        private val TAG = SecondStagePasswordViewModel::class.java.name
    }

    private var mPinLockState: MutableLiveData<PinLockState> = MutableLiveData()

    val pinInput: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply { value = "" }
    }

    val pinLockState: LiveData<PinLockState>
        get() = mPinLockState

    fun getPin(operatorId: String, pinLockPrefsPin: String, clientSecret: String): String? {
        return PinLockUtils.decryptPin(
            operatorId,
            pinLockPrefsPin,
            PinLockUtils.TRANSFORMATION,
            clientSecret
        )
    }

    fun setPin(pin: String, operatorId: String, clientSecret: String, prefs: PinLockPrefs): String? {
        val transformation = PinLockUtils.TRANSFORMATION
        val encryptedPin = PinLockUtils.encryptPin(operatorId, pin, transformation, clientSecret)

        //Refactored since encryptedPin can return null
        encryptedPin?.let {
            prefs.setPin(encryptedPin)
        }

        return encryptedPin
    }

    fun setPinLockState(state: PinLockState): PinLockState {
        mPinLockState.value = state
        return state
    }

    fun confirmPin(enteredText: String): Boolean {
        return if (this.pinInput.value.isNullOrEmpty()) {
            false
        } else {
            pinInput.value.equals(enteredText, true)
        }
    }
}