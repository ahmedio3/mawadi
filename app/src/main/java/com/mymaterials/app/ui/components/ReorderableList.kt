package com.mymaterials.app.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

/**
 * قائمة تدعم إعادة الترتيب بالسحب من مقبض مخصص.
 * - السحب من المقبض فقط، وباقي الصف لا يتأثر.
 * - [canMove] تحدد إن كان مسموحا نقل عنصر من موضع لآخر (مثلا داخل نفس المجموعة فقط).
 * - [onCommit] تُستدعى مرة واحدة عند إفلات العنصر لحفظ الترتيب الجديد.
 */
@Composable
fun <T : Any> ReorderableLazyColumn(
    items: List<T>,
    key: (T) -> Any,
    canMove: (fromItem: T, toItem: T) -> Boolean = { _, _ -> true },
    onCommit: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalSpacing: Dp = 10.dp,
    itemContent: @Composable (item: T, index: Int, dragHandleModifier: Modifier) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val spacingPx = with(LocalDensity.current) { verticalSpacing.toPx() }

    var currentList by remember(items) { mutableStateOf(items) }
    var draggingKey by remember { mutableStateOf<Any?>(null) }
    var draggingIndexState by remember { mutableStateOf(-1) }
    var dragOffsetYState by remember { mutableStateOf(0f) }

    // مراجع محدّثة دائما: كاشف السحب لا يُعاد إنشاؤه، فيقرأ القيم الطازجة من هنا
    val listRef = androidx.compose.runtime.rememberUpdatedState(currentList)
    val indexRef = androidx.compose.runtime.rememberUpdatedState(draggingIndexState)
    val offsetRef = androidx.compose.runtime.rememberUpdatedState(dragOffsetYState)

    // مزامنة القائمة مع البيانات القادمة من الخارج عند عدم السحب
    LaunchedEffect(items) {
        if (draggingKey == null) currentList = items
    }

    fun visibleTop(index: Int): Float? {
        return listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.offset?.toFloat()
    }

    fun visibleHeight(index: Int): Float {
        return listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.size?.toFloat() ?: 200f
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        itemsIndexed(currentList, key = { _, item -> key(item) }) { index, item ->
            val itemKey = key(item)
            val isDragging = draggingKey != null && itemKey == draggingKey

            val handleModifier = Modifier.pointerInput(itemKey) {
                detectDragGestures(
                    onDragStart = {
                        draggingKey = itemKey
                        draggingIndexState = index
                        dragOffsetYState = 0f
                    },
                    onDragCancel = {
                        draggingKey = null
                        draggingIndexState = -1
                        dragOffsetYState = 0f
                    },
                    onDragEnd = {
                        val result = listRef.value
                        draggingKey = null
                        draggingIndexState = -1
                        dragOffsetYState = 0f
                        onCommit(result)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = offsetRef.value + dragAmount.y
                        dragOffsetYState = newOffset
                        val fromIdx = indexRef.value

                        val h = visibleHeight(fromIdx)
                        val top = visibleTop(fromIdx) ?: return@detectDragGestures
                        val center = top + newOffset + h / 2f
                        val info = listState.layoutInfo
                        val visible = info.visibleItemsInfo

                        // تمرير تلقائي عند الاقتراب من الحواف
                        val edge = 140f
                        if (center < info.viewportStartOffset + edge) {
                            scope.launch { listState.scrollBy(-28f) }
                        } else if (center > info.viewportEndOffset - edge) {
                            scope.launch { listState.scrollBy(28f) }
                        }

                        val target = visible.firstOrNull {
                            center >= it.offset && center < it.offset + it.size
                        }?.index
                        val snapshot = listRef.value
                        if (target != null && target != fromIdx && target in snapshot.indices) {
                            if (canMove(snapshot[fromIdx], snapshot[target])) {
                                val mutable = snapshot.toMutableList()
                                val moved = mutable.removeAt(fromIdx)
                                mutable.add(target, moved)
                                currentList = mutable
                                // تعديل الإزاحة بمقدار العنصر المتجاوَز لتبقى الحركة سلسة
                                val stepped = (visible.firstOrNull { it.index == target }?.size?.toFloat() ?: h) + spacingPx
                                dragOffsetYState = if (target > fromIdx) {
                                    offsetRef.value - stepped
                                } else {
                                    offsetRef.value + stepped
                                }
                                draggingIndexState = target
                            }
                        }
                    }
                )
            }

            var rowModifier: Modifier = Modifier
                .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffsetYState else 0f
                        shadowElevation = if (isDragging) 12.dp.toPx() else 0f
                    }
            // العناصر غير المسحوبة تتحرك بحركة ناعمة لمواضعها الجديدة
            if (!isDragging) rowModifier = rowModifier.animateItem()
            androidx.compose.foundation.layout.Box(modifier = rowModifier) {
                itemContent(item, index, handleModifier)
            }
        }
    }
}
