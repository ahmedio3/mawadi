package com.mymaterials.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymaterials.app.data.preferences.ThemeMode
import com.mymaterials.app.ui.theme.AppTheme
import com.mymaterials.app.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentThemeMode by viewModel.themeMode.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(
                context = context,
                uri = uri,
                onSuccess = { Toast.makeText(context, "تم تصدير النسخة الاحتياطية بنجاح", Toast.LENGTH_SHORT).show() },
                onError = { err -> Toast.makeText(context, "فشل التصدير: $err", Toast.LENGTH_SHORT).show() }
            )
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreBackup(
                context = context,
                uri = uri,
                onSuccess = { Toast.makeText(context, "تم استيراد النسخة الاحتياطية بنجاح", Toast.LENGTH_SHORT).show() },
                onError = { err -> Toast.makeText(context, "فشل الاستيراد: $err", Toast.LENGTH_SHORT).show() }
            )
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // قسم المظهر
            item {
                SettingsSectionHeader("المظهر")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column {
                        ThemeOptionRow(
                            title = "تلقائي (حسب النظام)",
                            subtitle = "التوافق مع مظهر الجهاز الحالي",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = currentThemeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = AppTheme.colors.separator.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                        ThemeOptionRow(
                            title = "وضع فاتح",
                            subtitle = "واجهة بيضاء كلاسيكية هادئة",
                            icon = Icons.Default.LightMode,
                            isSelected = currentThemeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = AppTheme.colors.separator.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                        ThemeOptionRow(
                            title = "وضع ليلي",
                            subtitle = "ألوان داكنة مريحة للعين ليلاً",
                            icon = Icons.Default.DarkMode,
                            isSelected = currentThemeMode == ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                        )
                    }
                }
            }

            // قسم النسخ الاحتياطي
            item {
                SettingsSectionHeader("البيانات والنسخ الاحتياطي")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column {
                        SettingsActionRow(
                            title = "تصدير نسخة احتياطية",
                            subtitle = "حفظ المواد والدروس كملف JSON",
                            icon = Icons.Default.FileUpload,
                            onClick = { exportLauncher.launch("mawadi_backup.json") }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = AppTheme.colors.separator.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                        SettingsActionRow(
                            title = "استيراد نسخة احتياطية",
                            subtitle = "استرجاع البيانات من ملف JSON سابق",
                            icon = Icons.Default.FileDownload,
                            onClick = { importLauncher.launch(arrayOf("application/json")) }
                        )
                    }
                }
            }

            // قسم حول التطبيق
            item {
                SettingsSectionHeader("حول التطبيق")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppTheme.colors.blue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = AppTheme.colors.blue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("موادي - My Subjects", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(2.dp))
                            Text("الإصدار 1.2 • تنظيم المناهج والدروس", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = AppTheme.colors.textSecondary,
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
    )
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) AppTheme.colors.blue else AppTheme.colors.gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 15.sp,
                color = if (isSelected) AppTheme.colors.blue else AppTheme.colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = AppTheme.colors.textSecondary
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "محدد",
                tint = AppTheme.colors.blue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.blue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = AppTheme.colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = AppTheme.colors.textSecondary
            )
        }
    }
}
