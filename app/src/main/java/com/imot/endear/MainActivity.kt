package com.imot.endear

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

   // private fun showSignInOptions() {

//        val intent = Intent(this, HomeActivity::class.java).also {
//            startActivity(it)
//            //putExtraData(Common.loggedUser)
//        }
        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {



                    val fireBaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                    //Check if user exists on DB
                    user_information.orderByKey()
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
                                                User(fireBaseUser.uid, fireBaseUser.email!!)
                                            //Add to database
                                            user_information.child(Common.loggedUser!!.uid!!)
                                                .setValue(Common.loggedUser)


                                        }
                                    } else// if user is available
                                    {
                                        if (fireBaseUser != null) {
                                            Common.loggedUser = snapshot.child(fireBaseUser.uid)
                                                .getValue(User::class.java)!!
                                        }

                                    }

                                    //Save UID to storage to update location from killed mode
                                    Paper.book()
                                        .write(Common.USER_UID_SAVE_KEY, Common.loggedUser!!.uid!!)
                                    updateToken(fireBaseUser)
                                    setupUI()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.w(ContentValues.TAG, "Echec de lecture.", error.toException())
                                }
                            })
//                    }

                //   if (result.resultCode == Activity.RESULT_OK) {
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    //.setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    //.setTheme(R.style.LoginTheme)
                    // .setLogo(R.drawable.ic_melomania_blue_light)
                    .build()
                REQUEST_CODE
                //   }

            }
        //startForResult.launch(intent)

   // }

    companion object {
        private val REQUEST_CODE = 7946
    }

    lateinit var user_information : DatabaseReference
    lateinit var providers : List<AuthUI.IdpConfig>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //  init DB
        Paper.init(this)

        //Init Firebase
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

        //Init Provider
        providers = listOf(
             AuthUI.IdpConfig.EmailBuilder().build(),
             AuthUI.IdpConfig.GoogleBuilder().build()
        )

        //Request permission location
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {

                    val intent = Intent(this@MainActivity, HomeActivity::class.java).also {
                        startActivity(it)
                        //putExtraData(Common.loggedUser)
                    }
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Permission already granted",
//                        Toast.LENGTH_SHORT
//                    ).show()


                    //showSignInOptions()

                    startForResult.launch(intent)

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
}// end onCreate



    //Caller
  //  val intentRes = Intent(this, MainActivity::class.java)
//    getResult.launch(intent)

    // Receiver
  //  @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
////        val intentRes = Intent(this@MainActivity, MainActivity::class.java).also {
////            startActivity(it)
////            //putExtraData(Common.loggedUser)
////        }
////
////        val getResult =
////            registerForActivityResult(
////
////                ActivityResultContracts.StartActivityForResult()
////
////            ) { it ->
//
//
//                    if (requestCode == REQUEST_CODE) {
//
//                        val fireBaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
//                        //Check if user exists on DB
//                        user_information.orderByKey()
//                            .equalTo(fireBaseUser?.uid)
//                            .addListenerForSingleValueEvent(
//                                object : ValueEventListener {
//                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                        // Removing the event listener will also prevent any further calls into onDataChange
//                                        //If user doesn't exists
//                                        if (snapshot.value == null) {
////                                                if (fireBaseUser != null) {
//                                            if (!snapshot.child(fireBaseUser!!.uid)
//                                                    .exists()
//                                            )//If key uid doesn't exists
//                                            {
//                                                Common.loggedUser =
//                                                    User(fireBaseUser.uid, fireBaseUser.email!!)
//                                                //Add to database
//                                                user_information.child(Common.loggedUser!!.uid!!)
//                                                    .setValue(Common.loggedUser)
//
//
//                                            }
//                                        } else// if user is available
//                                        {
//                                            if (fireBaseUser != null) {
//                                                Common.loggedUser = snapshot.child(fireBaseUser.uid)
//                                                    .getValue(User::class.java)!!
//                                            }
//
//                                        }
//
//                                        //Save UID to storage to update location from killed mode
//                                        Paper.book()
//                                            .write(Common.USER_UID_SAVE_KEY, Common.loggedUser!!.uid!!)
//                                        updateToken(fireBaseUser)
//                                        setupUI()
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//                                    }
//                                })
////                    }
//               }
//
//       // getResult.launch(intentRes)
//    }



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








