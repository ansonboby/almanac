package com.ansonboby.almanac.ui.settings

import android.app.TimePickerDialog
import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.R
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onToggleTheme: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(initialValue = 1)
    val geotag by viewModel.geotagEnabled.collectAsStateWithLifecycle(initialValue = false)
    val reminderOn by viewModel.reminderEnabled.collectAsStateWithLifecycle(initialValue = false)
    val reminderTime by viewModel.reminderTime.collectAsStateWithLifecycle(initialValue = 20 to 0)
    val (remHour, remMinute) = reminderTime
    val message by viewModel.message.collectAsStateWithLifecycle(initialValue = null)

    var showTimePicker by remember { mutableStateOf(false) }
    var showPurge by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri: Uri? -> uri?.let { viewModel.backup(it) } }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri: Uri? -> uri?.let { viewModel.exportPdf(it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? -> uri?.let { viewModel.restore(it) } }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.setReminder(true, remHour, remMinute)
        } else {
            Toast.makeText(context, R.string.settings_reminder_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.settings_eyebrow), style = StampType.metadata, color = FieldLedgerPalette.Brass)
                        Text(stringResource(R.string.settings_title), style = AlmanacTypography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Appearance
            SectionHeader(stringResource(R.string.settings_appearance))
            Text(stringResource(R.string.settings_appearance_body), style = AlmanacTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SegmentedTheme(
                isInk = themeMode != 2,
                onSelectInk = { viewModel.setTheme(1) },
                onSelectParchment = { viewModel.setTheme(2) },
            )

            // Privacy
            SectionHeader(stringResource(R.string.settings_privacy))
            SwitchRow(
                title = stringResource(R.string.settings_geotag),
                subtitle = stringResource(R.string.settings_geotag_body),
                checked = geotag,
                onChecked = { viewModel.setGeotag(it) },
            )

            // Reminders
            SectionHeader(stringResource(R.string.settings_reminders))
            SwitchRow(
                title = stringResource(R.string.settings_reminder_toggle),
                subtitle = stringResource(R.string.settings_reminder_body),
                checked = reminderOn,
                onChecked = { on ->
                    if (on && android.os.Build.VERSION.SDK_INT >= 33 &&
                        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setReminder(on, remHour, remMinute)
                    }
                },
            )
            if (reminderOn) {
                Row(
                    Modifier.fillMaxWidth().clickable { showTimePicker = true }.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.settings_reminder_time), style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(String.format(Locale.getDefault(), "%02d:%02d", remHour, remMinute), style = StampType.counter, color = FieldLedgerPalette.Brass)
                }
            }

            // Data management
            SectionHeader(stringResource(R.string.settings_data))
            DataRow(stringResource(R.string.settings_backup), stringResource(R.string.settings_backup_body)) {
                backupLauncher.launch("almanac_backup_${System.currentTimeMillis()}.zip")
            }
            DataRow(stringResource(R.string.settings_restore), stringResource(R.string.settings_restore_body)) {
                restoreLauncher.launch(arrayOf("application/zip"))
            }
            DataRow(stringResource(R.string.settings_export_pdf), stringResource(R.string.settings_export_pdf_body)) {
                pdfLauncher.launch("field_report_${System.currentTimeMillis()}.pdf")
            }
            DataRow(
                stringResource(R.string.settings_purge),
                stringResource(R.string.settings_purge_body),
                destructive = true,
            ) { showPurge = true }

            // Colophon
            Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .border(1.dp, FieldLedgerPalette.Moss.copy(alpha = 0.5f))
                        .rotate(-2f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.colophon),
                            style = StampType.metadata,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        AndroidTimePicker(
            initialHour = remHour,
            initialMinute = remMinute,
            onConfirm = { h, m ->
                showTimePicker = false
                viewModel.setReminderTime(h, m)
            },
            onDismiss = { showTimePicker = false },
        )
    }

    if (showPurge) {
        AlertDialog(
            onDismissRequest = { showPurge = false },
            title = { Text(stringResource(R.string.settings_purge_confirm_title)) },
            text = { Text(stringResource(R.string.settings_purge_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showPurge = false
                    viewModel.purge()
                }) { Text(stringResource(R.string.settings_purge_confirm), color = FieldLedgerPalette.DustyRose) }
            },
            dismissButton = { TextButton(onClick = { showPurge = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    message?.let {
        LaunchedEffect(it) {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text.uppercase(),
            style = StampType.metadata,
            color = FieldLedgerPalette.Moss,
            modifier = Modifier.padding(end = 12.dp),
        )
        Box(
            Modifier.weight(1f).height(1.dp).background(FieldLedgerPalette.Moss.copy(alpha = 0.2f)),
        )
    }
}

@Composable
private fun SegmentedTheme(isInk: Boolean, onSelectInk: () -> Unit, onSelectParchment: () -> Unit) {
    Row(Modifier.fillMaxWidth().border(1.dp, FieldLedgerPalette.Moss.copy(alpha = 0.4f)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        SegmentButton(stringResource(R.string.theme_ink), isInk, Modifier.weight(1f), onSelectInk)
        SegmentButton(stringResource(R.string.theme_parchment), !isInk, Modifier.weight(1f), onSelectParchment)
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = if (selected) FieldLedgerPalette.Moss else MaterialTheme.colorScheme.background
    val fg = if (selected) FieldLedgerPalette.Parchment else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier.fillMaxWidth().background(bg).clickable(onClick = onClick).padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, style = StampType.counter, color = fg) }
}

@Composable
private fun FieldToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor = if (checked) FieldLedgerPalette.Moss else Color.Transparent
    val knobColor = if (checked) FieldLedgerPalette.Parchment else MaterialTheme.colorScheme.outline
    Box(
        Modifier
            .width(40.dp)
            .height(22.dp)
            .clip(RoundedCornerShape(50))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
            .background(trackColor)
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(knobColor))
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = AlmanacTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FieldToggle(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun DataRow(title: String, subtitle: String, destructive: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).border(1.dp, if (destructive) FieldLedgerPalette.DustyRose.copy(alpha = 0.3f) else FieldLedgerPalette.Moss.copy(alpha = 0.2f)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = AlmanacTypography.bodyMedium, color = if (destructive) FieldLedgerPalette.DustyRose else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = AlmanacTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", style = AlmanacTypography.displaySmall, color = FieldLedgerPalette.Brass)
    }
}

@Composable
private fun AndroidTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    androidx.compose.runtime.DisposableEffect(Unit) {
        val dialog = TimePickerDialog(
            context,
            { _, h, m -> onConfirm(h, m) },
            initialHour,
            initialMinute,
            true,
        )
        dialog.setOnCancelListener { onDismiss() }
        dialog.show()
        onDispose { dialog.dismiss() }
    }
}
