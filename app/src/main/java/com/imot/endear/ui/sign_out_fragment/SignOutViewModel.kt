package com.imot.endear.ui.sign_out_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignOutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is SignOut Fragment"
    }
    val text: LiveData<String> = _text
}