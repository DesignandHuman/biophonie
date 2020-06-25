package com.example.biophonie.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.biophonie.R
import com.example.biophonie.databinding.BottomSheetLayoutBinding
import com.example.biophonie.network.ApiClient
import com.example.biophonie.network.ApiError
import com.example.biophonie.network.ApiInterface
import com.example.biophonie.network.ErrorUtils
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.domain.GeoPointResponse
import com.example.biophonie.domain.Sound
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
    private var shortAnimationDuration: Int = 0

    private lateinit var binding: BottomSheetLayoutBinding
    private lateinit var mListener: SoundSheetListener
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_layout,
            container,
            false)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.root)

        /* Trying to make fitsSystemWindow work */
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding.close.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            else
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.soundImage.setOnClickListener {
            /*fitsSystemWindow = true on Fragment not working as expected. Use this empty listener to avoid map dragging while dragging fragment*/
        }

        binding.waveForm.setOnClickListener { Toast.makeText(activity, "Lecture du son", Toast.LENGTH_SHORT).show() }

        binding.left.setOnClickListener {
            soundsIterator.previous()
            val sound: Sound = soundsIterator.previous()
            displaySound(sound)
        }

        binding.right.setOnClickListener {
            val sound: Sound = soundsIterator.next()
            displaySound(sound)
        }


        binding.expand.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            if (!imageDisplayed){
                displayImage()
            }
            else{
                displayWaveForm()
            }
        }
        binding.pin.translationY = 0F

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                measure()

                val obs: ViewTreeObserver = binding.root.viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
            }
        })

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            // No other solution was found to pin a view to the bottom of the BottomSheet
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val bottomSheetVisibleHeight = bottomSheet.height - bottomSheet.top
                binding.pin.translationY = (bottomSheetVisibleHeight - heightExpanded).toFloat()
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
                            binding.close.setImageResource(R.drawable.ic_marker)
                            if (imageDisplayed) {
                                displayWaveForm()
                            }
                            binding.waveForm.apply{requestLayout()}.layoutParams.height = dpToPx(150)
                            //(bottomSheetBehavior as? LockableBottomSheetBehavior<*>)?.lock()
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            state = BottomSheetBehavior.STATE_EXPANDED
                            binding.close.setImageResource(R.drawable.arrow_down)
                            binding.waveForm.apply{requestLayout()}.layoutParams.height = 0
                            //(bottomSheetBehavior as? LockableBottomSheetBehavior<*>)?.unlock()
                        }
                        else -> {
                            binding.close.setImageResource(R.drawable.ic_marker)
                            //(bottomSheetBehavior as? LockableBottomSheetBehavior<*>)?.unlock()
                        }
                    }
                }
            })

        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        return binding.root
    }

    private fun crossfade(fadeIn: View, fadeOut: View) {
        fadeIn.apply {
            // Set the content view to 0% opacity but visible
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
        // Animate the loading view to 0% opacity.
        // After the animation ends, set its visibility to GONE
        fadeOut.animate()
            .alpha(0f)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fadeOut.visibility = View.GONE
                    /*waveForm.apply{ requestLayout() }
                    image.apply { requestLayout() }*/
                }
            })
    }

    private fun dpToPx(dp: Int): Int {
        return dp*(context!!.resources.displayMetrics.density).toInt()
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
        binding.apply {
            location.text = geoPoint.name
            date.text = SimpleDateFormat("dd/MM/yy", Locale.FRANCE).format(sound.date.time)
            coordinates.text = geoPoint.coordinatesToString()
            datePicker.text = SimpleDateFormat("MMM yyyy", Locale.FRANCE).format(sound.date.time)
        }
        changeWidgetsVisibility(true)
    }

    private fun displayImage(){
        crossfade(binding.soundImage, binding.waveForm)
        binding.expand.text = "Voir le son"
        imageDisplayed = true
    }

    private fun displayWaveForm(){
        crossfade(binding.waveForm, binding.soundImage)
        binding.waveForm.layoutParams.height = 0
        binding.expand.text = "Voir l'image"
        imageDisplayed = false
    }

    private fun measure() {
        binding.apply{
            bottomSheetBehavior.peekHeight = pin.height*2
            val screenHeight: Int = DisplayMetrics().also { activity!!.windowManager.defaultDisplay.getMetrics(it) }.heightPixels
            heightExpanded = 2*container.height - container.top // A bit mysterious but it works
            bottomSheetBehavior.halfExpandedRatio = (pin.height*2 + waveForm.height).toFloat() / screenHeight.toFloat()
        }
    }

    private fun checkClickability(sounds: List<Sound>){
        // A bit of a hack due to ListIterators' behavior.
        // The index is between two elements.
        binding.apply {
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
    }

    private fun changeWidgetsVisibility(makeVisible: Boolean){
        binding.apply {
            if (makeVisible){
                location.visibility = View.VISIBLE
                date.visibility = View.VISIBLE
                coordinates.visibility = View.VISIBLE
                close.visibility = View.VISIBLE
                waveForm.visibility = View.VISIBLE
                pin.visibility = View.VISIBLE

                progressBar.visibility = View.GONE
            }
            else{
                location.visibility = View.GONE
                date.visibility = View.GONE
                coordinates.visibility = View.GONE
                close.visibility = View.GONE
                waveForm.visibility = View.GONE
                pin.visibility = View.GONE

                progressBar.visibility = View.VISIBLE
            }
        }
    }

}
