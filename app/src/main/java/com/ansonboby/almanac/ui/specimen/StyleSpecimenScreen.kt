package com.ansonboby.almanac.ui.specimen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ansonboby.almanac.R
import com.ansonboby.almanac.ui.components.DateStamp
import com.ansonboby.almanac.ui.theme.AlmanacTheme
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType
import java.time.LocalDate

/**
 * Phase 0 sanity screen: a visual specimen of the Field Ledger design system —
 * palette, type scale, and the signature date stamp. It lets the aesthetic be
 * verified before any real features are built. Not a shipping screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleSpecimenScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // ---- Title block ----------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.specimen_title),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.specimen_subtitle),
                        style = StampType.metadata,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Text(
                    text = if (darkTheme) "INK ●" else "PARCHMENT ○",
                    style = StampType.counter,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(3.dp),
                        )
                        .clickable(onClickLabel = stringResource(R.string.cd_theme_toggle)) {
                            onToggleTheme()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }

            SectionRule()

            // ---- Palette ---------------------------------------------------
            SectionHeader(stringResource(R.string.specimen_palette_header))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Swatch("ink", "#22261F", FieldLedgerPalette.Ink)
                Swatch("parchment", "#E9E4D3", FieldLedgerPalette.Parchment)
                Swatch("moss", "#6B7A5A", FieldLedgerPalette.Moss)
                Swatch("brass", "#B8934A", FieldLedgerPalette.Brass)
                Swatch("dusty-rose", "#C08B7A", FieldLedgerPalette.DustyRose)
            }

            SectionRule()

            // ---- Type scale ------------------------------------------------
            SectionHeader(stringResource(R.string.specimen_type_header))
            TypeRow("Fraunces · display", "Log the day", MaterialTheme.typography.displaySmall)
            TypeRow("Fraunces · headline", "A field journal", MaterialTheme.typography.headlineMedium)
            TypeRow(
                "Inter · body",
                "Everything read at length stays highly legible — no personality tax on readability.",
                MaterialTheme.typography.bodyLarge,
            )
            TypeRow("Inter · label", "PRIMARY BUTTON", MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "IBM Plex Mono · stamped metadata",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "14 JUL 2026",
                style = StampType.stampDate,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "51.5074° N, 0.1278° W",
                style = StampType.metadata,
                color = MaterialTheme.colorScheme.secondary,
            )

            SectionRule()

            // ---- Date stamp ------------------------------------------------
            SectionHeader(stringResource(R.string.specimen_stamp_header))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DateStamp(date = LocalDate.of(2026, 7, 14))
                DateStamp(
                    date = LocalDate.of(2026, 3, 2),
                    inkColor = FieldLedgerPalette.DustyRose,
                )
                DateStamp(
                    date = LocalDate.of(2026, 11, 27),
                    inkColor = FieldLedgerPalette.Moss,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "The one signature flourish. Mono type, brass ink, a hand-stamped tilt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun SectionRule() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 20.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun Swatch(name: String, hex: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Box(
            modifier = Modifier
                .size(width = 88.dp, height = 56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
        )
        Spacer(Modifier.height(6.dp))
        Text(name, style = StampType.metadata, color = MaterialTheme.colorScheme.onBackground)
        Text(hex, style = StampType.metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TypeRow(label: String, sample: String, style: androidx.compose.ui.text.TextStyle) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = sample,
            style = style,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StyleSpecimenPreviewDark() {
    AlmanacTheme(darkTheme = true) {
        Surface { StyleSpecimenScreen(darkTheme = true, onToggleTheme = {}) }
    }
}

@Preview(showBackground = true)
@Composable
private fun StyleSpecimenPreviewLight() {
    AlmanacTheme(darkTheme = false) {
        Surface { StyleSpecimenScreen(darkTheme = false, onToggleTheme = {}) }
    }
}
