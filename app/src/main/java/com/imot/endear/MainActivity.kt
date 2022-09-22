package com.imot.endear

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.imot.endear.dataclasses.UserData
import com.imot.endear.model.User
import com.imot.endear.utils.Common
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import io.paperdb.Paper


/*Activity allowing login via google Auth. Firebase realtime DB should log the user's email, name, photo, and real-time location.*/

class MainActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {


    lateinit var user_information : DatabaseReference
    lateinit var providers : List<AuthUI.IdpConfig>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo : BiometricPrompt.PromptInfo
    private lateinit var mainLayout : ScrollView

    private val RC_SIGN_IN = 101
    private val TAG = "GOOGLEAUTH"
    var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    var dialog: Dialog? = null


//        private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//
//    }

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
        fullscreenContentControls?.visibility = VISIBLE
    }
    private var visible_l: Boolean = false
    private val hideRunnable = Runnable { hide() }

    private var dummyButton: Button? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        // Biometric connection
        mainLayout = findViewById(R.id.main_layout)
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)){
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                Toast.makeText(
                    applicationContext,
                    "Le lecteur d'empreinte digitale n'a pas fonctionné correctement.",
                    Toast.LENGTH_SHORT
                ).show()
                //return
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                Toast.makeText(
                    applicationContext,
                    "Votre appareil ne dispose pas de lecteur d'empreinte digitale",
                    Toast.LENGTH_SHORT
                ).show()

                //return
            }
//            BiometricManager.BIOMETRIC_SUCCESS ->
//                return
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                Toast.makeText(
                    applicationContext,
                    "Aucune empreinte digitale trouvée",
                    Toast.LENGTH_SHORT
                ).show()
                //return
            }
//            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
//                return

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(
                    applicationContext,
                    "Une erreur est survenue !!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Toast.makeText(
                    applicationContext,
                    "Une erreur est survenue !!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Toast.makeText(
                    applicationContext,
                    "Une erreur est survenue !!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
//                Toast.makeText(
//                    applicationContext,
//                    "Bienvenue",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        }
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this@MainActivity, executor, object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(
                    applicationContext,
                    "Paramètres biométriques non vérifiés",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(
                    applicationContext,
                    "Accès autorisé",
                    Toast.LENGTH_SHORT
                ).show()
                mainLayout.visibility = VISIBLE
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(
                    applicationContext,
                    "Accès refusé",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("IProtect")
            .setDescription("Dévérrouiller l'application avec votre empreinte digitale.").setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText("Annuler").build()

        biometricPrompt.authenticate(promptInfo)
        //  init DB
        Paper.init(this)


//        oneTapClient = Identity.getSignInClient(this)
//        signUpRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.web_client_id))
//                    // Show all accounts on the device.
//                    .setFilterByAuthorizedAccounts(false)
//                    .build())
//            .build()


        //Init Firebase
        user_information = FirebaseDatabase.getInstance().getReference("Users")

        // Init provider
        providers = listOf(
            EmailBuilder().build(),
            GoogleBuilder().build()
        )

        //Request permission location
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {

//                   Intent(this@MainActivity, MainActivity::class.java).also {
//                        startActivity(it)
////                        putExtraData(Common.loggedUser)
//
//                    }
                    if(isConnected(this@MainActivity)){
                        Toast.makeText(
                            this@MainActivity,
                            "Vous êtes connecté à Internet.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(
                        this@MainActivity,
                        "L'application requiert cette permission pour fonctionner correctement.",
                        Toast.LENGTH_LONG
                    ).show()
                    //finish()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?,
                ) {
                    token!!.continuePermissionRequest()
                }
            }).check()

                // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        dialog = Dialog(this@MainActivity)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.dialog_wait1)
        dialog!!.setCanceledOnTouchOutside(false)
        // Getting the Button Click
        val signInbtn = findViewById<Button>(R.id.google_signIn)
//        signInbtn.setOnClickListener {
//            signIn()
//        }

//        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//
//            var requestCode = RC_SIGN_IN
//            if (requestCode == RC_SIGN_IN) {
//                dialog?.show()
//                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//                val googleCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
//                val idToken = googleCredential.googleIdToken
//                try {
//                    // Google Sign In was successful, authenticate with Firebase
//                    val account = task.getResult(ApiException::class.java)
//                    //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
//                    firebaseAuthWithGoogle(account.idToken)
//
//                    when (result.resultCode) {
//                        RC_SIGN_IN -> {
//                            try {
////                        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
////                        val idToken = credential.googleIdToken
//
//                                when {
//                                    idToken != null -> {
//                                        // Got an ID token from Google. Use it to authenticate
//                                        // with Firebase.
//                                        val firebaseCredential =
//                                            GoogleAuthProvider.getCredential(idToken, null)
//                                        mAuth!!.signInWithCredential(firebaseCredential)
//                                            .addOnCompleteListener(this) { task ->
//                                                if (task.isSuccessful) {
//                                                    // Sign in success, update UI with the signed-in user's information
//                                                    Log.d(TAG, "signInWithCredential:success")
//                                                    val user = mAuth!!.currentUser
//                                                    //Common.loggedUser = user
//                                                    Intent(this, HomeActivity::class.java).also {
//                                                        startActivity(it)
//                                                    }
//                                                    setupUI()
//                                                    Toast.makeText(this,
//                                                        "Bienvenue $user",
//                                                        Toast.LENGTH_SHORT)
//                                                        .show()
//                                                } else {
//                                                    // If sign in fails, display a message to the user.
//                                                    Log.w(TAG,
//                                                        "signInWithCredential:failure",
//                                                        task.exception)
//                                                    Toast.makeText(this,
//                                                        "Désolé, l'authentification a échouée",
//                                                        Toast.LENGTH_SHORT).show()
//                                                }
//                                            }
//                                        // Got an ID token from Google. Use it to authenticate
//                                        // with Firebase.
//                                        val fireBaseUser: FirebaseUser? =
//                                            FirebaseAuth.getInstance().currentUser
//                                        //Check if user exists on DB
//                                        user_information.orderByKey()
//                                            .equalTo(fireBaseUser?.uid)
//                                            .addListenerForSingleValueEvent(
//                                                object : ValueEventListener {
//                                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                                        // Removing the event listener will also prevent any further calls into onDataChange
//                                                        //If user doesn't exists
//                                                        if (snapshot.value == null) {
////                                                if (fireBaseUser != null) {
//                                                            if (!snapshot.child(fireBaseUser!!.uid)
//                                                                    .exists()
//                                                            )//If key uid doesn't exists
//                                                            {
//                                                                Common.loggedUser =
//                                                                    User(fireBaseUser.uid,
//                                                                        fireBaseUser.email!!)
//                                                                //Add to database
//                                                                user_information.child(Common.loggedUser!!.uid!!)
//                                                                    .setValue(Common.loggedUser)
//
//
//                                                            }
//                                                        } else// if user is available
//                                                        {
//                                                            if (fireBaseUser != null) {
//                                                                Common.loggedUser =
//                                                                    snapshot.child(fireBaseUser.uid)
//                                                                        .getValue(User::class.java)!!
//                                                            }
//
//                                                        }
//
//                                                        //Save UID to storage to update location from killed mode
//                                                        Paper.book()
//                                                            .write(Common.USER_UID_SAVE_KEY,
//                                                                Common.loggedUser!!.uid!!)
//                                                        updateToken(fireBaseUser)
//                                                        setupUI()
//                                                    }
//
//                                                    override fun onCancelled(error: DatabaseError) {
//                                                        Log.w(TAG,
//                                                            "Echec de lecture.",
//                                                            error.toException())
//                                                    }
//                                                })
//                                        Log.d(TAG, "Got ID token.")
//                                    }
//                                    else -> {
//                                        // Shouldn't happen.
//                                        Log.d(TAG, "No ID token!")
//                                    }
//                                }
//                            } catch (e: ApiException) {
//                                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
//                            }
//            }
//        }
//
//                } catch (e: ApiException) {
//                    // Google Sign In failed, update UI appropriately
//                    Log.w(TAG, "Google sign in failed", e)
//                    dialog?.dismiss()
//                    // ...
//                }
//            }else {
//                Toast.makeText(this,
//                    "!!! Une erreur est survenue lors de la connexion. Veuillez réessayer plus tard !!!",
//                    Toast.LENGTH_LONG).show()
//            }
//        }
//
        signInbtn.setOnClickListener {

            if(isConnected(this)){
                signIn()
                //Snackbar.make(view, "Bienvenue ", Snackbar.LENGTH_LONG).show()


//                var location = LocationListener.
//
//
//                val dbRef = FirebaseDatabase.getInstance().getReference("Users")
//                val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
//                val currentUser = mAuth?.currentUser
//
//                val name = account?.displayName
//                val image = account?.photoUrl.toString()
//                val uid = account?.id
//                val email = account?.email
//                //val location = account?.location
//
//
////        val name = currentUser?.displayName
////        val image = currentUser?.photoUrl.toString()
////        val uid = currentUser?.uid
////        val email = currentUser?.email
//
//
//                val User = UserData(name,image,uid,email, location)
//
//                dbRef.child(name!!).setValue(User).addOnSuccessListener {
//                    Toast.makeText(this, "Enregistrement réussi",Toast.LENGTH_SHORT).show()
//                }.addOnFailureListener{
//                    Toast.makeText(this, "Echec de l'Enregistrement",Toast.LENGTH_SHORT).show()
//                }

            }else{
                showCustomDialog()
            }
        }
    }//end onCreate


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


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.currentUser

        if(currentUser!=null){
            Intent(this, HomeActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun showCustomDialog() {
        var builder = AlertDialog.Builder(this)
        builder.setMessage("Vérifiez votre connexion internet svp")
            .setCancelable(false)
                builder.setNegativeButton("Activer la Wifi") { _, _ ->
                    Intent(Settings.ACTION_WIFI_SETTINGS).also {
                        startActivity(it)
                    }
                    if(isConnected(this)){
                        Toast.makeText(applicationContext,
                            "Wifi activé", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setPositiveButton ("Activer les données mobiles") { _, _ ->

                    try {
                        if (intent.resolveActivity(packageManager) != null) {

                            Intent(Settings.ACTION_SETTINGS).also {
                                startActivity(it)
//                                Toast.makeText(applicationContext,
//                                    "Données mobiles activées", Toast.LENGTH_SHORT).show()

                        }
                        }else{

                                Intent(Settings.ACTION_DATA_USAGE_SETTINGS).also {
                                    startActivity(it)

                                    if(isConnected(applicationContext)){
                                        Toast.makeText(applicationContext,
                                            "Données mobiles activées", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            }

                    }catch (e: ActivityNotFoundException) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Fail to open Settings", e)
                        dialog?.dismiss()
                        // ...
                    }

                }

        builder.setNeutralButton("Annuler") { _, _ ->
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                Toast.makeText(applicationContext,
                    "Cette application nécessite la connection Internet pour fonctionner.", Toast.LENGTH_SHORT).show()
            }
        }
                builder.show()
    }

//    private fun isConnected(): Boolean {
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
//        return if (connectivityManager is ConnectivityManager) {
//            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
//            networkInfo?.isConnected ?: false
//        } else false
//    }

    @SuppressLint("MissingPermission")
    private fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {

            val nw      = connectivityManager.activeNetwork?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }


    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
//            .apply {
//            //putExtra(EXTRA_MESSAGE, RC_SIGN_IN)
//            setResult(Activity.RESULT_OK, intent);
//        }
//        startActivity(signInIntent)
        startActivityForResult(signInIntent, RC_SIGN_IN)
        //resultLauncher.launch(signInIntent)

        //activityResultLauncher.launch(intent)

        //        var mLastLocation = location
//
//        //Place current location marker
//        val latLng = LatLng(location.latitude, location.longitude)
//
//        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
//        val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
//        val currentUser = mAuth?.currentUser
//
//                val name = account?.displayName
//                val image = account?.photoUrl.toString()
//                val uid = account?.id
//                val email = account?.email
//                //val location = account?.location
//
//
////        val name = currentUser?.displayName
////        val image = currentUser?.photoUrl.toString()
////        val uid = currentUser?.uid
////        val email = currentUser?.email
//
//
//        val User = UserData(name,image,uid,email,mLastLocation)
//
//        dbRef.child(name!!).setValue(User).addOnSuccessListener {
//            Toast.makeText(this, "Enregistrement réussi",Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener{
//            Toast.makeText(this, "Echec de l'Enregistrement",Toast.LENGTH_SHORT).show()
//        }
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            dialog?.show()
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val response : IdpResponse? = IdpResponse.fromResultIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.idToken)

                // Got an ID token from Google. Use it to authenticate
                                        // with Firebase.
                                        val fireBaseUser: FirebaseUser? =
                                            FirebaseAuth.getInstance().currentUser
                                        //Check if user exists on DB
                                        user_information.orderByKey()
                                            .equalTo(fireBaseUser?.displayName.toString())
                                            .addListenerForSingleValueEvent(
                                                object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        // Removing the event listener will also prevent any further calls into onDataChange
                                                        //If user doesn't exists
                                                        if (snapshot.value == null) {
                                                if (fireBaseUser != null) {
                                                            if (!snapshot.child(fireBaseUser!!.uid)
                                                                    .exists()
                                                            )//If key uid doesn't exists
                                                            {
                                                                Common.loggedUser =
                                                                    User(fireBaseUser.uid,
                                                                        fireBaseUser.email!!,
                                                                        fireBaseUser.displayName!!,
                                                                    fireBaseUser.photoUrl.toString())
                                                                //Add to database
                                                                user_information.child(Common.loggedUser.uid!!)
                                                                    .setValue(Common.loggedUser)
                                                            }
                                                        } else// if user is available
                                                        {
                                                            if (fireBaseUser != null) {
                                                                Common.loggedUser =
                                                                    snapshot.child(fireBaseUser.uid)
                                                                        .getValue(User::class.java)!!
                                                            }
                                                        }
                                                            Common.loggedUser =
                                                                User(Common.FROM_UID,
                                                                    Common.FROM_EMAIL,
                                                                    Common.FROM_NAME,
                                                                    Common.FROM_IMAGE)

                                                            //Save UID to storage to update location from killed mode
                                                            Paper.book()
                                                            .write(Common.USER_UID_SAVE_KEY,
                                                                Common.loggedUser.uid!!)
                                                            updateToken(fireBaseUser)
                                                            setupUI()
                                                    }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        Log.w(TAG,
                                                            "Echec de lecture.",
                                                            error.toException())
                                                    }
                                                })
                                        Log.d(TAG, "Got ID token.")

                                                                    setupUI()
                val user = mAuth?.currentUser

                if (fireBaseUser != null) {
                    Toast.makeText(applicationContext,
                        "Bienvenue"+
                                fireBaseUser.displayName!!,
                        Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                dialog?.dismiss()
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth!!.currentUser
                    val i = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(i)
                    finish()
                    dialog?.dismiss()
                    //  updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    //  Log.w(TAG, "signInWithCredential:failure", task.getException());
                    //  Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                    // updateUI(null);
                    dialog?.dismiss()
                    Toast.makeText(this@MainActivity, "Login failed", Toast.LENGTH_SHORT).show()
                }

                // ...
            }
    }


    private fun setupUI() {
        //After all done ! Navigate Home
        Intent(this@MainActivity, HomeActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }


    private fun updateToken(fireBaseUser: FirebaseUser?) {
        val tokens = FirebaseDatabase.getInstance() // Use for send and receive notifications
            .getReference(Common.TOKENS)

        //Get Token
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this
        ) { instanceIdResult ->
            if (fireBaseUser != null) {
                tokens.child(fireBaseUser.uid)
                        ///////////////////////////////////////////////
                    .setValue(instanceIdResult)
                val newToken = instanceIdResult.toString()
                Log.d("newToken", newToken)
            }
        }.addOnFailureListener(this
        ) {
            Toast.makeText(
                this@MainActivity,
                "" + it.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

//    override fun onLocationChanged(location: Location)  {
//}

}










