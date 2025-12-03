package dev.romainguy.graphics.flatpath

import android.graphics.PointF
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathUtils

@Composable
fun QuadEditor(modifier: Modifier, analyticalSubdivision: Boolean, referenceRender: Boolean) {
    var startPoint by remember { mutableStateOf(Offset(0.0f, 0.0f)) }
    var midPoint by remember { mutableStateOf(Offset(0.0f, 0.0f)) }
    var endPoint by remember { mutableStateOf(Offset(0.0f, 0.0f)) }
    var currentControlPoint by remember { mutableIntStateOf(-1) }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val radius = with(density) { 5.dp.toPx() }

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                val hmid = size.width / 2.0f
                val hoffset = size.width / 12.0f
                val start = with(density) { 40.dp.toPx() }
                val voffset = start + with(density) { 60.dp.toPx() }

                startPoint = Offset(hoffset, voffset)
                midPoint = Offset(hmid, start)
                endPoint = Offset(size.width - hoffset, voffset)
            }
            .drawWithCache {
                val baseColor = Color(0.118f, 0.533f, 0.898f, 1.0f)
                val controlColor = Color(0.847f, 0.106f, 0.376f, 1.0f)
                val segmentColor = Color(1.0f, 1.0f, 1.0f, 1.0f)

                val lineSize = 1.dp.toPx()
                val segmentSize = 1.dp.toPx()

                val path = Path().apply {
                    moveTo(startPoint.x, startPoint.y)
                    quadraticTo(
                        midPoint.x, midPoint.y,
                        endPoint.x, endPoint.y
                    )
                }

                val tolerance = 1.0f
                val segments = if (analyticalSubdivision) {
                    segmentsSubdivision(Quadratic(startPoint, midPoint, endPoint), tolerance)
                } else {
                    PathUtils
                        .flatten(path.asAndroidPath(), tolerance)
                        .toTypedArray()
                }
                val label =
                    "${if (analyticalSubdivision) "Analytical" else "Recursive"} = ${segments.size}"

                onDrawBehind {
                    drawLine(
                        controlColor,
                        startPoint,
                        midPoint,
                        lineSize,
                        alpha = 0.6f
                    )
                    drawLine(
                        controlColor,
                        midPoint,
                        endPoint,
                        lineSize,
                        alpha = 0.6f
                    )

                    if (referenceRender) {
                        drawPath(
                            path,
                            baseColor,
                            0.5f,
                            Stroke(radius)
                        )
                    } else {
                        segments.forEach { segment ->
                            drawLine(
                                baseColor,
                                Offset(segment.start.x, segment.start.y),
                                Offset(segment.end.x, segment.end.y),
                                radius,
                                alpha = 0.5f
                            )
                        }
                    }

                    segments.forEach { segment ->
                        drawLine(
                            segmentColor,
                            Offset(segment.start.x, segment.start.y),
                            Offset(segment.end.x, segment.end.y),
                            segmentSize
                        )
                        drawCircle(
                            segmentColor,
                            segmentSize * 2.0f,
                            Offset(segment.end.x, segment.end.y)
                        )
                    }

                    drawCircle(
                        selectColor(currentControlPoint, 0, baseColor, controlColor),
                        selectRadius(currentControlPoint, 0, radius),
                        startPoint
                    )
                    drawCircle(
                        selectColor(currentControlPoint, 1, baseColor, controlColor),
                        selectRadius(currentControlPoint, 1, radius),
                        midPoint
                    )
                    drawCircle(
                        selectColor(currentControlPoint, 2, baseColor, controlColor),
                        selectRadius(currentControlPoint, 2, radius),
                        endPoint
                    )

                    drawText(textMeasurer, label, Offset(16.dp.toPx(), 16.dp.toPx()))
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val dragStart = down.position
                    currentControlPoint = if (inCircle(dragStart, startPoint, radius)) {
                        0
                    } else if (inCircle(dragStart, midPoint, radius)) {
                        1
                    } else if (inCircle(dragStart, endPoint, radius)) {
                        2
                    } else -1

                    var lastPosition = dragStart
                    drag(down.id) { change ->
                        change.consume()

                        val dragAmount = change.position - lastPosition
                        when (currentControlPoint) {
                            0 -> startPoint += dragAmount
                            1 -> midPoint += dragAmount
                            2 -> endPoint += dragAmount
                        }
                        lastPosition = change.position
                    }

                    currentControlPoint = -1
                }
            }
    )
}

fun selectColor(currentControlPoint: Int, index: Int, baseColor: Color, activeColor: Color) =
    if (currentControlPoint == index) activeColor else baseColor

fun selectRadius(currentControlPoint: Int, index: Int, radius: Float) =
    if (currentControlPoint == index) radius * 1.1f else radius

fun inCircle(p: Offset, center: Offset, radius: Float): Boolean {
    val d = (p - center)
    return d.x * d.x + d.y * d.y < radius * radius
}

@Suppress("NOTHING_TO_INLINE")
inline fun Offset.toPoint() = PointF(x, y)
