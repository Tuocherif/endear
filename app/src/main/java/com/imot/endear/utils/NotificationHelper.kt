package com.imot.endear.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import com.imot.endear.R

class NotificationHelper(base:Context):ContextWrapper(base) {

    companion object {
        private val EDMT_CHANNEL_ID = "com.imot.endear"
        private val EDMT_CHANNEL_NAME = "Endear"
    }

    private var manager:NotificationManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }
    }

    private fun createChannel(defaultUri: Uri?) {
        val edmtChannel = NotificationChannel(EDMT_CHANNEL_ID, EDMT_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT)

        edmtChannel.apply {
            enableLights(true)
            enableVibration(true)
            lockscreenVisibility=Notification.VISIBILITY_PRIVATE
        }

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
            edmtChannel.setSound(defaultUri!!,audioAttributes)

            getManager()!!.createNotificationChannel(edmtChannel)
    }

    fun getManager(): NotificationManager {
        if (manager == null){
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        }
        return manager!!
    }

    fun getRealTimeTrackingNotification(title:String,content: String):Notification.Builder{
        return Notification.Builder(applicationContext, EDMT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)
    }
}