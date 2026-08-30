package com.mymaterials.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymaterials.app.data.entity.LessonStatus
import com.mymaterials.app.data.entity.Subject
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.components.IosProgressCircle
import com.mymaterials.app.ui.theme.IosBackground
import com.mymaterials.app.ui.theme.IosBlue
import com.mymaterials.app.ui.theme.IosCard
import com.mymaterials.app.ui.theme.IosGray
import com.mymaterials.app.util.BackupManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubjectsScreen(
    repository: MaterialsRepository,
    onSubjectClick: (Long) -> Unit,
    onStatsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val subjects by repository.getAllSubjects().collectAsState(initial = emptyList())
    // نحتاج progress لكل مادة - نجمع الدروس لكل مادة
    var lessonsCountMap by remember { mutableStateOf<Map<Long, Pair<Int, Int>>>(emptyMap()) } // subjectId -> (remaining, total)

    // حساب المتبقي لكل مادة
    LaunchedEffect(subjects) {
        val map = mutableMapOf<Long, Pair<Int, Int>>()
        for (s in subjects) {
            val lessons = repository.getLessonsForSubject(s.id) // flow, نحتاج sync
            // للبساطة: نستخدم getAllForBackup للعد؟ لا. نستخدم suspend للحصول
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Subject?>(null) }
    var showActionSheet by remember { mutableStateOf<Subject?>(null) }
    var textField by remember { mutableStateOf("") }

    // Backup launchers - زر أيقونة فقط
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            try {
                val data = repository.getAllForBackup()
                BackupManager.writeToUri(context, uri, data)
                Toast.makeText(context, "تم التصدير", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Toast.makeText(context, "فشل: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val data = BackupManager.readFromUri(context, uri)
                repository.restoreFromBackup(data)
                Toast.makeText(context, "تم الاستيراد", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Toast.makeText(context, "فشل: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    var showBackupSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = IosBackground,
        topBar = {
            TopAppBar(
                title = { Text("موادي", fontWeight = FontWeight.Bold, fontSize = 28.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IosBackground),
                actions = {
                    IconButton(onClick = { showBackupSheet = true }) {
                        Icon(Icons.Default.Share, contentDescription = "نسخ احتياطي")
                    }
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "إحصائيات")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { textField = ""; showAddDialog = true },
                containerColor = IosBlue,
                contentColor = IosCard,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        if (subjects.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ابدأ بإضافة مادتك الأولى", color = IosGray, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { textField=""; showAddDialog=true }, colors = ButtonDefaults.buttonColors(containerColor = IosBlue)) {
                        Text("إضافة مادة")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        repository = repository,
                        onClick = { onSubjectClick(subject.id) },
                        onLongClick = { showActionSheet = subject }
                    )
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("مادة جديدة") },
            text = {
                OutlinedTextField(
                    value = textField,
                    onValueChange = { textField = it },
                    placeholder = { Text("اسم المادة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (textField.isBlank()) return@TextButton
                    scope.launch {
                        repository.addSubject(textField)
                        showAddDialog = false
                        textField = ""
                    }
                }) { Text("حفظ", color = IosBlue) }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") } }
        )
    }

    // Edit Dialog
    if (editTarget != null) {
        var editText by remember(editTarget) { mutableStateOf(editTarget!!.name) }
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("تعديل المادة") },
            text = {
                OutlinedTextField(value = editText, onValueChange = { editText = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editText.isBlank()) return@TextButton
                    scope.launch {
                        repository.updateSubject(editTarget!!.copy(name = editText.trim()))
                        editTarget = null
                    }
                }) { Text("حفظ", color = IosBlue) }
            },
            dismissButton = { TextButton(onClick = { editTarget = null }) { Text("إلغاء") } }
        )
    }

    // Action Sheet - ضغط طويل
    if (showActionSheet != null) {
        val s = showActionSheet!!
        ModalBottomSheet(onDismissRequest = { showActionSheet = null }) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                Button(onClick = { editTarget = s; showActionSheet = null; textField = s.name }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IosCard, contentColor = IosBlue)) {
                    Text("تعديل الاسم")
                }
                Button(onClick = {
                    scope.launch { repository.togglePin(s); showActionSheet = null }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IosCard, contentColor = IosBlue)) {
                    Text(if (s.isPinned) "إلغاء التثبيت" else "تثبيت 📌")
                }
                Button(onClick = {
                    scope.launch { repository.deleteSubject(s); showActionSheet = null }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("حذف")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Backup Sheet - زر أيقونة فقط
    if (showBackupSheet) {
        ModalBottomSheet(onDismissRequest = { showBackupSheet = false }) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("نسخ احتياطي", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(onClick = {
                    showBackupSheet = false
                    exportLauncher.launch("mawadi_backup_${System.currentTimeMillis()}.json")
                }, modifier = Modifier.fillMaxWidth()) { Text("تصدير نسخة") }
                OutlinedButton(onClick = {
                    showBackupSheet = false
                    importLauncher.launch(arrayOf("application/json"))
                }, modifier = Modifier.fillMaxWidth()) { Text("استيراد نسخة") }
                Text("الاستيراد سيستبدل كل البيانات الحالية", fontSize = 12.sp, color = IosGray)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubjectCard(
    subject: Subject,
    repository: MaterialsRepository,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // نحسب التقدم والمتبقي بشكل Flow
    val lessons by repository.getLessonsForSubject(subject.id).collectAsState(initial = emptyList())
    val total = lessons.size
    val done = lessons.count { it.status == LessonStatus.DONE }
    val progress = if (total == 0) 0f else done.toFloat() / total
    val remaining = total - done

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(subject.name, fontWeight = FontWeight.Medium, fontSize = 16.sp, maxLines = 1)
                    if (subject.isPinned) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp), tint = IosBlue)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (total == 0) "لا يوجد دروس" else "متبقي $remaining من $total",
                    fontSize = 13.sp,
                    color = IosGray
                )
            }
            IosProgressCircle(progress = progress, size = 40, showPercent = true)
        }
    }
}
