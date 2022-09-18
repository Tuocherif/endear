package com.imot.endear.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.imot.endear.utils.Common
import io.paperdb.Paper

class MyLocationReceiver: BroadcastReceiver() {
    var publicLocation:DatabaseReference = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCTION)
    lateinit var uid:String

    companion object{
        val ACTION = "com.imot.endear.UPDATE_LOCATION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Paper.init(context!!)

        uid = Paper.book().read<String>(Common.USER_UID_SAVE_KEY).toString()

        if (intent != null){
            val action = intent.action
            if (action == ACTION){
                val result = LocationResult.extractResult(intent)
                if (null != result){
                    val location = result.lastLocation
                    if (Common.loggedUser != null){
                        //App is running
                        publicLocation.child(Common.loggedUser.uid!!).setValue(location)
                    }else{
                        //App in killed mode
                        publicLocation.child(uid).setValue(location)

                    }
                }
            }
        }
    }
}