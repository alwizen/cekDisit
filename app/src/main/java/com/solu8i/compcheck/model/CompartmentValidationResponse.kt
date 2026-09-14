package com.solu8i.compcheck.model

import com.google.gson.annotations.SerializedName

data class CompartmentValidationResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: CompartmentValidationData?
)

data class CompartmentValidationData(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("compartment_no")
    val compartmentNo: Int?,

    @SerializedName("capacity_kl")
    val capacityKl: Int?,

    @SerializedName("rfid_uid")
    val rfidUid: String?
)