package com.imot.endear.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imot.endear.R
import com.imot.endear.dataclasses.UserData
import com.imot.endear.model.User
import java.lang.reflect.Array

class FindPeopleAdapter(private val userList: ArrayList<User>):
    RecyclerView.Adapter<FindPeopleAdapter.FindPeopleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindPeopleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_user, parent, false)
        return FindPeopleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FindPeopleViewHolder, position: Int) {

        val currentItem = userList[position]

            holder.tv_user_name_u.text = currentItem.name
            holder.tv_user_name_u.setTypeface(holder.tv_user_name_u.typeface, Typeface.ITALIC)

            val bitmap = Array.getInt(currentItem.image!!, 0)
            holder.tv_user_image_u.setImageResource(bitmap)

    }

    override fun getItemCount(): Int {
        return userList.size
    }


    class FindPeopleViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){

        val tv_user_name_u : TextView = itemView.findViewById(R.id.tv_user_name_u)
        val tv_user_image_u : ImageView = itemView.findViewById(R.id.tv_user_image_u)

    }
}