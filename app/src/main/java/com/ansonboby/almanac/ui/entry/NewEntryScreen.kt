package com.ansonboby.almanac.ui.entry

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.local.EntryType
import com.ansonboby.almanac.data.util.FileStorage
import com.ansonboby.almanac.data.util.LocalDateUtil
import com.ansonboby.almanac.ui.components.DateStamp
import com.ansonboby.almanac.ui.components.Mood
import com.ansonboby.almanac.ui.components.MoodWeatherGlyph
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(
    onDone: () -> Unit,
    onToggleTheme: () -> Unit,
    viewModel: NewEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val storage = remember { FileStorage.get(context) }
    var showPhotoChooser by remember { mutableStateOf(false) }
    var pendingCameraRequest by remember { mutableStateOf(false) }
    val stampScope = rememberCoroutineScope()
    val stampScale = remember { Animatable(1f) }
    val stampRotation = remember { Animatable(0f) }

    // System camera app -> writes to a temp app-private file via FileProvider.
    var pendingCaptureUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { saved ->
        if (saved) pendingCaptureUri?.let { viewModel.setPhoto(it.toString()) }
        pendingCaptureUri = null
    }

    // Gallery -> copy picked URI into app-private storage.
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            storage.importUri(it)?.let { imported ->
                viewModel.setPhoto(imported.toString())
            } ?: Toast.makeText(context, "Could not import photo", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCamera() {
        val file = storage.newCaptureFile()
        storage.uriForFile(file).also { uri ->
            pendingCaptureUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        showPhotoChooser = false
        if (granted) openCamera() else Toast.makeText(context, R.string.new_entry_camera_denied, Toast.LENGTH_SHORT).show()
    }

    fun requestCamera() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(pendingCameraRequest) {
        if (pendingCameraRequest) {
            pendingCameraRequest = false
            requestCamera()
        }
    }

    val geotagEnabled by viewModel.geotagEnabled.collectAsStateWithLifecycle(initialValue = false)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.requestGeoTag()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.new_entry_eyebrow),
                            style = StampType.metadata,
                            color = FieldLedgerPalette.Brass,
                        )
                        Text(
                            text = stringResource(R.string.new_entry_title),
                            style = AlmanacTypography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Type selector
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EntryTypeChip(stringResource(R.string.new_entry_photo), state.type == EntryType.PHOTO) { showPhotoChooser = true }
                EntryTypeChip(stringResource(R.string.new_entry_text), state.type == EntryType.TEXT) { viewModel.setType(EntryType.TEXT) }
                EntryTypeChip(stringResource(R.string.new_entry_mood), state.type == EntryType.MOOD) { viewModel.setMood(state.moodScore ?: 0) }
            }

            // Optional geotag (opt-in, requested contextually)
            if (geotagEnabled) {
                GeoTagRow(
                    name = state.geoTag?.name,
                    onAdd = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    onClear = { viewModel.setGeoTag(null) },
                )
            }

            // Content by type
            when {
                state.photoUri != null -> {
                    PhotoPreview(
                        uri = state.photoUri!!,
                        caption = state.caption,
                        onCaptionChange = { viewModel.setPhoto(state.photoUri!!, it) },
                        onRetake = { viewModel.clearPhoto(); showPhotoChooser = true },
                        onGallery = { galleryLauncher.launch("image/*") },
                    )
                }
                state.type == EntryType.TEXT || (state.type == null && state.text.isNotBlank()) -> {
                    LedgerTextField(
                        value = state.text,
                        onValueChange = viewModel::setText,
                        hint = stringResource(R.string.new_entry_text_hint),
                    )
                    LedgerTextField(
                        value = state.tags,
                        onValueChange = viewModel::setTags,
                        hint = stringResource(R.string.new_entry_tags_hint),
                    )
                }
                state.type == EntryType.MOOD -> {
                    MoodPicker(selected = state.moodScore) { viewModel.setMood(it) }
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = FieldLedgerPalette.DustyRose,
                    style = StampType.counter,
                )
            }

            // Stamp into Ledger
            val canStamp = state.type != null || state.text.isNotBlank() || state.photoUri != null || state.moodScore != null
            Box(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = stampScale.value
                        scaleY = stampScale.value
                        rotationZ = stampRotation.value
                    }
                    .shadow(4.dp, RoundedCornerShape(0.dp))
                    .background(if (canStamp) FieldLedgerPalette.Moss else FieldLedgerPalette.Moss.copy(alpha = 0.3f))
                    .clickable(enabled = canStamp) {
                        stampScope.launch {
                            launch {
                                stampScale.animateTo(0.92f, animationSpec = tween(90))
                                stampScale.animateTo(
                                    1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                            }
                            launch {
                                stampRotation.animateTo(-2.5f, animationSpec = tween(90))
                                stampRotation.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                            }
                        }
                        viewModel.stampIntoLedger { onDone() }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.new_entry_stamp).uppercase(),
                    style = StampType.counter,
                    color = if (canStamp) FieldLedgerPalette.ParchmentText else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                stringResource(R.string.new_entry_privacy),
                style = StampType.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showPhotoChooser) {
        AlertDialog(
            onDismissRequest = { showPhotoChooser = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.new_entry_photo_source_title),
                    style = AlmanacTypography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            text = {
                Text(
                    stringResource(R.string.new_entry_photo_source_body),
                    style = AlmanacTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { showPhotoChooser = false; pendingCameraRequest = true }) {
                    Text(stringResource(R.string.new_entry_capture).uppercase(), style = StampType.counter, color = FieldLedgerPalette.Brass)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoChooser = false; galleryLauncher.launch("image/*") }) {
                    Text(stringResource(R.string.new_entry_from_gallery).uppercase(), style = StampType.counter, color = FieldLedgerPalette.Moss)
                }
            },
        )
    }
}

@Composable
private fun EntryTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) FieldLedgerPalette.Brass else FieldLedgerPalette.Moss
    Box(
        Modifier
            .border(1.dp, border)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = StampType.counter,
            color = if (selected) FieldLedgerPalette.Brass else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GeoTagRow(name: String?, onAdd: () -> Unit, onClear: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().border(1.dp, FieldLedgerPalette.Moss).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(stringResource(R.string.new_entry_location), style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                name ?: stringResource(R.string.new_entry_location_hint),
                style = StampType.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            if (name != null) stringResource(R.string.new_entry_location_clear) else stringResource(R.string.new_entry_location_add),
            style = StampType.counter,
            color = FieldLedgerPalette.Brass,
            modifier = Modifier.clickable { if (name != null) onClear() else onAdd() },
        )
    }
}

@Composable
private fun LedgerTextField(value: String, onValueChange: (String) -> Unit, hint: String) {
    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                .border(0.dp, androidx.compose.ui.graphics.Color.Transparent)
                .padding(vertical = 10.dp),
            textStyle = AlmanacTypography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(hint, style = AlmanacTypography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                inner()
            },
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(FieldLedgerPalette.Moss))
    }
}

@Composable
private fun MoodPicker(selected: Int?, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("How does the day read?", style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Mood.all.forEach { mood ->
                val isSel = selected == mood.score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(mood.score) }
                        .border(if (isSel) 1.dp else 0.dp, if (isSel) FieldLedgerPalette.Brass else androidx.compose.ui.graphics.Color.Transparent)
                        .padding(8.dp),
                ) {
                    MoodWeatherGlyph(mood = mood, size = 40.dp, color = mood.tint)
                    Text(mood.label, style = StampType.metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PhotoPreview(
    uri: String,
    caption: String,
    onCaptionChange: (String) -> Unit,
    onRetake: () -> Unit,
    onGallery: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.rotate(-2f)) {
                Box(
                    Modifier
                        .shadow(4.dp, RoundedCornerShape(0.dp))
                        .background(FieldLedgerPalette.Parchment)
                        .padding(12.dp),
                ) {
                    androidx.compose.foundation.Image(
                        painter = coil3.compose.rememberAsyncImagePainter(uri),
                        contentDescription = stringResource(R.string.cd_entry_photo),
                        modifier = Modifier.width(240.dp).aspectRatio(4f / 5f)
                            .background(FieldLedgerPalette.Parchment),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            DateStamp(
                epochDayLocal = LocalDateUtil.todayLocalDay(),
                size = 64.dp,
                inkColor = FieldLedgerPalette.Brass,
                modifier = Modifier.alpha(0.8f),
            )
        }
        LedgerTextField(value = caption, onValueChange = onCaptionChange, hint = stringResource(R.string.new_entry_caption_hint))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EntryTypeChip(stringResource(R.string.new_entry_capture), false) { onRetake() }
            EntryTypeChip(stringResource(R.string.new_entry_from_gallery), false) { onGallery() }
        }
    }
}

