package com.ansonboby.almanac.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.repository.EntryFilter
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

/**
 * Shared search + type-filter bar for the Today / Month views (PRD: search &
 * filter). Field Ledger styled: a ruled mono search line, filter chips as sharp
 * moss boxes. Voice kept quiet — labels are short nouns, not sentences.
 */
@Composable
fun EntryFilterBar(
    query: String,
    filter: EntryFilter,
    onQueryChange: (String) -> Unit,
    onFilterChange: (EntryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = EntryFilter.entries
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = StampType.metadata.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier
                .weight(1f)
                .border(1.dp, FieldLedgerPalette.Moss, RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_hint),
                        style = StampType.metadata,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                inner()
            },
        )
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters) { f ->
            val selected = f == filter
            val color = if (selected) FieldLedgerPalette.Brass else FieldLedgerPalette.Moss
            Text(
                text = stringResource(labelFor(f)),
                style = StampType.counter,
                color = if (selected) FieldLedgerPalette.Brass else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .border(1.dp, color, RoundedCornerShape(0.dp))
                    .clickable { onFilterChange(f) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

private fun labelFor(filter: EntryFilter) = when (filter) {
    EntryFilter.ALL -> R.string.filter_all
    EntryFilter.PHOTO -> R.string.filter_photo
    EntryFilter.TEXT -> R.string.filter_text
    EntryFilter.MOOD -> R.string.filter_mood
    EntryFilter.NOTE -> R.string.filter_note
}
