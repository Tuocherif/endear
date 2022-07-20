package com.imot.endear

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.imot.endear.databinding.ActivityHomeBinding
import com.imot.endear.databinding.FragmentFriendrequestBinding
import com.imot.endear.databinding.FragmentFriendslistBinding
import com.imot.endear.databinding.FragmentSignOutBinding
import com.imot.endear.ui.find_people.FindPeopleFragment
import com.imot.endear.ui.friend_request.FriendRequestFragment
import com.imot.endear.ui.friends_list.FriendsListFragment
import com.imot.endear.ui.home.HomeFragment
import com.imot.endear.ui.settings.SettingsFragment
import com.imot.endear.ui.sign_out_fragment.SignOutFragment
import com.squareup.picasso.Picasso
import pl.droidsonroids.gif.GifImageView

class HomeActivity : AppCompatActivity() , OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private var service: LocationManager? = null
    private var enabled: Boolean? = null
    private var mLocationRequest: LocationRequest? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private var mCurrLocationMarker: Marker? = null
    private lateinit var mMap: GoogleMap
    private var REQUEST_LOCATION_CODE = 101
    private lateinit var  drawerLayout: DrawerLayout

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var hView: View
    private lateinit var user_email: TextView
    private lateinit var user_image: ImageView
    private lateinit var imageMenu: ImageView
    private lateinit var navigationView: NavigationView


    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest.create().apply {
        interval = 1000
        fastestInterval = 1000
        priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        // Check if enabled and if not send user to the GPS settings
        if (!enabled!!) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Activez la localisation svp." +
                    "\nCette application necessite la localisation GPS pour fonctionner correctement.",
                Toast.LENGTH_LONG).show()
        }
        // Check if permission is granted or not
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient?.let {
                LocationServices.FusedLocationApi.requestLocationUpdates(it,
                    mLocationRequest!!, this)
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }


        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //setContentView(R.layout.content_home)

            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)

            drawerLayout = binding.drawerLayout


            service = this.getSystemService(LOCATION_SERVICE) as LocationManager
        enabled = service!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_home) as SupportMapFragment
        mapFragment.getMapAsync(this)



        //var navHeaderHomeBinding = NavHeaderHomeBinding.inflate(layoutInflater)

        setResult(Activity.RESULT_OK, intent)

            //binding.appBarHome
            //setSupportActionBar(binding.appBarHome.toolbar)



        val alarm_fix = findViewById<ImageView>(R.id.alarm_fix)
        val alarm = findViewById<GifImageView>(R.id.alarm)


        alarm_fix.setOnClickListener { view ->
            alarm_fix.visibility = View.GONE
            alarm.visibility = View.VISIBLE
            Snackbar.make(view, "!!! Alerte d'urgence envoyée !!!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
//            Intent(this, AllPeopleActivity::class.java).also {
//                startActivity(it)
//            }
        }




        alarm.setOnClickListener { view ->
            alarm_fix.visibility = View.VISIBLE
            alarm.visibility = View.GONE
            Snackbar.make(view, "!!! Fin de l'alerte !!!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
//            Intent(this, AllPeopleActivity::class.java).also {
//                startActivity(it)
//            }
        }

            val navView: NavigationView = binding.navView
            //val navController = findNavController(R.id.nav_host_fragment_content_home)
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
            val navController = navHostFragment.navController

            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home,
                R.id.nav_friends_list,
                R.id.nav_find_people,
                R.id.nav_friend_request,
                R.id.nav_sign_out
            ),
                drawerLayout)
//            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            //navigationView.setNavigationItemSelectedListener(this)

            navView.setNavigationItemSelectedListener{

                it.isChecked = true

                when(it.itemId){
                    R.id.nav_home -> replaceFragment(HomeFragment())
                    R.id.nav_friends_list -> replaceFragment(FriendsListFragment())
                    R.id.nav_find_people -> replaceFragment(FindPeopleFragment())
                    R.id.nav_friend_request -> replaceFragment(FriendRequestFragment())
                    R.id.nav_settings -> replaceFragment(SettingsFragment())
                    R.id.nav_sign_out -> replaceFragment(SignOutFragment())
                }
                true
            }


            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            //navigationView = findViewById(R.id.nav_view)
            navigationView  = navView.findViewById(R.id.nav_view)
            hView = navigationView.getHeaderView(0)

            user_email = hView.findViewById(R.id.user_email)
            user_image = hView.findViewById(R.id.user_image)
            imageMenu = findViewById(R.id.ImageMenu)

            if (account != null) {
                user_email.text = account.displayName
                //user_image.setImageURI(account.photoUrl)
                Picasso.get().load(account.photoUrl)
                    .into(user_image)
                Picasso.get().load(account.photoUrl)
                    .into(imageMenu)
            }



            imageMenu.setOnClickListener {
                onSupportNavigateUp()
            }

    } // end onCreate

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main_drawer, menu)

        return true
    }



        override fun onSupportNavigateUp(): Boolean {
//            val navController = findNavController(R.id.nav_host_fragment_content_home)
//            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
            val navController = navHostFragment.navController
            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient()
                mMap.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            buildGoogleApiClient()
            mMap.isMyLocationEnabled = true
        }
    }

    @Synchronized
    fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        mGoogleApiClient!!.connect()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient()
                        }
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Autorisation refusée!", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Autorisation de localisation requise.")
                    .setMessage("Cette application necessite l'autorisation de la localisation pour fonctionner correctement. Veuillez accepter d'utiliser la fonctionnalité de localisation")
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_LOCATION_CODE)
                    }
                    .create()
                    .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onLocationChanged(location: Location)  {
            mLastLocation = location
            if (mCurrLocationMarker != null) {
                mCurrLocationMarker!!.remove()
            }

            //Place current location marker
            val latLng = LatLng(location.latitude, location.longitude)
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("Ma position actuelle")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            mCurrLocationMarker = mMap.addMarker(markerOptions)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))}

    }



