package dev.romainguy.graphics.flatpath

import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.PathSegment
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

data class Quadratic(val p0: Offset, val p1: Offset, val p2: Offset)

data class Parabola(val x0: Double, val x2: Double, val scale: Double)

fun subdivide(q: Quadratic, tolerance: Float = 0.5f): FloatArray {
    // TODO: Reuse Parabola
    val p = mapToBasicParabola(q)

    val a0 = fastIntegral(p.x0)
    val a2 = fastIntegral(p.x2)

    val count = ceil(0.5 * abs(a2 - a0) * sqrt(p.scale / tolerance)).toInt()

    val u0 = fastIntegralInverse(a0)
    val u2 = fastIntegralInverse(a2)

    // TODO: Reuse single array of 32 (never seen more than 24 but recursive mode yields up to 32
    //       segments. Either way we need to return the size (count+1).
    val results = FloatArray(count + 1)
    results[0] = 0.0f
    results[count] = 1.0f

    for (i in 1 until count) {
        val u = fastIntegralInverse(a0 + ((a2 - a0) * i) / count)
        results[i] = ((u - u0) / (u2 - u0)).toFloat()
    }

    return results
}

fun segmentsSubdivision(q: Quadratic, tolerance: Float = 0.5f): Array<PathSegment> {
    val subdivisions = subdivide(q, tolerance)

    var lastP = q.p0
    var lastT = 0.0f

    return Array(subdivisions.size - 1) { i ->
        val t = subdivisions[i + 1]
        val p = evaluate(q, t)
        val segment = PathSegment(lastP.toPoint(), lastT, p.toPoint(), t)
        lastP = p
        lastT = t
        segment
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun evaluate(q: Quadratic, t: Float): Offset {
    val a = 1.0f - t
    val x = q.p0.x * a * a + 2.0f * q.p1.x * t * a + q.p2.x * t * t
    val y = q.p0.y * a * a + 2.0f * q.p1.y * t * a + q.p2.y * t * t
    return Offset(x, y)
}

@Suppress("NOTHING_TO_INLINE")
inline fun fastIntegral(x: Double) = x / (0.33 + (0.20151121 + 0.25 * x * x).pow(0.25))

@Suppress("NOTHING_TO_INLINE")
inline fun fastIntegralInverse(x: Double) = x  * (0.41 + sqrt(0.1521 + 0.25 * x * x))

fun mapToBasicParabola(q: Quadratic): Parabola {
    val (x0, y0) = q.p0
    val (x1, y1) = q.p1
    val (x2, y2) = q.p2

    val ddx = 2.0 * x1 - x0 - x2
    val ddy = 2.0 * y1 - y0 - y2

    val u0 = (x1 - x0) * ddx + (y1 - y0) * ddy
    val u2 = (x2 - x1) * ddx + (y2 - y1) * ddy

    val cross = (x2 - x0) * ddy - (y2 - y0) * ddx

    val a0 = u0 / cross
    val a2 = u2 / cross

    val scale = abs(cross) / (hypot(ddx, ddy) * abs(a2 - a0))

    return Parabola(a0, a2, scale)
}