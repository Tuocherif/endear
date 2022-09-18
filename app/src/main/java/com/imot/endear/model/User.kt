package com.imot.endear.model

import android.location.Location
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class User{
//    private val fireBaseUser: FirebaseUser? =
//        FirebaseAuth.getInstance().currentUser
//    var uid : String = fireBaseUser?.uid!!
//   var email : String = fireBaseUser?.email!!
//   var name : String = fireBaseUser?.displayName!!
//   var image  = fireBaseUser?.photoUrl.toString()
//    var location: Location? = null
//    var acceptList: HashMap<String,User>?=null // List friends users

    var uid : String? = null
    var email : String? = null
    var name : String? = null
    var image  : String?= null
    var location: Location? = null
    var acceptList: HashMap<String,User>?=null // List friends users

    init {
        acceptList = HashMap()
    }

    constructor()

    constructor(uid: String, email: String, name: String, image: String){
        this.uid = uid
        this.email = email
        this.name = name
        this.image = image
        acceptList = HashMap()
    }
}