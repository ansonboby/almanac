package com.ansonboby.almanac.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.util.LocalDateUtil
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
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.today_eyebrow) +
                                " — VOL. ${LocalDateUtil.year(state.day) % 100}",
                            style = StampType.metadata,
                            color = FieldLedgerPalette.Brass,
                        )
                        Text(
                            text = "Today",
                            style = AlmanacTypography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewEntry,
                containerColor = FieldLedgerPalette.Brass,
                contentColor = FieldLedgerPalette.Ink,
            ) {
                Text(
                    text = stringResource(R.string.today_new_entry),
                    style = StampType.counter,
                    modifier = Modifier.padding(horizontal = 14.dp),
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
            }
        } else {
            LazyColumn(
                Modifier.fillMaxWidth().padding(padding).padding(horizontal = 12.dp),
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    EntryRow(entry = entry, onClick = { onOpenEntry(entry.id) })
                }
            }
        }
    }
}
