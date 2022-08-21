package com.imot.endear.viewHolder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imot.endear.R
import com.imot.endear.interfaces.InterfaceRecyclerItemClickListener

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener  {

    lateinit var InterfaceRecyclerItemClickListener : InterfaceRecyclerItemClickListener

    var tv_user_name : TextView


     fun setClick(
        InterfaceRecyclerItemClickListener : InterfaceRecyclerItemClickListener) {
        this.InterfaceRecyclerItemClickListener = InterfaceRecyclerItemClickListener
    }

    init {
        tv_user_name = itemView.findViewById(R.id.tv_user_name) as TextView

        itemView.setOnClickListener({this})
    }
     fun UserViewHolder ( itemView: View){
         tv_user_name = itemView.findViewById(R.id.tv_user_name)

        itemView.setOnClickListener(View.OnClickListener {  this})
    }

    override fun onClick (view: View?){

        InterfaceRecyclerItemClickListener.onItemClickListener(view!!,absoluteAdapterPosition)
    }





}

