package com.ansonboby.almanac.data.location

/** A resolved place for a check-in. [name] is the reverse-geocoded label. */
data class GeoTag(
    val lat: Double,
    val lng: Double,
    val name: String?,
)
