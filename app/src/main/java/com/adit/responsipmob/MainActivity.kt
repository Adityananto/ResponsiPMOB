package com.adit.responsipmob

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val viewModel: TrafficViewModel by viewModels()
    private lateinit var sheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet))
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isTrafficEnabled = true // FITUR UTAMA: Layer Kemacetan Google

        // Atur agar ketika marker diklik, muncul panel detail
        mMap.setOnMarkerClickListener { marker ->
            val event = marker.tag as? TrafficEvent
            event?.let { showBottomSheet(it) }
            false
        }

        viewModel.loadTrafficData()
        viewModel.trafficEvents.observe(this) { events ->
            addMarkersToMap(events)
        }

        // Fokus awal ke Jakarta
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-6.200, 106.816), 13f))
    }

    private fun addMarkersToMap(events: List<TrafficEvent>) {
        events.forEach { event ->
            val color = when(event.severity) {
                Severity.HIGH -> BitmapDescriptorFactory.HUE_RED
                Severity.MEDIUM -> BitmapDescriptorFactory.HUE_ORANGE
                else -> BitmapDescriptorFactory.HUE_YELLOW
            }
            val marker = mMap.addMarker(
                MarkerOptions().position(event.location).title(event.title)
                .icon(BitmapDescriptorFactory.defaultMarker(color)))
            marker?.tag = event // Simpan data objek ke dalam marker
        }
    }

    private fun showBottomSheet(event: TrafficEvent) {
        findViewById<TextView>(R.id.txtDetailTitle).text = event.title
        findViewById<TextView>(R.id.txtDetailSeverity).text = "Tingkat Bahaya: ${event.severity}"
        findViewById<TextView>(R.id.txtDetailDesc).text = event.description
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}