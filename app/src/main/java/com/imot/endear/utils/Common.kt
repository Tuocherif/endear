package com.imot.endear.utils

import com.imot.endear.model.User
import com.imot.endear.remote.IFCMService
import com.imot.endear.remote.RetrofitClient
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


object Common {

    var trackingUser: User? = null
    var loggedUser : User? = null

    val USER_INFORMATION = "UserInformation"
    val USER_UID_SAVE_KEY = "SAVE_KEY"
    val TOKENS = "Tokens"
    val ACCEPT_LIST ="acceptList"
    val FROM_UID ="FROMUid"
    val TO_EMAIL = "FromName"
    val TO_UID = "ToUid"
    val FROM_EMAIL = "ToName"
    val FRIEND_REQUEST = "FriendRequest"
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