package com.imot.endear.model

import com.google.firebase.database.DataSnapshot

class User {
    var uid : String? = null
    var email : String? = null
    var acceptList: HashMap<String,User> // List user friend



    constructor(uid: String, email: String){
        this.uid = uid
        this.email = email
        acceptList = HashMap()

    }

//    fun getUid() : String{
//        return uid
//    }
//
//    fun setUid(uid : String){
//        this.uid = uid
//    }
//
//    fun getEmail(): String {
//        return email
//    }




}