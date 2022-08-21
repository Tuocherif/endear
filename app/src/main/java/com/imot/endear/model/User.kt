package com.imot.endear.model

import android.location.Location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class User(uid: String, email: String, name: String, image: String) {
    val fireBaseUser: FirebaseUser? =
        FirebaseAuth.getInstance().currentUser
    var uid : String = fireBaseUser?.uid!!
   var email : String = fireBaseUser?.email!!
   var name : String = fireBaseUser?.displayName!!
   var image :String = fireBaseUser?.photoUrl.toString()
    var location: Location? = null
    var acceptList: HashMap<String,User> // List friends users


    init {
        acceptList = HashMap()
    }

}