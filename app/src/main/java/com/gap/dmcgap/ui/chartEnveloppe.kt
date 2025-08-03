package com.gap.dmcgap.ui


import android.annotation.SuppressLint
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color



// Data class pour représenter un segment d'enveloppe
data class EnvelopeSegment(
    val column: Int,    // Utilisé pour déterminer le style (pair → pointillé, impair → plein)
    val index1: Float,  // Indice du point de départ (axe x)
    val mass1: Float,   // Masse du point de départ (en tonnes, axe y)
    val index2: Float,  // Indice du point d'arrivée
    val mass2: Float    // Masse du point d'arrivée
)


/**
 * Trace un path reliant les points (indice, masse),
 * avec une couleur et une épaisseur configurables.
 *
 * @param points liste de points (Indice, Masse)
 * @param minIndice, maxIndice : borne de l'axe X (indices)
 * @param minMass, maxMass : borne de l'axe Y (masse)
 * @param color : couleur de la ligne
 * @param strokeWidth : épaisseur de la ligne
 */
fun DrawScope.drawDomainLine(
    points: List<Pair<Float, Float>>,
    minIndice: Float,
    maxIndice: Float,
    minMass: Float,
    maxMass: Float,
    color: Color = Color.Red,
    strokeWidth: Float = 8f
) {
    // Calculer l'étendue
    val indiceRange = maxIndice - minIndice
    val massRange = maxMass - minMass

    // Conversion (Indice, Masse) → Offset(x, y)
    fun convertToCanvasCoords(indice: Float, mass: Float): Offset {
        val x = ((indice - minIndice) / indiceRange) * size.width
        val y = size.height - ((mass - minMass) / massRange) * size.height
        return Offset(x, y)
    }

    if (points.isEmpty()) return

    val path = Path().apply {
        // Aller au premier point
        val (x0, y0) = convertToCanvasCoords(points[0].first, points[0].second)
        moveTo(x0, y0)

        // Relier tous les points suivants
        for (i in 1 until points.size) {
            val (xi, yi) = convertToCanvasCoords(points[i].first, points[i].second)
            lineTo(xi, yi)
        }
    }

    // Dessiner le path
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}



@SuppressLint("DefaultLocale")
@Composable
fun EnvelopeChart(
    modifier: Modifier = Modifier,
    result: com.gap.dmcgap.calculation.BalanceResult? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .width(250.dp)
                .height(300.dp)
                .padding(bottom = 16.dp)
                .align(Alignment.BottomStart)
        ) {
            // --- Début du Canvas scope ---

            val canvasWidth = size.width
            val canvasHeight = size.height

            // bornes X et Y
            val minIndice   = -60f
            val maxIndice   =  60f
            val indiceRange = maxIndice - minIndice

            val minMass   = 12f
            val maxMass   = 24f
            val massRange = maxMass - minMass


            // (1) Quadrillage vertical et horizontal…
            // Lignes horizontales et leurs étiquettes de masse sur la gauche
            val horizontalGridCount = 7
            for (i in 0 until horizontalGridCount) {
                val massValue = minMass + i * (massRange / (horizontalGridCount - 1))
                val yPos = canvasHeight - ((massValue - minMass) / massRange) * canvasHeight
                // Tracer la ligne horizontale
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, yPos),
                    end = Offset(canvasWidth, yPos),
                    strokeWidth = 1f
                )
                // Étiquette de masse (à gauche)
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        textSize = 30f
                        color = android.graphics.Color.BLACK
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawText(
                        String.format("%.1f t", massValue),
                        10f,
                        yPos - 5f,
                        paint
                    )
                }
            }


            val verticalGridCount = 13  // Nombre de ticks sur l'axe horizontal
            for (j in 0 until verticalGridCount) {
                // Calculer l'indice correspondant
                val indiceValue = minIndice + j * (indiceRange / (verticalGridCount - 1))
                val xPos = ((indiceValue - minIndice) / indiceRange) * canvasWidth

                // Tracer la ligne verticale
                drawLine(
                    color = Color.LightGray,
                    start = Offset(xPos, 0f),
                    end = Offset(xPos, canvasHeight),
                    strokeWidth = 1f
                )

                // Étiquette de l'axe inférieur : l'indice
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        textSize = 30f
                        color = android.graphics.Color.BLACK
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawText(
                        String.format("%.0f", indiceValue),
                        xPos - 15f,
                        canvasHeight - 10f,
                        paint
                    )
                }
            }

            // (2) drawDomainLine pour chaque enveloppe…
            val segments = listOf(
                // Colonne 10 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 10,
                    index1 = -27.5f,
                    mass1 = 12f,
                    index2 = -55f,
                    mass2 = 24f
                ),
                // Colonne 11 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 11,
                    index1 = -25.5f,
                    mass1 = 12f,
                    index2 = -52f,
                    mass2 = 24f
                ),
                // Colonne 12 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 12,
                    index1 = -24.0f,
                    mass1 = 12f,
                    index2 = -48f,
                    mass2 = 24f
                ),
                // Colonne 13 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 13,
                    index1 = -22f,
                    mass1 = 12f,
                    index2 = -44f,
                    mass2 = 24f
                ),
                // Colonne 14 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 14,
                    index1 = -20f,
                    mass1 = 12f,
                    index2 = -40.1f,
                    mass2 = 24f
                ),
                // Colonne 15 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 15,
                    index1 = -18f,
                    mass1 = 12f,
                    index2 = -36.5f,
                    mass2 = 24f
                ),
                // Colonne 16 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 16,
                    index1 = -16.3f,
                    mass1 = 12f,
                    index2 = -33f,
                    mass2 = 24f
                ),
                // Colonne 17 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 17,
                    index1 = -14.8f,
                    mass1 = 12f,
                    index2 = -29.2f,
                    mass2 = 24f
                ),
                // Colonne 18 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 18,
                    index1 = -13f,
                    mass1 = 12f,
                    index2 = -25.5f,
                    mass2 = 24f
                ),
                // Colonne 19 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 19,
                    index1 = -11f,
                    mass1 = 12f,
                    index2 = -22f,
                    mass2 = 24f
                ),
                // Colonne 20 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 20,
                    index1 = -9f,
                    mass1 = 12f,
                    index2 = -18.1f,
                    mass2 = 24f
                ),
                // Colonne 21 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 21,
                    index1 = -7.1f,
                    mass1 = 12f,
                    index2 = -15f,
                    mass2 = 24f
                ),
                // Colonne 22 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 22,
                    index1 = -5.2f,
                    mass1 = 12f,
                    index2 = -11.2f,
                    mass2 = 24f
                ),
                // Colonne 23 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 23,
                    index1 = -3.5f,
                    mass1 = 12f,
                    index2 = -7f,
                    mass2 = 24f
                ),
                // Colonne 24 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 24,
                    index1 = -2f,
                    mass1 = 12f,
                    index2 = -3.5f,
                    mass2 = 24f
                ),
                // Colonne 25 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 25,
                    index1 = -0f,
                    mass1 = 12f,
                    index2 = -0f,
                    mass2 = 24f
                ),
                // Colonne 26 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 26,
                    index1 = 2f,
                    mass1 = 12f,
                    index2 = 4f,
                    mass2 = 24f
                ),
                // Colonne 27 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 27,
                    index1 = 4f,
                    mass1 = 12f,
                    index2 = 7.8f,
                    mass2 = 24f
                ),
                // Colonne 28 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 28,
                    index1 = 5.5f,
                    mass1 = 12f,
                    index2 = 11.5f,
                    mass2 = 24f
                ),
                // Colonne 29 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 29,
                    index1 = 7.5f,
                    mass1 = 12f,
                    index2 = 15f,
                    mass2 = 24f
                ),
                // Colonne 30 (pair, donc trait pointillé) : de (-27.5, 12) à (-55, 24)
                EnvelopeSegment(
                    column = 30,
                    index1 = 9.2f,
                    mass1 = 12f,
                    index2 = 18.5f,
                    mass2 = 24f
                ),
                // Colonne 31 (impair, trait plein) : de (-25.5, 12) à (-52, 24)
                EnvelopeSegment(
                    column = 31,
                    index1 = 11f,
                    mass1 = 12f,
                    index2 = 22.2f,
                    mass2 = 24f
                ),
                EnvelopeSegment(
                    column = 32,
                    index1 = 13f,
                    mass1 = 12f,
                    index2 = 26f,
                    mass2 = 24f
                ),
                EnvelopeSegment(
                    column = 33,
                    index1 = 15f,
                    mass1 = 12f,
                    index2 = 29.1f,
                    mass2 = 24f
                ),
                EnvelopeSegment(
                    column = 34,
                    index1 = 17f,
                    mass1 = 12f,
                    index2 = 33f,
                    mass2 = 24f
                ),
                EnvelopeSegment(
                    column = 35,
                    index1 = 18.5f,
                    mass1 = 12f,
                    index2 = 37f,
                    mass2 = 24f
                ),
                EnvelopeSegment(
                    column = 36,
                    index1 = 20f,
                    mass1 = 12f,
                    index2 = 41f,
                    mass2 = 24f
                ),
                EnvelopeSegment(
                    column = 37,
                    index1 = 22f,
                    mass1 = 12f,
                    index2 = 44.5f,
                    mass2 = 24f
                ),
                EnvelopeSegment(
                    column = 38,
                    index1 = 24f,
                    mass1 = 12f,
                    index2 = 48f,
                    mass2 = 24f
                ),
                EnvelopeSegment(
                    column = 39,
                    index1 = 26f,
                    mass1 = 12f,
                    index2 = 51.4f,
                    mass2 = 24f
                ),

                )

            segments.forEach { segment ->
                // Conversion des coordonnées de (indice, masse) en pixels
                val x1 = ((segment.index1 - minIndice) / indiceRange) * canvasWidth
                val y1 = canvasHeight - ((segment.mass1 - minMass) / massRange) * canvasHeight
                val x2 = ((segment.index2 - minIndice) / indiceRange) * canvasWidth
                val y2 = canvasHeight - ((segment.mass2 - minMass) / massRange) * canvasHeight

                // Pour les colonnes paires, appliquer un trait pointillé
                val pathEffect = if (segment.column % 2 == 0) {
                    PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                } else null

                drawLine(
                    color = Color.Blue,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f,
                    pathEffect = pathEffect
                )
            }
            // Ajout des lignes de domaines de vols
            //1) Premier path rouge :
            val points1 = listOf(
                Pair(-27.5f, 12f),
                Pair(-41.2f, 18f),
                Pair(-24.5f, 23f),
                Pair(49.2f, 23f),
                Pair(26f, 12f)
            )
            drawDomainLine(
                points = points1,
                minIndice = -60f,
                maxIndice = 60f,
                minMass = 12f,
                maxMass = 24f,
                color = Color.Black,
                strokeWidth = 10f
            )

            // 2) Deuxième path (ex. Noir) :
            val points2 = listOf(
                Pair(-29.5f, 12.9f),
                Pair(-25f, 15f),
                Pair(-30f, 18f),
                Pair(-15f, 23f),
                Pair(42.5f, 23f),
                Pair(22f, 12f)
            )
            drawDomainLine(
                points = points2,
                minIndice = -60f,
                maxIndice = 60f,
                minMass = 12f,
                maxMass = 24f,
                color = Color.Black,
                strokeWidth = 10f
            )
            // 3) Troisieme enveloppe:Limites OPS :
            val points3 = listOf(
                Pair(-17.5f, 13f),
                Pair(-15f, 14.7f),
                Pair(-21.5f, 18f),
                Pair(-3.5f, 23f),
                Pair(32f, 23f),
                Pair(10f, 13f)
            )
            drawDomainLine(
                points = points3,
                minIndice = -60f,
                maxIndice = 60f,
                minMass = 12f,
                maxMass = 24f,
                color = Color.Green,
                strokeWidth = 13f
            )
            // 4) Limites ZFM LDW :
            val points4 = listOf(
                Pair(-16.8f, 22.35f),
                Pair(41f, 22.35f),
                Pair(38.5f, 21f),
                Pair(-19.5f, 21f),

                )
            drawDomainLine(
                points = points4,
                minIndice = -60f,
                maxIndice = 60f,
                minMass = 12f,
                maxMass = 24f,
                color = Color.Black,
                strokeWidth = 10f
            )

            // (3) Dessin des points de résultat
            result?.let { res ->
                // conversion en tonnes
                val zfmTon = res.zfmMass.toFloat() / 1000f
                val towTon = res.towMass.toFloat() / 1000f
                val ldwTon = res.ldwMass.toFloat() / 1000f

                // liste des (indice, masse) à marquer
                val pts = listOf(
                    res.zfmIdx.toFloat() to zfmTon,
                    res.towIdx.toFloat() to towTon,
                    res.ldwIdx.toFloat() to ldwTon
                )

                fun toCanvas(ind: Float, mas: Float): Offset {
                    val x = ((ind - minIndice) / indiceRange) * size.width
                    val y = size.height - ((mas - minMass) / massRange) * size.height
                    return Offset(x, y)
                }

                listOf(
                    res.zfmIdx.toFloat() to zfmTon,
                    res.towIdx.toFloat() to towTon,
                    res.ldwIdx.toFloat() to ldwTon
                ).forEach { (ind, mas) ->
                    drawCircle(
                        color  = Color.Red,
                        radius = 15f,
                        center = toCanvas(ind, mas)
                    )
                }




            }

        } // <-- Fin du Canvas { … } scope

    } // <-- Fin du Box { … } scope
}     // <-- Fin du @Composable EnvelopeChart()


