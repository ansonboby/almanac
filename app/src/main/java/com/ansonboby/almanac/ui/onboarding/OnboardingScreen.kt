package com.ansonboby.almanac.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ansonboby.almanac.R
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    var cameraPrimer by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onFinish() }

    if (cameraPrimer) {
        CameraPrimer(
            onGrant = { cameraLauncher.launch(Manifest.permission.CAMERA) },
            onSkip = onFinish,
        )
        return
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(28.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("ALMANAC", style = StampType.counter, color = FieldLedgerPalette.Brass)
            Text(
                stringResource(R.string.onboarding_title),
                style = AlmanacTypography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.onboarding_body),
                style = AlmanacTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.onboarding_privacy),
                style = StampType.metadata,
                color = FieldLedgerPalette.Moss,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.fillMaxWidth().background(FieldLedgerPalette.Brass)
                    .clickable { cameraPrimer = true }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.onboarding_begin), style = StampType.counter, color = FieldLedgerPalette.Ink)
            }
            Text(
                stringResource(R.string.onboarding_skip),
                style = StampType.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onFinish() }.padding(8.dp),
            )
        }
    }
}

@Composable
private fun CameraPrimer(onGrant: () -> Unit, onSkip: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(28.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.onboarding_camera_title), style = AlmanacTypography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.onboarding_camera_body), style = AlmanacTypography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.fillMaxWidth().background(FieldLedgerPalette.Brass).clickable { onGrant() }.padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.new_entry_grant), style = StampType.counter, color = FieldLedgerPalette.Ink)
            }
            Text(
                stringResource(R.string.new_entry_deny),
                style = StampType.metadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onSkip() }.padding(8.dp),
            )
        }
    }
}
