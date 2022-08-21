package com.imot.endear

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.imot.endear.databinding.ActivityTrackingBinding
import com.imot.endear.model.MyLocation
import com.imot.endear.utils.Common

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityTrackingBinding

    private lateinit var trackingUserLocation : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerEventRealtime()
    } //end onCreate

    private fun registerEventRealtime() {
        trackingUserLocation = FirebaseDatabase.getInstance()
            .getReference(Common.PUBLIC_LOCTION)
            .child(Common.trackingUser!!.uid!!)

        trackingUserLocation.addValueEventListener(this)

    }

    override fun onResume() {
        super.onResume()
        trackingUserLocation.addValueEventListener(this)
    }


    override fun onStop() {
        trackingUserLocation.removeEventListener(this)
        super.onStop()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        //Skin
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.my_uber_style))
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.value != null){
            val location = snapshot.getValue(MyLocation::class.java)

            //Add Marker
            val userMarker = LatLng(location!!.latitude,location.longitude)
            mMap.addMarker(MarkerOptions().position(userMarker).title(Common.trackingUser!!.email))?.snippet =
                Common.getDateFormatted(Common.convertTimeStampToDate(location.time))

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker,16f))
        }
    }

    override fun onCancelled(error: DatabaseError) {
        Log.w(TAG, "Echec de lecture.", error.toException())    }
}