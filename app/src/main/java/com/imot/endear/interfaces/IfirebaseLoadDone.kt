package com.imot.endear.interfaces

interface IfirebaseLoadDone {

    fun onFirebaseLoadUserNameDone(lstEmail : List<String>)
    fun onFirebaseLoadUserNameFailed(message : String)

}