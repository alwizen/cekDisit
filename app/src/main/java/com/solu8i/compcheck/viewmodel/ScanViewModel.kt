package com.solu8i.compcheck.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solu8i.compcheck.model.ScanData
import com.solu8i.compcheck.model.ScanLogItem
import com.solu8i.compcheck.repository.ScanRepository

enum class ScanMode {
    NFC, QR
}

class ScanViewModel(private val repository: ScanRepository) : ViewModel() {

    var driverName by mutableStateOf(repository.getDriverName())
        private set

    var driverRole by mutableStateOf(repository.getDriverRole())
        private set

    var scanMode by mutableStateOf(ScanMode.NFC)

    var nfcStatus by mutableStateOf("Tempelkan Kartu NFC")
    var rfidUid by mutableStateOf("")
    private var rejectedUid: String? = null
    var contentStatus by mutableStateOf<String?>(null)
    var note by mutableStateOf("")
    var locationText by mutableStateOf("Mencari GPS...")
    var latitude by mutableStateOf<Double?>(null)
    var longitude by mutableStateOf<Double?>(null)

    var isSending by mutableStateOf(false)
    var isValidating by mutableStateOf(false)
    var lastScanSuccess by mutableStateOf<Boolean?>(null)
    var lastScanResult by mutableStateOf<ScanData?>(null)

    var scanHistoryList by mutableStateOf<List<ScanLogItem>>(emptyList())
    var isHistoryLoading by mutableStateOf(false)
    var historyError by mutableStateOf<String?>(null)

    private val resetHandler = Handler(Looper.getMainLooper())
    private val resetRunnable = Runnable {
        resetScanState()
    }

    fun setLocation(lat: Double?, lng: Double?, statusText: String) {
        latitude = lat
        longitude = lng
        locationText = statusText
    }

    fun setNfcAvailabilityStatus(status: String) {
        if (rfidUid.isEmpty() && !isSending && lastScanSuccess == null) {
            nfcStatus = status
        }
    }

    fun onTagDetected(uidHex: String, deviceUuid: String) {
        validateBeforeModal(uidHex, deviceUuid, "Kartu Terdeteksi!")
    }

    fun onQrDetected(uidStr: String, deviceUuid: String) {
        if (isSending || isValidating) return

        if (rfidUid.isNotEmpty()) {
            if (lastScanSuccess == true && rfidUid == uidStr) {
                lastScanSuccess = false
                lastScanResult = null
                nfcStatus = "Kompartemen ini sudah discan"
                resetHandler.removeCallbacks(resetRunnable)
                resetHandler.postDelayed(resetRunnable, 6000)
            }

            return
        }

        validateBeforeModal(uidStr, deviceUuid, "QR Code Terbaca!")
    }

    private fun validateBeforeModal(uid: String, deviceUuid: String, detectedMessage: String) {
        if (isSending || isValidating || rfidUid.isNotEmpty() || rejectedUid == uid) return

        isValidating = true
        nfcStatus = "Memeriksa UID..."
        repository.validateCompartment(uid, deviceUuid) { isValid, message ->
            isValidating = false

            if (!isValid) {
                rejectedUid = uid
                lastScanSuccess = false
                lastScanResult = null
                nfcStatus = message
                resetHandler.removeCallbacks(resetRunnable)
                resetHandler.postDelayed(resetRunnable, 6000)
                return@validateCompartment
            }

            rejectedUid = null
            rfidUid = uid
            nfcStatus = detectedMessage
            lastScanSuccess = null
            lastScanResult = null
            resetHandler.removeCallbacks(resetRunnable)
        }
    }

    fun submitScan(deviceUuid: String, onResult: ((Boolean, String) -> Unit)? = null) {
        val uid = rfidUid
        val selectedContentStatus = contentStatus

        if (uid.isEmpty() || selectedContentStatus.isNullOrEmpty() || isSending) return

        isSending = true

        repository.sendScanData(
            deviceUuid = deviceUuid,
            rfidUid = uid,
            contentStatus = selectedContentStatus,
            note = note.trim().ifEmpty { null },
            latitude = latitude,
            longitude = longitude
        ) { success, message, scanResponse ->
            isSending = false
            if (success && scanResponse?.data != null) {
                lastScanSuccess = true
                lastScanResult = scanResponse.data

                val isInside = scanResponse.data.geofence?.isInside == true
                val locationName = scanResponse.data.geofence?.locationName

                nfcStatus = if (isInside) {
                    "✓ Di Dalam Geofence (${locationName ?: "Lokasi Parkir"})"
                } else {
                    "⚠️ Di Luar Geofence Parkir MT"
                }
                onResult?.invoke(true, "Scan Berhasil Disimpan!")
                loadRiwayat() // Auto refresh history
            } else {
                lastScanSuccess = false
                lastScanResult = null
                nfcStatus = message
                onResult?.invoke(false, message)
            }

            resetHandler.removeCallbacks(resetRunnable)
            resetHandler.postDelayed(resetRunnable, 6000)
        }
    }

    fun resetScanState() {
        nfcStatus = if (scanMode == ScanMode.NFC) "Tempelkan Kartu NFC" else "Arahkan Kamera ke QR Code"
        rfidUid = ""
        rejectedUid = null
        isValidating = false
        contentStatus = null
        note = ""
        lastScanSuccess = null
        lastScanResult = null
    }

    fun loadRiwayat() {
        isHistoryLoading = true
        historyError = null
        repository.getScanHistory { success, message, list ->
            isHistoryLoading = false
            if (success && list != null) {
                scanHistoryList = list
            } else {
                historyError = message
            }
        }
    }

    fun logout() {
        repository.logout()
    }
}
