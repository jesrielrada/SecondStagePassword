package com.prometheus_service.midas.secondstagepassword.data

import java.io.Serializable

data class PinlockData(
    val operatorId : String,
    val clientSecret: String,
    val pin: String
) : Serializable
