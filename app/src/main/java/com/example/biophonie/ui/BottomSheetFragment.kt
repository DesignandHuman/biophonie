package com.example.biophonie.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.biophonie.R
import com.example.biophonie.databinding.BottomSheetLayoutBinding
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.viewmodels.BottomSheetViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.geometry.LatLng

class BottomSheetFragment : Fragment() {

    private val TAG: String? = "BottomSheetFragment:"
    private var heightExpanded: Int = 400
    private var imageDisplayed: Boolean = false
    private var state: Int = 0
    private var shortAnimationDuration: Int = 0

    private val viewModel: BottomSheetViewModel by lazy {
        ViewModelProvider(this, BottomSheetViewModel.ViewModelFactory()).get(BottomSheetViewModel::class.java)
    }
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
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.sound = viewModel.sound
        binding.geoPoint = viewModel.geoPoint.value
        bottomSheetBehavior = BottomSheetBehavior.from(binding.root)

        /* Trying to make fitsSystemWindow work */
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding.close.setOnClickListener {onClose()}

        binding.soundImage.setOnClickListener {
            //"fitsSystemWindow = true" on Fragment not working as expected. Use this empty listener to avoid map dragging while dragging fragment
        }

        binding.waveForm.setOnClickListener { Toast.makeText(activity, "Lecture du son", Toast.LENGTH_SHORT).show() }

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
        setUpObservers()
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        return binding.root
    }

    private fun onNetworkError() {
        if(!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }

    fun clickOnGeoPoint(id: String, name: String, coordinates: LatLng){
        viewModel.getGeoPoint(id, name, coordinates)
    }

    private fun setUpObservers() {
        viewModel.leftClickable.observe(viewLifecycleOwner, Observer<Boolean> {setArrowClickable(binding.left,it)})
        viewModel.rightClickable.observe(viewLifecycleOwner, Observer<Boolean> {setArrowClickable(binding.right,it)})
        viewModel.visibility.observe(viewLifecycleOwner, Observer<Boolean>{changeWidgetsVisibility(it)})
        viewModel.bottomSheetState.observe(viewLifecycleOwner, Observer<Int>{bottomSheetBehavior.state = it})
        viewModel.eventNetworkError.observe(viewLifecycleOwner, Observer<Boolean> { isNetworkError ->
            if (isNetworkError) onNetworkError()
        })
        viewModel.geoPoint.observe(viewLifecycleOwner, Observer<GeoPoint>{
            binding.invalidateAll()
        })
    }

    private fun setArrowClickable(view: TextView, clickable: Boolean) {
        if (clickable) {
            view.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.colorPrimaryDark))
            view.isClickable = true
        }
        else{
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            view.isClickable = false
        }
    }

    private fun onClose() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        else
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun crossFade(fadeIn: View, fadeOut: View) {
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
        return dp*(requireContext().resources.displayMetrics.density).toInt()
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

    private fun displayImage(){
        crossFade(binding.soundImage, binding.waveForm)
        binding.expand.text = "Voir le son"
        imageDisplayed = true
    }

    private fun displayWaveForm(){
        crossFade(binding.waveForm, binding.soundImage)
        binding.waveForm.layoutParams.height = 0
        binding.expand.text = "Voir l'image"
        imageDisplayed = false
    }

    private fun measure() {
        binding.apply{
            bottomSheetBehavior.peekHeight = pin.height*2
            val screenHeight: Int = DisplayMetrics().also { requireActivity().windowManager.defaultDisplay.getMetrics(it) }.heightPixels
            heightExpanded = 2*container.height - container.top // A bit mysterious but it works
            bottomSheetBehavior.halfExpandedRatio = (pin.height*2 + waveForm.height).toFloat() / screenHeight.toFloat()
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
