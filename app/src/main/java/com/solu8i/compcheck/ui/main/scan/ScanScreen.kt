package com.solu8i.compcheck.ui.main.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.compcheck.viewmodel.ScanMode
import com.solu8i.compcheck.viewmodel.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    deviceUuid: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab selector for Scan Mode
        TabRow(
            selectedTabIndex = viewModel.scanMode.ordinal,
            containerColor = colorScheme.surface,
            contentColor = colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = viewModel.scanMode == ScanMode.NFC,
                onClick = {
                    viewModel.scanMode = ScanMode.NFC
                    viewModel.resetScanState()
                },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("NFC Tap", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            )
            Tab(
                selected = viewModel.scanMode == ScanMode.QR,
                onClick = {
                    viewModel.scanMode = ScanMode.QR
                    viewModel.resetScanState()
                },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Scan QR", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (viewModel.rfidUid.isNotEmpty() && viewModel.lastScanSuccess == null) {
            ScanDetailsDialog(
                viewModel = viewModel,
                deviceUuid = deviceUuid
            )
        }

        when (viewModel.scanMode) {
            ScanMode.NFC -> NfcScanArea(viewModel = viewModel)
            ScanMode.QR -> QrScanArea(viewModel = viewModel, deviceUuid = deviceUuid)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanDetailsDialog(
    viewModel: ScanViewModel,
    deviceUuid: String
) {
    val contentOptions = listOf(
        "kosong" to "Kosong",
        "air" to "Air",
        "sisa_minyak" to "Sisa Minyak",
        "lainnya" to "Lainnya"
    )
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val selectedLabel = contentOptions.firstOrNull { it.first == viewModel.contentStatus }?.second.orEmpty()

    AlertDialog(
        onDismissRequest = { viewModel.resetScanState() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        title = {
            Text(
                text = "Detail Kompartemen",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "UID: ${viewModel.rfidUid}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { if (!viewModel.isSending) isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Isi Kompartemen") },
                        placeholder = { Text("Pilih isi kompartemen") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !viewModel.isSending,
                        isError = viewModel.contentStatus == null
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        contentOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.contentStatus = value
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.note,
                    onValueChange = { viewModel.note = it },
                    label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isSending,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitScan(deviceUuid)
                },
                enabled = viewModel.contentStatus != null && !viewModel.isSending
            ) {
                Text(if (viewModel.isSending) "Menyimpan..." else "Simpan Scan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.resetScanState() },
                enabled = !viewModel.isSending
            ) {
                Text("Batal")
            }
        }
    )
}

