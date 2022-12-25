package com.example.biophonie.viewmodels

import android.R.attr
import android.R.attr.bitmap
import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.example.biophonie.templates
import com.mapbox.geojson.Point
import fr.haran.soundwave.controller.AacRecorderController
import fr.haran.soundwave.ui.RecPlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


private const val TAG = "RecViewModel"
class RecViewModel(application: Application) : AndroidViewModel(application), AacRecorderController.InformationRetriever {

    private var captureUri: Uri? = null
    val mTitle = ObservableField<String>()

    private var recorderController: AacRecorderController? = null
    private lateinit var currentAmplitudes: List<Int>
    private lateinit var currentPhotoPath: String
    private lateinit var currentSoundPath: String
    private lateinit var coordinates: Point
    var currentId: Int = 0

    private val _landscapeUri = MutableLiveData(getResourceUri(templates.values.first()))
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
        val timeStamp = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_DATE_TIME )
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
        _landscapeUri.value = getResourceUri(templates.values.elementAt(i))
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

    private fun convertToWebp(path: String): String {
        val compressedPath = path.replaceAfterLast(".", "webp")
        viewModelScope.launch { compressPicture(path, compressedPath) }
        return compressedPath
    }

    private suspend fun compressPicture(path: String, newPath: String) {
        withContext(Dispatchers.IO) {
            val picture = BitmapFactory.decodeFile(path)
            val out = FileOutputStream(newPath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                picture.compress(Bitmap.CompressFormat.WEBP_LOSSY, 75,out)
            else
                picture.compress(Bitmap.CompressFormat.WEBP,75,out)
            out.close()
            File(path).deleteOnExit()
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
                AacRecorderController(recPlayerView,
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
        val instant = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT )
        val title = mTitle.get()
        title?.let {
            if (it.length < 7)
                _toast.value = ToastModel("Le titre doit faire plus de 7 caractères", Toast.LENGTH_SHORT)
            else {
                var landscapePath = ""
                var templatePath = ""

                if (_fromDefault.value == false) landscapePath = convertToWebp(currentPhotoPath)
                else templatePath = templates.keys.elementAt(currentId)
                _result.value =
                    Result(it, instant, currentAmplitudes, coordinates, currentSoundPath, landscapePath, templatePath)
            }
        }
    }

    fun onNextFragment(){
        _goToNext.value = false
    }

    fun setCoordinates(extras: Bundle?) {
        extras?.let {
            coordinates = Point.fromLngLat(it.getDouble("longitude"), it.getDouble("latitude"))
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
                      val landscapePath: String,
                      val templatePath: String)
}