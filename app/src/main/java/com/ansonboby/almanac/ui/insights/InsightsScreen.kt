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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.roundToInt

private const val STAMP_COUNT = 30

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
        Box(Modifier.fillMaxSize().padding(padding)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val x = 14.dp.toPx()
                        drawLine(
                            FieldLedgerPalette.Moss.copy(alpha = 0.3f),
                            Offset(x, 0f),
                            Offset(x, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    },
            )
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { SectionHeader(stringResource(R.string.insights_mood)) }
                item {
                    MoodTrendChart(
                        mood = state.mood,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item { SectionHeader(stringResource(R.string.insights_frequency)) }
                item {
                    FrequencyChart(
                        frequency = state.frequency,
                        modifier = Modifier.fillMaxWidth(),
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
                        Text(stringResource(R.string.finis_opus), style = StampType.counter, color = FieldLedgerPalette.Brass.copy(alpha = 0.45f))
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
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = StampType.metadata.copy(color = FieldLedgerPalette.Moss),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun MoodTrendChart(mood: List<DayMood>, modifier: Modifier = Modifier) {
    val brass = FieldLedgerPalette.Brass
    val moss = FieldLedgerPalette.Moss
    Box(
        modifier
            .fillMaxWidth()
            .height(192.dp)
            .drawBehind {
                val y = size.height - 1.dp.toPx()
                drawLine(moss.copy(alpha = 0.4f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (mood.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val pad = 18.dp.toPx()
            val min = -2f
            val max = 2f
            val yFor: (Float) -> Float = { v -> pad + (1f - (v - min) / (max - min)) * (h - 2 * pad) }
            val xFor: (Int) -> Float = { i -> pad + i * (w - 2 * pad) / (mood.size - 1).coerceAtLeast(1) }

            // weekly markers
            val marker = moss.copy(alpha = 0.4f)
            listOf(0.25f, 0.5f, 0.75f).forEach { f ->
                drawLine(
                    marker,
                    Offset(w * f, pad),
                    Offset(w * f, h - pad),
                    strokeWidth = 0.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx())),
                )
            }

            // neutral baseline
            drawLine(moss.copy(alpha = 0.5f), Offset(pad, yFor(0f)), Offset(w - pad, yFor(0f)), 1.dp.toPx())

            val pts = mood.mapIndexedNotNull { i, dm ->
                if (dm.count == 0) null else Offset(xFor(i), yFor(dm.avgScore))
            }
            if (pts.isEmpty()) return@Canvas

            val linePath = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                pts.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val fillPath = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                pts.drop(1).forEach { lineTo(it.x, it.y) }
                lineTo(pts.last().x, h)
                lineTo(pts.first().x, h)
                close()
            }
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    listOf(brass.copy(alpha = 0.3f), moss.copy(alpha = 0.05f)),
                    startY = 0f,
                    endY = h,
                ),
            )
            drawPath(linePath, color = brass, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Square))
        }
        if (mood.isNotEmpty()) {
            val labelStyle = StampType.metadata.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text(
                LocalDateUtil.dayMonth(mood.first().day),
                style = labelStyle,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 6.dp),
            )
            Text(
                LocalDateUtil.dayMonth(mood.last().day),
                style = labelStyle,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun FrequencyChart(frequency: List<DayCount>, modifier: Modifier = Modifier) {
    val moss = FieldLedgerPalette.Moss
    Box(
        modifier
            .fillMaxWidth()
            .height(120.dp)
            .drawBehind {
                val y = size.height - 1.dp.toPx()
                drawLine(moss.copy(alpha = 0.4f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (frequency.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val pad = 18.dp.toPx()
            val max = (frequency.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
            val n = frequency.size
            val slot = (w - 2 * pad) / n
            val bw = slot * 0.55f
            frequency.forEachIndexed { i, dc ->
                val bh = if (dc.count == 0) 0f else (dc.count.toFloat() / max) * (h - 2 * pad)
                val x = pad + i * slot + (slot - bw) / 2
                val y = h - pad - bh
                if (bh > 0f) drawRect(moss, topLeft = Offset(x, y), size = Size(bw, bh))
            }
        }
        if (frequency.isNotEmpty()) {
            val labelStyle = StampType.metadata.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text(
                LocalDateUtil.dayMonth(frequency.first().day),
                style = labelStyle,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 6.dp),
            )
            Text(
                LocalDateUtil.dayMonth(frequency.last().day),
                style = labelStyle,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun HabitConsistencyRow(hc: HabitConsistency) {
    val tint = habitTintColor(hc.habit.tint)
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                hc.habit.title,
                style = StampType.metadata.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("×${hc.streak}", style = StampType.counter, color = tint)
                Text("${(hc.rate * 100).toInt()}%", style = StampType.counter, color = FieldLedgerPalette.Brass)
            }
        }
        FlowRow(
            Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val filled = (hc.rate * STAMP_COUNT).roundToInt().coerceIn(0, STAMP_COUNT)
            repeat(STAMP_COUNT) { i ->
                Box(
                    Modifier
                        .size(width = 8.dp, height = 12.dp)
                        .then(
                            if (i < filled) {
                                Modifier.background(FieldLedgerPalette.Moss)
                            } else {
                                Modifier.border(1.dp, FieldLedgerPalette.Moss.copy(alpha = 0.2f), RoundedCornerShape(0.dp))
                            },
                        ),
                )
            }
        }
    }
}
