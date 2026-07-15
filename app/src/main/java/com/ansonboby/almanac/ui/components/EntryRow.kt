package com.ansonboby.almanac.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.util.LocalDateUtil
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

/**
 * One line-item in the daily ledger (PRD 3.3: a vertical ledger line with a thin
 * moss hairline, not floating cards). Composes the date stamp, content
 * (photo-as-pressed-specimen / text), mood glyph, and the archival № + tags.
 *
 * Tapping opens Entry Detail.
 */
@Composable
fun EntryRow(
    entry: Entry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val moss = MaterialTheme.colorScheme.outline
    val seed = remember(entry.id) { (entry.id * 31L % 7).toInt() - 3 }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "entry row" }
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
    ) {
        // Left margin rule — the notebook's red/margin line, here in moss.
        Box(
            Modifier
                .width(2.dp)
                .height(72.dp)
                .background(moss),
        )
        Spacer(Modifier.width(14.dp))

        DateStamp(epochDayLocal = entry.epochDayLocal, size = 64.dp)

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            // Archival № (DESIGN.md voice) + time.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "№ ${entry.archivalNo}",
                    style = StampType.counter,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = "  ·  ${java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                        .format(java.time.Instant.ofEpochMilli(entry.createdAt)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalTime())}",
                    style = StampType.metadata,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(6.dp))

            when {
                entry.photoUri != null -> PhotoSpecimen(
                    uri = entry.photoUri,
                    caption = entry.textContent,
                    seed = seed,
                )
                entry.textContent != null -> Text(
                    text = entry.textContent!!,
                    style = AlmanacTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                entry.moodScore != null -> {
                    val mood = Mood.fromScore(entry.moodScore)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (mood != null) {
                            MoodWeatherGlyph(mood = mood, size = 22.dp, color = mood.tint)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = mood?.label ?: "Mood",
                            style = AlmanacTypography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            if (!entry.tags.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = entry.tags!!,
                    style = AlmanacTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    HorizontalDivider(color = moss.copy(alpha = 0.5f), thickness = 1.dp)
}

/** Photo presented like a tucked/pressed specimen: parchment mat, slight
 *  deterministic tilt, subtle shadow (the one sanctioned place for depth). */
@Composable
private fun PhotoSpecimen(
    uri: String,
    caption: String?,
    seed: Int,
    modifier: Modifier = Modifier,
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current).data(uri).build(),
    )
    Column(modifier) {
            Box(
                Modifier
                    .rotate(seed.toFloat())
                    .shadow(4.dp)
                    .background(FieldLedgerPalette.Parchment)
                    .padding(6.dp),
            ) {
            Image(
                painter = painter,
                contentDescription = stringResource(R.string.cd_entry_photo),
                modifier = Modifier.size(120.dp, 90.dp),
                contentScale = ContentScale.Crop,
            )
        }
        if (!caption.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = caption,
                style = AlmanacTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
