package com.imot.endear.ViewHolder

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imot.endear.R

class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var tv_user_email : TextView
    var img_accept: ImageView
    var img_decline: ImageView


    init {
        tv_user_email = itemView.findViewById(R.id.tv_user_email) as TextView

        img_accept = itemView.findViewById(R.id.img_accept) as ImageView
        img_decline = itemView.findViewById(R.id.img_decline) as ImageView

        //itemView.setOnClickListener(View.OnClickListener {  this})
    }
}