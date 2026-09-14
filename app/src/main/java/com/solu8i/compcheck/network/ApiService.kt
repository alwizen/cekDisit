package com.solu8i.compcheck.network

import com.solu8i.compcheck.model.LoginRequest
import com.solu8i.compcheck.model.LoginResponse
import com.solu8i.compcheck.model.CompartmentValidationResponse
import com.solu8i.compcheck.model.ScanHistoryResponse
import com.solu8i.compcheck.model.ScanRequest
import com.solu8i.compcheck.model.ScanResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/driver-login")
    fun loginDriver(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("api/scan")
    fun sendScanData(
        @Body request: ScanRequest
    ): Call<ScanResponse>

    @GET("api/scan/validate-compartment")
    fun validateCompartment(
        @Query("rfid_uid") rfidUid: String,
        @Query("driver_id") driverId: Int,
        @Query("device_uuid") deviceUuid: String
    ): Call<CompartmentValidationResponse>

    @GET("api/scan-history")
    fun getScanHistory(
        @Query("driver_id") driverId: Int
    ): Call<ScanHistoryResponse>
}