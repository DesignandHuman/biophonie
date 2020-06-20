package com.example.biophonie.ui

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.biophonie.R
import com.example.biophonie.api.ApiClient
import com.example.biophonie.api.ApiError
import com.example.biophonie.api.ApiInterface
import com.example.biophonie.api.ErrorUtils
import com.example.biophonie.classes.GeoPoint
import com.example.biophonie.classes.GeoPointResponse
import com.example.biophonie.classes.Sound
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.geometry.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BottomSheetFragment : Fragment() {

    private val TAG: String? = "BottomSheetFragment:"
    private lateinit var soundsIterator: ListIterator<Sound>
    private lateinit var geoPoint: GeoPoint
    private var heightExpanded: Int = 400
    private var imageDisplayed: Boolean = false
    private var state: Int = 0

    private lateinit var parentView: View
    private lateinit var mListener: SoundSheetListener
    private lateinit var location: TextView
    private lateinit var date: TextView
    private lateinit var coords: TextView
    private lateinit var close: ImageView
    private lateinit var waveForm: ImageView
    private lateinit var image: ImageView
    private lateinit var left: TextView
    private lateinit var datePicker: TextView
    private lateinit var right: TextView
    private lateinit var expand: TextView
    private lateinit var pin: ConstraintLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parentView = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        bottomSheetBehavior = BottomSheetBehavior.from(parentView)

        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        location = parentView.findViewById(R.id.location)
        date = parentView.findViewById(R.id.date)
        coords = parentView.findViewById(R.id.coordinates)

        close = parentView.findViewById(R.id.close)
        close.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            else
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        waveForm = parentView.findViewById(R.id.wave_form)
        image = parentView.findViewById(R.id.sound_image)
        image.setOnClickListener {
            Toast.makeText(parentView.context, "Click sur l'image", Toast.LENGTH_SHORT).show() }
        waveForm.setOnClickListener { Toast.makeText(parentView.context, "Lecture du son", Toast.LENGTH_SHORT).show() }

        left = parentView.findViewById(R.id.left)
        left.setOnClickListener {
            soundsIterator.previous()
            val sound: Sound = soundsIterator.previous()
            displaySound(sound)
        }

        datePicker = parentView.findViewById(R.id.date_picker)

        right = parentView.findViewById(R.id.right)
        right.setOnClickListener {
            val sound: Sound = soundsIterator.next()
            displaySound(sound)
        }


        expand = parentView.findViewById(R.id.expand)
        expand.setOnClickListener {
            if (!imageDisplayed){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                image.visibility = View.VISIBLE
                imageDisplayed = true
                waveForm.visibility = View.GONE
                expand.text = "Voir le son"
                parentView.fitsSystemWindows = false
                parentView.fitsSystemWindows = true
            }
            else{
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                image.visibility = View.GONE
                imageDisplayed = false
                waveForm.visibility = View.VISIBLE
                waveForm.layoutParams.height = 0
                expand.text = "Voir l'image"
            }
        }
        progressBar = parentView.findViewById(R.id.progress_bar)
        pin = parentView.findViewById(R.id.pin)
        pin.translationY = 0F

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        parentView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                bottomSheetBehavior.peekHeight = pin.height*2
                val screenHeight: Int = DisplayMetrics().also { activity!!.windowManager.defaultDisplay.getMetrics(it) }.heightPixels
                heightExpanded = 2*parentView.height - parentView.top // A bit mysterious but it works
                bottomSheetBehavior.halfExpandedRatio = (pin.height*2 + waveForm.height).toFloat() / screenHeight.toFloat()

                val obs: ViewTreeObserver = parentView.viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
            }
        })

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            // No other solution was found to pin a view to the bottom of the BottomSheet
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val bottomSheetVisibleHeight = bottomSheet.height - bottomSheet.top
                    pin.translationY = (bottomSheetVisibleHeight - heightExpanded).toFloat()
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when(newState){
                        BottomSheetBehavior.STATE_DRAGGING -> if (state != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                            //(bottomSheetBehavior as? LockableBottomSheetBehavior<*>)?.lock()
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            state = BottomSheetBehavior.STATE_COLLAPSED
                            //(bottomSheetBehavior as? LockableBottomSheetBehavior<*>)?.unlock()
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            state = BottomSheetBehavior.STATE_HALF_EXPANDED
                            close.setImageResource(R.drawable.ic_marker)
                            if (imageDisplayed) {
                                image.visibility = View.GONE
                                waveForm.visibility = View.VISIBLE
                                expand.text = "Voir l'image"
                                imageDisplayed = false
                            }
                            waveForm.apply{requestLayout()}.layoutParams.height = 100*(context!!.resources.displayMetrics.density).toInt()
                            //(bottomSheetBehavior as? LockableBottomSheetBehavior<*>)?.lock()
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            state = BottomSheetBehavior.STATE_EXPANDED
                            close.setImageResource(R.drawable.arrow_down)
                            waveForm.apply{requestLayout()}.layoutParams.height = 0
                            //(bottomSheetBehavior as? LockableBottomSheetBehavior<*>)?.unlock()
                        }
                        else -> {
                            close.setImageResource(R.drawable.ic_marker)
                            //(bottomSheetBehavior as? LockableBottomSheetBehavior<*>)?.unlock()
                        }
                    }
                }
            })
        return parentView
    }

    interface SoundSheetListener{
        fun onButtonClicked(text: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as SoundSheetListener
        } catch (e: ClassCastException){
            throw kotlin.ClassCastException("$context must implement BottomSheetListener")
        }

    }

    /**
     * Display the sound inside the fragment
     *
     * @param id id of the sound to show
     * @param name name of the sound
     * @param coordinates coordinates of the location
     */
    fun show(id: String, name: String, coordinates: LatLng){
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        if (this::geoPoint.isInitialized && geoPoint.id == id)
            return
        changeWidgetsVisibility(false)
        val api: ApiInterface = ApiClient().createService(ApiInterface::class.java)
        val call: Call<GeoPointResponse> = api.getGeoPoint(id)
        call.enqueue(object: Callback<GeoPointResponse>{
            override fun onResponse(call: Call<GeoPointResponse>, response: Response<GeoPointResponse>) {
                if (response.isSuccessful){
                    geoPoint = response.body()!!.toGeoPoint().apply {
                        this.name = name
                        this.coordinates = coordinates
                    }
                    geoPoint.sounds?.let {
                        soundsIterator = it.listIterator()
                        val sound = soundsIterator.next()
                        displaySound(sound)
                        // TODO(build the waveForm corresponding to the urlAudio)
                    }
                } else {
                    val error: ApiError? = ErrorUtils().parseError(response)
                    if (error == null)
                        Toast.makeText(context, "Erreur : " + response.code(), Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Erreur : " + error.statusCode + " " + error.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeoPointResponse>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(context, "Problème réseau. Assurez-vous d'avoir une connection Internet suffisante", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "Erreur de conversion des données", Toast.LENGTH_SHORT).show();
                }
            }
        })
    }

    private fun displaySound(sound: Sound) {
        checkClickability(geoPoint.sounds!!)
        location.text = geoPoint.name
        date.text = SimpleDateFormat("dd/MM/yy", Locale.FRANCE).format(sound.date.time)
        Log.d(TAG, "displaySound: "+SimpleDateFormat("dd/MM/yy", Locale.FRANCE).format(sound.date.time))
        coords.text = geoPoint.coordinatesToString()
        datePicker.text = SimpleDateFormat("MMM yyyy", Locale.FRANCE).format(sound.date.time)
        changeWidgetsVisibility(true)
    }

    private fun checkClickability(sounds: List<Sound>){
        // A bit of a hack due to ListIterators' behavior.
        // The index is between two elements.
        try {
            Log.d(TAG, "checkClickability: previous URL " + sounds[soundsIterator.previousIndex()-1].urlAudio)
            left.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
            left.isClickable = true
        } catch (e: IndexOutOfBoundsException){
            left.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            left.isClickable = false
        }
        try{
            Log.d(TAG, "checkClickability: next URL "+ sounds[soundsIterator.nextIndex()].urlAudio)
            right.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
            right.isClickable = true
        } catch (e: IndexOutOfBoundsException){
            right.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            right.isClickable = false
        }
    }

    private fun changeWidgetsVisibility(makeVisible: Boolean){
        if (makeVisible){
            location.visibility = View.VISIBLE
            date.visibility = View.VISIBLE
            coords.visibility = View.VISIBLE
            close.visibility = View.VISIBLE
            waveForm.visibility = View.VISIBLE
            pin.visibility = View.VISIBLE

            progressBar.visibility = View.GONE
        }
        else{
            location.visibility = View.GONE
            date.visibility = View.GONE
            coords.visibility = View.GONE
            close.visibility = View.GONE
            waveForm.visibility = View.GONE
            pin.visibility = View.GONE

            progressBar.visibility = View.VISIBLE
        }
    }

}
