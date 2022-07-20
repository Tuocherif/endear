package com.imot.endear.interfaces

interface IfirebaseLoadDone {

    fun onFirebaseLoadUserNameDone(lstName : List<String>)
    fun onFirebaseLoadUserNameFailed(message : String)

}