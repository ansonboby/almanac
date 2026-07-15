package com.ansonboby.almanac.ui.month

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.local.DaySummary
import com.ansonboby.almanac.data.repository.EntryFilter
import com.ansonboby.almanac.data.util.LocalDateUtil
import com.ansonboby.almanac.ui.components.DateStamp
import com.ansonboby.almanac.ui.components.EntryFilterBar
import com.ansonboby.almanac.ui.components.Mood
import com.ansonboby.almanac.ui.components.ThemeToggleChip
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(
    onOpenDay: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    viewModel: MonthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val firstOfMonth = LocalDateUtil.toLocalDate(LocalDateUtil.startOfMonth(state.centerDay))
    val daysInMonth = firstOfMonth.lengthOfMonth()
    val leadingBlanks = (firstOfMonth.dayOfWeek.value % 7) // Mon=1..Sun=7 -> 0..6
    val monthLabel = LocalDateUtil.monthLabel(state.centerDay)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.month_eyebrow),
                            style = StampType.metadata,
                            color = FieldLedgerPalette.Brass,
                        )
                        Text(
                            text = monthLabel,
                            style = AlmanacTypography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = {
                    Text(
                        text = "‹",
                        style = AlmanacTypography.headlineMedium,
                        color = FieldLedgerPalette.Moss,
                        modifier = Modifier
                            .clickable { viewModel.goToMonth(LocalDateUtil.localDay(firstOfMonth.minusMonths(1))) }
                            .padding(horizontal = 12.dp),
                    )
                    Text(
                        text = "›",
                        style = AlmanacTypography.headlineMedium,
                        color = FieldLedgerPalette.Moss,
                        modifier = Modifier
                            .clickable { viewModel.goToMonth(LocalDateUtil.localDay(firstOfMonth.plusMonths(1))) }
                            .padding(horizontal = 12.dp),
                    )
                    ThemeToggleChip(onToggleTheme = onToggleTheme)
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
            EntryFilterBar(
                query = state.query,
                filter = state.filter,
                onQueryChange = viewModel::setQuery,
                onFilterChange = viewModel::setFilter,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { header ->
                    Text(
                        text = header,
                        style = StampType.metadata,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items((0 until leadingBlanks).toList()) { Box(Modifier) }
                items((1..daysInMonth).toList()) { dom ->
                    val epochDay = LocalDateUtil.localDay(
                        firstOfMonth.withDayOfMonth(dom),
                    )
                    val summary = state.summaries[epochDay]
                    DayCell(
                        epochDay = epochDay,
                        summary = summary,
                        onClick = { onOpenDay(epochDay) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    epochDay: Int,
    summary: DaySummary?,
    onClick: () -> Unit,
) {
    val hasEntry = summary != null
    val mood = summary?.moodScore?.let { Mood.fromScore(it) }
    val ink = when {
        !hasEntry -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        else -> FieldLedgerPalette.Brass
    }
    val scope = rememberCoroutineScope()
    val bleed = remember { Animatable(0f) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                scope.launch {
                    bleed.snapTo(0f)
                    bleed.animateTo(1f, animationSpec = tween(durationMillis = 320))
                }
                onClick()
            }
            .padding(2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (hasEntry && mood != null) mood.tint.copy(alpha = 0.14f) else Color.Transparent)
                .drawBehind {
                    if (bleed.value > 0f && bleed.value < 1f) {
                        val progress = bleed.value
                        drawCircle(
                            color = FieldLedgerPalette.Brass.copy(alpha = 0.28f * (1f - progress)),
                            radius = (size.minDimension / 2f) * progress,
                            center = center,
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            DateStamp(epochDayLocal = epochDay, size = 44.dp, inkColor = ink)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = LocalDateUtil.dayOfMonth(epochDay).toString(),
            style = StampType.metadata,
            color = if (hasEntry) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}
