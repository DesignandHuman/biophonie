package com.example.biophonie.viewmodels

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "RecViewModel"
const val REQUEST_CAMERA = 0
const val REQUEST_GALLERY = 1
const val REQUEST_PERMISSION_STORAGE = 0
class RecViewModel : ViewModel() {

    lateinit var currentPhotoPath: String
    private val _landscapeUri = MutableLiveData<Uri>()
    val landscapeUri: LiveData<Uri>
        get() = _landscapeUri
    init {
        _landscapeUri.value = Uri.parse("android.resource://com.example.biophonie/drawable/france")
    }

    fun activityResult(requestCode: Int, resultCode: Int, imageIntent: Intent?){
        if (resultCode == Activity.RESULT_OK){
                when(requestCode){
                    REQUEST_CAMERA -> {
                        _landscapeUri.value = Uri.parse(currentPhotoPath)
                    }
                    REQUEST_GALLERY -> {
                        _landscapeUri.value = imageIntent?.data
                    }
                }
                // TODO thumbnail.visibility = View.VISIBLE
            }
        }
}