package com.GAP.dmcgap

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.GAP.dmcgap.calculation.BalanceResult
import com.GAP.dmcgap.calculation.DmcConfig
import com.GAP.dmcgap.calculation.calculateBalance
import com.GAP.dmcgap.ui.AbsoluteResultPaxPicker
import com.GAP.dmcgap.ui.AbsoluteResultPicker
import com.GAP.dmcgap.ui.EnvelopeChart
import com.GAP.dmcgap.ui.PickerConfig
import com.GAP.dmcgap.ui.PickerSection
import com.GAP.dmcgap.ui.generateStringList
import kotlinx.coroutines.launch
import com.GAP.dmcgap.ui.IconCheckbox
import com.GAP.dmcgap.ui.theme.OverflowingCheckbox
import com.google.firebase.FirebaseApp
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.GAP.dmcgap.calculation.Distribution
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.text.toDouble




    class BalanceViewModel : ViewModel() {
        private val _result = MutableStateFlow<BalanceResult?>(null)
        val result: StateFlow<BalanceResult?> = _result

        /**
         * Lance le calcul en background et met à jour `result` une seule fois au retour.
         *
         * @param fuelKg            Fuel total embarqué (kg)
         * @param tripFuelKg        Fuel consommé pendant le vol (kg)
         * @param totalPax          Nombre de pax total
         * @param totalFretKg       Poids total fret (kg)
         * @param includePilotSeat  true si 3ème pilote
         * @param includeMec1       true si mécano 1
         * @param includeMec2       true si mécano 2
         * @param includeStrapAr    true si strapontin arrière
         * @param isFret1Fixed      true si on contraint S1
         * @param customFret1Kg     valeur de S1max choisie (kg)
         * @param distributionOverride
         *                          si non-null, on injecte cette répartition « manuelle »
         */

        private val _error = MutableSharedFlow<String>()
        val errors: SharedFlow<String> = _error

        fun calculate(
            fuelKg: Double, tripFuelKg: Double,
            totalPax: Int, totalFretKg: Double,
            includePilotSeat: Boolean,
            includeMec1: Boolean,
            includeMec2: Boolean,
            includeStrapAr: Boolean,
            isFret1Fixed: Boolean,
            customFret1Kg: Double,
            distributionOverride: Distribution? = null,
            config: DmcConfig = DmcConfig()
        ) {
            Log.d("VIEWMODEL_INPUT", """
        paxA: ${distributionOverride?.paxA ?: "null"}
        paxB: ${distributionOverride?.paxB ?: "null"}
        paxC: ${distributionOverride?.paxC ?: "null"}
    """.trimIndent())
            viewModelScope.launch {
                try {
                    val res = calculateBalance(
                        fuelKg               = fuelKg,
                        tripFuelKg           = tripFuelKg,
                        totalPax             = totalPax,
                        totalFretKg          = totalFretKg,
                        includePilotSeat     = includePilotSeat,
                        includeMec1          = includeMec1,
                        includeMec2          = includeMec2,
                        includeStrapAr       = includeStrapAr,
                        isFret1Fixed         = isFret1Fixed,
                        customFret1Kg        = customFret1Kg,
                        config               = config,
                        distributionOverride = distributionOverride
                    )
                    _result.value = res
                } catch (e: IllegalArgumentException) {
                    // on émet juste le message, l'UI l'affichera en Snackbar/Toast
                    _error.emit(e.message ?: "Répartition impossible")
                }
                catch (e: Exception) {
                    _error.emit("Erreur inattendue : ${e.localizedMessage}")
                }
            }
        }
    }



    class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()  // <-- ton composable principal
                }
            }
        }
        val apps = FirebaseApp.getApps(this)
        Log.d("FirebaseCheck", "FirebaseApp instances: $apps")
    }
}


/** Écran principal qui assemble tout. */
@SuppressLint("DefaultLocale")
@Preview(showBackground = true)
@Composable
fun MainScreen(
    viewModel: BalanceViewModel = viewModel()
) {
    Log.d("APP_TEST", "MainScreen recomposed")
    // 1) Récupère le résultat et les éventuelles erreurs du VM
    val context = LocalContext.current
    val result by viewModel.result.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.errors.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // 2) UI states
    var isManual         by rememberSaveable { mutableStateOf(false) }
    var pilotSeatChecked by remember { mutableStateOf(false) }
    var mec1Checked      by rememberSaveable { mutableStateOf(true) }
    var mec2Checked      by rememberSaveable { mutableStateOf(true) }
    var strapArChecked   by remember { mutableStateOf(false) }

    // 3) Config des pickers d’entrée
    val configs = listOf(
        PickerConfig(R.drawable.ic_refuel,    1500..5000, 50),
        PickerConfig(R.drawable.ic_tripfuel,   300..4000, 50),
        PickerConfig(R.drawable.ic_voyageur,      0..52, 1),
        PickerConfig(R.drawable.ic_fret,        0..7000,100)
    )
    val defaultValues = mapOf(
        configs[0] to 2400,
        configs[1] to  800,
        configs[2] to   26,
        configs[3] to 1200
    )
    val stateMap = remember {
        configs.associateWith { cfg ->
            val init = defaultValues[cfg]?.coerceIn(cfg.range) ?: cfg.range.first
            mutableStateOf(init)
        }
    }
    var selectedIcon by remember { mutableStateOf<PickerConfig?>(null) }

    // 4) Index des pickers de résultat (mode manuel)
    // Remplacez toutes les déclarations d'index par :
    var paxAIndex by remember { mutableIntStateOf(0) }
    var paxBIndex by remember { mutableIntStateOf(0) }
    var paxCIndex by remember { mutableIntStateOf(0) }
    var s1Index   by remember { mutableStateOf(0) }
    var s2Index   by remember { mutableStateOf(0) }
    var s3Index   by remember { mutableStateOf(0) }

    // 5) S1 “fixe vs auto”
    var isFixedF1    by remember { mutableStateOf(false) }
    val s1Items      = remember { generateStringList(0..950, step = 50) }
    // À chaque nouvelle valeur de `result`, on affiche le toast
    LaunchedEffect(result) {
        result?.let {
            val message = if (isFixedF1) {
                "Optimisation avec contrainte S1max=${s1Items[s1Index]} kg"
            } else {
                "Optimisation sans contrainte"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    // 6.0) récuperation des donnéees des pickers results pour le mode manuel
        val paxAItems = generateStringList(0..8, step = 1)
        val paxBItems = generateStringList(0..28, step = 1)
        val paxCItems = generateStringList(0..16, step = 1)
        val s2Items    = generateStringList(0..2000, step = 50)
        val s3Items    = generateStringList(0..750,  step = 50)

    // * Valeurs réelles issues des pickers de résultat *
        val manualPaxA  = paxAItems[paxAIndex].toInt()
        val manualPaxB  = paxBItems[paxBIndex].toInt()
        val manualPaxC  = paxCItems[paxCIndex].toInt()
        val manualF1    = s1Items[s1Index].toDouble()
        val manualF2    = s2Items[s2Index].toDouble()
        val manualF3    = s3Items[s3Index].toDouble()



    // 6) Distribution manuelle (ou null si auto)
    val manualDist: Distribution? = if (isManual) Distribution(
        paxA = paxAItems[paxAIndex].toInt(),
        paxB = paxBItems[paxBIndex].toInt(),
        paxC = paxCItems[paxCIndex].toInt(),
        fret1 = s1Items[s1Index].toDouble(),
        fret2 = s2Items[s2Index].toDouble(),
        fret3 = s3Items[s3Index].toDouble(),
        indexTOW      = 0.0,
        cgTOWPourcent = 0.0
    ) else null



    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ─── Fond d’écran ─────────────────────────────────────────────────────
            Image(
                painter        = painterResource(R.drawable.bkgrnd_atr72),
                contentScale   = ContentScale.Crop,
                contentDescription = null,
                modifier       = Modifier.matchParentSize()
            )

            // ─── Switch Mode manuel (en haut-centre) ───────────────────────────────
            Row(
                modifier           = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                verticalAlignment  = Alignment.CenterVertically
            ) {
                Text("Mode manuel")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked         = isManual,
                    onCheckedChange = { isManual = it }
                )
            }

            // ─── Zone pickers + bouton GO (en haut-gauche) ─────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.7f)
            ) {
                PickerSection(
                    configs      = configs,
                    stateMap     = stateMap,
                    selectedIcon = selectedIcon,
                    onIconClick  = { cfg ->
                        selectedIcon = if (selectedIcon == cfg) null else cfg
                    },
                    onGo         = {
                        val currentPaxA = paxAItems[paxAIndex].toInt()
                        val currentPaxB = paxBItems[paxBIndex].toInt()
                        val currentPaxC = paxCItems[paxCIndex].toInt()

                        Log.d("REAL_VALUES", """
                                PAX A: $currentPaxA (index $paxAIndex)
                                PAX B: $currentPaxB (index $paxBIndex)
                                PAX C: $currentPaxC (index $paxCIndex)
                            """.trimIndent()
                        )
                        viewModel.calculate(
                            fuelKg = stateMap[configs[0]]!!.value.toDouble(),
                            tripFuelKg = stateMap[configs[1]]!!.value.toDouble(),
                            totalPax = if (isManual) currentPaxA + currentPaxB + currentPaxC else stateMap[configs[2]]!!.value,
                            totalFretKg = if (isManual) manualF1 + manualF2 + manualF3 else stateMap[configs[3]]!!.value.toDouble(),
                            includePilotSeat = pilotSeatChecked,
                            includeMec1 = mec1Checked,
                            includeMec2 = mec2Checked,
                            includeStrapAr = strapArChecked,
                            isFret1Fixed = isFixedF1,
                            customFret1Kg = if (isFixedF1) s1Items[s1Index].toDouble() else 0.0,
                            distributionOverride = if (isManual) Distribution(
                                paxA = currentPaxA,
                                paxB = currentPaxB,
                                paxC = currentPaxC,
                                fret1 = s1Items[s1Index].toDouble(),
                                fret2 = s2Items[s2Index].toDouble(),
                                fret3 = s3Items[s3Index].toDouble(),
                                indexTOW = 0.0,      // Valeur par défaut
                                cgTOWPourcent = 0.0  // Valeur par défaut
                            ) else null,
                            config = DmcConfig()     // Votre configuration par défaut
                        )
                    },
                    result = result
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth(0.3f)
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconCheckbox(
                    iconRes        = R.drawable.ic_pilote,
                    checked        = pilotSeatChecked,
                    onCheckedChange= { pilotSeatChecked = it }
                )
                Spacer(Modifier.width(8.dp))
                // 2) S1 / Fret1
                result?.let { res ->
                    val resultF1Index = s1Items
                        .indexOf(res.distribution.fret1.toInt().toString())
                        .coerceAtLeast(0)

                    // on choisit l’index affiché (manuel ou issu du calcul)
                    val displayF1Index = if (isManual) s1Index else resultF1Index

                    IconCheckbox(
                        iconRes        = R.drawable.ic_fixedfret,
                        checked        = isFixedF1,
                        onCheckedChange= { isFixedF1 = it },
                        //modifier       = Modifier.absoluteOffset(x = 340.dp, y = 235.dp)
                    )


                    // on met le picker dans un Box à weight(1f) pour qu’il prenne tout l’espace restant
                    //Box(Modifier.weight(1f)) {
                    AbsoluteResultPicker(
                        label = "S1",
                        items = s1Items,
                        selectedIndex = displayF1Index,
                        enabled = isManual || isFixedF1,
                        onIndexChange = { newIndex -> s1Index = newIndex  },
                        showCheckboxSiS1 = true,
                        isChecked = isFixedF1,
                        onCheckedChange = { checked ->
                            isFixedF1 = checked
                            if (!checked && !isManual) {
                                // si on désactive S1 max en mode auto, on remet l’index sur la valeur calculée
                                s1Index = resultF1Index
                            }
                        }
                    )
                    //}

                }

                // 3) S2
                result?.let { res ->
                    LaunchedEffect(paxAIndex, paxBIndex, paxCIndex) {
                        Log.d("STATE_DEBUG", """
        PaxA: $paxAIndex (${paxAItems.getOrNull(paxAIndex)})
        PaxB: $paxBIndex (${paxBItems.getOrNull(paxBIndex)})
        PaxC: $paxCIndex (${paxCItems.getOrNull(paxCIndex)})
    """.trimIndent())
                    }
                    AbsoluteResultPicker(
                        label = "S2",
                        items = generateStringList(0..2100, step = 50),
                        selectedIndex = generateStringList(0..2100 step 50)
                            .indexOf(res.distribution.fret2.toInt().toString())
                            .coerceAtLeast(0),
                        enabled = isManual,
                        onIndexChange = {s2Index = it}
                    )


                    // 4) Pax A/B/C
                    // calculer l’index “à afficher” selon le mode
                    val displayPaxA = if (isManual)
                        paxAIndex
                    else generateStringList(0..8,1)
                        .indexOf(res.distribution.paxA.toString())
                        .coerceAtLeast(0)
                    AbsoluteResultPaxPicker(
                        pickerKey = "paxA_${paxAIndex}_${isManual}",
                        label = "Pax A",
                        items = generateStringList(0..8, step = 1),
                        selectedIndex = displayPaxA,
                        enabled = isManual,
                        onIndexChange = { newIndex -> paxAIndex = newIndex }
                    )
                    val displayPaxB = if (isManual)
                        paxBIndex
                    else
                        generateStringList(0..28, step = 1)
                            .indexOf(res.distribution.paxB.toInt().toString())
                            .coerceAtLeast(0)
                    AbsoluteResultPaxPicker(
                        pickerKey = "paxB_${paxBIndex}_${isManual}",
                        label = "Pax B",
                        items = generateStringList(0..28, step = 1),
                        selectedIndex = displayPaxB,
                        enabled = isManual,
                        onIndexChange = {Log.d("PICKER_DEBUG", "PaxB new index: $it")
                            paxBIndex = it }
                    )

                    val displayPaxC = if (isManual)
                        paxCIndex
                    else
                        generateStringList(0..16, step = 1)
                            .indexOf(res.distribution.paxC.toInt().toString())
                            .coerceAtLeast(0)
                    AbsoluteResultPaxPicker(
                        pickerKey = "paxC_${paxCIndex}_${isManual}",
                        label = "Pax C",
                        items = generateStringList(0..16, step = 1),
                        selectedIndex = displayPaxC,
                        enabled = isManual,
                        onIndexChange = {Log.d("PICKER_DEBUG", "PaxC new index: $it")
                            paxCIndex = it }
                    )
                }

                // 5) Mécanos + S3 en pied
                /*Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {*/
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconCheckbox(
                        iconRes        = R.drawable.ic_mecanot,
                        checked        = mec2Checked,
                        onCheckedChange= { mec2Checked = it }
                    )
                    Spacer(Modifier.width(2.dp))
                    OverflowingCheckbox(
                        checked = mec1Checked,
                        onCheckedChange = { mec1Checked = it }

                    )
                    //Spacer(Modifier.width(8.dp))


                }
                result?.let { res ->
                    AbsoluteResultPicker(
                        label = "S3",
                        items = generateStringList(0..750, step = 50),
                        selectedIndex = generateStringList(0..750 step 50)
                            .indexOf(res.distribution.fret3.toInt().toString())
                            .coerceAtLeast(0),
                        enabled = isManual,
                        onIndexChange = {s3Index = it}
                    )
                }
            }




            // — Carte Résultats (top‑right) —

            result?.let { res ->
                Card(
                    modifier = Modifier
                        .padding(16.dp)      // ton padding existant
                        .offset(y = 36.dp)   // +XX.dp vers le bas
                        .align(Alignment.TopEnd),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(Modifier.padding(12.dp), Arrangement.spacedBy(4.dp)) {
                        Text("ZFM : ${"%.1f".format(res.zfmMass)} kg")
                        Text("CG%Mac : ${"%.1f".format(res.towCg)} ")
                        Text("     Trim    : ${"%.1f".format(res.trim)}")
                    }
                }
            }

            // — EnvelopeChart (bot‑left) —
            EnvelopeChart(
                modifier = Modifier
                    .width(200.dp)
                    .height(250.dp)
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                result = result
            )

        }

    }
}

