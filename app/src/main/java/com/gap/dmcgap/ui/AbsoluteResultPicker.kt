package com.gap.dmcgap.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    label: String,
    items: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onIndexChange: (Int) -> Unit,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    // Nouveaux paramètres pour la roue
    recenterKey: Any? = null,
    gradientColor:Color   = Color(0xFF6650a4),
    frictionMultiplier: Float = 1f,
    borderColor: Color = Color.Gray,
) {



    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFFFF8F0),                // un blanc cassé
                shape = RoundedCornerShape(8.dp)
            )
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(4.dp)//moodifie l'espace autour des bordures du picker
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label)
            Log.d(
                "PICKER_F1",
                "modeManual=$enabled — recenterKey=$recenterKey — selectedIndex=$selectedIndex"
            )
            Spacer(Modifier.width(6.dp))


            // La roue
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height( 20.dp*2)       // 2 items visibles * itemHeight
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFCCCCCC), shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                WheelPicker(
                    items                  = items,
                    selectedIndex          = selectedIndex,
                    onSelectedIndexChanged = { newIndex ->
                        Log.d("AbsoluteResultPicker", "S1 wheel → $newIndex")
                        if (enabled) onIndexChange(newIndex) },
                    modifier               = Modifier.fillMaxSize(),
                    visibleItemsCount      = 2,
                    itemHeight             = 42.dp,
                    recenterKey            = recenterKey,
                    gradientColor          = gradientColor,
                    frictionMultiplier     = frictionMultiplier
                )
            }
        }
    }
}




@Composable
fun AbsoluteResultPaxPicker(
    pickerKey     : String,
    label         : String,
    items         : List<String>,
    selectedIndex : Int,
    onIndexChange : (Int) -> Unit,
    enabled       : Boolean,
    recenterKey   : Any? = null,
    gradientColor:Color   = Color(0xFF6650a4),
    borderColor: Color= Color.Gray,
    modifier      : Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFFFF8F0),                // un blanc cassé
                shape = RoundedCornerShape(8.dp)
            )
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(4.dp)//moodifie l'espace autour des bordures du picker
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label)
            Spacer(Modifier.height(4.dp))
            WheelPicker(
                items                    = items,
                selectedIndex            = selectedIndex,
                onSelectedIndexChanged   = onIndexChange,
                modifier                 = Modifier
                    .width(60.dp)
                    .height(40.dp),
                visibleItemsCount        = 3,
                itemHeight               = 40.dp,
                gradientColor = gradientColor,
                recenterKey              = recenterKey
            )
        }
    }
}
