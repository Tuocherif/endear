package com.imot.endear

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
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
import com.google.firebase.database.*
import com.imot.endear.databinding.ActivityHomeBinding
import com.imot.endear.dataclasses.UserData
import com.imot.endear.model.User
import com.imot.endear.model.MyResponse
import com.imot.endear.model.Request
import com.imot.endear.ui.endear.EndearFragment
import com.imot.endear.ui.find_people.FindPeopleFragment
import com.imot.endear.ui.friend_request.FriendRequestFragment
import com.imot.endear.ui.friends_list.FriendsListFragment
import com.imot.endear.ui.home.HomeFragment
import com.imot.endear.ui.settings.SettingsFragment
import com.imot.endear.ui.sign_out_fragment.SignOutFragment
import com.imot.endear.utils.Common
import com.imot.endear.utils.Common.fireBaseUser
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pl.droidsonroids.gif.GifImageView

/*This activity gives the location on map of all the users whose invitation the current user has accepted and who have accepted his.*/

class HomeActivity : AppCompatActivity() , OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener,
    SharedPreferences.OnSharedPreferenceChangeListener  {

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
    private lateinit var binding: com.imot.endear.databinding.ActivityHomeBinding
    private lateinit var hView: View
    private lateinit var user_email: TextView
    private lateinit var user_image: ImageView
    private lateinit var imageMenu: ImageView
    private lateinit var navigationView: NavigationView
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo : BiometricPrompt.PromptInfo
    val mode = AppCompatDelegate.getDefaultNightMode()
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbRef : DatabaseReference



    private val hideHandler = Handler(Looper.myLooper()!!)

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        this.window?.decorView?.systemUiVisibility = flags
        (this as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible_l: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {

            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var dummyButton: Button? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    val compositeDisposable = CompositeDisposable()

    val model =
        fireBaseUser?.let {
            User(it.uid,
                it.email!!,
                it.displayName!!,
                it.photoUrl.toString())
        }

    val list = fireBaseUser?.let {
        User(it.uid,
            it.email!!,
            it.displayName!!,
            it.photoUrl.toString()).acceptList
    }



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

            sharedPreferences = this.getSharedPreferences("custom_theme", Context.MODE_PRIVATE)

//            val biometricManager = BiometricManager.from(this)
//
//            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)){
//                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
//                    Toast.makeText(
//                        applicationContext,
//                        "Le lecteur d'empreinte digitale n'a pas fonctionné correctement.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    //return
//                }
//
//                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
//                    Toast.makeText(
//                        applicationContext,
//                        "Votre appareil ne dispose pas de lecteur d'empreinte digitale",
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                    //return
//                }
////            BiometricManager.BIOMETRIC_SUCCESS ->
////                return
//                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
//                    Toast.makeText(
//                        applicationContext,
//                        "Aucune empreinte digitale trouvée",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    //return
//                }
////            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
////                return
//
//                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
//                    Toast.makeText(
//                        applicationContext,
//                        "Une erreur est survenue !!!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
//                    Toast.makeText(
//                        applicationContext,
//                        "Une erreur est survenue !!!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
//                    Toast.makeText(
//                        applicationContext,
//                        "Une erreur est survenue !!!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                BiometricManager.BIOMETRIC_SUCCESS -> {
////                    Toast.makeText(
////                        applicationContext,
////                        "Bienvenue",
////                        Toast.LENGTH_SHORT
////                    ).show()
//                }
//            }
//            val executor = ContextCompat.getMainExecutor(this)
//
//            biometricPrompt = BiometricPrompt(this@HomeActivity, executor, object : BiometricPrompt.AuthenticationCallback(){
//                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                    super.onAuthenticationError(errorCode, errString)
//                    Toast.makeText(
//                        applicationContext,
//                        "Paramètres biométriques non vérifiés",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    finish()
//                }
//
//                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                    super.onAuthenticationSucceeded(result)
//                    Toast.makeText(
//                        applicationContext,
//                        "Accès autorisé",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    drawerLayout.visibility = View.VISIBLE
//                }
//
//                override fun onAuthenticationFailed() {
//                    super.onAuthenticationFailed()
//                    Toast.makeText(
//                        applicationContext,
//                        "Accès refusé",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })
//
//            promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("IProtect")
//                .setDescription("Dévérrouiller l'application avec votre empreinte digitale.").setAllowedAuthenticators(
//                    BiometricManager.Authenticators.BIOMETRIC_STRONG)
//                .setNegativeButtonText("Annuler").apply {
////                moveTaskToBack(true)
////                android.os.Process.killProcess(android.os.Process.myPid())
////                exitProcess(1)
//                    finish()
//            }.build()
//
//            biometricPrompt.authenticate(promptInfo)

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
            val editor = sharedPreferences.edit()



            alarm_fix.setOnClickListener { view ->
            alarm_fix.visibility = View.GONE
            alarm.visibility = View.VISIBLE

            Snackbar.make(view, "              !!! Alerte d'urgence envoyée !!!", Snackbar.LENGTH_LONG)
                .setBackgroundTintList(ContextCompat.getColorStateList(applicationContext, R.color.accent))
                .setAction("Action", null)
                .show()
            editor.putBoolean("dark_theme", true).apply()
            setTheme(R.style.AlertTheme)
            //reset()


                sendAlert(model, list)

//            val sendIntent = Intent().apply {
//                action = Intent.ACTION_SEND
//                putExtra(Intent.EXTRA_TEXT, Common.PUBLIC_LOCTION)
//                type = "location"
//            }
//            /*sendIntent.action = Intent.ACTION_SEND
//            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.firstapp.com/posts/${post.id}")
//            sendIntent.type = "text/plain"*/
//
//            val shareIntent = Intent.createChooser(sendIntent,"Partager")
//            var mContext: Context = this
//            mContext.startActivity(shareIntent)
        }




        alarm.setOnClickListener { view ->
            alarm_fix.visibility = View.VISIBLE
            alarm.visibility = View.GONE
            Snackbar.make(view, "                        !!! Fin de l'alerte !!!", Snackbar.LENGTH_LONG)
                .setBackgroundTintList(ContextCompat.getColorStateList(applicationContext, com.jpardogo.android.googleprogressbar.library.R.color.green))
                .setAction("Action", null).show()

            editor.putBoolean("DayTheme", true).apply()
            setTheme(R.style.DayTheme)
            //reset()

            stopAlert(model, list)
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
                R.id.nav_settings,
                R.id.nav_sign_out,
                R.id.nav_game
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
                    R.id.nav_game -> replaceFragment(EndearFragment())
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

//                val name = account.displayName
//                val image = account.photoUrl.toString()
//
//                dbRef = FirebaseDatabase.getInstance().getReference("Users")
//
//                val User = UserData(name,image)
//
//                dbRef.child(name!!).setValue(User).addOnSuccessListener {
//                    Toast.makeText(this, "Enregistrement réussi",Toast.LENGTH_SHORT).show()
//                }.addOnFailureListener{
//                    Toast.makeText(this, "Echec de l'Enregistrement",Toast.LENGTH_SHORT).show()
//                }

            }



            imageMenu.setOnClickListener {
                onSupportNavigateUp()
            }

    } // end onCreate

    override fun onBackPressed() {
        super.onBackPressed()
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    private fun sendAlert(model : User?, List: HashMap<String, User>?)  {
            //Get token to send friend request
            val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)
        if (model != null) {
            tokens
                .orderByKey().equalTo(model.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null)//user not available
                        {
                            Toast.makeText(applicationContext,"La personne que vous " +
                                    "essayez de joindre n'est pas disponible.", Toast.LENGTH_SHORT).show()
                        }else{
                            //Create request
                            val request = Request()
                            val dataSend = HashMap<String,String>()
                            dataSend[Common.FROM_UID] = Common.loggedUser!!.uid.toString() //sender's uid
                            dataSend[Common.FROM_EMAIL] = Common.loggedUser!!.email.toString() //sender's email
                            dataSend[Common.FROM_NAME] = Common.loggedUser!!.name.toString() //sender's name
                            dataSend[Common.FROM_IMAGE.toString()] =
                                Common.loggedUser!!.image.toString() //sender's image
                            dataSend[Common.PUBLIC_LOCTION] = Common.loggedUser!!.location!!.toString() //sender's Location
                            dataSend[Common.TO_UID] = model.uid.toString() //receiver's uid
                            dataSend[Common.TO_EMAIL] = model.email.toString() //receiver's email
                            dataSend[Common.TO_NAME] = model.name.toString() //receiver's name
                            dataSend[Common.TO_IMAGE] = model.image.toString() //receiver's image


                            //set request
                            request.to = snapshot.child(List!!.toString()).getValue(String::class.java)!!
                            request.data = dataSend

                            //send

                            compositeDisposable.add(Common.fcmService.sendFriendAlertToUser(request)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({t: MyResponse? ->
                                    if (t!!.success == 1){
                                        Toast.makeText(applicationContext,
                                            "Votre demande a bien été envoyée.",Toast.LENGTH_LONG).show()
                                    }
                                },{t: Throwable? ->
                                    Toast.makeText(applicationContext,
                                        t!!.message,Toast.LENGTH_LONG).show()
                                }))

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }


        }

    private fun stopAlert(model: User?, list: HashMap<String, User>?) {
            //Get token to send friend request
            val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)
            if (model != null) {
                tokens
                    .orderByKey().equalTo(model.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value == null)//user not available
                            {
                                Toast.makeText(applicationContext,"La personne que vous " +
                                        "essayez de joindre n'est pas disponible.", Toast.LENGTH_SHORT).show()
                            }else{
                                //Create request
                                val request = Request()
                                val dataSend = HashMap<String,String>()
                                dataSend[Common.FROM_UID] = Common.loggedUser!!.uid.toString() //sender's uid
                                dataSend[Common.FROM_EMAIL] = Common.loggedUser!!.email.toString() //sender's email
                                dataSend[Common.FROM_NAME] = Common.loggedUser!!.name.toString() //sender's name
                                dataSend[Common.FROM_IMAGE.toString()] =
                                    Common.loggedUser!!.image.toString() //sender's image
                                dataSend[Common.PUBLIC_LOCTION] = Common.loggedUser!!.location!!.toString() //sender's location
                                dataSend[Common.TO_UID] = model.uid.toString() //receiver's uid
                                dataSend[Common.TO_EMAIL] = model.email.toString() //receiver's email
                                dataSend[Common.TO_NAME] = model.name.toString() //receiver's name
                                dataSend[Common.TO_IMAGE] =
                                    model.image.toString() //receiver's image


                                //set request
                                request.to = snapshot.child(list!!.toString()).getValue(String::class.java)!!
                                request.data = dataSend

                                //send

                                compositeDisposable.add(Common.fcmService.sendFriendSafeToUser(request)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({t: MyResponse? ->
                                        if (t!!.success == 1){
                                            Toast.makeText(applicationContext,
                                                "Votre requête a bien été envoyée.\n " +
                                                        "Nous sommes contents de vous savoir de nouveau en sécurité.",Toast.LENGTH_LONG).show()

                                            findViewById<LinearLayout>(R.id.alert).setBackgroundColor(ContextCompat.getColor(applicationContext, com.jpardogo.android.googleprogressbar.library.R.color.green))
                                        }
                                    },{t: Throwable? ->
                                        Toast.makeText(applicationContext,
                                            t!!.message,Toast.LENGTH_LONG).show()
                                    }))

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        visible = true
//
////        dummyButton = binding.dummyButton
////        fullscreenContent = binding.fullscreenContent
////        fullscreenContentControls = binding.fullscreenContentControls
//        // Set up the user interaction to manually show or hide the system UI.
//        fullscreenContent?.setOnClickListener { toggle() }
//
//        // Upon interacting with UI controls, delay any scheduled hide()
//        // operations to prevent the jarring behavior of controls going away
//        // while interacting with the UI.
//        dummyButton?.setOnTouchListener(delayHideTouchListener)
//    }

    @SuppressLint("RestrictedApi")
    private fun reset() {
        intent = Intent(AuthUI.getApplicationContext(), MainActivity::class.java)
        startActivity(intent)

        intent = Intent(AuthUI.getApplicationContext(), HomeActivity::class.java)
        startActivity(intent)

//        intent = Intent(AuthUI.getApplicationContext(), FriendsListFragment::class.java)
//        startActivity(intent)
//
//        intent = Intent(AuthUI.getApplicationContext(), FragmentSettingsBinding::class.java)
//        startActivity(intent)
//
//        intent = Intent(AuthUI.getApplicationContext(), FindPeopleFragment::class.java)
//        startActivity(intent)
//
//        intent = Intent(AuthUI.getApplicationContext(), AddPeopleFragment::class.java)
//        startActivity(intent)
//
//        intent = Intent(AuthUI.getApplicationContext(), SignOutFragment::class.java)
//        startActivity(intent)

    }


    override fun onResume() {
        super.onResume()
        this.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        this.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dummyButton = null
        fullscreenContent = null
        fullscreenContentControls = null
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun toggle() {
        if (visible_l) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible_l = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible_l = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (this as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }


//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }


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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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





