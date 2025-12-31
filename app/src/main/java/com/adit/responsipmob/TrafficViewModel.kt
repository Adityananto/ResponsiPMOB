package com.adit.responsipmob

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*

class TrafficViewModel : ViewModel() {

    // Inisialisasi referensi Firebase ke tabel "traffic_events"
    private val dbRef = FirebaseDatabase.getInstance().getReference("traffic_events")

    private val _trafficEvents = MutableLiveData<List<TrafficEvent>>()
    val trafficEvents: LiveData<List<TrafficEvent>> = _trafficEvents

    init {
        // Otomatis update data
        listenToTrafficData()
    }

    private fun listenToTrafficData() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<TrafficEvent>()
                for (data in snapshot.children) {
                    val event = data.getValue(TrafficEvent::class.java)
                    event?.let { list.add(it) }
                }
                _trafficEvents.value = list
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun addManualEvent(title: String, notes: String, location: LatLng, severity: Severity) {
        val id = dbRef.push().key ?: return


        val event = TrafficEvent(
            id = id,
            title = title,
            description = notes,
            latitude = location.latitude,
            longitude = location.longitude,
            severity = severity.name
        )

        dbRef.child(id).setValue(event)
    }

    fun removeEvent(event: TrafficEvent) {
        event.id?.let {
            dbRef.child(it).removeValue()
        }
    }
}