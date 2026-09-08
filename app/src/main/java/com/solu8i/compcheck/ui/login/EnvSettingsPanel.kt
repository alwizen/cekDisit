package com.solu8i.compcheck.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solu8i.compcheck.viewmodel.LoginViewModel

@Composable
fun EnvSettingsPanel(
    viewModel: LoginViewModel,
    darkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var envDialogVisible by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
        ) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pengaturan",
                    tint = colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = menuExpanded,
                enter = fadeIn() + scaleIn(initialScale = 0.92f),
                exit = fadeOut() + scaleOut(targetScale = 0.92f)
            ) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { menuExpanded = false }
                ) {
                DropdownMenuItem(
                    text = { Text(if (darkMode) "Light Mode" else "Dark Mode") },
                    onClick = {
                        onDarkModeChanged(!darkMode)
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Env Settings") },
                    onClick = {
                        envDialogVisible = true
                        menuExpanded = false
                    }
                )
                }
            }
        }
    }

    if (envDialogVisible) {
        AlertDialog(
            onDismissRequest = { envDialogVisible = false },
            title = { Text("Env Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Server Base URL",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    OutlinedTextField(
                        value = viewModel.baseUrl,
                        onValueChange = { viewModel.onBaseUrlChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("http://192.168.x.x:8000/") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = OutlinedTextFieldDefaults.colors(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    viewModel.testStatus?.let { (success, message) ->
                        Text(
                            text = message,
                            color = if (success) colorScheme.primary else colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.testConnection() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isTesting && viewModel.baseUrl.isNotBlank(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.primary
                        )
                    ) {
                        if (viewModel.isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mengecek...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Koneksi", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { envDialogVisible = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
