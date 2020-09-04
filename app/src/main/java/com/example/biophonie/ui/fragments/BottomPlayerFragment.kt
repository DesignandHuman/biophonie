package com.example.biophonie.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import com.example.biophonie.databinding.FragmentBottomPlayerBinding
import com.example.biophonie.viewmodels.BottomPlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


private const val TAG = "BottomPlayerFragment"
class BottomPlayerFragment : Fragment() {

    private var heightExpanded: Int = 400
    private var imageDisplayed: Boolean = false
    private var shortAnimationDuration: Int = 0

    private val viewModel: BottomPlayerViewModel by lazy {
        ViewModelProvider(this, BottomPlayerViewModel.ViewModelFactory(requireContext())).get(
            BottomPlayerViewModel::class.java
        )
    }
    private lateinit var binding: FragmentBottomPlayerBinding
    lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_bottom_player,
            container,
            false
        )
        binding.viewModel = viewModel.apply {
            //TODO(run that somehow on another thread or not ?)
            setPlayerController(requireContext(), binding.playerView) }
        binding.lifecycleOwner = this

        bottomSheetBehavior = BottomSheetBehavior.from(binding.root)

        setUpClickListeners()
        addListenerForMeasurement()
        addCallbackOnBottomSheet()
        setUpObservers()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        return binding.root
    }

    private fun setUpClickListeners() {
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
                        binding.playerView.apply { requestLayout() }.layoutParams.height =
                            resources.getDimensionPixelSize(
                                R.dimen.wave_form
                            )
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
            Toast.makeText(activity, "Vérifiez votre connection réseau", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }

    fun clickOnGeoPoint(id: String, name: String, coordinates: LatLng){
        viewModel.getGeoPoint(id, name, coordinates)
    }

    private fun setUpObservers() {
        viewModel.leftClickable.observe(viewLifecycleOwner, Observer<Boolean> {
            setArrowClickable(
                binding.left,
                it
            )
        })
        viewModel.rightClickable.observe(viewLifecycleOwner, Observer<Boolean> {
            setArrowClickable(
                binding.right,
                it
            )
        })
        viewModel.visibility.observe(viewLifecycleOwner, Observer<Boolean> {
            changeWidgetsVisibility(
                it
            )
        })
        viewModel.bottomSheetState.observe(
            viewLifecycleOwner,
            Observer<Int> { bottomSheetBehavior.state = it })
        viewModel.eventNetworkError.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isNetworkError ->
                if (isNetworkError) onNetworkError()
            })
        viewModel.date.observe(viewLifecycleOwner, Observer<String> {
            viewModel.playerController.setTitle(SpannableStringBuilder()
                .bold { append("Le : ") }
                .append(it.split("\\s".toRegex())[0])
                .bold { append(" à ") }
                .append(it.split("\\s".toRegex())[1]))
        })
    }

    private fun setArrowClickable(view: TextView, clickable: Boolean) {
        if (clickable) {
            view.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimaryDark
                )
            )
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
                bottomSheetBehavior.peekHeight = pin.height*2 + playerView.height
                val previousState = bottomSheetBehavior.state
                // Very very mysterious but it works
                if (previousState == BottomSheetBehavior.STATE_HIDDEN){
                    heightExpanded = container.top - container.height // A bit mysterious but it works
                } else {
                    if (activity?.resources?.configuration?.orientation == ORIENTATION_LANDSCAPE) {
                        heightExpanded = (container.top - container.height) / 2
                        pin.translationY =
                            (container.top + heightExpanded - container.height).toFloat()
                    } else {
                        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
                            heightExpanded = (root.height - root.top)*4
                            pin.translationY = (heightExpanded - root.top).toFloat()
                        }
                    }
                }
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

    override fun onPause() {
        super.onPause()
        viewModel.playerController.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.playerController.destroyPlayer()
    }
}
