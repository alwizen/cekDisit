package com.solu8i.compcheck.model

import com.google.gson.annotations.SerializedName
data class LoginRequest(
    @SerializedName("driver_no")
    val driverNo: String
)