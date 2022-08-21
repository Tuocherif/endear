package com.imot.endear.utils


import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.imot.endear.model.User
import com.imot.endear.remote.IFCMService
import com.imot.endear.remote.RetrofitClient
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


object Common {

    val fireBaseUser: FirebaseUser? =
        FirebaseAuth.getInstance().currentUser
    var loggedUser: User? = null
//        User(fireBaseUser!!.uid,
//            fireBaseUser.email!!,
//            fireBaseUser.displayName!!)
    var trackingUser: User? = null
//    var image : Uri? = null

    val USER_INFORMATION : String = "USER_INFORMATION"
    val USER_UID_SAVE_KEY = "SAVE_KEY"
    val TOKENS = "Tokens"
    val ACCEPT_LIST ="acceptList"
    val FROM_UID ="FROMUid"
    val TO_EMAIL = "FromEmail"
    val TO_IMAGE = "FromImage"
    val TO_NAME = "FromName"
    val TO_UID = "ToUid"
    val FROM_EMAIL = "ToEmail"
    val FROM_NAME = "ToName"
    val FROM_IMAGE  = "fireBaseUser?.photoUrl"
    val FRIEND_REQUEST = "FriendRequest"
    val FRIEND_ALERT = "FriendAlert"
    val PUBLIC_LOCTION: String = "PublicLocation"




    val fcmService:IFCMService
     get() = RetrofitClient.getClient("https://fcm.googleapis.com/")
         .create(IFCMService::class.java)

    fun convertTimeStampToDate(time: Long): Date {

        return Date(Timestamp(time).time)
    }

    fun getDateFormatted(date: Date): String {

        return SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString()

    }
}