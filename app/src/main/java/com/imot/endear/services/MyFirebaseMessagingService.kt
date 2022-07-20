package com.imot.endear.services

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.browser.trusted.NotificationApiHelperForM
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.imot.endear.R
import com.imot.endear.model.User
import com.imot.endear.utils.Common
import com.imot.endear.utils.NotificationHelper
import java.util.*

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)
            tokens.child(user.uid).setValue(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.data != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                sendNotificationWithChannel(message)
            }else{
                sendNotification(message)
            }
            addRequestToUserInformation(message.data)
        }
    }

    private fun sendNotification(message: RemoteMessage) {
        val data = message.data
        val title = "Demande de proche"
        val content = "Nouvelle demande d'ami de "+data[Common.FROM_EMAIL]

        val builder= NotificationCompat.Builder(this,"")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(),builder.build())
    }

    private fun sendNotificationWithChannel(message: RemoteMessage) {
        val data = message.data
        val title = "Demande de proche"
        val content = "Nouvelle demande d'ami de "+data[Common.FROM_EMAIL]

        val helper: NotificationHelper = NotificationHelper(this)
        val builder:Notification.Builder =  helper.getRealTimeTrackingNotification(title,content)

        helper.getManager()!!.notify(Random().nextInt(),builder.build())



    }

    private fun addRequestToUserInformation(data: Map<String, String>) {
        //Pending Request
        val friend_request = FirebaseDatabase.getInstance()
            .getReference(Common.USER_INFORMATION)
            .child(data[Common.TO_UID]!!)
            .child(Common.FRIEND_REQUEST)

        val user = User(data[Common.FROM_UID]!!,data[Common.FROM_EMAIL]!!, data[Common.FROM_NAME]!!)
        friend_request.child(user.uid!!).setValue(user)
    }
}