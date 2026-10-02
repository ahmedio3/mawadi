package com.mymaterials.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymaterials.app.data.entity.Lesson
import com.mymaterials.app.data.entity.LessonStatus
import com.mymaterials.app.data.entity.StudyUnit
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.components.IosProgressCircle
import com.mymaterials.app.ui.components.ReorderIconButton
import com.mymaterials.app.ui.components.ReorderableLazyColumn
import com.mymaterials.app.ui.components.SkeletonLessonRow
import com.mymaterials.app.ui.theme.AppTheme
import com.mymaterials.app.ui.viewmodels.SubjectDetailViewModel

enum class LessonFilter { ALL, TODO, DONE, NEEDS_REVIEW }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel,
    onBack: () -> Unit
) {
    val subject by viewModel.subject.collectAsState()
    val units by viewModel.units.collectAsState()
    val lessons by viewModel.lessons.collectAsState()

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
    var reorderMode by remember { mutableStateOf(false) }

    val loadedLessons = lessons ?: emptyList()
    val loadedUnits = units ?: emptyList()

    Scaffold(
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (reorderMode) "ترتيب المحتوى" else (subject?.name ?: "المادة"),
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background),
                actions = {
                    if (reorderMode) {
                        TextButton(onClick = { reorderMode = false }) {
                            Text("تم", color = AppTheme.colors.blue, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // زر ترتيب بأيقونة مميزة وخلفية دائرية هادئة
                        ReorderIconButton(
                            onClick = {
                                if (loadedUnits.isNotEmpty() || loadedLessons.isNotEmpty()) reorderMode = true
                            }
                        )
                        IconButton(onClick = { showBulkDialog = true }) {
                            Icon(
                                Icons.Default.ListAlt,
                                contentDescription = "إضافة سريعة",
                                tint = AppTheme.colors.textPrimary
                            )
                        }
                        IconButton(onClick = { showAddChoice = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "إضافة",
                                tint = AppTheme.colors.textPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Filter - مخفي في وضع الترتيب
            if (!reorderMode) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val opts = listOf(
                        "الكل" to LessonFilter.ALL,
                        "متبقي" to LessonFilter.TODO,
                        "مكتمل" to LessonFilter.DONE,
                        "إعادة" to LessonFilter.NEEDS_REVIEW
                    )
                    opts.forEachIndexed { idx, (label, f) ->
                        SegmentedButton(
                            selected = filter == f,
                            onClick = { filter = f },
                            shape = SegmentedButtonDefaults.itemShape(idx, opts.size)
                        ) { Text(label, fontSize = 12.sp) }
                    }
                }
            }

            if (units == null || lessons == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false
                ) {
                    items(5) { SkeletonLessonRow() }
                }
            } else if (loadedUnits.isEmpty() && loadedLessons.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("لا يوجد دروس", color = AppTheme.colors.gray)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "اضغط + لإضافة وحدة أو درس مباشر",
                            fontSize = 12.sp,
                            color = AppTheme.colors.gray
                        )
                    }
                }
            } else if (reorderMode) {
                Text(
                    "اسحب من المقبض: الوحدة تتحرك مع الدروس، ودرس الوحدة يتحرك داخل وحدته فقط",
                    fontSize = 12.sp,
                    color = AppTheme.colors.gray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                val reorderRows = remember(loadedUnits, loadedLessons) {
                    buildReorderRows(loadedUnits, loadedLessons)
                }
                ReorderableLazyColumn(
                    items = reorderRows,
                    key = { row ->
                        when (row) {
                            is ReorderRow.UnitHeader -> "u_${row.unit.id}"
                            is ReorderRow.UnitLesson -> "l_${row.lesson.id}"
                            is ReorderRow.Direct -> "l_${row.lesson.id}"
                        }
                    },
                    canMove = { a, b ->
                        when {
                            a is ReorderRow.UnitLesson && b is ReorderRow.UnitLesson ->
                                a.lesson.unitId == b.lesson.unitId
                            a is ReorderRow.UnitLesson || b is ReorderRow.UnitLesson -> false
                            else -> true
                        }
                    },
                    onCommit = { rows ->
                        val topLevel = mutableListOf<MaterialsRepository.TopLevelItem>()
                        val perUnit = mutableMapOf<Long, MutableList<Lesson>>()
                        var top = 0
                        rows.forEach { row ->
                            when (row) {
                                is ReorderRow.UnitHeader -> topLevel.add(
                                    MaterialsRepository.TopLevelItem.UnitItem(row.unit.copy(order = top++))
                                )
                                is ReorderRow.Direct -> topLevel.add(
                                    MaterialsRepository.TopLevelItem.LessonItem(row.lesson.copy(order = top++))
                                )
                                is ReorderRow.UnitLesson -> {
                                    val uId = row.lesson.unitId!!
                                    perUnit.getOrPut(uId) { mutableListOf() }.add(row.lesson)
                                }
                            }
                        }
                        viewModel.reorderTopLevel(topLevel)
                        perUnit.values.forEach { uLessons ->
                            viewModel.reorderUnitLessons(uLessons)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) { row, _, handle ->
                    when (row) {
                        is ReorderRow.UnitHeader -> {
                            val count = loadedLessons.count { it.unitId == row.unit.id }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            row.unit.name,
                                            fontWeight = FontWeight.Medium,
                                            color = AppTheme.colors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("$count درس", fontSize = 12.sp, color = AppTheme.colors.gray)
                                    }
                                }
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = "سحب للترتيب",
                                    tint = AppTheme.colors.gray,
                                    modifier = handle.size(44.dp).padding(10.dp)
                                )
                            }
                        }
                        is ReorderRow.UnitLesson -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(24.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    LessonRow(lesson = row.lesson, onToggle = {}, onLongClick = {})
                                }
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = "سحب للترتيب",
                                    tint = AppTheme.colors.gray,
                                    modifier = handle.size(44.dp).padding(10.dp)
                                )
                            }
                        }
                        is ReorderRow.Direct -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LessonRow(lesson = row.lesson, onToggle = {}, onLongClick = {})
                                }
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = "سحب للترتيب",
                                    tint = AppTheme.colors.gray,
                                    modifier = handle.size(44.dp).padding(10.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                val directLessons = loadedLessons.filter { it.unitId == null }
                val mixed = remember(loadedUnits, directLessons) {
                    val list = mutableListOf<Pair<Int, MixedItem>>()
                    loadedUnits.forEach { list.add(it.order to MixedItem.UnitItem(it)) }
                    directLessons.forEach { list.add(it.order to MixedItem.DirectLesson(it)) }
                    list.sortBy { it.first }
                    list.map { it.second }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(mixed, key = { item ->
                        when (item) {
                            is MixedItem.UnitItem -> "u_${item.unit.id}"
                            is MixedItem.DirectLesson -> "l_${item.lesson.id}"
                        }
                    }) { item ->
                        when (item) {
                            is MixedItem.UnitItem -> {
                                val unitLessons = loadedLessons.filter { it.unitId == item.unit.id }
                                val filtered = unitLessons.filter { matchesFilter(it.status, filter) }
                                UnitCard(
                                    unit = item.unit,
                                    lessons = unitLessons,
                                    filteredLessons = filtered,
                                    filter = filter,
                                    onToggleExpand = { viewModel.toggleUnitExpanded(item.unit) },
                                    onLessonToggle = { viewModel.cycleLessonStatus(it) },
                                    onLessonLong = { showEditLesson = it },
                                    onUnitLong = { showEditUnit = item.unit }
                                )
                            }
                            is MixedItem.DirectLesson -> {
                                if (matchesFilter(item.lesson.status, filter)) {
                                    LessonRow(
                                        lesson = item.lesson,
                                        onToggle = { viewModel.cycleLessonStatus(item.lesson) },
                                        onLongClick = { showEditLesson = item.lesson }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Choice Sheet
    if (showAddChoice) {
        ModalBottomSheet(
            onDismissRequest = { showAddChoice = false },
            containerColor = AppTheme.colors.card
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "إضافة إلى المادة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { showAddChoice = false; showAddUnitDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.cardSecondary,
                        contentColor = AppTheme.colors.blue
                    )
                ) {
                    Text("وحدة جديدة (تحتوي دروس)")
                }
                Button(
                    onClick = { showAddChoice = false; showAddLessonDirectDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.cardSecondary,
                        contentColor = AppTheme.colors.blue
                    )
                ) {
                    Text("درس مباشر (بدون وحدة)")
                }
                Button(
                    onClick = { showAddChoice = false; showBulkDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.cardSecondary,
                        contentColor = AppTheme.colors.blue
                    )
                ) {
                    Text("إضافة سريعة (نص متعدد)")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Add Unit Dialog
    if (showAddUnitDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddUnitDialog = false },
            title = { Text("وحدة جديدة") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("اسم الوحدة (مثل: الباب الأول)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addUnit(name)
                        showAddUnitDialog = false
                    }
                }) { Text("حفظ", color = AppTheme.colors.blue) }
            },
            dismissButton = {
                TextButton(onClick = { showAddUnitDialog = false }) {
                    Text("إلغاء", color = AppTheme.colors.gray)
                }
            }
        )
    }

    // Add Direct Lesson Dialog
    if (showAddLessonDirectDialog) {
        var title by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddLessonDirectDialog = false },
            title = { Text("درس مباشر") },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("عنوان الدرس") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addLessonDirect(title)
                        showAddLessonDirectDialog = false
                    }
                }) { Text("حفظ", color = AppTheme.colors.blue) }
            },
            dismissButton = {
                TextButton(onClick = { showAddLessonDirectDialog = false }) {
                    Text("إلغاء", color = AppTheme.colors.gray)
                }
            }
        )
    }

    // Add Lesson to Unit Dialog
    if (showAddLessonToUnitDialog != null) {
        val unit = showAddLessonToUnitDialog!!
        var title by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddLessonToUnitDialog = null },
            title = { Text("درس في: ${unit.name}") },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("عنوان الدرس") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addLessonToUnit(unit.id, title)
                        showAddLessonToUnitDialog = null
                    }
                }) { Text("حفظ", color = AppTheme.colors.blue) }
            },
            dismissButton = {
                TextButton(onClick = { showAddLessonToUnitDialog = null }) {
                    Text("إلغاء", color = AppTheme.colors.gray)
                }
            }
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
                    Text(
                        "استخدم * لبداية وحدة و - في سطر لوحده لنهايتها",
                        fontSize = 11.sp,
                        color = AppTheme.colors.gray
                    )
                    Text(
                        "مثال:\n*الاسم\nمبتدا\nخبر\n-\nظن واخواتها",
                        fontSize = 11.sp,
                        color = AppTheme.colors.gray,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .background(AppTheme.colors.cardSecondary, RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { bulkText = it },
                        placeholder = { Text("اكتب هنا...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (bulkText.isNotBlank()) {
                        viewModel.bulkAdd(bulkText) {
                            showBulkDialog = false
                        }
                    }
                }) { Text("إضافة", color = AppTheme.colors.blue) }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDialog = false }) {
                    Text("إلغاء", color = AppTheme.colors.gray)
                }
            }
        )
    }

    // Edit Unit
    if (showEditUnit != null) {
        val u = showEditUnit!!
        var name by remember(u) { mutableStateOf(u.name) }
        AlertDialog(
            onDismissRequest = { showEditUnit = null },
            title = { Text(u.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showAddLessonToUnitDialog = u; showEditUnit = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.cardSecondary,
                            contentColor = AppTheme.colors.blue
                        )
                    ) { Text("إضافة درس في هذه الوحدة") }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("تعديل اسم الوحدة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.updateUnit(u, name)
                        showEditUnit = null
                    }
                }) { Text("حفظ", color = AppTheme.colors.blue) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { pendingDeleteUnit = u; showEditUnit = null }) {
                        Text("حذف", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showEditUnit = null }) {
                        Text("إلغاء", color = AppTheme.colors.gray)
                    }
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
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.updateLesson(l, title)
                        showEditLesson = null
                    }
                }) { Text("حفظ", color = AppTheme.colors.blue) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { pendingDeleteLesson = l; showEditLesson = null }) {
                        Text("حذف", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showEditLesson = null }) {
                        Text("إلغاء", color = AppTheme.colors.gray)
                    }
                }
            }
        )
    }

    if (pendingDeleteUnit != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteUnit = null },
            title = { Text("حذف الوحدة؟") },
            text = { Text("سيتم حذف كل دروسها") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUnit(pendingDeleteUnit!!)
                    pendingDeleteUnit = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteUnit = null }) {
                    Text("إلغاء", color = AppTheme.colors.gray)
                }
            }
        )
    }
    if (pendingDeleteLesson != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteLesson = null },
            title = { Text("حذف الدرس؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLesson(pendingDeleteLesson!!)
                    pendingDeleteLesson = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteLesson = null }) {
                    Text("إلغاء", color = AppTheme.colors.gray)
                }
            }
        )
    }
}

private sealed class MixedItem {
    data class UnitItem(val unit: StudyUnit) : MixedItem()
    data class DirectLesson(val lesson: Lesson) : MixedItem()
}

private sealed interface ReorderRow {
    data class UnitHeader(val unit: StudyUnit) : ReorderRow
    data class UnitLesson(val lesson: Lesson) : ReorderRow
    data class Direct(val lesson: Lesson) : ReorderRow
}

private fun buildReorderRows(units: List<StudyUnit>, lessons: List<Lesson>): List<ReorderRow> {
    val rows = mutableListOf<ReorderRow>()
    val directLessons = lessons.filter { it.unitId == null }
    val mixedUnits = units.sortedBy { it.order }
    val mixedDirect = directLessons.sortedBy { it.order }
    val all = mutableListOf<Pair<Int, ReorderRow>>()
    mixedUnits.forEach { all.add(it.order to ReorderRow.UnitHeader(it)) }
    mixedDirect.forEach { all.add(it.order to ReorderRow.Direct(it)) }
    all.sortBy { it.first }
    all.forEach { (_, row) ->
        rows.add(row)
        if (row is ReorderRow.UnitHeader) {
            lessons.filter { it.unitId == row.unit.id }.sortedBy { it.order }.forEach { lesson ->
                rows.add(ReorderRow.UnitLesson(lesson))
            }
        }
    }
    return rows
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
    val icon = when (lesson.status) {
        LessonStatus.TODO -> Icons.Default.Circle
        LessonStatus.DONE -> Icons.Default.CheckCircle
        LessonStatus.NEEDS_REVIEW -> Icons.Default.Refresh
    }
    val targetTint = when (lesson.status) {
        LessonStatus.TODO -> AppTheme.colors.gray
        LessonStatus.DONE -> AppTheme.colors.green
        LessonStatus.NEEDS_REVIEW -> AppTheme.colors.orange
    }
    val tint by androidx.compose.animation.animateColorAsState(
        targetValue = targetTint,
        animationSpec = tween(durationMillis = 300),
        label = "lessonTint"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(onClick = onToggle, onLongClick = onLongClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                lesson.title,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
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
    onLessonToggle: (Lesson) -> Unit,
    onLessonLong: (Lesson) -> Unit,
    onUnitLong: () -> Unit
) {
    val total = lessons.size
    val done = lessons.count { it.status == LessonStatus.DONE }
    val progress = if (total == 0) 0f else done.toFloat() / total

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onToggleExpand, onLongClick = onUnitLong),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (unit.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.colors.gray
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    unit.name,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text("$done/$total", fontSize = 12.sp, color = AppTheme.colors.gray)
                Spacer(Modifier.width(8.dp))
                IosProgressCircle(progress = progress, size = 28, stroke = 2, showPercent = false)
            }
            AnimatedVisibility(visible = unit.isExpanded) {
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (filteredLessons.isEmpty() && lessons.isNotEmpty()) {
                        Text(
                            "لا يوجد دروس بهذا الفلتر",
                            fontSize = 12.sp,
                            color = AppTheme.colors.gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else if (filteredLessons.isEmpty()) {
                        Text(
                            "لا يوجد دروس - اضغط مطولاً على الوحدة للإضافة",
                            fontSize = 12.sp,
                            color = AppTheme.colors.gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    filteredLessons.forEach { l ->
                        LessonRow(lesson = l, onToggle = { onLessonToggle(l) }, onLongClick = { onLessonLong(l) })
                    }
                }
            }
        }
    }
}
