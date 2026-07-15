package com.ansonboby.almanac.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.local.Habit
import com.ansonboby.almanac.data.local.HabitFrequency
import com.ansonboby.almanac.data.local.HabitWithStatus
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(viewModel: HabitsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var archiveTarget by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.habits_eyebrow),
                            style = StampType.metadata,
                            color = FieldLedgerPalette.Brass,
                        )
                        Text(
                            text = stringResource(R.string.habits_title),
                            style = AlmanacTypography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        ) {
            items(state.habits, key = { it.habit.id }) { item ->
                HabitRow(
                    item = item,
                    onToggle = { viewModel.toggleToday(item.habit.id) },
                    onEdit = { viewModel.openEdit(item.habit) },
                    onArchive = { archiveTarget = item.habit },
                )
            }
            if (state.habits.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.habits_empty),
                        style = AlmanacTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }

            item {
                AddHabitAffordance(
                    onNewHabit = viewModel::openNew,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }

            if (state.archived.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth().clickable { viewModel.setShowArchived(!state.showArchived) }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.habits_archived),
                            style = StampType.counter,
                            color = FieldLedgerPalette.Moss,
                        )
                        Text(
                            text = if (state.showArchived) "–" else "+",
                            style = AlmanacTypography.headlineMedium,
                            color = FieldLedgerPalette.Moss,
                        )
                    }
                }
                if (state.showArchived) {
                    items(state.archived, key = { it.id }) { habit ->
                        Text(
                            text = habit.title,
                            style = AlmanacTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openEdit(habit) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                        )
                    }
                }
            }

            item {
                Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.finis_opus), style = StampType.counter, color = FieldLedgerPalette.Brass)
                }
            }
        }
    }

    if (state.editorOpen) {
        HabitEditSheet(
            state = state,
            onTitleChange = viewModel::setTitle,
            onDescriptionChange = viewModel::setDescription,
            onFrequencyChange = viewModel::setFrequency,
            onCustomDaysChange = viewModel::setCustomDays,
            onTintChange = viewModel::setTint,
            onSave = viewModel::save,
            onClose = viewModel::closeEditor,
        )
    }

    if (archiveTarget != null) {
        AlertDialog(
            onDismissRequest = { archiveTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.archive(archiveTarget!!)
                    archiveTarget = null
                }) { Text(stringResource(R.string.habits_archive), color = FieldLedgerPalette.Brass) }
            },
            dismissButton = {
                TextButton(onClick = { archiveTarget = null }) {
                    Text(stringResource(android.R.string.cancel), color = FieldLedgerPalette.Moss)
                }
            },
            title = { Text(stringResource(R.string.habits_archive_title), style = AlmanacTypography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
            text = { Text(stringResource(R.string.habits_archive_body), style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            containerColor = MaterialTheme.colorScheme.background,
        )
    }
}

/**
 * Quiet inline "add a habit" affordance — matches Today's [AddEntryAffordance]
 * (moss "+" glyph + italic ghost label), replacing the filled/elevated FAB so the
 * ledger's boldness stays on the date stamp alone.
 */
@Composable
private fun AddHabitAffordance(
    onNewHabit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNewHabit),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+",
            style = StampType.counter,
            color = FieldLedgerPalette.Moss,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.habits_new),
            style = AlmanacTypography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun HabitRow(
    item: HabitWithStatus,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
) {
    val habit = item.habit
    val tint = habitTintColor(habit.tint)
    Row(
        Modifier.fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .border(1.5.dp, tint, CircleShape)
                .clickable(onClick = onToggle)
                .background(if (item.isDoneToday) tint else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (item.isDoneToday) {
                Canvas(Modifier.size(18.dp)) {
                    val stroke = 2.2.dp.toPx()
                    val check = Path().apply {
                        moveTo(size.width * 0.22f, size.height * 0.54f)
                        lineTo(size.width * 0.43f, size.height * 0.74f)
                        lineTo(size.width * 0.80f, size.height * 0.30f)
                    }
                    drawPath(
                        check,
                        color = FieldLedgerPalette.ParchmentText,
                        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = habit.title,
                style = AlmanacTypography.bodyLarge,
                color = if (item.isDoneToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                textDecoration = if (item.isDoneToday) TextDecoration.LineThrough else null,
            )
            Text(
                text = habit.description
                    ?: stringResource(HabitFrequency.fromKey(habit.frequency, habit.customDays).labelRes),
                style = StampType.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${item.streak.toString().padStart(2, '0')}d",
            style = StampType.counter,
            color = FieldLedgerPalette.Moss,
            modifier = Modifier.clickable(onClick = onArchive).padding(start = 10.dp),
        )
    }
    HorizontalDivider(color = FieldLedgerPalette.Moss.copy(alpha = 0.4f), thickness = 1.dp)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HabitEditSheet(
    state: com.ansonboby.almanac.ui.habit.HabitsUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onCustomDaysChange: (String) -> Unit,
    onTintChange: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    val freqs = listOf(HabitFrequency.Daily, HabitFrequency.Weekdays, HabitFrequency.Weekends, HabitFrequency.Custom)
    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = if (state.editingId == null) stringResource(R.string.habits_new_title) else stringResource(R.string.habits_edit_title),
                style = AlmanacTypography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            HorizontalDivider(Modifier.padding(vertical = 14.dp), thickness = 1.dp, color = FieldLedgerPalette.Moss.copy(alpha = 0.4f))

            TextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.habits_name_hint), style = StampType.metadata) },
                singleLine = true,
                colors = habitFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.habits_note_hint), style = StampType.metadata) },
                singleLine = true,
                colors = habitFieldColors(),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )

            Text(stringResource(R.string.habits_frequency), style = StampType.counter, color = FieldLedgerPalette.Moss, modifier = Modifier.padding(top = 16.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                freqs.forEach { f ->
                    val selected = f.key == state.frequencyKey
                    Text(
                        text = stringResource(f.labelRes),
                        style = StampType.counter,
                        color = if (selected) FieldLedgerPalette.Brass else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .border(1.dp, if (selected) FieldLedgerPalette.Brass else FieldLedgerPalette.Moss, RoundedCornerShape(0.dp))
                            .clickable { onFrequencyChange(f.key) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
            if (state.frequencyKey == HabitFrequency.Custom.key) {
                TextField(
                    value = state.customDays,
                    onValueChange = onCustomDaysChange,
                    label = { Text(stringResource(R.string.habits_custom_days_hint), style = StampType.metadata) },
                    singleLine = true,
                    colors = habitFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
            }

            Text(stringResource(R.string.habits_color), style = StampType.counter, color = FieldLedgerPalette.Moss, modifier = Modifier.padding(top = 16.dp))
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HABIT_TINTS.forEach { t ->
                    val selected = t == state.tint
                    Box(
                        Modifier.size(30.dp)
                            .border(if (selected) 2.dp else 1.dp, if (selected) FieldLedgerPalette.Brass else FieldLedgerPalette.Moss, RoundedCornerShape(0.dp))
                            .background(habitTintColor(t))
                            .clickable { onTintChange(t) },
                    )
                }
            }

            TextButton(
                onClick = onSave,
                modifier = Modifier.padding(top = 20.dp),
            ) {
                Text(stringResource(R.string.habits_save), style = StampType.counter, color = FieldLedgerPalette.Brass)
            }
        }
    }
}

@Composable
private fun habitFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
    focusedIndicatorColor = FieldLedgerPalette.Brass,
    unfocusedIndicatorColor = FieldLedgerPalette.Moss,
    focusedLabelColor = FieldLedgerPalette.Moss,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
