package com.imot.endear.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imot.endear.R
import com.imot.endear.interfaces.InterfaceRecyclerItemClickListener

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener  {

    lateinit var InterfaceRecyclerItemClickListener : InterfaceRecyclerItemClickListener

    var tv_user_name_u : TextView
    var tv_user_image_u: ImageView


     fun setClick(
        InterfaceRecyclerItemClickListener : InterfaceRecyclerItemClickListener) {
        this.InterfaceRecyclerItemClickListener = InterfaceRecyclerItemClickListener
    }

    init {
        tv_user_name_u = itemView.findViewById(R.id.tv_user_name_u) as TextView
        tv_user_image_u = itemView.findViewById(R.id.tv_user_image_u) as ImageView

        itemView.setOnClickListener(this)
    }
    fun UserViewHolder ( itemView: View){
        tv_user_name_u = itemView.findViewById(R.id.tv_user_name_u)
        tv_user_image_u = itemView.findViewById(R.id.tv_user_image_u)

        itemView.setOnClickListener(this)
    }

    override fun onClick (view: View?){
        InterfaceRecyclerItemClickListener.onItemClickListener(view!!,absoluteAdapterPosition)
    }

}

