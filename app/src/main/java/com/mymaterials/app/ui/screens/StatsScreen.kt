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
import com.mymaterials.app.data.entity.Subject
import com.mymaterials.app.ui.components.IosProgressCircle
import com.mymaterials.app.ui.theme.AppTheme
import com.mymaterials.app.ui.viewmodels.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val overallStats by viewModel.overallStats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    Scaffold(
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "الإحصائيات",
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IosProgressCircle(progress = overallStats.progress, size = 80, stroke = 6)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${overallStats.done} / ${overallStats.total}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            "درس مكتمل",
                            fontSize = 13.sp,
                            color = AppTheme.colors.gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${(overallStats.progress * 100).toInt()}% نسبة الإنجاز العام",
                            fontSize = 12.sp,
                            color = AppTheme.colors.gray
                        )
                    }
                }
            }
            item {
                Text(
                    "التقدم لكل مادة",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (subjects.isEmpty()) {
                item {
                    Text(
                        "لا يوجد مواد بعد",
                        color = AppTheme.colors.gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(subjects, key = { it.id }) { subject ->
                    SubjectStatRow(subject = subject, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun SubjectStatRow(subject: Subject, viewModel: StatsViewModel) {
    val lessons by viewModel.getLessonsForSubject(subject.id).collectAsState(initial = emptyList())
    val total = lessons.size
    val done = lessons.count { it.status == LessonStatus.DONE }
    val progress = if (total == 0) 0f else done.toFloat() / total
    val remaining = total - done

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    subject.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    if (total == 0) "لا يوجد دروس" else "متبقي $remaining من $total",
                    fontSize = 12.sp,
                    color = AppTheme.colors.gray
                )
            }
            IosProgressCircle(progress = progress, size = 36)
        }
    }
}
