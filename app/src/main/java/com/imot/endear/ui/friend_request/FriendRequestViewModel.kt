package com.imot.endear.ui.friend_request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FriendRequestViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is FriendRequest Fragment"
    }
    val text: LiveData<String> = _text
}