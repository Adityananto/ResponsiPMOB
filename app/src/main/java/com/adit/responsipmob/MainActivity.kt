package com.adit.responsipmob

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    private var selectedEvent: TrafficEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi UI
        sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet))
        setupSearch()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Logika Tombol Hapus
        findViewById<Button>(R.id.btnDeleteMarker).setOnClickListener {
            selectedEvent?.let { event ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Pin")
                    .setMessage("Hapus laporan ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        viewModel.removeEvent(event)
                        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        Toast.makeText(this, "Laporan dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isTrafficEnabled = true

        mMap.setOnMapLongClickListener { latLng -> showAddMarkerDialog(latLng) }

        mMap.setOnMarkerClickListener { marker ->
            val event = marker.tag as? TrafficEvent
            event?.let {
                selectedEvent = it
                updateDetailPanel(it)
            }
            false
        }

        viewModel.trafficEvents.observe(this) { events ->
            mMap.clear()
            mMap.isTrafficEnabled = true
            addMarkersToMap(events)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-6.200, 106.816), 13f))
    }

    private fun setupSearch() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    val geocoder = Geocoder(this@MainActivity)
                    try {
                        val addressList = geocoder.getFromLocationName(query, 1)
                        if (!addressList.isNullOrEmpty()) {
                            val address = addressList[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            searchView.clearFocus()
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun addMarkersToMap(events: List<TrafficEvent>) {
        events.forEach { event ->
            val color = when(event.severity) {
                "HIGH" -> BitmapDescriptorFactory.HUE_RED
                "MEDIUM" -> BitmapDescriptorFactory.HUE_ORANGE
                else -> BitmapDescriptorFactory.HUE_YELLOW
            }

            val posisi = LatLng(event.latitude, event.longitude)

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(posisi)
                    .title(event.title)
                    .snippet(event.description)
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
            )
            marker?.tag = event
        }
    }

    private fun updateDetailPanel(event: TrafficEvent) {
        findViewById<TextView>(R.id.txtDetailTitle).text = event.title
        findViewById<TextView>(R.id.txtDetailSeverity).text = "Tingkat Bahaya: ${event.severity}"
        findViewById<TextView>(R.id.txtDetailDesc).text = event.description
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showAddMarkerDialog(latLng: LatLng) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Lapor Kemacetan Baru")
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val inputTitle = android.widget.EditText(this).apply { hint = "Judul Laporan" }
        val inputNotes = android.widget.EditText(this).apply { hint = "Catatan/Notes" }
        val spinner = android.widget.Spinner(this)
        spinner.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("LOW", "MEDIUM", "HIGH"))

        layout.addView(inputTitle)
        layout.addView(inputNotes)
        layout.addView(spinner)

        builder.setView(layout)
        builder.setPositiveButton("Kirim") { _, _ ->
            val severity = when(spinner.selectedItemPosition) {
                0 -> Severity.LOW
                1 -> Severity.MEDIUM
                else -> Severity.HIGH
            }
            if (inputTitle.text.isNotEmpty()) {
                viewModel.addManualEvent(inputTitle.text.toString(), inputNotes.text.toString(), latLng, severity)
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }
}