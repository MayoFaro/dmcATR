package com.GAP.dmcgap.ui


import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.ui.layout.ContentScale
import com.GAP.dmcgap.R
import com.GAP.dmcgap.calculation.BalanceResult
import com.GAP.dmcgap.ui.theme.OverflowingCheckbox
import com.GAP.dmcgap.ui.theme.ThemedCheckbox

/**
 * Configuration d'une icône et de sa plage de valeurs.
 */
data class PickerConfig(
    @DrawableRes val iconRes: Int,
    val range: IntRange,
    val step: Int = 1
)
enum class PickerKey { F1, F2, PaxA, PaxB, PaxC, F3 }


@Composable
fun IconPicker(
    @DrawableRes iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val size = if (isSelected) 56.dp else 48.dp
    val border = BorderStroke(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
    )
    Image(
        painter           = painterResource(iconRes),
        contentDescription= null,
        modifier          = modifier
            .size(size)
            .border(border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    )
}

@Composable
fun IconWithAnimatedPicker(
    @DrawableRes iconRes: Int,
    items: List<String>,
    pickerValue: Int,
    onPickerChange: (Int) -> Unit,
    isSelected: Boolean,
    onIconClick: () -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconPicker(iconRes, isSelected, onIconClick)
        Spacer(Modifier.width(8.dp))
        ComposeWheelPicker(
            items         = items,
            selectedIndex = items.indexOf(pickerValue.toString()).coerceAtLeast(0),
            onSelect      = { idx -> onPickerChange(items[idx].toInt()) },
            modifier      = Modifier.width(64.dp),
            itemHeight    = 32.dp,
            visibleCount  = if (isSelected) 3 else 1
        )
    }
}



@Preview(showBackground = true, widthDp = 200, heightDp = 120)
@Composable
fun Preview_IconWithAnimatedPicker()
{
    MaterialTheme {
        // on génère notre liste 1500,1550,…,5000
        val items = remember { generateStringList(1500..5000 step 50) }
        IconWithAnimatedPicker(
            iconRes        = R.drawable.ic_refuel,
            items          = items,
            pickerValue    = 1500,      // valeur initiale (doit exister dans `items`)
            onPickerChange = { /* pas d’action en preview */ },
            isSelected     = true,      // pour voir la roue “déployée”
            onIconClick    = { /* pas d’action */ }
        )
    }
}




@Composable
fun PickerSection(
    modifier     : Modifier = Modifier,
    configs      : List<PickerConfig>,
    stateMap     : Map<PickerConfig, MutableState<Int>>,
    selectedIcon : PickerConfig?,
    onIconClick  : (PickerConfig) -> Unit,
    onGo         :() -> Unit,
    result: BalanceResult?
) {
    Column(
        modifier            = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        configs.forEach { cfg ->
            IconWithAnimatedPicker(
                iconRes        = cfg.iconRes,
                items          = remember(cfg) { generateStringList(cfg.range, cfg.step) },
                pickerValue    = stateMap[cfg]!!.value,
                onPickerChange = { stateMap[cfg]!!.value = it },
                isSelected     = (cfg == selectedIcon),
                onIconClick    = { onIconClick(cfg) }
            )
        }

        Spacer(Modifier.height(6.dp))

        Button(onClick = onGo as () -> Unit) {
            Text("GO !")
        }

        Spacer(Modifier.height(16.dp))

        // ——— La Card des masses —————————————————————
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                Modifier.padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val towMassText   = result?.towMass?.let { "%.0f kg".format(it) } ?: "--"
                val ldwMassText   = result?.ldwMass?.let { "%.0f kg".format(it) } ?: "--"
                val underloadText = result
                    ?.let { "${ "%.0f".format(23000 - it.towMass) } kg" }
                    ?: "--"

                Text("TOW : $towMassText")
                Text("LDW : $ldwMassText")
                Text("Underload  : $underloadText")
            }
        }
    }
}


/** configuration des deux iconescheckboxes pour 3eme pilote et fretF1*/
@Composable
fun IconCheckbox(
    @DrawableRes iconRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(4.dp))
        OverflowingCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange

        )

    }
}

