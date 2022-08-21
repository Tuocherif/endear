package com.imot.endear.ui.endear

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class EndearViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is Endear Fragment"
    }
    val text: LiveData<String> = _text
}