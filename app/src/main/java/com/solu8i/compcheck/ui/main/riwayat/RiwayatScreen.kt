package com.solu8i.compcheck.ui.main.riwayat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.compcheck.model.ScanLogItem
import com.solu8i.compcheck.viewmodel.ScanViewModel

@Composable
fun RiwayatScreen(viewModel: ScanViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadRiwayat()
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header of Riwayat tab with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Scan Driver",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Daftar aktivitas scan yang telah dilakukan",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            IconButton(
                onClick = { viewModel.loadRiwayat() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFFFFF))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (viewModel.isHistoryLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(color = Color(0xFF1E88E5))
                    Text("Memuat data riwayat...", fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }
        } else if (viewModel.historyError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(48.dp))
                    Text(viewModel.historyError ?: "Gagal memuat data", color = Color(0xFFDC2626), fontSize = 14.sp)
                    Button(
                        onClick = { viewModel.loadRiwayat() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Coba Lagi", color = Color.White)
                    }
                }
            }
        } else if (viewModel.scanHistoryList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(56.dp))
                    Text("Belum Ada Riwayat Scan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                    Text("Lakukan scan kompartemen untuk melihat histori", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }
        } else {
            val groupedHistory = remember(viewModel.scanHistoryList) {
                viewModel.scanHistoryList
                    .groupBy { item ->
                        item.scanSessionId?.toString() ?: "legacy-${item.scanLogId}"
                    }
                    .toList()
            }
            val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                groupedHistory.forEach { (groupKey, items) ->
                    item(key = groupKey) {
                        val expanded = expandedGroups[groupKey] ?: false
                        RiwayatGroupCard(
                            items = items,
                            expanded = expanded,
                            onToggle = { expandedGroups[groupKey] = !expanded }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiwayatGroupCard(
    items: List<ScanLogItem>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val firstItem = items.first()
    val isDone = items.all { it.scanStatus == "done" }
    val isInsideGeofence = firstItem.geofence?.isInside == true

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = firstItem.tanker?.nopol ?: "Nopol -",
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    StatusBadge(isDone = isDone, compact = true)
                    LocationBadge(isInside = isInsideGeofence, compact = true)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${items.size} kompartemen - ${firstItem.scannedAt ?: "-"}",
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Tutup ritase" else "Buka ritase",
                        tint = Color(0xFF6B7280)
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    RiwayatItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun RiwayatItemCard(item: ScanLogItem) {
    val isDone = item.scanStatus == "done"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                    Text(
                        text = item.scannedAt ?: "-",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        fontFamily = FontFamily.Monospace
                    )
                }

            }

            HorizontalDivider(color = Color(0xFFF3F4F6))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Komp. #${item.compartment?.compartmentNo ?: "-"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "${item.compartment?.capacityKl ?: 0} KL",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                StatusBadge(isDone = isDone)
            }
        }
    }
}

@Composable
private fun StatusBadge(isDone: Boolean, compact: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDone) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isDone) "Complete" else "Belum Lengkap",
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDone) Color(0xFF15803D) else Color(0xFFB45309)
        )
    }
}

@Composable
private fun LocationBadge(isInside: Boolean, compact: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isInside) Color(0xFFECFDF5) else Color(0xFFFFF7ED))
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isInside) Icons.Default.Place else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isInside) Color(0xFF16A34A) else Color(0xFFF59E0B),
            modifier = Modifier.size(if (compact) 11.dp else 12.dp)
        )
        Text(
            text = if (isInside) "Di Lokasi" else "Luar Lokasi",
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isInside) Color(0xFF16A34A) else Color(0xFFF59E0B)
        )
    }
}
