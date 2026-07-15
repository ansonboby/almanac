package com.ansonboby.almanac.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.repository.DayCount
import com.ansonboby.almanac.data.repository.DayMood
import com.ansonboby.almanac.data.repository.HabitConsistency
import com.ansonboby.almanac.data.util.LocalDateUtil
import com.ansonboby.almanac.ui.components.ThemeToggleChip
import com.ansonboby.almanac.ui.habit.habitTintColor
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onToggleTheme: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.insights_eyebrow),
                            style = StampType.metadata,
                            color = FieldLedgerPalette.Brass,
                        )
                        Text(
                            text = stringResource(R.string.insights_title),
                            style = AlmanacTypography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = { ThemeToggleChip(onToggleTheme = onToggleTheme) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader(stringResource(R.string.insights_mood)) }
            item {
                MoodTrendChart(
                    mood = state.mood,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                )
            }

            item { SectionHeader(stringResource(R.string.insights_frequency)) }
            item {
                FrequencyChart(
                    frequency = state.frequency,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                )
            }

            item { SectionHeader(stringResource(R.string.insights_habits)) }
            if (state.habits.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.insights_habits_empty),
                        style = AlmanacTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                items(state.habits, key = { it.habit.id }) { hc ->
                    HabitConsistencyRow(hc = hc)
                }
            }

            item {
                Column(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.finis_opus), style = StampType.counter, color = FieldLedgerPalette.Brass)
                    Text(
                        stringResource(R.string.colophon),
                        style = StampType.metadata,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = AlmanacTypography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun MoodTrendChart(mood: List<DayMood>, modifier: Modifier = Modifier) {
    val brass = FieldLedgerPalette.Brass
    val moss = FieldLedgerPalette.Moss
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (mood.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val pad = 18.dp.toPx()
            val min = -2f
            val max = 2f
            val yFor: (Float) -> Float = { v -> pad + (1f - (v - min) / (max - min)) * (h - 2 * pad) }
            val xFor: (Int) -> Float = { i -> pad + i * (w - 2 * pad) / (mood.size - 1).coerceAtLeast(1) }

            // neutral baseline
            drawLine(moss.copy(alpha = 0.5f), Offset(pad, yFor(0f)), Offset(w - pad, yFor(0f)), 1.dp.toPx())

            val path = Path()
            var started = false
            mood.forEachIndexed { i, dm ->
                if (dm.count == 0) return@forEachIndexed
                val x = xFor(i)
                val y = yFor(dm.avgScore)
                if (!started) { path.moveTo(x, y); started = true } else path.lineTo(x, y)
            }
            if (started) {
                drawPath(path, color = brass, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Square))
            }
            // endpoint ticks
            mood.forEachIndexed { i, dm ->
                if (dm.count == 0) return@forEachIndexed
                drawCircle(brass, radius = 2.5.dp.toPx(), center = Offset(xFor(i), yFor(dm.avgScore)))
            }
        }
        if (mood.isNotEmpty()) {
            val labelStyle = StampType.metadata.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                LocalDateUtil.dayMonth(mood.first().day),
                style = labelStyle,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 2.dp),
            )
            Text(
                LocalDateUtil.dayMonth(mood.last().day),
                style = labelStyle,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 2.dp),
            )
        }
    }
}

@Composable
private fun FrequencyChart(frequency: List<DayCount>, modifier: Modifier = Modifier) {
    val moss = FieldLedgerPalette.Moss
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (frequency.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val pad = 18.dp.toPx()
            val max = (frequency.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
            val n = frequency.size
            val slot = (w - 2 * pad) / n
            val bw = slot * 0.6f
            frequency.forEachIndexed { i, dc ->
                val bh = if (dc.count == 0) 0f else (dc.count.toFloat() / max) * (h - 2 * pad)
                val x = pad + i * slot + (slot - bw) / 2
                val y = h - pad - bh
                if (bh > 0f) drawRect(moss, topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(bw, bh))
            }
        }
        if (frequency.isNotEmpty()) {
            val labelStyle = StampType.metadata.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                LocalDateUtil.dayMonth(frequency.first().day),
                style = labelStyle,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 2.dp),
            )
            Text(
                LocalDateUtil.dayMonth(frequency.last().day),
                style = labelStyle,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 2.dp),
            )
        }
    }
}

@Composable
private fun HabitConsistencyRow(hc: HabitConsistency) {
    val tint = habitTintColor(hc.habit.tint)
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(hc.habit.title, style = AlmanacTypography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("×${hc.streak}", style = StampType.counter, color = tint)
        }
        Text(
            text = "${(hc.rate * 100).toInt()}%",
            style = StampType.metadata,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        Box(
            Modifier.fillMaxWidth().height(8.dp)
                .border(1.dp, FieldLedgerPalette.Moss, RoundedCornerShape(0.dp)),
        ) {
            Box(Modifier.fillMaxWidth(hc.rate).height(8.dp).background(tint))
        }
    }
    HorizontalDivider(color = FieldLedgerPalette.Moss.copy(alpha = 0.4f), thickness = 1.dp)
}
