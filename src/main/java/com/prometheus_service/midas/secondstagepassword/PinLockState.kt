package com.prometheus_service.midas.secondstagepassword

import java.io.Serializable

sealed class PinLockState : Serializable{
    object DisplayCreatePin : PinLockState()
    object DisplayConfirmPin : PinLockState()
    object DisplayLockedScreen : PinLockState()
}
