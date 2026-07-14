package com.ansonboby.almanac.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.util.LocalDateUtil
import com.ansonboby.almanac.ui.theme.AlmanacTheme
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlin.math.abs

/**
 * The signature Field Ledger element (PRD 3): a hand-inked, rubber-stamp-style
 * date mark. Monospace type, brass ink, and a small deterministic rotation so it
 * reads as "stamped by hand" — the same date always tilts the same way, never
 * jittering on recompose. This is the one decorative device in the app; keep
 * surrounding UI quiet.
 *
 * Two sizes: [DateStamp] (compact, for timeline corners and the Month sheet)
 * and [LargeDateStamp] (zoomed-in, for Entry Detail — PRD: "one stamp, zoomed").
 */
@Composable
fun DateStamp(
    epochDayLocal: Int,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    inkColor: Color = FieldLedgerPalette.Brass,
) {
    val date = remember(epochDayLocal) { LocalDateUtil.toLocalDate(epochDayLocal) }
    val rotation = remember(epochDayLocal) { deterministicRotation(epochDayLocal) }
    val month = date.month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault())
    val day = "%02d".format(date.dayOfMonth)
    val year = date.year.toString()

    val description = stringResource(R.string.cd_date_stamp, "$month $day $year")

    Box(
        modifier = modifier
            .size(size)
            .rotate(rotation)
            .clearAndSetSemantics { contentDescription = description }
            .drawBehind { drawStampBorder(inkColor) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = month, style = StampType.metadata, color = inkColor, textAlign = TextAlign.Center)
            Text(
                text = day,
                style = StampType.stampDate.copy(fontSize = StampType.stampDate.fontSize * 1.7f),
                color = inkColor,
                textAlign = TextAlign.Center,
            )
            Text(text = year, style = StampType.metadata, color = inkColor, textAlign = TextAlign.Center)
        }
    }
}

/** Zoomed stamp for Entry Detail (PRD 6: "Entry Detail view is one stamp, zoomed in"). */
@Composable
fun LargeDateStamp(
    epochDayLocal: Int,
    modifier: Modifier = Modifier,
    inkColor: Color = FieldLedgerPalette.Brass,
) {
    val date = remember(epochDayLocal) { LocalDateUtil.toLocalDate(epochDayLocal) }
    val rotation = remember(epochDayLocal) { deterministicRotation(epochDayLocal) }
    val month = date.month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault())
    val day = "%02d".format(date.dayOfMonth)
    val year = date.year.toString()
    val description = stringResource(R.string.cd_date_stamp, "$month $day $year")

    Box(
        modifier = modifier
            .size(160.dp)
            .rotate(rotation)
            .clearAndSetSemantics { contentDescription = description }
            .drawBehind { drawStampBorder(inkColor, thick = true) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = month, style = StampType.metadata.copy(fontSize = StampType.metadata.fontSize * 1.4f), color = inkColor)
            Text(
                text = day,
                style = StampType.stampDate.copy(fontSize = 44.sp),
                color = inkColor,
                textAlign = TextAlign.Center,
            )
            Text(
                text = year,
                style = StampType.metadata.copy(fontSize = StampType.metadata.fontSize * 1.4f),
                color = inkColor,
            )
        }
    }
}

/** Deterministic small tilt in [-3°, +3°], stable for a given day. */
private fun deterministicRotation(epochDayLocal: Int): Float {
    val seed = abs(epochDayLocal.toLong())
    return ((seed % 7) - 3).toFloat()
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStampBorder(
    ink: Color,
    thick: Boolean = false,
) {
    rotate(degrees = 0f) {
        val inset = size.minDimension * 0.07f
        val corner = size.minDimension * 0.05f
        drawRoundRect(
            color = ink,
            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = Stroke(width = if (thick) size.minDimension * 0.03f else size.minDimension * 0.028f),
        )
        val inset2 = inset + size.minDimension * 0.05f
        drawRoundRect(
            color = ink.copy(alpha = 0.55f),
            topLeft = androidx.compose.ui.geometry.Offset(inset2, inset2),
            size = androidx.compose.ui.geometry.Size(size.width - inset2 * 2, size.height - inset2 * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = Stroke(width = size.minDimension * 0.012f),
        )
    }
}

@Preview
@Composable
private fun DateStampPreview() {
    AlmanacTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DateStamp(epochDayLocal = LocalDateUtil.todayLocalDay(), modifier = Modifier.padding(24.dp))
        }
    }
}
