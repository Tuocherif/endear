package com.imot.endear.ui.find_people

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FindPeopleViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is FindPeople Fragment"
    }
    val text: LiveData<String> = _text
}