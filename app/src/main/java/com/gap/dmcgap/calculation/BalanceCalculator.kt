package com.gap.dmcgap.calculation

import android.util.Log
import kotlin.math.*

// -------------------------------
// Définition des enveloppes
// -------------------------------
private val minEnvelopeOps = listOf(
    13.0 to 21.0,
    18.0 to 18.5,
    23.0 to 24.0
)
private val maxEnvelopeOps = listOf(
    13.0 to 30.0,
    23.0 to 33.5
)

private val minEnvelopeTOW = listOf(
    13.0 to 14.0,
    18.0 to 14.0,
    23.0 to 21.0
)
private val maxEnvelopeTOW = listOf(
    13.0 to 37.0,
    23.0 to 37.0
)

private val minEnvelopeLDM = listOf(
    13.0 to 14.0,
    18.0 to 14.0,
    22.3 to 20.0
)
private val maxEnvelopeLDM = listOf(
    13.0 to 37.0,
    23.0 to 37.0
)

private val minEnvelopeZFM = listOf(
    13.0 to 14.0,
    18.0 to 14.0,
    21.0 to 18.5
)
private val maxEnvelopeZFM = listOf(
    13.0 to 37.0,
    23.0 to 37.0
)

// -------------------------------
// Interpolation linéaire “pièce par pièce”
// -------------------------------
fun interpolate(points: List<Pair<Double, Double>>, massTons: Double): Double {
    if (massTons <= points.first().first) return points.first().second
    if (massTons >= points.last().first) return points.last().second
    for (i in 0 until points.size - 1) {
        val (m0, c0) = points[i]
        val (m1, c1) = points[i + 1]
        if (massTons in m0..m1) {
            val frac = (massTons - m0) / (m1 - m0)
            return c0 + frac * (c1 - c0)
        }
    }
    return points.last().second
}

// -------------------------------
// Fonctions pour récupérer les limites d'enveloppe
// -------------------------------
fun envelopeLimits(
    pointsMin: List<Pair<Double, Double>>,
    pointsMax: List<Pair<Double, Double>>,
    massKg: Double
): Pair<Double, Double> {
    val massTons = massKg / 1000.0
    val low = interpolate(pointsMin, massTons)
    val high = interpolate(pointsMax, massTons)
    return low to high
}

fun envelopeOpsForMassKg(massKg: Double): Pair<Double, Double> =
    envelopeLimits(minEnvelopeOps, maxEnvelopeOps, massKg)

fun envelopeTOWForMassKg(massKg: Double): Pair<Double, Double> =
    envelopeLimits(minEnvelopeTOW, maxEnvelopeTOW, massKg)

fun envelopeLDMForMassKg(massKg: Double): Pair<Double, Double> =
    envelopeLimits(minEnvelopeLDM, maxEnvelopeLDM, massKg)

fun envelopeZFMForMassKg(massKg: Double): Pair<Double, Double> =
    envelopeLimits(minEnvelopeZFM, maxEnvelopeZFM, massKg)

// -------------------------------
// Vérification des enveloppes
// -------------------------------
fun checkEnvelope(cgPercent: Double, massKg: Double, phase: String) {
    val (minOps, maxOps) = envelopeOpsForMassKg(massKg)
    if (cgPercent < minOps || cgPercent > maxOps) {
        val (minCons, maxCons) = when (phase) {
            "TOW" -> envelopeTOWForMassKg(massKg)
            "LDM" -> envelopeLDMForMassKg(massKg)
            "ZFM" -> envelopeZFMForMassKg(massKg)
            else -> throw IllegalArgumentException("Phase inconnue: $phase")
        }
        if (cgPercent < minCons || cgPercent > maxCons) {
            throw RuntimeException(
                "CG en dehors des limites constructeur: " +
                        "${"%.2f".format(minCons)}–${"%.2f".format(maxCons)} %MAC"
            )
        }
    }
}

// -------------------------------
// Pourcentage cible
// -------------------------------
fun getTargetPercentForMass(massKg: Double): Double {
    val massT = massKg / 1000.0
    return when {
        massT <= 18.0 -> 25.5
        massT >= 23.0 -> 29.0
        else -> {
            val frac = (massT - 18.0) / (23.0 - 18.0)
            25.5 + frac * (29.0 - 25.5)
        }
    }
}

// -------------------------------
// Modèles de données
// -------------------------------

data class FuelData(val fuelWeightKg: Double, val effectiveArm: Double)
// -------------------------------
// Calculs de base
// -------------------------------
fun calculerMomentTotal(charges: List<Charge>): Double =
    charges.sumOf { it.masse * it.bras }

fun calculerMasseTotale(charges: List<Charge>): Double =
    charges.sumOf { it.masse }

fun calculerCgMetres(momentTotal: Double, masseTotale: Double): Double =
    momentTotal / masseTotale

fun calculerIndex(charge: Charge, brasReference: Double = 14.18): Double =
    (charge.masse * (charge.bras - brasReference)) / 150.0

fun calculerIndexTotal(charges: List<Charge>, brasReference: Double = 14.18): Double =
    charges.sumOf { calculerIndex(it, brasReference) }

fun calculerCgPourcentMAC(indexTotal: Double, masseTotale: Double): Double =
    (1500.0 * indexTotal) / (0.2303 * masseTotale) + 25.0

// -------------------------------
// Matrice fine du carburant
// -------------------------------
val fuelMatrix = listOf(
    FuelData(78.50, 14.590),
    FuelData(157.00, 14.535),
    FuelData(235.50, 14.505),
    FuelData(314.00, 14.488),
    FuelData(392.50, 14.477),
    FuelData(471.00, 14.470),
    FuelData(549.50, 14.466),
    FuelData(628.00, 14.463),
    FuelData(706.50, 14.460),
    FuelData(785.00, 14.458),
    FuelData(863.50, 14.457),
    FuelData(942.00, 14.456),
    FuelData(1020.50, 14.455),
    FuelData(1099.00, 14.455),
    FuelData(1177.50, 14.455),
    FuelData(1256.00, 14.454),
    FuelData(1334.50, 14.453),
    FuelData(1413.00, 14.452),
    FuelData(1491.50, 14.452),
    FuelData(1570.00, 14.452),
    FuelData(1648.50, 14.452),
    FuelData(1727.00, 14.451),
    FuelData(1805.50, 14.451),
    FuelData(1884.00, 14.450),
    FuelData(1962.50, 14.450),
    FuelData(2041.00, 14.448),
    FuelData(2119.50, 14.446),
    FuelData(2198.00, 14.444),
    FuelData(2276.50, 14.441),
    FuelData(2355.00, 14.438),
    FuelData(2433.50, 14.434),
    FuelData(2500.23, 14.430)
)

// -------------------------------
// Données carburant effectives
// -------------------------------
fun getFuelEffectiveData(fuelWeightKg: Double): FuelData {
    val halfFuel = (fuelWeightKg - 50.0) / 2.0
    if (halfFuel <= fuelMatrix.first().fuelWeightKg)
        return FuelData(fuelWeightKg, fuelMatrix.first().effectiveArm)
    if (halfFuel >= fuelMatrix.last().fuelWeightKg)
        return FuelData(fuelWeightKg, fuelMatrix.last().effectiveArm)
    for (i in 0 until fuelMatrix.size - 1) {
        val upper = fuelMatrix[i + 1]
        if (halfFuel <= upper.fuelWeightKg) {
            return FuelData(fuelWeightKg, upper.effectiveArm)
        }
    }
    return FuelData(fuelWeightKg, fuelMatrix.last().effectiveArm)
}

// -------------------------------
// Optimisation de la distribution pax + fret
// -------------------------------
fun optimizeDistribution(
    totalPax: Int,
    totalFret: Double,
    targetPercent: Double = 29.0,
    maxA: Int = 8, maxB: Int = 28, maxC: Int = 16,
    maxF1: Int = 928, maxF2: Int = 2050, maxF3: Int = 750,
    poidsParPax: Double, brasPAXA: Double, brasPAXB: Double, brasPAXC: Double,
    brasF1: Double, brasF2: Double, brasF3: Double,
    baseCharge: Charge,
    fuelUsableData: FuelData,
    additionalCharges: List<Charge>,
    isFret1Fixed: Boolean = false,
    poidsFret1Max: Double = 750.0,
    tolerance: Double = 1.0   // tolérance de 1% de CG
): Distribution {
    // 1) Préparation
    val fixed = listOf(baseCharge) + additionalCharges
    val totalFretRounded = ceil(totalFret / 50.0) * 50.0
    val actualMaxF1 = if (isFret1Fixed) poidsFret1Max else maxF1.toDouble()

    // 2) Pass 1 : on trouve l’erreur minimale sans contrainte de zones
    var bestErrorPrimary = Double.MAX_VALUE

    for (a in 0..min(totalPax, maxA)) {
        for (b in 0..min(totalPax - a, maxB)) {
            val c = totalPax - a - b
            if (c > maxC) continue

            val paxCharges = listOf(
                Charge("PAX A", a * poidsParPax, brasPAXA),
                Charge("PAX B", b * poidsParPax, brasPAXB),
                Charge("PAX C", c * poidsParPax, brasPAXC)
            )
            val baseList = fixed + paxCharges

            val maxF2i = min(maxF2.toDouble(), totalFretRounded).toInt()
            for (f2 in maxF2i downTo 0 step 50) {
                val remF = totalFretRounded - f2
                val maxF1i = min(actualMaxF1, remF).toInt()
                for (f1 in 0..maxF1i step 50) {
                    val f3 = remF - f1
                    if (f3 < 0.0 || f3 > maxF3.toDouble()) continue

                    val freight = listOf(
                        Charge("Fret1", f1.toDouble(), brasF1),
                        Charge("Fret2", f2.toDouble(), brasF2),
                        Charge("Fret3", f3, brasF3)
                    )
                    val chargesTOW = baseList + freight +
                            Charge(
                                "Fuel usable",
                                fuelUsableData.fuelWeightKg,
                                fuelUsableData.effectiveArm
                            )

                    val mTOW = calculerMasseTotale(chargesTOW)
                    val iTOW = calculerIndexTotal(chargesTOW)
                    val cgP = calculerCgPourcentMAC(iTOW, mTOW)
                    val error = abs(cgP - targetPercent)

                    if (error < bestErrorPrimary) {
                        bestErrorPrimary = error
                    }
                }
            }
        }
    }

    // 3) Pass 2 : on n’autorise que error ≤ bestErrorPrimary + tolerance,
    //    puis on minimise d’abord le nombre de zones de pax, puis l’erreur.
    var best: Distribution? = null
    var bestZonesUsed = Int.MAX_VALUE
    var bestConstrainedError = Double.MAX_VALUE

    for (a in 0..min(totalPax, maxA)) {
        for (b in 0..min(totalPax - a, maxB)) {
            val c = totalPax - a - b
            if (c > maxC) continue

            val paxCharges = listOf(
                Charge("PAX A", a * poidsParPax, brasPAXA),
                Charge("PAX B", b * poidsParPax, brasPAXB),
                Charge("PAX C", c * poidsParPax, brasPAXC)
            )
            val baseList = fixed + paxCharges

            val maxF2i = min(maxF2.toDouble(), totalFretRounded).toInt()
            for (f2 in maxF2i downTo 0 step 50) {
                val remF = totalFretRounded - f2
                val maxF1i = min(actualMaxF1, remF).toInt()
                for (f1 in 0..maxF1i step 50) {
                    val f3 = remF - f1
                    if (f3 < 0.0 || f3 > maxF3.toDouble()) continue

                    val freight = listOf(
                        Charge("Fret1", f1.toDouble(), brasF1),
                        Charge("Fret2", f2.toDouble(), brasF2),
                        Charge("Fret3", f3, brasF3)
                    )
                    val chargesTOW = baseList + freight +
                            Charge(
                                "Fuel usable",
                                fuelUsableData.fuelWeightKg,
                                fuelUsableData.effectiveArm
                            )

                    val mTOW = calculerMasseTotale(chargesTOW)
                    val iTOW = calculerIndexTotal(chargesTOW)
                    val cgP = calculerCgPourcentMAC(iTOW, mTOW)
                    val error = abs(cgP - targetPercent)

                    // si hors tolérance on saute
                    if (error > bestErrorPrimary + tolerance) continue

                    // nb de zones de pax utilisées
                    val zonesUsed = listOf(a, b, c).count { it > 0 }

                    // comparaison lexicographique (zonesUsed, then error)
                    val isBetter = when {
                        zonesUsed < bestZonesUsed -> true
                        zonesUsed == bestZonesUsed && error < bestConstrainedError -> true
                        else -> false
                    }

                    if (isBetter) {
                        bestZonesUsed = zonesUsed
                        bestConstrainedError = error
                        best = Distribution(a, b, c, f1.toDouble(), f2.toDouble(), f3, iTOW, cgP)
                    }
                }
            }
        }
    }


    return best ?: throw IllegalArgumentException(
        buildString {
            append(
                "Impossible de répartir : passagers=$totalPax, fretTotal=${
                    "%.0f".format(
                        totalFret
                    )
                } kg"
            )
            if (isFret1Fixed) {
                append(", contrainte S1max=${"%.0f".format(poidsFret1Max)} kg")
            }
        }
    )
}




// -------------------------------
// Calcul de centrage (intégration UI)
// -------------------------------

/**
 * Résultat complet du calcul de centrage.
 */



/**
 * Calcule ZFM, TOW, LDW, optimise distribution et trim en fonction des pickers.
 */



// … (tes min/max envelopes, interpolate, envelopeLimits, checkEnvelope, getTargetPercentForMass, FuelData, etc.) …

data class Charge(val nom: String, val masse: Double, val bras: Double)
data class Distribution(
    val paxA: Int, val paxB: Int, val paxC: Int,
    val fret1: Double, val fret2: Double, val fret3: Double,
    val indexTOW: Double, val cgTOWPourcent: Double
)
data class BalanceResult(
    val zfmMass: Double,    val zfmCg: Double,    val zfmIdx: Double,
    val towIdx: Double,     val towMass: Double,  val towCg: Double,
    val ldwMass: Double,    val ldwCg: Double,    val ldwIdx: Double,
    val distribution: Distribution,
    val trim: Double, val warnings: List<String>
)
data class DmcConfig(
    val baseMass: Double     = 13461.0,
    val baseArm:  Double     = 14.07,
    val poidsParPax: Double  = 85.0,
    val brasPilot: Double    = 5.50,
    val brasPNC: Double      = 21.448,
    val brasStrapAr: Double  = 22.172,
    val brasMec1: Double     = 21.448,
    val brasMec2: Double     = 21.448,
    val brasPAXA: Double     = 11.923,
    val brasPAXB: Double     = 15.352,
    val brasPAXC: Double     = 19.543,
    val brasF1: Double       = 6.697,
    val brasF2: Double       = 9.637,
    val brasF3: Double       = 23.778,
    val maxZFM: Int = 21000,
    val maxTow: Int = 23000,
    val maxLDW: Int = 22350
)

/**
 * Calcule ZFM, TOW, LDW, optimise (ou non) la distribution et le trim.
 *
 * @param distributionOverride si non-null → mode manuel, on l’utilise tel quel.
 */
fun calculateBalance(
    fuelKg: Double,
    tripFuelKg: Double,
    totalPax: Int,
    totalFretKg: Double,
    includePilotSeat: Boolean,
    includeMec1: Boolean,
    includeMec2: Boolean,
    includeStrapAr: Boolean,
    isFret1Fixed: Boolean,
    customFret1Kg: Double,
    config: DmcConfig = DmcConfig(),
    distributionOverride: Distribution? = null
): BalanceResult
    {

        // au début de calculateBalance()
        val warnings = mutableListOf<String>()
        val isManual = (distributionOverride != null)
                Log.d("BalanceCalc", "calculateBalance called with fuel=$fuelKg, tripFuel=$tripFuelKg, " +
                        "pax=$totalPax, fret=$totalFretKg, isF1Fixed=$isFret1Fixed, customF1=$customFret1Kg, " +
                        "override=${distributionOverride != null}")



        // ─── 1) Base + équipage ─────────────────────────────────────────────────────
                val baseCharge = Charge("Masse de base", config.baseMass, config.baseArm)
                val additional = mutableListOf<Charge>().apply {
                    if (includePilotSeat) add(Charge("3ᵉ pilote", config.poidsParPax, config.brasPilot))
                    if (includeMec1)    add(Charge("Mécanicien 1", config.poidsParPax, config.brasMec1))
                    if (includeMec2)    add(Charge("Mécanicien 2", config.poidsParPax, config.brasMec2))
                    if (includeStrapAr) add(Charge("Strapontin AR", config.poidsParPax, config.brasStrapAr))
                }

                // ─── 2) Fuel usable + restant ────────────────────────────────────────────────
                val fuelUsableData    = getFuelEffectiveData(fuelKg - 50.0)
                val remainingFuelKg   = max(0.0, fuelKg - (tripFuelKg + 50.0))
                val fuelRemainingData = getFuelEffectiveData(remainingFuelKg)

                // ─── 2Bis) Vérification des limites────────────────────────────────────────────
                // --- 1) Calcul du ZFM (Zero Fuel Mass)
                        val paxWeight = totalPax * config.poidsParPax         // ex : 90 kg/pax
                        val crewWeight = (if (includePilotSeat) 1 else 0) * config.poidsParPax   +
                                (if (includeMec1)  1 else 0) * config.poidsParPax  +
                                (if (includeMec2)  1 else 0) * config.poidsParPax   +
                                (if (includeStrapAr) 1 else 0) * config.poidsParPax

                        val zfm = config.baseMass+ paxWeight + totalFretKg + crewWeight
                        Log.d("BalanceCalc", "→ zfm=$zfm, maxZFM=${config.maxZFM}")

                            if (zfm > config.maxZFM) {
                                if (isManual) {
                                    Log.w("BalanceCalc", "⚠️ ZFM = ${zfm.roundToInt()} kg dépasse ${config.maxZFM} kg, mais mode manuel → on continue")
                                } else {
                                    throw IllegalArgumentException(
                                        "Répartition impossible : ZFM = ${zfm.roundToInt()} kg " +
                                                "> masse à vide max ${config.maxZFM} kg."
                                    )
                                }
                            }

                            val tow = zfm + fuelKg
                            if (tow > config.maxTow) {
                                if (isManual) {
                                    Log.w("BalanceCalc", "⚠️ TOW = ${tow.roundToInt()} kg dépasse ${config.maxTow} kg, mais mode manuel → on continue")
                                } else {
                                    throw IllegalArgumentException(
                                        "Répartition impossible : TOW = ${tow.roundToInt()} kg " +
                                                "> masse Décollage max ${config.maxTow} kg."
                                    )
                                }
                            }

                            val ldw = tow - tripFuelKg
                            if (ldw > config.maxLDW) {
                                if (isManual) {
                                    Log.w("BalanceCalc", "⚠️ LDW = ${ldw.roundToInt()} kg dépasse ${config.maxLDW} kg, mais mode manuel → on continue")
                                } else {
                                    throw IllegalArgumentException(
                                        "Répartition impossible : LDW = ${ldw.roundToInt()} kg " +
                                                "> masse Atterrissage max ${config.maxLDW} kg."
                                    )
                                }
                            }


        // ─── 3) Choix de la distribution finale ─────────────────────────────────────
                val dist = distributionOverride ?: run {
                    // 3a) Calcul du target %MAC pour le TOW
                    val targetPercent = getTargetPercentForMass(
                        config.baseMass +
                                totalPax * config.poidsParPax +
                                totalFretKg +
                                additional.sumOf { it.masse } +
                                fuelKg
                    )
                    // 3b) Appel à l’optimiseur
                    optimizeDistribution(
                        totalPax        = totalPax,
                        totalFret       = totalFretKg,
                        targetPercent   = targetPercent,
                        maxA            = 8,   maxB = 28,  maxC = 16,
                        maxF1           = 920, maxF2 = 2050, maxF3 = 750,
                        poidsParPax     = config.poidsParPax,
                        brasPAXA        = config.brasPAXA,
                        brasPAXB        = config.brasPAXB,
                        brasPAXC        = config.brasPAXC,
                        brasF1          = config.brasF1,
                        brasF2          = config.brasF2,
                        brasF3          = config.brasF3,
                        baseCharge      = baseCharge,
                        fuelUsableData  = fuelUsableData,
                        additionalCharges = additional,
                        isFret1Fixed    = isFret1Fixed,
                        poidsFret1Max   = customFret1Kg
                    )
                }
        // 5) Vérifications d’enveloppe, avec gestion du mode manuel
        fun safeCheckEnvelope(cg: Double, mass: Double, phase: String) {
            try {
                checkEnvelope(cg, mass, phase)
            } catch (e: RuntimeException) {
                val msg = "${phase} CG=${"%.2f".format(cg)}% hors enveloppe (${e.message})"
                if (isManual) {
                    warnings += msg  // ← on stocke le warning
                    Log.w("BalanceCalc", msg)
                } else {
                    throw e
                }
            }
        }

                // ─── 4) Reconstruction des listes de charges par phase ──────────────────────
                val paxCharges = listOf(
                    Charge("PAX A", dist.paxA * config.poidsParPax, config.brasPAXA),
                    Charge("PAX B", dist.paxB * config.poidsParPax, config.brasPAXB),
                    Charge("PAX C", dist.paxC * config.poidsParPax, config.brasPAXC)
                )
                val freightCharges = listOf(
                    Charge("Fret 1", dist.fret1, config.brasF1),
                    Charge("Fret 2", dist.fret2, config.brasF2),
                    Charge("Fret 3", dist.fret3, config.brasF3)
                )

                val chargesZFM = listOf(baseCharge) + additional + paxCharges + freightCharges
                val chargesTOW = chargesZFM + Charge("Fuel usable",   fuelUsableData.fuelWeightKg,    fuelUsableData.effectiveArm)
                val chargesLDW = chargesZFM + Charge("Fuel restant",  fuelRemainingData.fuelWeightKg, fuelRemainingData.effectiveArm)


                // ─── 5) Calculs de centrage & enveloppes ────────────────────────────────────
                val zfmMass = calculerMasseTotale(chargesZFM)
                val zfmIdx  = calculerIndexTotal(chargesZFM)
                val zfmCg   = calculerCgPourcentMAC(zfmIdx, zfmMass)
                safeCheckEnvelope(zfmCg, zfmMass, "ZFM")

                val towMass = calculerMasseTotale(chargesTOW)
                val towIdx  = calculerIndexTotal(chargesTOW)
                val towCg   = calculerCgPourcentMAC(towIdx, towMass)
                safeCheckEnvelope(towCg, towMass, "TOW")

                val ldwMass = calculerMasseTotale(chargesLDW)
                val ldwIdx  = calculerIndexTotal(chargesLDW)
                val ldwCg   = calculerCgPourcentMAC(ldwIdx, ldwMass)
                safeCheckEnvelope(ldwCg, ldwMass, "LDM")



                // ─── 6) Trim (linéaire) ──────────────────────────────────────────────────────
                val rawTrim = -0.108695652173913 * towCg + 4.02173913043478
                val trim    = round(rawTrim * 10) / 10.0

                return BalanceResult(
                    zfmMass        = zfmMass,
                    zfmCg          = zfmCg,
                    zfmIdx         = zfmIdx,
                    towIdx         = towIdx,
                    towMass        = towMass,
                    towCg          = towCg,
                    ldwMass        = ldwMass,
                    ldwCg          = ldwCg,
                    ldwIdx         = ldwIdx,
                    distribution   = dist,
                    trim           = trim,
                    warnings     = warnings.toList()
                )
    }
