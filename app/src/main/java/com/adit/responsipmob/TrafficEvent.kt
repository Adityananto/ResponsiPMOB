package com.adit.responsipmob

import com.google.android.gms.maps.model.LatLng

enum class Severity { LOW, MEDIUM, HIGH }
data class TrafficEvent(
    var id: String? = null,
    var title: String = "",
    var description: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var severity: String = "LOW"
)
