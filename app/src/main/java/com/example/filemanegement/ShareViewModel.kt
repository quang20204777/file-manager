package com.example.filemanegement

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShareViewModel: ViewModel() {
    val currentPath = MutableLiveData<String>()

}