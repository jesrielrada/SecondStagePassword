package com.prometheus_service.midas.secondstagepassword.ui

interface SecondStageFragmentCallback {
    fun onCancelPinLockSetup()
    fun onPinInputMaxAttempt()
    fun onRemoveSecondStageFragment()
}