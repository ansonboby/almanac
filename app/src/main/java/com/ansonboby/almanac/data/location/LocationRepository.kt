package com.ansonboby.almanac.data.location

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Opt-in location check-ins (PRD 3 / Phase 3). Pulls the device's last known
 * position via [FusedLocationProviderClient] and reverse-geocodes it to a human
 * label with [Geocoder]. Callers must hold a location permission; if not, this
 * returns null rather than throwing. All I/O runs on [Dispatchers.IO].
 */
@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    suspend fun currentGeoTag(): GeoTag? = withContext(Dispatchers.IO) {
        try {
            val loc = Tasks.await(client.lastLocation) ?: return@withContext null
            if (loc.latitude == 0.0 && loc.longitude == 0.0) return@withContext null
            val name = reverseGeocode(loc.latitude, loc.longitude)
            GeoTag(lat = loc.latitude, lng = loc.longitude, name = name)
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double): String? {
        if (!Geocoder.isPresent()) return null
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.firstOrNull()?.let { addr ->
                buildString {
                    addr.locality?.let { append(it) }
                    addr.adminArea?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                    if (isBlank()) addr.countryName?.let { append(it) }
                }.ifBlank { null }
            }
        } catch (_: Exception) {
            null
        }
    }
}
