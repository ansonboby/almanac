package com.ansonboby.almanac.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom line-style nav icons for the Field Ledger (DESIGN.md: no stock Material
 * icons). Thin 1.5f stroke, square caps — ink-on-paper register. Pure Canvas so
 * they inherit the caller's tint and stay crisp.
 */
private val ICON_STROKE = 1.5f.dp

@Composable
fun LedgerIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) {
        val s = size.toPx()
        val st = Stroke(width = ICON_STROKE.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
        val p = s * 0.06f
        // book: outer rect + spine + a few led lines
        drawRect(tint, topLeft = Offset(p, p), size = androidx.compose.ui.geometry.Size(s - 2 * p, s - 2 * p), style = st)
        drawLine(tint, Offset(s * 0.36f, p), Offset(s * 0.36f, s - p), st.width)
        drawLine(tint, Offset(p, s * 0.4f), Offset(s - p, s * 0.4f), st.width)
        drawLine(tint, Offset(s * 0.45f, s * 0.6f), Offset(s - p, s * 0.6f), st.width)
    }
}

@Composable
fun StampSheetIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) {
        val s = size.toPx()
        val st = Stroke(width = ICON_STROKE.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
        val p = s * 0.08f
        val cell = (s - 2 * p) / 2
        for (r in 0..1) for (c in 0..1) {
            val x = p + c * cell
            val y = p + r * cell
            drawRect(tint, topLeft = Offset(x + cell * 0.12f, y + cell * 0.12f), size = androidx.compose.ui.geometry.Size(cell * 0.76f, cell * 0.76f), style = st)
        }
    }
}

@Composable
fun InsightsIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) {
        val s = size.toPx()
        val st = Stroke(width = ICON_STROKE.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
        val p = s * 0.08f
        drawLine(tint, Offset(p, p), Offset(p, s - p), st.width)
        drawLine(tint, Offset(p, s - p), Offset(s - p, s - p), st.width)
        val path = Path().apply {
            moveTo(p + s * 0.12f, s * 0.7f)
            lineTo(s * 0.42f, s * 0.4f)
            lineTo(s * 0.62f, s * 0.58f)
            lineTo(s - p, s * 0.28f)
        }
        drawPath(path, color = tint, style = st)
    }
}

@Composable
fun HabitIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) {
        val s = size.toPx()
        val st = Stroke(width = ICON_STROKE.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
        val p = s * 0.12f
        // sprout: a stem with two seed-leaves — a daily practice taking root
        val stemX = s * 0.5f
        drawLine(tint, Offset(stemX, s - p), Offset(stemX, s * 0.34f), st.width)
        drawLine(tint, Offset(stemX, s * 0.5f), Offset(s * 0.26f, s * 0.34f), st.width)
        drawLine(tint, Offset(stemX, s * 0.62f), Offset(s * 0.74f, s * 0.44f), st.width)
        drawCircle(tint, radius = s * 0.07f, center = Offset(stemX, s * 0.3f), style = st)
    }
}

@Composable
fun CogIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) {
        val s = size.toPx()
        val st = Stroke(width = ICON_STROKE.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
        val cx = s / 2f
        val cy = s / 2f
        val r = s * 0.26f
        // gear: circle + 8 teeth
        drawCircle(tint, radius = r, center = Offset(cx, cy), style = st)
        drawCircle(tint, radius = r * 0.4f, center = Offset(cx, cy), style = st)
        for (i in 0 until 8) {
            val a = Math.PI / 4 * i
            val x1 = cx + (r + s * 0.04f) * kotlin.math.cos(a).toFloat()
            val y1 = cy + (r + s * 0.04f) * kotlin.math.sin(a).toFloat()
            val x2 = cx + (r + s * 0.13f) * kotlin.math.cos(a).toFloat()
            val y2 = cy + (r + s * 0.13f) * kotlin.math.sin(a).toFloat()
            drawLine(tint, Offset(x1, y1), Offset(x2, y2), st.width)
        }
    }
}
