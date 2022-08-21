package com.imot.endear.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imot.endear.R

class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var tv_user_name : TextView
    var tv_user_alert : TextView
    var alert : LinearLayout
    var tv_user_image : ImageView
    var tv_user_image_alert : ImageView
    var img_accept: ImageView
    var img_decline: ImageView


    init {
        tv_user_name = itemView.findViewById(R.id.tv_user_name) as TextView
        tv_user_alert = itemView.findViewById(R.id.tv_user_alert) as TextView
        alert = itemView.findViewById(R.id.alert) as LinearLayout
        tv_user_image = itemView.findViewById(R.id.tv_user_image) as ImageView
        tv_user_image_alert = itemView.findViewById(R.id.tv_user_image_alert) as ImageView

        img_accept = itemView.findViewById(R.id.img_accept) as ImageView
        img_decline = itemView.findViewById(R.id.img_decline) as ImageView

        //itemView.setOnClickListener(View.OnClickListener {  this})
    }
}