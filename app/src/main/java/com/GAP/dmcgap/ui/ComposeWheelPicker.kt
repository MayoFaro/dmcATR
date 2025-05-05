package com.GAP.dmcgap.ui

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clipToBounds
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.foundation.layout.fillMaxSize
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.rememberPickerState


@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 3,
    itemHeight: Dp = 48.dp
) {
    val pickerState = rememberPickerState(
        initialNumberOfOptions = items.size,
        initiallySelectedOption = selectedIndex
    )

    // Synchronisation avec l'état externe
    LaunchedEffect(selectedIndex) {
        Log.d("PICKER_FLOW", "External change: $selectedIndex")
        if (pickerState.selectedOption != selectedIndex) {
            pickerState.scrollToOption(selectedIndex)
        }
    }

    // Mise à jour de l'état lorsqu'on scroll
    LaunchedEffect(pickerState) {
        snapshotFlow { pickerState.selectedOption }
            .distinctUntilChanged()
            .collect { index ->
                Log.d("PICKER_FLOW", "Internal change: $index")
                if (index in items.indices && index != selectedIndex) {
                    onSelectedIndexChanged(index)
                }
            }
    }
    // Log du rendu
    Log.d("PICKER_RENDER", "Rendering with index=${pickerState.selectedOption}")
    Picker(
        state = pickerState,
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .fillMaxWidth(),
        contentDescription = "Wheel Picker"
    ) { optionIndex ->
        Log.d("PICKER_ITEM", "Drawing item $optionIndex")
        Text(
            text = items[optionIndex],
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
    }
}
/**
 * Génère une liste de chaînes à partir d'une plage et d'un pas.
 */
fun generateStringList(range: IntRange, step: Int = 1): List<String> =
    (range.first..range.last step step).map { it.toString() }

// Nouvelle surcharge pour IntProgression
fun generateStringList(progression: IntProgression): List<String> =
    progression.map { it.toString() }

/**
 * Roue Compose 100% native avec snapping et animation de mise en avant.
 */



@Composable
fun ComposeWheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 48.dp,
    visibleCount: Int = 3
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Scroll programmatique
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    // Détection de l'item centré
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val centerLine = listState.layoutInfo.viewportStartOffset +
                            listState.layoutInfo.viewportSize.height / 2
                    val centeredItem = visibleItems.minByOrNull { item ->
                        abs(item.offset + item.size / 2 - centerLine)
                    }

                    centeredItem?.let { item ->
                        if (item.index in items.indices) {
                            onSelect(item.index)
                        }
                    }
                }
            }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(itemHeight * visibleCount)
            .wrapContentWidth(),
        contentPadding = PaddingValues(vertical = (itemHeight * (visibleCount - 1) / 2)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        itemsIndexed(items) { index, item ->
            val centered by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val centerLine = layoutInfo.viewportStartOffset +
                            layoutInfo.viewportSize.height / 2
                    val itemOffset = layoutInfo.visibleItemsInfo
                        .firstOrNull { it.index == index }
                        ?.let { it.offset + it.size / 2 } ?: 0
                    abs(itemOffset - centerLine) < with(density) { itemHeight.toPx() } / 2
                }
            }

            val scale by animateFloatAsState(
                targetValue = if (centered) 1.2f else 1f,
                animationSpec = tween(durationMillis = 200)
            )

            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                        onSelect(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    fontSize = if (centered) 16.sp else 12.sp,
                    fontWeight = if (centered) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 120,
    heightDp = 300,
    name = "Aperçu WheelPicker"
)
@Composable
fun Preview_WheelPicker() {
    ComposeWheelPicker(
        items         = List(20) { (it * 50).toString() },
        selectedIndex = 5,
        onSelect      = { /* rien */ },
        modifier      = Modifier.width(100.dp),
        itemHeight    = 20.dp,
        visibleCount  = 1
    )
}


@Composable
fun ComposeWheelAbPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 24.dp    // ← ajuste ici pour assez de place
) {
    // Toujours une seule ligne visible
    val visibleCount = 1
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // ① Quand selectedIndex change, on scroll directement
    LaunchedEffect(selectedIndex) {
        listState.scrollToItem(selectedIndex)
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(itemHeight * visibleCount)
            .wrapContentWidth()
            .clipToBounds(),
        contentPadding = PaddingValues(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        itemsIndexed(items) { index, item ->
            val centered = listState.firstVisibleItemIndex == index
            Box(
                Modifier
                    .height(itemHeight)
                    .wrapContentWidth()
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = item,
                    fontSize   = if (centered) 14.sp else 12.sp,
                    fontWeight = if (centered) FontWeight.Bold else FontWeight.Normal,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.width(itemHeight * 2) // fixe une largeur maxi
                )
            }
        }
    }

    // ② Écoute des snaps pour notifier la sélection
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest(onSelect)
    }
}
@Composable
fun ComposeWheelCenteredPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 24.dp,
    visibleCount: Int = 3
) {
    val listState = rememberLazyListState()
    // snapBehavior identique
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    // la « moitié » de visibleCount
    val half = visibleCount / 2

    // convertit Dp→px
    val offsetPx = with(LocalDensity.current) { (itemHeight * half).roundToPx() }

    // dès que selectedIndex change, on scroll pour le mettre à la ligne 2
    LaunchedEffect(selectedIndex) {
        listState.scrollToItem(selectedIndex, scrollOffset = offsetPx)
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(itemHeight * visibleCount)
            .wrapContentWidth()
            .clipToBounds(),                  // on clippe tout dépassement
        contentPadding = PaddingValues(vertical = itemHeight * half),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        itemsIndexed(items) { index, item ->
            val centered = index == selectedIndex
            Box(
                Modifier
                    .height(itemHeight)
                    .wrapContentWidth()
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = item,
                    fontSize   = if (centered) 16.sp else 14.sp,
                    fontWeight = if (centered) FontWeight.Bold else FontWeight.Normal,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.width(itemHeight * 2)
                )
            }
        }
    }

    // on notifie le nouvel index quand on snap
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest(onSelect)
    }
}
