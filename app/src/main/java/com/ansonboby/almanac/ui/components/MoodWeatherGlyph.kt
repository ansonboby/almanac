package com.ansonboby.almanac.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom hand-drawn-style weather glyph (DESIGN.md: "don't use stock Material
 * icons for the mood glyphs — those need custom treatment"). Pure Compose Canvas
 * so it inherits theme color and scales crisply. Always carries a
 * contentDescription via the surrounding semantics — informational, not decor.
 */
@Composable
fun MoodWeatherGlyph(
    mood: Mood,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    color: Color = mood.tint,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val stroke = Stroke(width = w * 0.07f)
        val c = center
        when (mood) {
            Mood.RADIANT, Mood.CLEAR -> {
                // Sun: disc + eight rays.
                val r = w * 0.22f
                drawCircle(color, radius = r, center = c, style = stroke)
                val rayLen = w * 0.16f
                for (i in 0 until 8) {
                    val a = Math.PI / 4 * i
                    val x1 = c.x + (r + w * 0.06f) * kotlin.math.cos(a)
                    val y1 = c.y + (r + w * 0.06f) * kotlin.math.sin(a)
                    val x2 = c.x + (r + w * 0.06f + rayLen) * kotlin.math.cos(a)
                    val y2 = c.y + (r + w * 0.06f + rayLen) * kotlin.math.sin(a)
                    drawLine(color, Offset(x1.toFloat(), y1.toFloat()), Offset(x2.toFloat(), y2.toFloat()), stroke.width)
                }
            }
            Mood.FAIR -> {
                // Single soft cloud outline.
                drawCloud(c, w, color, stroke, squash = 1.15f)
            }
            Mood.CLOUDY -> {
                // Two overlapping clouds.
                drawCloud(Offset(c.x - w * 0.08f, c.y + w * 0.04f), w * 0.86f, color, stroke, 1.1f)
                drawCloud(Offset(c.x + w * 0.12f, c.y - w * 0.06f), w * 0.7f, color, stroke, 1.1f)
            }
            Mood.STORM -> {
                // Cloud + three rain strokes.
                drawCloud(c, w * 0.9f, color, stroke, 1.1f)
                val baseY = c.y + w * 0.22f
                for (i in -1..1) {
                    val x = c.x + i * w * 0.16f
                    drawLine(
                        color,
                        Offset(x, baseY),
                        Offset(x - w * 0.05f, baseY + w * 0.18f),
                        stroke.width,
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(
    center: Offset,
    w: Float,
    color: Color,
    stroke: Stroke,
    squash: Float,
) {
    val r = w * 0.2f
    val p = Path()
    p.addOval(androidx.compose.ui.geometry.Rect(center.x - r * 1.6f, center.y - r * 0.2f, center.x - r * 0.2f, center.y + r * squash))
    p.addOval(androidx.compose.ui.geometry.Rect(center.x - r * 0.6f, center.y - r * 0.7f, center.x + r * 0.8f, center.y + r * 0.7f * squash))
    p.addOval(androidx.compose.ui.geometry.Rect(center.x + r * 0.4f, center.y - r * 0.2f, center.x + r * 1.8f, center.y + r * squash))
    p.addOval(androidx.compose.ui.geometry.Rect(center.x - r * 1.4f, center.y + r * 0.1f, center.x + r * 1.6f, center.y + r * 1.0f * squash))
    drawPath(p, color, style = stroke)
}
