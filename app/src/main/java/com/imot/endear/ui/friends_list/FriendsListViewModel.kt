package com.imot.endear.ui.friends_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FriendsListViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is FriendsList Fragment"
    }
    val text: LiveData<String> = _text
}