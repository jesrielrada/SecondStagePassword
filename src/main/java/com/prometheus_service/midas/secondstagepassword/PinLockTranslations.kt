package com.prometheus_service.midas.secondstagepassword

import java.io.Serializable

data class PinLockTranslations(
    val pinLockScreenHeader: String,
    val pinLockCreate: String,
    val pinLockConfirm: String,
    val pinLockEnter: String,
    val pinLockIncorrect: String,
    val pinLockIncorrectNew: String,
    val pinLockConfirmOld: String,
    val pinLockConfirmNew: String,
    val pinLockConfirmVerify: String,
    val pinLockConfirmOldIncorrect: String,
    val pinLockForgotAlert: String,
    val pinLockForgotButton: String,
    val pinLockCancelButton: String,
    val pinLockAttempts: String
) : Serializable
