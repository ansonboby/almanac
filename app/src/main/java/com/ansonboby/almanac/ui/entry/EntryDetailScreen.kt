package com.ansonboby.almanac.ui.entry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.util.LocalDateUtil
import com.ansonboby.almanac.ui.components.LargeDateStamp
import com.ansonboby.almanac.ui.components.Mood
import com.ansonboby.almanac.ui.components.MoodWeatherGlyph
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    onBack: () -> Unit,
    onToggleTheme: () -> Unit,
    viewModel: EntryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val entry = state.entry
    var editing by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = entry?.let { "№ ${it.archivalNo}" } ?: "",
                        style = StampType.counter,
                        color = FieldLedgerPalette.Brass,
                    )
                },
                navigationIcon = {
                    Text(
                        stringResource(R.string.entry_detail_back),
                        style = StampType.counter,
                        color = FieldLedgerPalette.Moss,
                        modifier = Modifier
                            .clickable { onBack() }
                            .padding(horizontal = 16.dp),
                    )
                },
                actions = {
                    if (entry != null) {
                        Text(
                            stringResource(R.string.entry_detail_edit),
                            style = StampType.counter,
                            color = FieldLedgerPalette.Moss,
                            modifier = Modifier.clickable { editing = !editing }.padding(horizontal = 12.dp),
                        )
                        Text(
                            stringResource(R.string.entry_detail_delete),
                            style = StampType.counter,
                            color = FieldLedgerPalette.DustyRose,
                            modifier = Modifier.clickable { confirmDelete = true }.padding(horizontal = 12.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        if (entry == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Entry not found", style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        val mood = entry.moodScore?.let { Mood.fromScore(it) }
        var draftText by remember(entry.id) { mutableStateOf(entry.textContent ?: "") }
        var draftTags by remember(entry.id) { mutableStateOf(entry.tags ?: "") }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                LargeDateStamp(epochDayLocal = entry.epochDayLocal)
            }

            if (entry.photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(entry.photoUri).build(),
                    ),
                    contentDescription = stringResource(R.string.cd_entry_photo),
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                        .background(FieldLedgerPalette.Parchment).padding(6.dp),
                    contentScale = ContentScale.Crop,
                )
            }

            if (mood != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoodWeatherGlyph(mood = mood, size = 36.dp, color = mood.tint)
                    Text("  ${mood.label}", style = AlmanacTypography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (editing) {
                LedgerEditField(value = draftText, onValueChange = { draftText = it }, hint = stringResource(R.string.new_entry_text_hint))
                LedgerEditField(value = draftTags, onValueChange = { draftTags = it }, hint = stringResource(R.string.new_entry_tags_hint))
                Box(
                    Modifier.fillMaxWidth().background(FieldLedgerPalette.Brass)
                        .clickable {
                            viewModel.saveEdit(draftText, "", draftTags, entry.moodScore) { editing = false }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Re-stamp", style = StampType.counter, color = FieldLedgerPalette.Ink)
                }
            } else {
                if (!entry.textContent.isNullOrBlank()) {
                    Text(entry.textContent!!, style = AlmanacTypography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                if (!entry.tags.isNullOrBlank()) {
                    Text(entry.tags!!, style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                LocalDateUtil.dayLabel(entry.epochDayLocal),
                style = StampType.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                stringResource(R.string.new_entry_privacy),
                style = StampType.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.entry_detail_delete_confirm), style = AlmanacTypography.titleLarge, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(stringResource(R.string.entry_detail_delete_confirm_body), style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { viewModel.delete { onBack() } }) {
                    Text(stringResource(R.string.entry_detail_delete), style = StampType.counter, color = FieldLedgerPalette.DustyRose)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Keep", style = StampType.counter, color = FieldLedgerPalette.Moss)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun LedgerEditField(value: String, onValueChange: (String) -> Unit, hint: String) {
    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 10.dp),
            textStyle = AlmanacTypography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(hint, style = AlmanacTypography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                inner()
            },
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(FieldLedgerPalette.Moss))
    }
}
