package com.ansonboby.almanac.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.repository.EntryFilter
import com.ansonboby.almanac.data.util.LocalDateUtil
import com.ansonboby.almanac.ui.components.EntryFilterBar
import com.ansonboby.almanac.ui.components.EntryRow
import com.ansonboby.almanac.ui.components.ThemeToggleChip
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNewEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    onToggleTheme: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dayLabel = LocalDateUtil.dayLabel(state.day)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                androidx.compose.material3.TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.today_eyebrow) +
                                    " — VOL. ${LocalDateUtil.year(state.day) % 100}",
                                style = StampType.metadata,
                                color = FieldLedgerPalette.Brass,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Today",
                                style = AlmanacTypography.displaySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = dayLabel,
                                style = AlmanacTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = { ThemeToggleChip(onToggleTheme = onToggleTheme) },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
                EntryFilterBar(
                    query = state.query,
                    filter = state.filter,
                    onQueryChange = viewModel::setQuery,
                    onFilterChange = viewModel::setFilter,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        },
    ) { padding ->
        if (state.entries.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.today_empty_title),
                    style = AlmanacTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.today_empty_body),
                    style = AlmanacTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                AddEntryAffordance(
                    onNewEntry = onNewEntry,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxWidth().padding(padding).padding(horizontal = 16.dp),
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    EntryRow(entry = entry, onClick = { onOpenEntry(entry.id) })
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    AddEntryAffordance(
                        onNewEntry = onNewEntry,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Quiet "add a new line…" affordance (HTML reference: a ghost inline link with a
 * moss [add] glyph, not a filled/elevated FAB — keeps the ledger's boldness on
 * the date stamp alone).
 */
@Composable
private fun AddEntryAffordance(
    onNewEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNewEntry),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+",
            style = StampType.counter,
            color = FieldLedgerPalette.Moss,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.today_new_entry),
            style = AlmanacTypography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
