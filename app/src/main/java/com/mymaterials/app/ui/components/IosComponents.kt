package com.mymaterials.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mymaterials.app.ui.theme.IosBlue
import com.mymaterials.app.ui.theme.IosCard
import com.mymaterials.app.ui.theme.IosGray
import com.mymaterials.app.ui.theme.IosGreen
import com.mymaterials.app.ui.theme.IosOrange

@Composable
fun IosProgressCircle(
    progress: Float, // 0..1
    size: Int = 36,
    stroke: Int = 3,
    showPercent: Boolean = true,
    modifier: Modifier = Modifier
) {
    val color = when {
        progress >= 1f -> IosGreen
        progress > 0f -> IosBlue
        else -> IosGray.copy(alpha = 0.3f)
    }
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size.dp),
            color = color,
            strokeWidth = stroke.dp,
            trackColor = IosGray.copy(alpha = 0.15f)
        )
        if (showPercent) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = (if (size <= 28) 8 else 10).sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val base = IosGray.copy(alpha = 0.18f)
    val highlight = IosGray.copy(alpha = 0.08f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate - 300f, 0f),
        end = Offset(translate, 0f)
    )
}

@Composable
fun SkeletonSubjectCard() {
    val brush = shimmerBrush()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.55f).height(18.dp)
                        .clip(RoundedCornerShape(5.dp)).background(brush)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(0.35f).height(13.dp)
                        .clip(RoundedCornerShape(5.dp)).background(brush)
                )
            }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(brush)
            )
        }
    }
}

@Composable
fun SkeletonLessonRow() {
    val brush = shimmerBrush()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(brush))
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(0.6f).height(15.dp)
                    .clip(RoundedCornerShape(5.dp)).background(brush)
            )
        }
    }
}

@Composable
fun IosCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(IosCard)
    ) {
        content()
    }
}
