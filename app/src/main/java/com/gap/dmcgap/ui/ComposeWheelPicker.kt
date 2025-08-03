package com.gap.dmcgap.ui


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
import androidx.compose.ui.draw.clipToBounds
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.rememberPickerState
import kotlinx.coroutines.flow.filter
import androidx.compose.animation.core.exponentialDecay
import androidx.wear.compose.material.PickerDefaults

@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 3,
    itemHeight: Dp = 48.dp,
    recenterKey: Any? = null,
    frictionMultiplier: Float = 0.7f,
    gradientColor: Color = Color(0xFFC49360)
    ) {
    val pickerState = rememberPickerState(
        initialNumberOfOptions  = items.size,
        initiallySelectedOption = selectedIndex
    )

    if (recenterKey != null) {
        LaunchedEffect(recenterKey) {
            pickerState.scrollToOption(selectedIndex)
        }
    }

    // crée ton DecayAnimationSpec
    val decaySpec = exponentialDecay<Float>(
        frictionMultiplier = frictionMultiplier
    )

    // passe-le en position ici
    val flingBehavior = PickerDefaults.flingBehavior(
        pickerState,
        decaySpec
    )

    LaunchedEffect(pickerState) {
        snapshotFlow { pickerState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { inProgress -> !inProgress }
            .collect {
                onSelectedIndexChanged(pickerState.selectedOption)
            }
    }

    Picker(
        state            = pickerState,
        modifier         = modifier
            .height(itemHeight * visibleItemsCount)
            .fillMaxWidth(),
        gradientColor = gradientColor, //(0xFF6650a4),
        flingBehavior    = flingBehavior,
        contentDescription = "Wheel picker"
    ) { optionIndex ->
        Text(
            text      = items[optionIndex],
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines  = 1
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
fun ComposeWheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 48.dp,
    visibleCount: Int = 3,
    textColor: Color
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
                    textAlign = TextAlign.Center,
                    color= textColor
                )
            }
        }
    }
}
