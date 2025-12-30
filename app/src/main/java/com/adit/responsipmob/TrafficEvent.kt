package com.adit.responsipmob

import com.google.android.gms.maps.model.LatLng

enum class Severity { LOW, MEDIUM, HIGH }
class TrafficEvent (
    val title: String,
    val description: String,
    val location: LatLng,
    val severity: Severity
)
