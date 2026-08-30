package com.mymaterials.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymaterials.app.data.entity.LessonStatus
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.components.IosProgressCircle
import com.mymaterials.app.ui.theme.IosBackground
import com.mymaterials.app.ui.theme.IosCard
import com.mymaterials.app.ui.theme.IosGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(repository: MaterialsRepository, onBack: () -> Unit) {
    val subjects by repository.getAllSubjects().collectAsState(initial = emptyList())

    // نحسب إجمالي عام - نحتاج كل الدروس لكل المواد
    var total by remember { mutableStateOf(0) }
    var done by remember { mutableStateOf(0) }
    LaunchedEffect(subjects) {
        // طريقة بسيطة: نجمع من backup data
        val data = repository.getAllForBackup()
        total = data.lessons.size
        done = data.lessons.count { it.status == LessonStatus.DONE }
    }
    val progress = if (total == 0) 0f else done.toFloat() / total

    Scaffold(
        containerColor = IosBackground,
        topBar = {
            TopAppBar(
                title = { Text("الإحصائيات", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IosBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IosCard),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        IosProgressCircle(progress = progress, size = 80, stroke = 6)
                        Spacer(Modifier.height(12.dp))
                        Text("$done / $total", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("درس مكتمل", fontSize = 13.sp, color = IosGray)
                        Spacer(Modifier.height(8.dp))
                        Text("${(progress * 100).toInt()}% نسبة الإنجاز العام", fontSize = 12.sp, color = IosGray)
                    }
                }
            }
            item {
                Text("التقدم لكل مادة", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }
            if (subjects.isEmpty()) {
                item { Text("لا يوجد مواد بعد", color = IosGray, modifier = Modifier.padding(16.dp)) }
            } else {
                items(subjects, key = { it.id }) { subject ->
                    SubjectStatRow(subject = subject, repository = repository)
                }
            }
        }
    }
}

@Composable
private fun SubjectStatRow(subject: com.mymaterials.app.data.entity.Subject, repository: MaterialsRepository) {
    val lessons by repository.getLessonsForSubject(subject.id).collectAsState(initial = emptyList())
    val total = lessons.size
    val done = lessons.count { it.status == LessonStatus.DONE }
    val progress = if (total == 0) 0f else done.toFloat() / total
    val remaining = total - done

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(subject.name, fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 1)
                Text(if (total == 0) "لا يوجد دروس" else "متبقي $remaining من $total", fontSize = 12.sp, color = IosGray)
            }
            IosProgressCircle(progress = progress, size = 36)
        }
    }
}
