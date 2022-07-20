package com.imot.endear

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.biometric.BiometricPrompt
import android.provider.Settings
import android.util.Log
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.imot.endear.model.User
import com.imot.endear.utils.Common
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.paperdb.Paper

class MainActivity : AppCompatActivity() {


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        // Biometric connection
        mainLayout = findViewById(R.id.main_layout)
        var biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)){
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

        }
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this@MainActivity, executor, object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(
                    applicationContext,
                    "Paramètres biométriques vérifiés",
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
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

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
                        "L'application requiert cette permission pour fonctionner.",
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
        signInbtn.setOnClickListener {view ->

            if(isConnected(this)){
                signIn()
                //Snackbar.make(view, "Bienvenue ", Snackbar.LENGTH_LONG).show()
            }else{
                showCustomDialog()
            }
        }
    }//end onCreate



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

    private fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return false
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

    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            dialog?.show()
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
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
                                        user_information.orderByValue()
                                            .equalTo(fireBaseUser?.uid)
                                            .addListenerForSingleValueEvent(
                                                object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        // Removing the event listener will also prevent any further calls into onDataChange
                                                        //If user doesn't exists
                                                        if (snapshot.value == null) {
//                                                if (fireBaseUser != null) {
                                                            if (!snapshot.child(fireBaseUser!!.uid)
                                                                    .exists()
                                                            )//If key uid doesn't exists
                                                            {
                                                                Common.loggedUser =
                                                                    User(fireBaseUser.uid,
                                                                        fireBaseUser.email!!,
                                                                        fireBaseUser.displayName!!)
                                                                //Add to database
                                                                user_information.child(Common.loggedUser!!.uid!!)
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

                                                        //Save UID to storage to update location from killed mode
                                                        Paper.book()
                                                            .write(Common.USER_UID_SAVE_KEY,
                                                                Common.loggedUser!!.uid!!)
                                                        updateToken(fireBaseUser)
                                                        setupUI()
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

                Toast.makeText(this,
                                                        "Bienvenue $user",
                                                        Toast.LENGTH_LONG)
                                                        .show()
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
        val tokens = FirebaseDatabase.getInstance()
            .getReference(Common.TOKENS)

        //Get Tokens
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this
        ) { instanceIdResult ->
            if (fireBaseUser != null) {
                tokens.child(fireBaseUser.uid)
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
}










