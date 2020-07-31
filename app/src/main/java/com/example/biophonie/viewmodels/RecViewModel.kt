package com.example.biophonie.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "RecViewModel"
const val REQUEST_CAMERA = 0
const val REQUEST_GALLERY = 1
const val REQUEST_PERMISSION_STORAGE = 0
class RecViewModel(application: Application) : AndroidViewModel(application) {

    //Necessary to retrieve files
    private val context = getApplication<Application>().applicationContext
    private lateinit var currentPhotoPath: String

    val landscapeUri = MutableLiveData<Uri>()

    private val _activityIntent = MutableLiveData<ActivityIntent>()
    val activityIntent: LiveData<ActivityIntent>
        get() = _activityIntent

    init {
        landscapeUri.value = Uri.parse("android.resource://com.example.biophonie/drawable/france")
    }

    fun activityResult(requestCode: Int, resultCode: Int, imageIntent: Intent?){
        if (resultCode == Activity.RESULT_OK){
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
        }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
        val storageDir: File? = File(context.externalCacheDir?.absolutePath + File.separator + "images" + File.separator)
        return if (storageDir == null){
            /* TODO Toast.makeText(
                requireContext(),
                "Veuillez accorder la permission d'accès au stockage du téléphone",
                Toast.LENGTH_LONG
            ).show()*/
            null
        } else {
            if (!storageDir.exists())
                storageDir.mkdir()
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
                .apply { currentPhotoPath = absolutePath }
        }
    }

    fun dispatchTakePictureIntent(choice: Int){
        when(choice){
            REQUEST_CAMERA -> {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).let { takePictureIntent ->
                    takePictureIntent.resolveActivity(context.packageManager)?.let {
                        val photoFile: File? = try {
                            createImageFile()
                        } catch (ex: IOException) {
                            /*TODO Toast.makeText(requireContext(), "Impossible d'écrire dans le stockage",
                                Toast.LENGTH_SHORT).show()*/
                            null
                        }
                        photoFile?.let {
                            val photoURI: Uri = FileProvider.getUriForFile(
                                context,
                                "com.example.biophonie.fileprovider",
                                photoFile
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            //TODO startActivityForResult(takePictureIntent, REQUEST_CAMERA)
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
}