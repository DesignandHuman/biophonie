package com.example.biophonie.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.biophonie.R
import com.example.biophonie.databinding.BottomSheetLayoutBinding
import com.example.biophonie.viewmodels.BottomSheetViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.geometry.LatLng
import fr.haran.soundwave.controller.DefaultPlayerController
import java.io.IOException

class BottomSheetFragment : Fragment() {

    private var heightExpanded: Int = 400
    private var imageDisplayed: Boolean = false
    private var shortAnimationDuration: Int = 0
    private var amplitudes = arrayOf(-0.009, 0.005, 0.004, 0.0, 0.001, -0.004, 0.002, -0.004, -0.002, -0.002, 0.006, 0.008, 0.019, -0.039, 0.121, 0.126, 0.046, -0.026, -0.027, -0.095, -0.0, -0.017, 0.006, -0.067, 0.026, -0.08, 0.009, 0.018, -0.018, -0.005, -0.002, -0.003, 0.005, 0.011, 0.002, -0.004, 0.003, 0.01, 0.011, -0.011, -0.003, 0.002, -0.001, -0.008, 0.001, 0.001, -0.011, -0.009, 0.004, 0.012, -0.05, 0.235, 0.152, -0.073, -0.152, -0.006, 0.091, 0.001, -0.068, -0.063, -0.021, -0.018, 0.05, 0.001, -0.018, 0.048, 0.001, -0.029, -0.004, 0.009, -0.014, 0.006, -0.001, 0.001, 0.001, -0.798, -0.37, 0.749, -0.496, -0.14, -0.036, -0.014, 0.014, 0.038, -0.014, -0.026, -0.004, -0.01, 0.003, -0.011, -0.001, -0.01, -0.015, 0.001, -0.002, -0.002, 0.015, -0.019, -0.019, 0.028, 0.014, 0.003, -0.054, -0.047, -0.013, 0.005, 0.007, -0.005, 0.006, -0.03, 0.147, 0.056, -0.051, 0.067, 0.048, 0.347, 0.039, -0.319, 0.386, 0.339, 0.036, 0.152, -0.016, -0.013, 0.016, -0.011, -0.0, -0.002, 0.006, 0.0, -0.002, -0.011, -0.008, -0.009, -0.001, -0.004, 0.016, 0.013, 0.004, 0.004, 0.004, 0.002, -0.002, 0.004, 0.001, 0.008, 0.001, -0.007, -0.001, -0.001, -0.009, -0.003, 0.011, 0.028, 0.074, 0.042, -0.006, 0.023, 0.051, -0.003, 0.001, 0.167, 0.235, -0.191, -0.016, -0.043, 0.434, -0.266, 0.045, -0.128, -0.08, 0.068, -0.075, 0.042, 0.076, 0.068, 0.012, -0.009, 0.006, 0.025, 0.021, 0.024, 0.023, -0.005, -0.017, 0.003, -0.01, 0.001, -0.022, -0.006, 0.032, 0.009, 0.016, -0.002, -0.017, 0.001, 0.006, -0.002, 0.587, -0.614, -0.108, -0.274, -0.06, -0.01, -0.005, -0.01, 0.01, -0.001, -0.003, -0.007, 0.008, 0.009, -0.005, 0.004, 0.014, -0.003, 0.004, -0.007, 0.007, 0.01, 0.002, -0.007, 0.019, -0.001, -0.008, 0.002, -0.021, 0.006, -0.051, 0.033, -0.016, 0.025, -0.029, -0.024, 0.021, -0.014, -0.002, -0.001, -0.016, 0.022, 0.011, -0.002, 0.011, -0.001, -0.01, 0.008, -0.001, 0.005, 0.001, -0.009, 0.006, -0.007, 0.037, -0.133, -0.021, 0.024, -0.009, -0.035, -0.049, 0.041, 0.012, -0.016, 0.041, -0.014, 0.038, -0.055, -0.012, 0.028, 0.022, -0.011, 0.0, 0.004, 0.003, -0.006, 0.006, -0.002, 0.001, -0.007, -0.004, -0.008, 0.005, -0.004, -0.004, -0.011, 0.009, 0.0, 0.004, -0.01, 0.007, -0.021, 0.115, -0.198, 0.044, 0.028, -0.006, -0.068, -0.036, -0.005, 0.014, -0.015, 0.016, 0.006, -0.043, -0.009, 0.0, -0.017, -0.008, -0.011, -0.005, -0.002, -0.011, 0.011, -0.012, 0.008, 0.0, 0.674, 0.06, -0.032, -0.353, 0.186, -0.021, 0.08, -0.028, -0.021, 0.081, -0.027, -0.006, -0.009, 0.004, -0.002, 0.003, 0.008, 0.007, 0.006, -0.01, -0.016, 0.017, 0.001, 0.004, 0.036, 0.031, -0.028, -0.079, 0.031, -0.017, -0.014, 0.007, 0.001, 0.009, 0.014, 0.066, 0.008, -0.025, -0.021, -0.066, -0.245, 0.419, 0.41, -0.157, -0.103, 0.128, 0.139, -0.058, 0.029, -0.015, 0.003, 0.001, -0.006, 0.005, -0.006, 0.004, -0.005, -0.005, -0.01, 0.004, -0.001, 0.017, -0.001, -0.002, -0.001, -0.002, -0.014, 0.001, -0.001, -0.001, 0.004, 0.013, 0.012, -0.01, -0.01, 0.0, -0.004, -0.004, 0.068, 0.155, -0.033, -0.029, -0.063, 0.011, 0.052, 0.052, -0.071, -0.001, 0.193, -0.101, -0.041, -0.103, -0.085, -0.041, -0.177, -0.099, 0.054, 0.041, -0.017, 0.079, -0.025, -0.026, 0.02, -0.008, -0.009, 0.017, 0.007, -0.012, 0.007, 0.031, -0.018, 0.011, 0.002, 0.002, -0.001, -0.01, 0.019, -0.013, 0.002, -0.005, 0.003, 0.004, 0.004, -0.186, -0.759, 0.197, 0.172, 0.186, -0.094, 0.006, 0.012, 0.0, -0.008, 0.006, 0.007, 0.006, 0.009, 0.004, 0.006, 0.004, 0.004, 0.004, -0.007, 0.001, -0.007, -0.006, 0.014, -0.019, -0.021, 0.005, 0.045, 0.004, 0.001, 0.028, -0.007, 0.028, -0.014, 0.022, 0.036, 0.027, 0.013, 0.004, 0.002, 0.015, -0.009, -0.0, 0.014, 0.006, -0.005, 0.003, 0.002, -0.004, -0.002, 0.002, -0.006, -0.008, -0.014, 0.072, -0.182, 0.022, -0.047, 0.043, 0.025, -0.011, 0.006, -0.04, 0.038, -0.08, -0.01, -0.014, 0.034, 0.008, -0.001, 0.019, -0.013, 0.001, 0.007, 0.016, -0.003, -0.008, 0.002, 0.009, 0.004, -0.007, -0.003, -0.001, 0.006, -0.005, 0.01, -0.005, 0.005, 0.005, -0.003, 0.005, -0.011, 0.084, -0.236, -0.112, 0.034, 0.014, 0.01, -0.048, 0.001, 0.009, 0.009, 0.014, 0.014, 0.03, 0.001, -0.017, -0.014, -0.009, 0.009, -0.004, -0.006, -0.001, 0.006, 0.005, -0.009, 0.0, -0.04, 0.865, 0.59, 0.472, 0.04, 0.077, -0.013, -0.011, -0.043, -0.055, 0.037, 0.004, -0.01, 0.003, 0.006, -0.008, 0.016, -0.008, 0.009, 0.009, -0.017, 0.001, -0.012, 0.018, -0.036, -0.026, 0.008, 0.05, -0.048, 0.004, 0.009, 0.017, 0.001, 0.009, 0.004, 0.002, 0.026, -0.01, 0.075, 0.008, 0.194, -0.388, -0.164, 0.148, 0.015, 0.056, 0.191, 0.107, -0.016, -0.021, 0.004, 0.0, -0.004, -0.006, -0.006, -0.002, 0.007, -0.003, 0.0, -0.001, -0.007, -0.007, -0.005, 0.005, 0.003, 0.002, -0.01, -0.003, 0.007, -0.008, -0.009, -0.001, 0.019, -0.0, -0.002, -0.004, 0.007, -0.004, 0.054, 0.022, -0.033, -0.051, -0.017, 0.17, -0.063, 0.068, 0.009, 0.01, -0.118, -0.222, -0.03, -0.004, 0.121, -0.034, -0.119, -0.174, -0.013, 0.021, -0.071, 0.022, -0.046, -0.007, 0.011, -0.001, -0.007, -0.034, -0.018, 0.001, -0.021, 0.024, 0.019, 0.002, 0.002, -0.014, -0.006, -0.015, 0.006, 0.009, -0.009, -0.01, -0.002, 0.015, -0.002, -0.819, 1.0, 0.217, -0.031, 0.098, 0.021, -0.025, 0.011, -0.019, -0.005, -0.006, -0.009, -0.003, 0.01, -0.006, 0.0, 0.006, 0.003, -0.003, 0.004, 0.006, 0.012, -0.02, 0.005, 0.022, -0.002, 0.012, -0.042, 0.004, -0.02, 0.002, 0.011, 0.045, -0.0, 0.011, 0.024, -0.048, 0.009, -0.001, -0.003, 0.006, 0.036, -0.001, 0.001, 0.0, 0.002, -0.005, -0.008, 0.001, -0.006, 0.015, -0.002, -0.005, -0.01, -0.098, -0.095, 0.07, -0.149, -0.03, 0.028, 0.004, 0.063, 0.006, 0.026, 0.093, -0.011, 0.006, -0.021, -0.056, 0.046, -0.004, 0.0, 0.008, -0.004, -0.001, -0.001, 0.005, -0.003, -0.008, -0.005, 0.001, 0.016, 0.009, 0.011, -0.004, -0.001, -0.012, 0.001, 0.006, 0.005, 0.002, -0.053, 0.043, 0.12, -0.031, 0.066, 0.036, -0.005, 0.031, 0.014, 0.04, -0.01, -0.003, -0.019, 0.021, -0.02, 0.051, -0.03, -0.022, 0.007, -0.005, -0.003, -0.009, 0.008, 0.001, -0.004, -0.05, 0.651, -0.385, 0.104, 0.486, 0.183, -0.032, 0.013, 0.031, 0.05, 0.018, -0.038, -0.005, 0.004, 0.004, 0.0, 0.003, -0.003, 0.001, 0.013, 0.003, -0.007, -0.002, -0.009, -0.006, -0.009, -0.006, -0.004, 0.043, 0.01, -0.022, 0.013, -0.015, -0.016, 0.014, -0.121, -0.095, 0.033, 0.019, -0.014, -0.287, 0.362, 0.491, 0.239, -0.105, 0.153, 0.153, -0.017, -0.055, -0.007, 0.002, -0.01, 0.001, 0.003, 0.009, 0.009, -0.015, -0.01, 0.001, 0.012, 0.003, -0.001, -0.003, -0.011, -0.006, 0.006, -0.001, -0.009, 0.001, -0.0, -0.014, -0.006, 0.004, -0.021, -0.004, 0.007, 0.0, -0.007, 0.011, 0.057, 0.008, -0.02, 0.009, -0.031, 0.013, 0.142, -0.299, -0.011, 0.078, 0.072, 0.109, 0.1, -0.055, -0.065, 0.0, -0.093, 0.036, 0.01, -0.008, -0.027, -0.076, -0.057, -0.009, 0.025, 0.014, -0.0, 0.019, -0.005, -0.023, 0.009, 0.022, 0.061, 0.007, -0.002, 0.008, -0.003, -0.002, -0.015, -0.01, 0.001, 0.001, -0.015, -0.001, 0.009, 0.101, -0.691, 0.313, 0.52, -0.063, 0.0, 0.009, -0.013, -0.002, -0.001, -0.002, -0.004, 0.001, 0.003, -0.004, 0.0, 0.011, -0.013, -0.013, -0.001, 0.005, -0.006, -0.006, 0.001, 0.005, -0.006, 0.003, -0.059, -0.036, -0.052, -0.019, 0.026, -0.003, -0.061, 0.008, -0.005, -0.0, -0.005, -0.005, -0.004, 0.003, -0.007)

    private val viewModel: BottomSheetViewModel by lazy {
        ViewModelProvider(this, BottomSheetViewModel.ViewModelFactory()).get(BottomSheetViewModel::class.java)
    }
    private lateinit var binding: BottomSheetLayoutBinding
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

        //TODO(the controller should be used inside the ViewModel)
        val playerController = DefaultPlayerController(binding.playerView).apply { setListener() }
        val uri = Uri.parse("android.resource://${requireContext().packageName}/raw/france")
        try {
            playerController.addAudioFileUri(requireContext(), uri, amplitudes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //TODO(the date should not be hard-coded)
        val s = SpannableStringBuilder()
            .bold { append("Le : ") }
            .append("02/06/1997")
        playerController.setTitle(s)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.root)

        setUpOnClickListeners()
        addListenerForMeasurement()
        addCallbackOnBottomSheet()
        setUpObservers()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        return binding.root
    }

    private fun setUpOnClickListeners() {
        binding.close.setOnClickListener { onClose() }
        binding.expand.setOnClickListener { onExpand() }
    }

    private fun onExpand() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        if (!imageDisplayed) {
            displayImage()
        } else {
            displayWaveForm()
        }
    }

    private fun addListenerForMeasurement() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                measure()
                val obs: ViewTreeObserver = binding.root.viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun addCallbackOnBottomSheet() {
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            // No other solution was found to pin a view to the bottom of the BottomSheet
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.pin.translationY = (heightExpanded - bottomSheet.top).toFloat()
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    //easier with setFitToContent = false
                    // Maybe try with a collapsing toolbar ? or a motion layout ?
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.close.setImageResource(R.drawable.ic_marker)
                        if (imageDisplayed) {
                            displayWaveForm()
                        }
                        binding.playerView.apply { requestLayout() }.layoutParams.height = resources.getDimensionPixelSize(R.dimen.wave_form)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.close.setImageResource(R.drawable.arrow_down)
                        binding.playerView.apply { requestLayout() }.layoutParams.height = 0
                    }
                    else -> {
                        binding.close.setImageResource(R.drawable.ic_marker)
                    }
                }
            }
        })
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
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
                }
            })
    }

    private fun displayImage(){
        crossFade(binding.soundImage, binding.playerView)
        binding.expand.text = "Voir le son"
        imageDisplayed = true
    }

    private fun displayWaveForm(){
        crossFade(binding.playerView, binding.soundImage)
        binding.playerView.layoutParams.height = 0
        binding.expand.text = "Voir l'image"
        imageDisplayed = false
    }

    private fun measure() {
        binding.apply{
            bottomSheetBehavior.peekHeight = pin.height*2
            val screenHeight: Int = DisplayMetrics().also { requireActivity().windowManager.defaultDisplay.getMetrics(it) }.heightPixels
            heightExpanded = container.top - container.height // A bit mysterious but it works
            //TODO Set collapsed height here
            bottomSheetBehavior.peekHeight = pin.height*2 + playerView.height
        }
    }

    private fun changeWidgetsVisibility(makeVisible: Boolean){
        binding.apply {
            if (makeVisible){
                location.visibility = View.VISIBLE
                close.visibility = View.VISIBLE
                playerView.visibility = View.VISIBLE
                pin.visibility = View.VISIBLE
                coordinates.visibility = View.VISIBLE

                progressBar.visibility = View.GONE
            }
            else{
                location.visibility = View.GONE
                close.visibility = View.GONE
                playerView.visibility = View.GONE
                pin.visibility = View.GONE
                coordinates.visibility = View.GONE

                progressBar.visibility = View.VISIBLE
            }
        }
    }

}
