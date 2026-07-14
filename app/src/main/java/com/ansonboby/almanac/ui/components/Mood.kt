package com.ansonboby.almanac.ui.components

import androidx.compose.ui.graphics.Color

/**
 * Weather-report mood register (PRD 3.3: "mood as weather, not emoji"). Five
 * glyphs mapped from the stored [Int] score -2..+2. Each carries a label, a
 * contentDescription (informational, not decorative — PRD 7 accessibility),
 * and a tint so the Month grid can color a day by its mood without relying on
 * the glyph shape alone (no color-only signaling).
 */
enum class Mood(
    val score: Int,
    val label: String,
    val description: String,
    val tint: Color,
) {
    STORM(-2, "Storm", "Stormy — a heavy day", Color(0xFF8C7E8E)),
    CLOUDY(-1, "Cloudy", "Cloudy — low and grey", Color(0xFF9AA0A6)),
    FAIR(0, "Fair", "Fair — even, unmarked", Color(0xFF6B7A5A)),
    CLEAR(1, "Clear", "Clear — bright and steady", Color(0xFFB8934A)),
    RADIANT(2, "Radiant", "Radiant — wide open", Color(0xFFC08B7A)),
    ;

    companion object {
        fun fromScore(score: Int?): Mood? =
            entries.firstOrNull { it.score == (score ?: return null) }

        val all: List<Mood> get() = entries.toList()
    }
}
