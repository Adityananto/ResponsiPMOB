package com.adit.responsipmob

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class TrafficViewModel : ViewModel() {
    val trafficEvents = MutableLiveData<List<TrafficEvent>>()

    fun loadTrafficData() {
        val mockData = listOf(
            TrafficEvent("Macet Total: Sudirman", "Kecelakaan beruntun di jalur cepat.",
                LatLng(-6.220, 106.820), Severity.HIGH),
            TrafficEvent("Padat Merayap: Kuningan", "Volume kendaraan meningkat.", LatLng(-6.230, 106.830), Severity.MEDIUM),
            TrafficEvent("Perbaikan Jalan: Thamrin", "Hati-hati penyempitan lajur.", LatLng(-6.190, 106.822), Severity.LOW)
        )
        trafficEvents.value = mockData
    }
}