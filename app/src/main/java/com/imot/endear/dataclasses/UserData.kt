package com.imot.endear.dataclasses

import android.location.Location
import android.net.Uri
import android.widget.ImageView
import com.imot.endear.model.User
import retrofit2.http.Url

data class UserData(val name: String?=null, val image: String?=null, val uid: String?=null, val email: String?=null,
                    val location : Location?)