package com.imot.endear

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView

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

/*Activity inflated when clicking on the alert sent in the FriendRequestFragment to track down the person who sent it on map.*/

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityTrackingBinding

    private lateinit var trackingUserLocation : DatabaseReference

    private lateinit var videoView : VideoView

    val REQUEST_VIDEO_CAPTURE = 1968

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_track) as SupportMapFragment
        mapFragment.getMapAsync(this)

        videoView = findViewById(R.id.videoView)

        // Media controller for play, pause, ...
        val mediaControl = MediaController(this)
        mediaControl.setAnchorView(videoView)
        videoView.setMediaController(mediaControl)

//        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
//
//        try {
//            startActivityForResult(takeVideoIntent,REQUEST_VIDEO_CAPTURE)
//        }catch (e:ActivityNotFoundException){
//            Toast.makeText(this, "Impossible d'accéder à la caméra" +e.localizedMessage, Toast.LENGTH_SHORT).show()
//        }

        registerEventRealtime()
        spyView()
    } //end onCreate

    private fun spyView() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        try {
            startActivityForResult(takeVideoIntent,REQUEST_VIDEO_CAPTURE)
        }catch (e:ActivityNotFoundException){
            Toast.makeText(this, "Impossible d'accéder à la caméra" +e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK){
            val videoUri = data?.data
            videoView.setVideoURI(videoUri)
            videoView.start()
        }
    }

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