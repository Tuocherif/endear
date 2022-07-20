package com.imot.endear.model


class User(uid: String, email: String, name: String) {
    var uid : String? = uid
    var email : String? = email
    var name : String? = name
    var acceptList: HashMap<String,User> // List user friend


    init {
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