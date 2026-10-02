package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.AiEngineMode
import com.example.services.ApiSettings
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsApiDialog(
    currentSettings: ApiSettings,
    onSaveSettings: (apiKey: String, endpoint: String, model: String, enabled: Boolean) -> Unit,
    onTestConnection: suspend (apiKey: String) -> Pair<Boolean, String>,
    onOpenUpdateDialog: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var isEnteringNewKey by remember { mutableStateOf(currentSettings.externalApiKey.isBlank()) }
    var apiKeyInput by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf(currentSettings.customEndpoint) }
    var selectedModel by remember { mutableStateOf(currentSettings.selectedModel) }
    var isExternalEnabled by remember { mutableStateOf(currentSettings.isExternalEnabled) }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CyanTelemetry,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI ENGINE & API SETTINGS",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Android Keystore-Backed External API Key",
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Active Engine Mode Banner
                val modeLabel = when (currentSettings.activeEngineMode) {
                    AiEngineMode.EXTERNAL_KEY -> "ACTIVE: PERSONAL GEMINI KEY (SECURE KEYSTORE)"
                    AiEngineMode.OFFLINE_DETERMINISTIC -> "ACTIVE: SIMULATED / OFFLINE EVALUATION"
                }
                val modeColor = when (currentSettings.activeEngineMode) {
                    AiEngineMode.EXTERNAL_KEY -> EmeraldGate
                    AiEngineMode.OFFLINE_DETERMINISTIC -> AmberFlame
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(modeColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, modeColor.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(modeColor, shape = androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = modeLabel,
                            color = modeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle: Enable Custom Key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Use Custom API Key", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Android Keystore এনক্রিপ্টেড পার্সোনাল কী", color = TextMuted, fontSize = 9.sp)
                    }
                    Switch(
                        checked = isExternalEnabled,
                        onCheckedChange = { isExternalEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGate,
                            checkedTrackColor = EmeraldGate.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // API Key Field - Masked UI: never display the full key again after saving
                if (!isEnteringNewKey && currentSettings.maskedApiKey.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, BorderHairline, shape = RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldGate, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SAVED KEY (KEYSTORE ENCRYPTED)", color = EmeraldGate, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentSettings.maskedApiKey,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "নিরাপত্তার স্বার্থে সংরক্ষিত API Key সম্পূর্ণ প্রদর্শিত হবে না।",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    isEnteringNewKey = true
                                    apiKeyInput = ""
                                },
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("CHANGE KEY", color = CyanTelemetry, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            testResult = null
                        },
                        label = { Text("Enter Gemini API Key", color = TextMuted, fontSize = 10.sp) },
                        placeholder = { Text("AIzaSy...", color = TextMuted.copy(alpha = 0.5f), fontSize = 10.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = CyanTelemetry, modifier = Modifier.size(16.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanTelemetry,
                            unfocusedBorderColor = BorderHairline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Model Selector
                Text("Select Model:", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val models = listOf("gemini-2.5-flash", "gemini-1.5-flash", "gemini-1.5-pro")
                    models.forEach { m ->
                        val isSelected = selectedModel == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) IndigoNexus.copy(alpha = 0.3f) else Color(0xFF0F172A),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyanTelemetry else BorderHairline,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedModel = m }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = m.replace("gemini-", ""),
                                color = if (isSelected) CyanTelemetry else TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Endpoint Override (Optional)
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("API Endpoint URL", color = TextMuted, fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanTelemetry,
                        unfocusedBorderColor = BorderHairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Test Result Status Display
                if (testResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val (success, msg) = testResult!!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (success) EmeraldGate.copy(alpha = 0.12f) else CrimsonAlert.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (success) EmeraldGate else CrimsonAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = msg,
                            color = if (success) EmeraldGate else CrimsonAlert,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // In-App OTA Update Shortcut Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, CyanTelemetry.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                        .clickable {
                            onDismiss()
                            onOpenUpdateDialog()
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = CyanTelemetry,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "IN-APP OTA UPDATER",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "আনইন্সটল ছাড়াই সরাসরি আপডেট চেক ও ইনস্টল",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = "OPEN >",
                        color = CyanTelemetry,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            val keyToUse = if (isEnteringNewKey && apiKeyInput.isNotBlank()) apiKeyInput.trim() else currentSettings.externalApiKey
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Test Connection Button
                Button(
                    onClick = {
                        isTesting = true
                        testResult = null
                        coroutineScope.launch {
                            val res = onTestConnection(keyToUse)
                            testResult = res
                            isTesting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    enabled = !isTesting
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = CyanTelemetry, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = CyanTelemetry, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TEST", color = CyanTelemetry, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Save Button
                Button(
                    onClick = {
                        onSaveSettings(keyToUse, endpoint.trim(), selectedModel, isExternalEnabled)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoNexus)
                ) {
                    Text("SAVE SETTINGS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted, fontSize = 10.sp)
            }
        },
        containerColor = SlateSurfaceElevated,
        shape = RoundedCornerShape(12.dp)
    )
}
