package com.example.biophonie.viewmodels

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.example.biophonie.R
import com.mapbox.geojson.Point
import fr.haran.soundwave.controller.DefaultRecorderController
import fr.haran.soundwave.ui.RecPlayerView
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "RecViewModel"
const val REQUEST_CAMERA = 0
const val REQUEST_GALLERY = 1
class RecViewModel(application: Application) : AndroidViewModel(application), DefaultRecorderController.InformationRetriever {

    private var captureUri: Uri? = null
    val mTitle = ObservableField<String>()

    private var recorderController: DefaultRecorderController? = null
    private lateinit var currentAmplitudes: List<Int>
    private lateinit var currentPhotoPath: String
    private lateinit var currentSoundPath: String
    private lateinit var coordinates: Point
    var currentId: Int = 0
    val defaultDrawableIds = listOf(R.drawable.france, R.drawable.gabon, R.drawable.japon, R.drawable.russie)
    val defaultLandscapeTitle = listOf("Forêt", "Plaine", "Montagne", "Marais")

    private val _pictureUri = MutableLiveData<Uri>()
    val pictureUri: LiveData<Uri>
        get() = _pictureUri

    private val _landscapeUri = MutableLiveData(getResourceUri(defaultDrawableIds[0]))
    val landscapeUri: LiveData<Uri>
        get() = _landscapeUri

    private val _landscapeThumbnail = MutableLiveData<Uri>()
    val landscapeThumbnail: LiveData<Uri>
        get() = _landscapeThumbnail

    private val _toast = MutableLiveData<ToastModel>()
    val toast: LiveData<ToastModel>
        get() = _toast

    private val _fromDefault = MutableLiveData(true)
    val fromDefault: LiveData<Boolean>
        get() = _fromDefault

    private val _adviceText = MutableLiveData<String>()
    val adviceText: LiveData<String>
        get() = _adviceText

    private val _goToNext = MutableLiveData(false)
    val goToNext: LiveData<Boolean>
        get() = _goToNext

    private val _result = MutableLiveData<Result>()
    val result: LiveData<Result>
        get() = _result

    fun pictureResult(picture: Uri?){
        updateFromDefault(false)
        _landscapeUri.value = picture ?: captureUri
        _landscapeThumbnail.value = _landscapeUri.value
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(getApplication<Application>().applicationContext.externalCacheDir?.absolutePath + File.separator + "images" + File.separator)
        return run {
            if (!storageDir.exists())
                storageDir.mkdir()
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        }
    }

    /**
     * Takes a picture from a camera.
     * It is to be noted that the picture will be stored inside the external
     * cache directory but also at the default location. This only applies on some smartphones.
     * TODO: To overcome this issue, it is advised to implement your own camera module.
     * See https://stackoverflow.com/questions/6390163/deleting-a-gallery-image-after-camera-intent-photo-taken
     *
     */
    fun createCaptureUri() : Uri? {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            _toast.value =
                ToastModel("Impossible d'écrire dans le stockage", Toast.LENGTH_SHORT)
            null
        }
        if (photoFile != null) {
            currentPhotoPath = photoFile.absolutePath
            captureUri = FileProvider.getUriForFile(
                getApplication<Application>().applicationContext,
                "com.example.biophonie.fileprovider",
                photoFile
            )
        }
        return captureUri
    }

    fun onToastDisplayed(){
        _toast.value = null
    }

    fun restorePreviewFromThumbnail() {
        updateFromDefault(false)
        _landscapeUri.value = landscapeThumbnail.value
    }

    fun onClickDefault(i: Int) {
        updateFromDefault(true)
        currentId = i
        _landscapeUri.value = getResourceUri(defaultDrawableIds[i])
    }

    private fun getResourceUri(@DrawableRes id: Int) =
        Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getApplication<Application>().applicationContext.resources.getResourcePackageName(id)
                + '/' + getApplication<Application>().applicationContext.resources.getResourceTypeName(id)
                + '/' + getApplication<Application>().applicationContext.resources.getResourceEntryName(id))

    private fun updateFromDefault(fromDefault: Boolean){
        if (fromDefault != _fromDefault.value)
            _fromDefault.value = fromDefault
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

    data class ToastModel(var message: String, var length: Int)

    override fun setPath(path: String) {
        currentSoundPath = path
    }

    override fun setAmplitudes(amplitudes: List<Int>) {
        currentAmplitudes = amplitudes
    }

    fun setRecorderController(recPlayerView: RecPlayerView): Boolean {
        if (recorderController == null){
            recorderController = getApplication<Application>().applicationContext.externalCacheDir?.absolutePath?.let {
                DefaultRecorderController(recPlayerView,
                    it,
                    this
                ).apply { setRecorderListener(
                    start = { _adviceText.value = "Chhhhhut, écoutez !" },
                    complete = { _adviceText.value = "C'est tout bon !" },
                    validate = { _goToNext.value = true })}
            }
            recorderController?.prepareRecorder()
            return true
        } else {
            recorderController!!.recPlayerView = recPlayerView
            recorderController!!.restoreStateOnNewRecView()
            recorderController!!.prepareRecorder()
            return false
        }
    }

    fun validationAndSubmit(){
        val date = Calendar.getInstance().time
        val dateAsString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(date)
        val title = mTitle.get()
        title?.let {
            if (it.length < 7)
                _toast.value = ToastModel("Le titre doit faire plus de 7 caractères", Toast.LENGTH_SHORT)
            else
                _result.value = Result(it, dateAsString, currentAmplitudes, coordinates, currentSoundPath,_landscapeUri.value!!.path!!)
        }
    }

    fun onNextFragment(){
        _goToNext.value = false
    }

    fun setCoordinates(extras: Bundle?) {
        extras?.let {
            coordinates = Point.fromLngLat(it.getDouble("latitude"), it.getDouble("longitude"))
        }
    }

    fun startRecording() {
        recorderController?.startRecording()
    }

    fun destroyController(){
        recorderController?.destroyController()
        recorderController = null
    }

    data class Result(val title: String,
                      val date: String,
                      val amplitudes: List<Int>,
                      val coordinates: Point,
                      val soundPath: String,
                      val landscapePath: String)
}