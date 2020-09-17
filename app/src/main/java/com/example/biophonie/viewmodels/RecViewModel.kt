package com.example.biophonie.viewmodels

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.example.biophonie.R
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.haran.soundwave.controller.DefaultRecorderController
import fr.haran.soundwave.ui.RecPlayerView
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "RecViewModel"
const val REQUEST_CAMERA = 0
const val REQUEST_GALLERY = 1
class RecViewModel(application: Application) : AndroidViewModel(application), DefaultRecorderController.InformationRetriever {

    val mTitle = ObservableField<String>()

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

    private var recorderController: DefaultRecorderController? = null
    //Necessary to retrieve files
    private val context = getApplication<Application>().applicationContext
    private lateinit var currentAmplitudes: List<Int>
    private lateinit var currentPhotoPath: String
    private lateinit var currentSoundPath: String
    private lateinit var coordinates: LatLng
    var currentId: Int = 0
    val defaultDrawableIds = listOf(R.drawable.france, R.drawable.gabon, R.drawable.japon, R.drawable.russie)
    val defaultLandscapeTitle = listOf("Forêt", "Plaine", "Montagne", "Marais")

    private val _landscapeUri = MutableLiveData<Uri>(getResourceUri(defaultDrawableIds[0]))
    val landscapeUri: LiveData<Uri>
        get() = _landscapeUri

    private val _landscapeThumbnail = MutableLiveData<Uri>()
    val landscapeThumbnail: LiveData<Uri>
        get() = _landscapeThumbnail

    private val _activityIntent = MutableLiveData<ActivityIntent>()
    val activityIntent: LiveData<ActivityIntent>
        get() = _activityIntent

    private val _toast = MutableLiveData<ToastModel>()
    val toast: LiveData<ToastModel>
        get() = _toast

    private val _fromDefault = MutableLiveData<Boolean>(true)
    val fromDefault: LiveData<Boolean>
        get() = _fromDefault

    private val _adviceText = MutableLiveData<String>()
    val adviceText: LiveData<String>
        get() = _adviceText

    private val _goToNext = MutableLiveData<Boolean>(false)
    val goToNext: LiveData<Boolean>
        get() = _goToNext

    private val _result = MutableLiveData<Result>()
    val result: LiveData<Result>
        get() = _result

    fun activityResult(requestCode: Int, imageIntent: Intent?){
        updateFromDefault(false)
        when(requestCode){
            REQUEST_CAMERA -> {
                _landscapeUri.value = Uri.fromFile(File(currentPhotoPath))
                _landscapeThumbnail.value = _landscapeUri.value
            }
            REQUEST_GALLERY -> {
                _landscapeUri.value = imageIntent?.data
                _landscapeThumbnail.value = _landscapeUri.value
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = File(context.externalCacheDir?.absolutePath + File.separator + "images" + File.separator)
        return if (storageDir == null){
            _toast.value = ToastModel("Veuillez accorder la permission d'accès au stockage du téléphone", Toast.LENGTH_LONG)
            null
        } else {
            if (!storageDir.exists())
                storageDir.mkdir()
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        }
    }

    fun dispatchTakePictureIntent(choice: Int){
        Log.d(TAG, "dispatchTakePictureIntent: $choice")
        when(choice){
            REQUEST_CAMERA -> {
                captureImage()?.also { _activityIntent.value = ActivityIntent(it, REQUEST_CAMERA) }
            }
            REQUEST_GALLERY -> Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).also {
                _activityIntent.value = ActivityIntent(it, REQUEST_GALLERY)
                }
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
    private fun captureImage() : Intent? =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).let { takePictureIntent ->
            takePictureIntent.resolveActivity(context.packageManager)?.let {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    _toast.value =
                        ToastModel("Impossible d'écrire dans le stockage", Toast.LENGTH_SHORT)
                    null
                }
                photoFile?.let {
                    currentPhotoPath = it.absolutePath
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context,
                        "com.example.biophonie.fileprovider",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }
            }
        }


    fun onRequestActivityStarted(){
        _activityIntent.value = null
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
        Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.resources.getResourcePackageName(id) + '/' + context.resources.getResourceTypeName(id) + '/' + context.resources.getResourceEntryName(id))

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

    data class ActivityIntent(var intent: Intent, var requestCode: Int)
    data class ToastModel(var message: String, var length: Int)

    override fun setPath(path: String) {
        currentSoundPath = path
    }

    override fun setAmplitudes(amplitudes: List<Int>) {
        currentAmplitudes = amplitudes
    }

    fun setRecorderController(recPlayerView: RecPlayerView): Boolean {
        if (recorderController == null){
            recorderController = context.externalCacheDir?.absolutePath?.let {
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

    fun onNextFragment(){
        _goToNext.value = false
    }

    fun destroyController(){
        recorderController?.destroyController()
        recorderController = null
    }

    fun setCoordinates(extras: Bundle?) {
        extras?.let {
            coordinates = LatLng(it.getDouble("latitude"), it.getDouble("longitude"))
        }
    }

    fun startRecording() {
        recorderController?.startRecording()
    }

    data class Result(val title: String,
                      val date: String,
                      val amplitudes: List<Int>,
                      val coordinates: LatLng,
                      val soundPath: String,
                      val landscapePath: String)
}