package com.mymaterials.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymaterials.app.data.entity.LessonStatus
import com.mymaterials.app.data.entity.Subject
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.components.IosProgressCircle
import com.mymaterials.app.ui.components.ReorderIconButton
import com.mymaterials.app.ui.components.ReorderableLazyColumn
import com.mymaterials.app.ui.components.SkeletonSubjectCard
import com.mymaterials.app.ui.theme.AppTheme
import com.mymaterials.app.ui.viewmodels.SubjectsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    repository: MaterialsRepository,
    onSubjectClick: (Long) -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val subjects by viewModel.subjects.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Subject?>(null) }
    var showActionSheet by remember { mutableStateOf<Subject?>(null) }
    var textField by remember { mutableStateOf("") }
    var reorderMode by remember { mutableStateOf(false) }

    val isDark = AppTheme.colors.isDark

    Scaffold(
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (reorderMode) "ترتيب المواد" else "موادي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = AppTheme.colors.textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background
                ),
                actions = {
                    if (reorderMode) {
                        TextButton(onClick = { reorderMode = false }) {
                            Text("تم", color = AppTheme.colors.blue, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // زر ترتيب بأيقونة مميزة وخلفية كبسولة هادئة
                        ReorderIconButton(
                            onClick = {
                                if (subjects?.isNotEmpty() == true) reorderMode = true
                            }
                        )
                        Spacer(Modifier.width(4.dp))
                        // زر التبديل السريع بين الوضعين الليلي والنهاري
                        IconButton(
                            onClick = { viewModel.toggleTheme(isDark) }
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDark) "الوضع الفاتح" else "الوضع الداكن",
                                tint = if (isDark) AppTheme.colors.orange else AppTheme.colors.blue
                            )
                        }
                        // زر الإحصائيات
                        IconButton(onClick = onStatsClick) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = "إحصائيات",
                                tint = AppTheme.colors.textPrimary
                            )
                        }
                        // زر الإعدادات
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "الإعدادات",
                                tint = AppTheme.colors.textPrimary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!reorderMode) {
                FloatingActionButton(
                    onClick = { textField = ""; showAddDialog = true },
                    containerColor = AppTheme.colors.blue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة مادة")
                }
            }
        }
    ) { padding ->
        val current = subjects
        if (current == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                userScrollEnabled = false
            ) {
                items(4) { SkeletonSubjectCard() }
            }
        } else if (current.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "ابدأ بإضافة مادتك الأولى",
                        color = AppTheme.colors.gray,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { textField = ""; showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.blue)
                    ) {
                        Text("إضافة مادة", color = Color.White)
                    }
                }
            }
        } else if (reorderMode) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    "اسحب من المقبض لتحريك المادة داخل مجموعتها",
                    fontSize = 12.sp,
                    color = AppTheme.colors.gray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                ReorderableLazyColumn(
                    items = current,
                    key = { it.id },
                    canMove = { a, b -> a.isPinned == b.isPinned },
                    onCommit = { reordered -> viewModel.reorderSubjects(reordered) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) { subject, _, handle ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            SubjectCard(
                                subject = subject,
                                repository = repository,
                                onClick = {},
                                onLongClick = {}
                            )
                        }
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "سحب للترتيب",
                            tint = AppTheme.colors.gray,
                            modifier = handle
                                .size(44.dp)
                                .padding(10.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                itemsIndexed(current, key = { _, s -> s.id }) { index, subject ->
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = androidx.compose.animation.fadeIn(
                            animationSpec = tween(durationMillis = 250, delayMillis = (index * 60).coerceAtMost(300))
                        ) + androidx.compose.animation.slideInVertically(
                            animationSpec = tween(durationMillis = 250, delayMillis = (index * 60).coerceAtMost(300)),
                            initialOffsetY = { it / 3 }
                        )
                    ) {
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
                    if (textField.isNotBlank()) {
                        viewModel.addSubject(textField)
                        showAddDialog = false
                        textField = ""
                    }
                }) {
                    Text("حفظ", color = AppTheme.colors.blue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = AppTheme.colors.gray)
                }
            }
        )
    }

    // Edit Dialog
    if (editTarget != null) {
        var editText by remember(editTarget) { mutableStateOf(editTarget!!.name) }
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("تعديل المادة") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editText.isNotBlank()) {
                        viewModel.updateSubject(editTarget!!, editText)
                        editTarget = null
                    }
                }) {
                    Text("حفظ", color = AppTheme.colors.blue)
                }
            },
            dismissButton = {
                TextButton(onClick = { editTarget = null }) {
                    Text("إلغاء", color = AppTheme.colors.gray)
                }
            }
        )
    }

    // Action Sheet - ضغط طويل
    if (showActionSheet != null) {
        val s = showActionSheet!!
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = null },
            containerColor = AppTheme.colors.card
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    s.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = {
                        editTarget = s
                        showActionSheet = null
                        textField = s.name
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.cardSecondary,
                        contentColor = AppTheme.colors.blue
                    )
                ) {
                    Text("تعديل الاسم")
                }
                Button(
                    onClick = {
                        viewModel.togglePin(s)
                        showActionSheet = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.cardSecondary,
                        contentColor = AppTheme.colors.blue
                    )
                ) {
                    Text(if (s.isPinned) "إلغاء التثبيت" else "تثبيت 📌")
                }
                Button(
                    onClick = {
                        viewModel.deleteSubject(s)
                        showActionSheet = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف", color = Color.White)
                }
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
    val lessons by repository.getLessonsForSubject(subject.id).collectAsState(initial = emptyList())
    val total = lessons.size
    val done = lessons.count { it.status == LessonStatus.DONE }
    val progress = if (total == 0) 0f else done.toFloat() / total
    val isComplete = total > 0 && done == total

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        subject.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1
                    )
                    if (subject.isPinned) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AppTheme.colors.blue
                        )
                    }
                    if (isComplete) {
                        Spacer(Modifier.width(6.dp))
                        Text("✅", fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (total == 0) "لا يوجد دروس" else "تم $done من $total",
                    fontSize = 13.sp,
                    color = AppTheme.colors.gray
                )
            }
            IosProgressCircle(progress = progress, size = 40, showPercent = true)
        }
    }
}
