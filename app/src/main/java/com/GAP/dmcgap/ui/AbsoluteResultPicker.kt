package com.GAP.dmcgap.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.wear.compose.material.rememberPickerState
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Affiche un label, un ComposeWheelPicker à la position (x,y) fixes,
 * et optionnellement une Checkbox pour S1.
 *
 * @param x, y Position absolue en dp
 * @param label texte à gauche du picker
 * @param items liste des valeurs en String
 * @param selectedIndex index sélectionné dans [items]
 * @param enabled si false, on désactive l’interaction et on force l’affichage du résultat
 * @param onIndexChange callback quand l’utilisateur change la valeur (uniquement si enabled==true)
 * @param showCheckboxSiS1 si vrai, on affiche en plus la checkbox “cocheFret1”
 * @param isChecked état de cette checkbox
 * @param onCheckedChange callback quand on coche/décoche
 */
@Composable
fun AbsoluteResultPicker(
    label:String,
    items: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onIndexChange: (Int) -> Unit,
    showCheckboxSiS1: Boolean = false,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Box(
        modifier = Modifier
            //.absoluteOffset(x, y)
            .background(
                color = Color(0xFFFFF8F0),                // un blanc cassé
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
            .padding(4.dp)//moodifie l'espace autour des bordures du picker
    ) {
        Row(verticalAlignment = Alignment.CenterVertically)
            {
                Text(label, /* … */)
                Spacer(Modifier.width(6.dp))//modifie l'espace entre le label et le picker

                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 20.dp * 2) // largeur fixe, hauteur = itemHeight
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(4.dp))
                ) {
                    ComposeWheelAbPicker(
                        items         = items,
                        selectedIndex = selectedIndex,
                        onSelect      = { if (enabled) onIndexChange(it) },
                        modifier      = Modifier.fillMaxSize(),
                        itemHeight    = 42.dp
                    )
                }
            }


    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 160)
@Composable
fun Preview_AbsoluteResultPicker() {
    MaterialTheme {
        // Construis une petite liste pour l’exemple (0, 50, 100, … 750)
        val items = remember { generateStringList(0..950 step 50) }
        AbsoluteResultPicker(
            label             = "S1",
            items             = items,
            selectedIndex     = 3,          // par exemple 100 kg (3×50)
            enabled           = true,       // pour voir le wheel déployé
            onIndexChange     = { /* no-op en preview */ },
            showCheckboxSiS1  = true,       // affiche la checkbox
            isChecked         = false,      // décochée par défaut
            onCheckedChange   = { /* no-op */ }
        )
    }
}






@Composable
fun AbsoluteResultPaxPicker(
    pickerKey: String, // Clé unique pour la recomposition
    label: String,
    items: List<String>,
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
)
    {
        Log.d("PICKER_PROPS", """
            label: $label
            selectedIndex: $selectedIndex
            enabled: $enabled
        """.trimIndent())
        // 1. Gestion d'état avec synchronisation
        val pickerState = rememberPickerState(
            initialNumberOfOptions = items.size,
            initiallySelectedOption = selectedIndex
        )

        // 2. Synchronisation externe -> interne
        LaunchedEffect(selectedIndex) {
            if (pickerState.selectedOption != selectedIndex) {
                pickerState.scrollToOption(selectedIndex)
            }
        }

        // 3. Synchronisation interne -> externe
        /*LaunchedEffect(pickerState) {
            snapshotFlow { pickerState.selectedOption }
                .distinctUntilChanged()
                .collect { index ->
                    if (enabled && index in items.indices) {
                        onIndexChange(index)
                    }
                }
        }*/
        LaunchedEffect(pickerState) {
            snapshotFlow { pickerState.selectedOption }
                .distinctUntilChanged()
                .collect { idx ->
                    if (enabled) {
                        onIndexChange(idx)
                    }
                }
        }

        // 4. UI avec recomposition forcée
        Box(
            modifier
                .background(Color(0xFFFFF8F0), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label)
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(width = 30.dp, height = 40.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(4.dp))
                ) {
                        WheelPicker(
                            items = items,
                            selectedIndex = pickerState.selectedOption, // Utilise l'état interne
                            onSelectedIndexChanged = { /* Géré par le snapshotFlow */ },
                            modifier = Modifier.fillMaxSize(),
                            itemHeight = 20.dp,
                            visibleItemsCount = 2
                        )
                }
            }
        }
    }
