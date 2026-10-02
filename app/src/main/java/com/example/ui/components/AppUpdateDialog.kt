package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.AppUpdateManager
import com.example.services.ReleaseInfo
import com.example.services.UpdateStatus
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat

@Composable
fun AppUpdateDialog(
    updateManager: AppUpdateManager,
    updateStatus: UpdateStatus,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings = updateManager.updateSettings.value

    var githubRepo by remember { mutableStateOf(settings.githubRepo) }
    var customApkUrl by remember { mutableStateOf(settings.customApkUrl) }
    var showConfigFields by remember { mutableStateOf(false) }

    val formatMb = remember { DecimalFormat("#,##0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyanTelemetry.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, CyanTelemetry.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "OTA IN-APP UPDATER",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ইনস্টল থাকা অবস্থায় আনইন্সটল ছাড়াই স্বয়ংক্রিয় আপডেট",
                        color = TextMuted,
                        fontSize = 10.sp
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
                // Version Status Pill Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceElevated, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, BorderHairline, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INSTALLED VERSION",
                            color = TextMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "v${updateManager.currentVersionName} (Build ${updateManager.currentVersionCode})",
                            color = CyanTelemetry,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF334155), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "CHANNEL: STABLE",
                            color = EmeraldGate,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle repository settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RELEASE SOURCE: $githubRepo",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Text(
                        text = if (showConfigFields) "HIDE CONFIG" else "EDIT REPO",
                        color = CyanTelemetry,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                if (showConfigFields) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = githubRepo,
                        onValueChange = {
                            githubRepo = it
                            updateManager.saveSettings(it, customApkUrl, true)
                        },
                        label = { Text("GitHub Repo (owner/repo)", color = TextMuted, fontSize = 10.sp) },
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

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customApkUrl,
                        onValueChange = {
                            customApkUrl = it
                            updateManager.saveSettings(githubRepo, it, true)
                        },
                        label = { Text("Direct Fallback APK URL (Optional)", color = TextMuted, fontSize = 10.sp) },
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

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Status Body
                when (updateStatus) {
                    is UpdateStatus.Idle -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0B132B), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = CyanTelemetry,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "READY TO CHECK",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "GitHub Releases চেক করার জন্য নিচের বাটনে চাপ দিন। নতুন ভার্সন থাকলে আনইন্সটল না করেই সরাসরি ইন-প্লেস আপডেট হবে।",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    is UpdateStatus.Checking -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, CyanTelemetry.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = CyanTelemetry,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "GitHub Releases স্ক্যান করা হচ্ছে...",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Target: $githubRepo",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    is UpdateStatus.UpToDate -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldGate.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, EmeraldGate.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldGate,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "APPLICATION IS UP TO DATE",
                                        color = EmeraldGate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "আপনার ফোনে সর্বশেষ ভার্সন (v${updateStatus.currentVersion}) ইনস্টল রয়েছে। নতুন কোনো রিলিজ নেই।",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    is UpdateStatus.Available -> {
                        val release = updateStatus.release
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, CyanTelemetry, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.NewReleases,
                                            contentDescription = null,
                                            tint = CyanTelemetry,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "NEW UPDATE AVAILABLE!",
                                            color = CyanTelemetry,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(CyanTelemetry.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = release.tagName,
                                            color = CyanTelemetry,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = release.title,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (release.apkSize > 0) {
                                    val sizeMb = release.apkSize / (1024.0 * 1024.0)
                                    Text(
                                        text = "APK SIZE: ${formatMb.format(sizeMb)} MB",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Changelog Box
                                Text(
                                    text = "RELEASE NOTES & CHANGELOG:",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 100.dp)
                                        .background(Color(0xFF050B14), shape = RoundedCornerShape(4.dp))
                                        .border(1.dp, BorderHairline, shape = RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = release.changelog,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldGate,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ইন-প্লেস আপডেট: আপনার পূর্বের কোনো ডেটা নষ্ট হবে না।",
                                        color = EmeraldGate,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    is UpdateStatus.Downloading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, CyanTelemetry, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "DOWNLOADING APK...",
                                        color = CyanTelemetry,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "${updateStatus.progressPercent}%",
                                        color = CyanTelemetry,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { updateStatus.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = CyanTelemetry,
                                    trackColor = Color(0xFF1E293B)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                val dlMb = updateStatus.downloadedBytes / (1024.0 * 1024.0)
                                val totMb = updateStatus.totalBytes / (1024.0 * 1024.0)
                                Text(
                                    text = if (updateStatus.totalBytes > 0) {
                                        "${formatMb.format(dlMb)} MB / ${formatMb.format(totMb)} MB"
                                    } else {
                                        "${formatMb.format(dlMb)} MB ডাউনলোড হয়েছে"
                                    },
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ডাউনলোড সম্পন্ন হলে প্যাকেজ ইনস্টলার সরাসরি চালু হবে।",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    is UpdateStatus.ReadyToInstall -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldGate.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, EmeraldGate, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DownloadDone,
                                        contentDescription = null,
                                        tint = EmeraldGate,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "APK DOWNLOAD COMPLETE",
                                        color = EmeraldGate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ভার্সন ${updateStatus.release.tagName} সম্পূর্ণ ডাউনলোড হয়েছে। আনইন্সটল ছাড়াই সরাসরি ইনস্টল করতে নিচের বাটনে চাপ দিন।",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    is UpdateStatus.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CrimsonAlert.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, CrimsonAlert.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = CrimsonAlert,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "UPDATE NOTICE",
                                        color = CrimsonAlert,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = updateStatus.message,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Safe In-place note
                Text(
                    text = "🔒 Zero Data Loss: Android প্যাকেজ ম্যানেজার একই সাইনিং কি (Signing Key) ও অ্যাপ্লিকেশন আইডি বজায় রেখে ডাটাবেজ, ব্রাঞ্চ ও সেটিংস সম্পূর্ণ অক্ষত রেখে আপডেট সম্পাদন করে।",
                    color = TextMuted,
                    fontSize = 9.sp,
                    lineHeight = 12.sp
                )
            }
        },
        confirmButton = {
            when (updateStatus) {
                is UpdateStatus.Available -> {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                updateManager.downloadUpdate(updateStatus.release)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanTelemetry,
                            contentColor = Color(0xFF050B14)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ডাউনলোড ও আপডেট করুন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is UpdateStatus.ReadyToInstall -> {
                    Button(
                        onClick = {
                            updateManager.installApk(context, updateStatus.apkFile)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGate,
                            contentColor = Color(0xFF050B14)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "এখনই ইনস্টল করুন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is UpdateStatus.Downloading -> {
                    // No action while downloading
                }

                else -> {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                updateManager.checkForUpdates(githubRepo, customApkUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanTelemetry,
                            contentColor = Color(0xFF050B14)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আপডেট চেক করুন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderHairline),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(text = "বন্ধ করুন", fontSize = 11.sp)
            }
        },
        containerColor = Color(0xFF090E17),
        shape = RoundedCornerShape(12.dp)
    )
}
