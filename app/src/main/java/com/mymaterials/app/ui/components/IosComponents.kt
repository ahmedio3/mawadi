package com.mymaterials.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
