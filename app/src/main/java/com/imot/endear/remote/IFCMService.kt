package com.imot.endear.remote

import com.imot.endear.model.MyResponse
import com.imot.endear.model.Request
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers("Content-Type:application/json",
            "authorization:key=AAAA5U9VFjw:APA91bHv9vtGYq78yQP_GfSWppaBVSmOoHo-pZHvj9tCyvpHW-xy-FM-i6Fa5mFGAeSm62fEw1DQC3AeLyHYZ_I49SLpaN4dyGKoPjgFp10N_5fkVSQMRL32jWnrAWSkfEpCYF0d9-Hv")
    @POST("fcm/send")
    fun sendFriendRequestToUser(@Body body: Request): Observable<MyResponse>

    }