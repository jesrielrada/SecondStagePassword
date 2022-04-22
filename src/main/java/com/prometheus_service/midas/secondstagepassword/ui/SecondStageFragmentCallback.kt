package com.prometheus_service.midas.secondstagepassword.ui

import com.prometheus_service.midas.secondstagepassword.PinLockTranslations

interface SecondStageFragmentCallback {
    fun onCancelPinLockSetup()
    fun onPinInputMaxAttempt()
    fun onRemoveFragment()
    fun initTranslations(translations: PinLockTranslations)
}