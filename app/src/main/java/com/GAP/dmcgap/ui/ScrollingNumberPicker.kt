package com.GAP.dmcgap.ui

import android.widget.NumberPicker
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Un composable qui affiche un NumberPicker (roue) Android classique.
 *
 * @param value valeur courante
 * @param range plage de valeurs (min..max)
 * @param onValueChange callback quand l'utilisateur change la valeur
 * @param modifier Modifier pour la taille / le padding / l'alignement
 */


@Composable
fun ScrollingNumberPicker(
    value: Int,
    range: IntRange,
    step: Int = 1,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            NumberPicker(ctx).apply {
                if (step == 1) {
                    minValue = range.first
                    maxValue = range.last
                } else {
                    // on mappe l’indice interne [0..count-1] vers la vraie valeur
                    val count = ((range.last - range.first) / step) + 1
                    minValue = 0
                    maxValue = count - 1
                    displayedValues = Array(count) { i -> "${range.first + i * step}" }
                }
                this.value = if (step == 1) value else (value - range.first) / step
                wrapSelectorWheel = true
                setOnValueChangedListener { _, _, newVal ->
                    onValueChange(
                        if (step == 1) newVal
                        else range.first + newVal * step
                    )
                }
            }
        },
        update = { picker ->
            val target = if (step == 1) value else (value - range.first) / step
            if (picker.value != target) picker.value = target
        }
    )
}
