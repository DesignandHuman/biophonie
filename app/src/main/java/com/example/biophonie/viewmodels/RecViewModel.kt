package com.example.biophonie.viewmodels

import android.app.Application
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "RecViewModel"
const val REQUEST_CAMERA = 0
const val REQUEST_GALLERY = 1
class RecViewModel(application: Application) : AndroidViewModel(application) {

    //Necessary to retrieve files
    private val context = getApplication<Application>().applicationContext
    private lateinit var currentPhotoPath: String

    val landscapeUri = MutableLiveData<Uri>(
        Uri.parse("android.resource://com.example.biophonie/drawable/france"))

    private val _activityIntent = MutableLiveData<ActivityIntent?>()
    val activityIntent: LiveData<ActivityIntent?>
        get() = _activityIntent

    private val _toast = MutableLiveData<ToastModel?>()
    val toast: LiveData<ToastModel?>
        get() = _toast


    fun activityResult(requestCode: Int, imageIntent: Intent?){
        when(requestCode){
            REQUEST_CAMERA -> {
                landscapeUri.value = Uri.parse(currentPhotoPath)
            }
            REQUEST_GALLERY -> {
                landscapeUri.value = imageIntent?.data
            }
        }
        // TODO thumbnail.visibility = View.VISIBLE
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
        val storageDir: File? = File(context.externalCacheDir?.absolutePath + File.separator + "images" + File.separator)
        return if (storageDir == null){
            _toast.value = ToastModel("Veuillez accorder la permission d'accès au stockage du téléphone", Toast.LENGTH_LONG)
            null
        } else {
            if (!storageDir.exists())
                storageDir.mkdir()
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
                .apply { currentPhotoPath = absolutePath }
        }
    }

    fun dispatchTakePictureIntent(choice: Int){
        Log.d(TAG, "dispatchTakePictureIntent: $choice")
        when(choice){
            REQUEST_CAMERA -> {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).let { takePictureIntent ->
                    takePictureIntent.resolveActivity(context.packageManager)?.let {
                        val photoFile: File? = try {
                            createImageFile()
                        } catch (ex: IOException) {
                            _toast.value = ToastModel("Impossible d'écrire dans le stockage", Toast.LENGTH_SHORT)
                            null
                        }
                        photoFile?.let {
                            val photoURI: Uri = FileProvider.getUriForFile(
                                context,
                                "com.example.biophonie.fileprovider",
                                photoFile
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        }
                    }
                } ?.also { _activityIntent.value = ActivityIntent(it, REQUEST_CAMERA) }
            }
            REQUEST_GALLERY -> Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).also {
                _activityIntent.value = ActivityIntent(it, REQUEST_GALLERY)
                }
        }
    }

    fun onRequestActivityStarted(){
        _activityIntent.value = null
    }

    fun onToastDisplayed(){
        _toast.value = null
    }

    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }
    }

    data class ActivityIntent(var intent: Intent, var requestCode: Int)
    data class ToastModel(var message: String, var length: Int)
}