package fr.labomg.biophonie.viewmodels

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
import fr.labomg.biophonie.templates
import com.mapbox.geojson.Point
import fr.haran.soundwave.controller.AacRecorderController
import fr.haran.soundwave.ui.RecPlayerView
import fr.labomg.biophonie.BuildConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RecViewModel(application: Application) : AndroidViewModel(application), AacRecorderController.InformationRetriever {

    private var captureUri: Uri? = null
    val mTitle = ObservableField<String>()

    private var recorderController: AacRecorderController? = null
    private lateinit var currentAmplitudes: List<Int>
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

    private val _recordComplete = MutableLiveData(false)
    val recordComplete: LiveData<Boolean>
        get() = _recordComplete

    private val _result = MutableLiveData<Result>()
    val result: LiveData<Result>
        get() = _result

    fun pictureResult(picture: Uri? = null){
        updateFromDefault(false)
        _landscapeUri.value = picture ?: captureUri
        _landscapeThumbnail.value = _landscapeUri.value
    }

    @Throws(IOException::class)
    private fun createImageFile(extension: String): File {
        val timeStamp = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_DATE_TIME )
        val storageDir = File(getApplication<Application>().applicationContext.externalCacheDir?.absolutePath + File.separator + "images" + File.separator)
        return run {
            if (!storageDir.exists())
                storageDir.mkdir()
            File.createTempFile("${timeStamp}_", extension, storageDir)
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
    private fun createLocalImageUri(extension: String) : Uri? {
        val photoFile: File? = try {
            createImageFile(extension)
        } catch (ex: IOException) {
            _toast.value =
                ToastModel("Impossible d'écrire dans le stockage", Toast.LENGTH_SHORT)
            null
        }
        return if (photoFile != null)
            FileProvider.getUriForFile(
                getApplication<Application>().applicationContext,
                "fr.labomg.biophonie.fileprovider",
                photoFile
            )
        else null
    }

    fun getCaptureUri(): Uri? {
        captureUri = createLocalImageUri(".jpg")
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

    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
                    stop = {
                        if (BuildConfig.DEBUG)
                            this.complete()
                        else
                            _adviceText.value = "L’enregistrement doit durer 2 minutes."
                           },
                    complete = { _adviceText.value = "C’est tout bon !" },
                    validate = { _recordComplete.value = true },
                )}
            }
            recorderController?.prepareRecorder()
            return false
        } else {
            if (_recordComplete.value == true)
                _adviceText.value = "L’enregistrement doit durer 2 minutes."
            else
                _adviceText.value = "C’est tout bon !"
            recorderController!!.recPlayerView = recPlayerView.apply {
                toggleValidate(_recordComplete.value != true)
            }
            recorderController!!.restoreStateOnNewRecView()
            recorderController!!.prepareRecorder()
            return true
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
                if (_fromDefault.value == false) {
                    landscapePath = getApplication<Application>().applicationContext.externalCacheDir?.absolutePath + File.separator + "images" + File.separator + _landscapeUri.value!!.path!!.substringAfterLast(File.separator)
                }
                else templatePath = templates.keys.elementAt(currentId)
                _result.value =
                    Result(it, instant, currentAmplitudes, coordinates, currentSoundPath, landscapePath, templatePath)
            }
        }
    }

    fun onValidateRecording(){
        _recordComplete.value = false
    }

    fun setCoordinates(extras: Bundle?) {
        extras?.let {
            coordinates = Point.fromLngLat(it.getDouble("longitude"), it.getDouble("latitude"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        recorderController?.destroyController()
        recorderController = null
    }

    fun stopRecording() {
        recorderController?.stopRecording(false,false)
    }

    fun startRecording() {
        recorderController?.startRecording()
    }

    data class Result(val title: String,
                      val date: String,
                      val amplitudes: List<Int>,
                      val coordinates: Point,
                      val soundPath: String,
                      val landscapePath: String,
                      val templatePath: String)
}