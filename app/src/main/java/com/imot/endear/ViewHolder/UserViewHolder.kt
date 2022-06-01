package com.imot.endear.ViewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.imot.endear.R
import com.imot.endear.interfaces.InterfaceRecyclerItemClickListener

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener  {

    lateinit var InterfaceRecyclerItemClickListener : InterfaceRecyclerItemClickListener

    var tv_user_email : TextView


     fun setClick(
        InterfaceRecyclerItemClickListener : InterfaceRecyclerItemClickListener) {
        this.InterfaceRecyclerItemClickListener = InterfaceRecyclerItemClickListener
    }

    init {
        tv_user_email = itemView.findViewById(R.id.tv_user_email) as TextView

        itemView.setOnClickListener(View.OnClickListener {  this})
    }
     fun UserViewHolder ( itemView: View){
        tv_user_email = itemView.findViewById(R.id.tv_user_email)

        itemView.setOnClickListener(View.OnClickListener {  this})
    }

    override fun onClick (view: View?){

        InterfaceRecyclerItemClickListener.onItemClickListener(view!!,absoluteAdapterPosition)
    }





}

