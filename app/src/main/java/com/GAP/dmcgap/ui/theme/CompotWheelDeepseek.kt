package com.GAP.dmcgap.ui.theme
/*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextForegroundStyle.Unspecified.alpha

@Composable
fun ComposeWheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 40.dp,
    visibleCount: Int = 5
) {
    require(visibleCount % 2 == 1) { "visibleCount should be an odd number for balanced display" }

    val visibleHeight = itemHeight * visibleCount
    val middleIndex = visibleCount / 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex - middleIndex)

    Box(modifier = modifier.height(visibleHeight), contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(visibleHeight)
        ) {
            items(items.size) { index ->
                val isSelected = index == selectedIndex
                val scale = if (isSelected) 1f else 0.8f
                val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .animateItemPlacement()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = if (isSelected) 1f else 0.7f
                        }
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        color = color,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Add center indicator lines
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = -itemHeight / 2),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = itemHeight / 2),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        // Detect when scrolling stops to snap to the nearest item
        LaunchedEffect(listState.isScrollInProgress) {
            if (!listState.isScrollInProgress) {
                val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
                if (visibleItemsInfo.isNotEmpty()) {
                    val middleItem = visibleItemsInfo[middleIndex]
                    val targetIndex = middleItem.index
                    if (targetIndex != selectedIndex) {
                        onSelect(targetIndex)
                    }

                    // Snap to center position
                    val centerOffset = visibleHeight / 2 - itemHeight / 2
                    val currentCenterItemOffset = middleItem.offset
                    val scrollCorrection = centerOffset - currentCenterItemOffset
                    listState.animateScrollBy(scrollCorrection.toFloat())
                }
            }
        }
    }
}*/