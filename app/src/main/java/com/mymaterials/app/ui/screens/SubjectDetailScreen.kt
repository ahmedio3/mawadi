package com.mymaterials.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymaterials.app.data.entity.Lesson
import com.mymaterials.app.data.entity.LessonStatus
import com.mymaterials.app.data.entity.StudyUnit
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.components.IosProgressCircle
import com.mymaterials.app.ui.theme.*
import com.mymaterials.app.util.BulkParser
import kotlinx.coroutines.launch

enum class LessonFilter { ALL, TODO, DONE, NEEDS_REVIEW }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubjectDetailScreen(
    subjectId: Long,
    repository: MaterialsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val subject by repository.getSubjectById(subjectId).collectAsState(initial = null)
    val units by repository.getUnitsForSubject(subjectId).collectAsState(initial = emptyList())
    val lessons by repository.getLessonsForSubject(subjectId).collectAsState(initial = emptyList())

    var filter by remember { mutableStateOf(LessonFilter.ALL) }
    var showAddChoice by remember { mutableStateOf(false) }
    var showAddUnitDialog by remember { mutableStateOf(false) }
    var showAddLessonDirectDialog by remember { mutableStateOf(false) }
    var showAddLessonToUnitDialog by remember { mutableStateOf<StudyUnit?>(null) }
    var showBulkDialog by remember { mutableStateOf(false) }
    var showEditUnit by remember { mutableStateOf<StudyUnit?>(null) }
    var showEditLesson by remember { mutableStateOf<Lesson?>(null) }
    var pendingDeleteUnit by remember { mutableStateOf<StudyUnit?>(null) }
    var pendingDeleteLesson by remember { mutableStateOf<Lesson?>(null) }

    val total = lessons.size
    val done = lessons.count { it.status == LessonStatus.DONE }
    val progress = if (total == 0) 0f else done.toFloat() / total

    Scaffold(
        containerColor = IosBackground,
        topBar = {
            TopAppBar(
                title = { Text(subject?.name ?: "المادة", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IosBackground),
                actions = {
                    IconButton(onClick = { showBulkDialog = true }) { Icon(Icons.Default.ListAlt, contentDescription = "إضافة سريعة") }
                    IconButton(onClick = { showAddChoice = true }) { Icon(Icons.Default.Add, null) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Header progress
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IosCard)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("التقدم العام", fontSize = 13.sp, color = IosGray)
                        Text("$done / $total درس مكتمل", fontWeight = FontWeight.Medium)
                    }
                    IosProgressCircle(progress, size = 48)
                }
            }

            // Filter
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val opts = listOf("الكل" to LessonFilter.ALL, "متبقي" to LessonFilter.TODO, "مكتمل" to LessonFilter.DONE, "إعادة" to LessonFilter.NEEDS_REVIEW)
                opts.forEachIndexed { idx, (label, f) ->
                    SegmentedButton(
                        selected = filter == f,
                        onClick = { filter = f },
                        shape = SegmentedButtonDefaults.itemShape(idx, opts.size)
                    ) { Text(label, fontSize = 12.sp) }
                }
            }

            if (units.isEmpty() && lessons.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("لا يوجد دروس", color = IosGray)
                        Spacer(Modifier.height(8.dp))
                        Text("اضغط + لإضافة وحدة أو درس مباشر", fontSize = 12.sp, color = IosGray)
                    }
                }
            } else {
                // نبني قائمة عرض مختلطة حسب order
                val directLessons = lessons.filter { it.unitId == null }
                // ترتيب مختلط: ندمج الوحدات والدروس المباشرة مرتبة حسب order
                val mixed = remember(units, directLessons) {
                    val items = mutableListOf<MixedItem>()
                    units.forEach { items.add(MixedItem.UnitItem(it)) }
                    directLessons.forEach { items.add(MixedItem.DirectLesson(it)) }
                    items.sortedBy { when (it) { is MixedItem.UnitItem -> it.unit.order; is MixedItem.DirectLesson -> it.lesson.order } }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(mixed, key = { when (it) { is MixedItem.UnitItem -> "u_${it.unit.id}"; is MixedItem.DirectLesson -> "l_${it.lesson.id}" } }) { item ->
                        when (item) {
                            is MixedItem.DirectLesson -> {
                                if (matchesFilter(item.lesson.status, filter)) {
                                    LessonRow(
                                        lesson = item.lesson,
                                        onToggle = { scope.launch { repository.cycleLessonStatus(item.lesson) } },
                                        onLongClick = { showEditLesson = item.lesson }
                                    )
                                }
                            }
                            is MixedItem.UnitItem -> {
                                val unit = item.unit
                                val unitLessons = lessons.filter { it.unitId == unit.id }
                                val filtered = unitLessons.filter { matchesFilter(it.status, filter) }
                                // لو الفلتر يخفي كل دروس الوحدة، لا نظهر الوحدة إلا لو ALL
                                if (filtered.isNotEmpty() || filter == LessonFilter.ALL || unitLessons.isEmpty()) {
                                    UnitCard(
                                        unit = unit,
                                        lessons = unitLessons,
                                        filteredLessons = filtered,
                                        filter = filter,
                                        onToggleExpand = { scope.launch { repository.toggleUnitExpanded(unit) } },
                                        onAddLesson = { showAddLessonToUnitDialog = unit },
                                        onLessonToggle = { l -> scope.launch { repository.cycleLessonStatus(l) } },
                                        onLessonLong = { l -> showEditLesson = l },
                                        onUnitLong = { showEditUnit = unit }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // اختيار نوع الإضافة
    if (showAddChoice) {
        ModalBottomSheet(onDismissRequest = { showAddChoice = false }) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("إضافة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(onClick = { showAddChoice = false; showAddUnitDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("وحدة جديدة") }
                Button(onClick = { showAddChoice = false; showAddLessonDirectDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("درس مباشر") }
                OutlinedButton(onClick = { showAddChoice = false; showBulkDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("إضافة سريعة (Bulk)") }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Add Unit
    if (showAddUnitDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddUnitDialog = false },
            title = { Text("وحدة جديدة") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("اسم الوحدة") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isBlank()) return@TextButton
                    scope.launch { repository.addUnit(subjectId, name); showAddUnitDialog = false }
                }) { Text("حفظ", color = IosBlue) }
            },
            dismissButton = { TextButton(onClick = { showAddUnitDialog = false }) { Text("إلغاء") } }
        )
    }

    // Add Direct Lesson
    if (showAddLessonDirectDialog) {
        var title by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddLessonDirectDialog = false },
            title = { Text("درس مباشر") },
            text = { OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text("عنوان الدرس") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isBlank()) return@TextButton
                    scope.launch { repository.addLessonDirect(subjectId, title); showAddLessonDirectDialog = false }
                }) { Text("حفظ", color = IosBlue) }
            },
            dismissButton = { TextButton(onClick = { showAddLessonDirectDialog = false }) { Text("إلغاء") } }
        )
    }

    // Add Lesson to Unit
    if (showAddLessonToUnitDialog != null) {
        var title by remember { mutableStateOf("") }
        val unit = showAddLessonToUnitDialog!!
        AlertDialog(
            onDismissRequest = { showAddLessonToUnitDialog = null },
            title = { Text("درس في: ${unit.name}") },
            text = { OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text("عنوان الدرس") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isBlank()) return@TextButton
                    scope.launch { repository.addLessonToUnit(subjectId, unit.id, title); showAddLessonToUnitDialog = null }
                }) { Text("حفظ", color = IosBlue) }
            },
            dismissButton = { TextButton(onClick = { showAddLessonToUnitDialog = null }) { Text("إلغاء") } }
        )
    }

    // Bulk Dialog
    if (showBulkDialog) {
        var bulkText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBulkDialog = false },
            title = { Text("إضافة سريعة") },
            text = {
                Column {
                    Text("استخدم * لبداية وحدة و - في سطر لوحده لنهايتها", fontSize = 11.sp, color = IosGray)
                    Text("مثال:\n*الاسم\nمبتدا\nخبر\n-\nظن واخواتها", fontSize = 11.sp, color = IosGray, modifier = Modifier.padding(vertical = 4.dp).background(IosBackground, RoundedCornerShape(6.dp)).padding(6.dp))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { bulkText = it },
                        placeholder = { Text("اكتب هنا...") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (bulkText.isBlank()) return@TextButton
                    scope.launch {
                        val nextUnitOrder = (units.maxOfOrNull { it.order } ?: -1) + 1
                        val nextLessonOrder = (lessons.filter { it.unitId == null }.maxOfOrNull { it.order } ?: -1) + 1
                        val parsed = BulkParser.parse(bulkText, subjectId, nextUnitOrder, nextLessonOrder)
                        // إدخال الوحدات أولا وربط الدروس
                        val unitIdMap = mutableMapOf<Long, Long>() // temp negative id -> real id
                        for (u in parsed.units) {
                            val realId = repository.getUnitsForSubjectSync(subjectId) // dummy to avoid unused
                            val insertedId = repository.addUnit(subjectId, u.name)
                            // نحتاج map: parsed lesson unitId = -(index+1) -> insertedId
                            val idx = parsed.units.indexOf(u)
                            unitIdMap[-(idx + 1).toLong()] = insertedId
                        }
                        for (l in parsed.lessons) {
                            val realUnitId = if (l.unitId != null && l.unitId!! < 0) unitIdMap[l.unitId] else null
                            if (realUnitId != null) repository.addLessonToUnit(subjectId, realUnitId, l.title)
                            else repository.addLessonDirect(subjectId, l.title)
                        }
                        showBulkDialog = false
                    }
                }) { Text("إضافة", color = IosBlue) }
            },
            dismissButton = { TextButton(onClick = { showBulkDialog = false }) { Text("إلغاء") } }
        )
    }

    // Edit Unit
    if (showEditUnit != null) {
        val u = showEditUnit!!
        var name by remember(u) { mutableStateOf(u.name) }
        AlertDialog(
            onDismissRequest = { showEditUnit = null },
            title = { Text("تعديل الوحدة") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.updateUnit(u.copy(name = name.trim())); showEditUnit = null }
                }) { Text("حفظ", color = IosBlue) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { pendingDeleteUnit = u; showEditUnit = null }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { showEditUnit = null }) { Text("إلغاء") }
                }
            }
        )
    }

    // Edit Lesson
    if (showEditLesson != null) {
        val l = showEditLesson!!
        var title by remember(l) { mutableStateOf(l.title) }
        AlertDialog(
            onDismissRequest = { showEditLesson = null },
            title = { Text("تعديل الدرس") },
            text = { OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.updateLesson(l.copy(title = title.trim())); showEditLesson = null }
                }) { Text("حفظ", color = IosBlue) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { pendingDeleteLesson = l; showEditLesson = null }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { showEditLesson = null }) { Text("إلغاء") }
                }
            }
        )
    }

    if (pendingDeleteUnit != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteUnit = null },
            title = { Text("حذف الوحدة؟") },
            text = { Text("سيتم حذف كل دروسها") },
            confirmButton = { TextButton(onClick = { scope.launch { repository.deleteUnit(pendingDeleteUnit!!); pendingDeleteUnit = null } }) { Text("حذف", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { pendingDeleteUnit = null }) { Text("إلغاء") } }
        )
    }
    if (pendingDeleteLesson != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteLesson = null },
            title = { Text("حذف الدرس؟") },
            confirmButton = { TextButton(onClick = { scope.launch { repository.deleteLesson(pendingDeleteLesson!!); pendingDeleteLesson = null } }) { Text("حذف", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { pendingDeleteLesson = null }) { Text("إلغاء") } }
        )
    }
}

private sealed class MixedItem {
    data class UnitItem(val unit: StudyUnit) : MixedItem()
    data class DirectLesson(val lesson: Lesson) : MixedItem()
}

private fun matchesFilter(status: LessonStatus, filter: LessonFilter): Boolean = when (filter) {
    LessonFilter.ALL -> true
    LessonFilter.TODO -> status == LessonStatus.TODO
    LessonFilter.DONE -> status == LessonStatus.DONE
    LessonFilter.NEEDS_REVIEW -> status == LessonStatus.NEEDS_REVIEW
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LessonRow(lesson: Lesson, onToggle: () -> Unit, onLongClick: () -> Unit) {
    val (icon, tint, bg) = when (lesson.status) {
        LessonStatus.TODO -> Triple(Icons.Default.Circle, IosGray, Color.Transparent)
        LessonStatus.DONE -> Triple(Icons.Default.CheckCircle, IosGreen, IosGreen.copy(alpha = 0.12f))
        LessonStatus.NEEDS_REVIEW -> Triple(Icons.Default.Refresh, IosOrange, IosOrange.copy(alpha = 0.12f))
    }
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onToggle, onLongClick = onLongClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (bg == Color.Transparent) IosCard else bg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(lesson.title, modifier = Modifier.weight(1f), fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UnitCard(
    unit: StudyUnit,
    lessons: List<Lesson>,
    filteredLessons: List<Lesson>,
    filter: LessonFilter,
    onToggleExpand: () -> Unit,
    onAddLesson: () -> Unit,
    onLessonToggle: (Lesson) -> Unit,
    onLessonLong: (Lesson) -> Unit,
    onUnitLong: () -> Unit
) {
    val total = lessons.size
    val done = lessons.count { it.status == LessonStatus.DONE }
    val progress = if (total == 0) 0f else done.toFloat() / total

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onToggleExpand, onLongClick = onUnitLong),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (unit.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = IosGray)
                Spacer(Modifier.width(6.dp))
                Text(unit.name, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text("$done/$total", fontSize = 12.sp, color = IosGray)
                Spacer(Modifier.width(8.dp))
                IosProgressCircle(progress = progress, size = 28, stroke = 2, showPercent = false)
                IconButton(onClick = onAddLesson, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = IosBlue)
                }
            }
            AnimatedVisibility(visible = unit.isExpanded) {
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (filteredLessons.isEmpty() && lessons.isNotEmpty()) {
                        Text("لا يوجد دروس بهذا الفلتر", fontSize = 12.sp, color = IosGray, modifier = Modifier.padding(start = 8.dp))
                    } else if (filteredLessons.isEmpty()) {
                        Text("لا يوجد دروس - اضغط + لإضافة", fontSize = 12.sp, color = IosGray, modifier = Modifier.padding(start = 8.dp))
                    }
                    filteredLessons.forEach { l ->
                        LessonRow(lesson = l, onToggle = { onLessonToggle(l) }, onLongClick = { onLessonLong(l) })
                    }
                }
            }
        }
    }
}
