package com.solu8i.compcheck.repository

import com.solu8i.compcheck.model.ScanHistoryResponse
import com.solu8i.compcheck.model.ScanLogItem
import com.solu8i.compcheck.model.ScanRequest
import com.solu8i.compcheck.model.ScanResponse
import com.solu8i.compcheck.model.CompartmentValidationResponse
import com.solu8i.compcheck.network.RetrofitClient
import com.solu8i.compcheck.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanRepository(private val sessionManager: SessionManager) {

    fun getDriverId(): Int = sessionManager.getDriverId()
    fun getDriverName(): String = sessionManager.getDriverName() ?: "Driver"
    fun getDriverRole(): String = sessionManager.getDriverRole() ?: "driver"
    fun logout() = sessionManager.logout()

    fun validateCompartment(
        rfidUid: String,
        deviceUuid: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val driverId = sessionManager.getDriverId()
        val baseUrl = sessionManager.getBaseUrl()

        RetrofitClient.getInstance(baseUrl).validateCompartment(rfidUid, driverId, deviceUuid)
            .enqueue(object : Callback<CompartmentValidationResponse> {
                override fun onResponse(
                    call: Call<CompartmentValidationResponse>,
                    response: Response<CompartmentValidationResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        onResult(
                            body?.success == true,
                            body?.message ?: "Kompartemen tidak dapat divalidasi"
                        )
                    } else {
                        onResult(false, extractErrorMessage(response))
                    }
                }

                override fun onFailure(call: Call<CompartmentValidationResponse>, t: Throwable) {
                    onResult(false, "Gagal koneksi ke server: ${t.message}")
                }
            })
    }

    fun sendScanData(
        deviceUuid: String,
        rfidUid: String,
        contentStatus: String,
        note: String?,
        latitude: Double?,
        longitude: Double?,
        onResult: (Boolean, String, ScanResponse?) -> Unit
    ) {
        val driverId = sessionManager.getDriverId()
        val baseUrl = sessionManager.getBaseUrl()

        val request = ScanRequest(
            driverId = driverId,
            deviceUuid = deviceUuid,
            rfidUid = rfidUid,
            contentStatus = contentStatus,
            note = note,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0
        )

        RetrofitClient.getInstance(baseUrl).sendScanData(request).enqueue(object : Callback<ScanResponse> {
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                if (response.isSuccessful) {
                    val scanResponse = response.body()
                    if (scanResponse != null && scanResponse.success) {
                        onResult(true, "Scan Berhasil Disimpan!", scanResponse)
                    } else {
                        onResult(false, scanResponse?.message ?: "Gagal scan", scanResponse)
                    }
                } else {
                    onResult(false, extractErrorMessage(response), null)
                }
            }

            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                onResult(false, "Gagal koneksi ke server: ${t.message}", null)
            }
        })
    }

    private fun <T> extractErrorMessage(response: Response<T>): String {
        val fallback = "Error Server: ${response.code()}"
        val errorBody = response.errorBody()?.string()

        if (errorBody.isNullOrEmpty()) return fallback

        return try {
            org.json.JSONObject(errorBody).optString("message", fallback)
        } catch (e: Exception) {
            fallback
        }
    }

    fun getScanHistory(onResult: (Boolean, String, List<ScanLogItem>?) -> Unit) {
        val driverId = sessionManager.getDriverId()
        val baseUrl = sessionManager.getBaseUrl()

        RetrofitClient.getInstance(baseUrl).getScanHistory(driverId).enqueue(object : Callback<ScanHistoryResponse> {
            override fun onResponse(call: Call<ScanHistoryResponse>, response: Response<ScanHistoryResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        onResult(true, body.message, body.data ?: emptyList())
                    } else {
                        onResult(false, body?.message ?: "Gagal mengambil riwayat", null)
                    }
                } else {
                    onResult(false, "Error ${response.code()}", null)
                }
            }

            override fun onFailure(call: Call<ScanHistoryResponse>, t: Throwable) {
                onResult(false, "Koneksi gagal: ${t.message}", null)
            }
        })
    }
}
