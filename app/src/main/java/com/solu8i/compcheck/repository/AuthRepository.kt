package com.solu8i.compcheck.repository

import com.solu8i.compcheck.model.LoginRequest
import com.solu8i.compcheck.model.LoginResponse
import com.solu8i.compcheck.network.RetrofitClient
import com.solu8i.compcheck.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository(private val sessionManager: SessionManager) {

    fun login(driverNo: String, onResult: (Boolean, String, LoginResponse?) -> Unit) {
        val request = LoginRequest(driverNo)
        val baseUrl = sessionManager.getBaseUrl()

        RetrofitClient.getInstance(baseUrl).loginDriver(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        sessionManager.saveSession(
                            driverId = body.data.id,
                            driverNo = body.data.driverNo,
                            name = body.data.name,
                            role = body.data.role
                        )
                        onResult(true, "Selamat datang, ${body.data.name}", body)
                    } else {
                        onResult(false, body?.message ?: "Gagal login", body)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Login Gagal (Error ${response.code()})"
                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            val jsonObject = org.json.JSONObject(errorBody)
                            errorMessage = jsonObject.optString("message", errorMessage)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    onResult(false, errorMessage, null)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult(false, "Koneksi gagal: ${t.message}", null)
            }
        })
    }

    fun testConnection(baseUrl: String, onResult: (Boolean, String) -> Unit) {
        RetrofitClient.testConnection(baseUrl, onResult)
    }

    fun saveBaseUrl(url: String) {
        sessionManager.saveBaseUrl(url)
    }

    fun getBaseUrl(): String = sessionManager.getBaseUrl()

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}
