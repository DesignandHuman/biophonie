package com.example.biophonie.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.biophonie.BiophonieApplication
import com.example.biophonie.R
import com.example.biophonie.data.Coordinates
import com.example.biophonie.databinding.FragmentBottomPlayerBinding
import com.example.biophonie.ui.activities.MapActivity
import com.example.biophonie.util.ScreenMetricsCompat
import com.example.biophonie.viewmodels.BottomPlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BottomPlayerFragment : Fragment() {

    private var imageDisplayed: Boolean = false
    private var animationDuration: Int = 0

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val viewModel: BottomPlayerViewModel by lazy {
        ViewModelProvider(this, BottomPlayerViewModel.ViewModelFactory(requireContext().applicationContext as BiophonieApplication)).get(
            BottomPlayerViewModel::class.java
        )
    }
    private var _binding: FragmentBottomPlayerBinding? = null
    private val binding get() = _binding!!
    lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var progressBarAnimation: AnimatedVectorDrawableCompat? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_bottom_player,
            container,
            false
        )
        binding.viewModel = viewModel.apply {
            //TODO(run that somehow on another thread or not ?)
            setPlayerController(binding.playerView) }
        binding.lifecycleOwner = viewLifecycleOwner

        bottomSheetBehavior = BottomSheetBehavior.from(binding.root)

        setUpClickListeners()
        addCallbackOnBottomSheet()
        setUpObservers()
        setPeekHeight()
        setProgressBarPosition()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        animationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        progressBarAnimation = AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.loader).apply {
            this?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    _binding?.progressBar?.post { this@apply.start() }
                }
            })
        }
        return binding.root
    }

    private fun setProgressBarPosition() {
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val params = binding.progressBar.layoutParams as ConstraintLayout.LayoutParams
            params.verticalBias = 0.2f
            binding.progressBar.layoutParams = params
        }
    }

    private fun setPeekHeight() {
        val screenHeight = context?.let { ScreenMetricsCompat.getScreenSize(it) }?.height
        if (screenHeight != null) {
            bottomSheetBehavior.peekHeight =
                if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) screenHeight / 2
                else screenHeight / 3
        }
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch { //TODO: superflu ?
                withContext(Dispatchers.Main){
                    pin.translationY = - root.top.toFloat()
                    errorMessage.translationY = - root.top.toFloat() / 2.5f
                    retryFab.translationY = - root.top.toFloat() / 2.5f
                    progressBar.translationY = - root.top.toFloat() / 2f
                    if (imageDisplayed)
                        updateViewMargins(soundImage, root.top)
                    else
                        updateViewMargins(playerView, root.top)
                }
            }
        }
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

    private fun addCallbackOnBottomSheet() {
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            // No other solution was found to pin a view to the bottom of the BottomSheet
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.pin.translationY = -bottomSheet.top.toFloat()
                binding.errorMessage.translationY = -bottomSheet.top.toFloat() / 2.5f
                binding.retryFab.translationY = -bottomSheet.top.toFloat() / 2.5f
                binding.progressBar.translationY = -bottomSheet.top.toFloat() / 2f
                if (imageDisplayed)
                    updateViewMargins(binding.soundImage, bottomSheet.top)
                else
                    updateViewMargins(binding.playerView, bottomSheet.top)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    // Maybe try with a collapsing toolbar ? or a motion layout ?
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.close.setImageResource(R.drawable.ic_close)
                        if (imageDisplayed)
                            displayWaveForm()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.close.setImageResource(R.drawable.ic_stripe_down)
                        binding.playerView.apply { requestLayout() }.layoutParams.height = 0
                    }
                    else -> binding.close.setImageResource(R.drawable.ic_close)
                }
            }
        })
    }

    private fun updateViewMargins(view: View, margin: Int) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.bottomMargin = margin
        view.layoutParams = params
    }

    fun clickOnGeoPoint(id: Int){
        viewModel.setGeoPointQuery(id)
    }

    fun displayClosestGeoPoint(coord: Coordinates){
        viewModel.displayClosestGeoPoint(coord)
    }

    private fun setUpObservers() {
        viewModel.leftClickable.observe(viewLifecycleOwner) {
            binding.left.isEnabled = it
        }
        viewModel.rightClickable.observe(viewLifecycleOwner) {
            binding.right.isEnabled = it
        }
        viewModel.bottomSheetState.observe(viewLifecycleOwner) {
            bottomSheetBehavior.state = it
        }
        viewModel.event.observe(viewLifecycleOwner) {
            if (it == BottomPlayerViewModel.Event.LOADING) {
                binding.progressBar.setImageDrawable(progressBarAnimation)
                progressBarAnimation?.start()
            } else {
                progressBarAnimation?.stop()
            }
        }
        viewModel.geoPoint.observe(viewLifecycleOwner) {
            it?.let {
                (activity as? MapActivity)?.selectPoint(
                    Point.fromLngLat(it.coordinates.longitude,it.coordinates.latitude),
                    viewModel.geoPointId.value!!
                )
            }
        }
    }

    private fun onClose() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            (activity as? MapActivity)?.onBottomSheetClose()
        }
    }

    private fun crossFade(fadeIn: View, fadeOut: View) {
        fadeIn.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(animationDuration.toLong())
                .setListener(null)
        }
        fadeOut.animate()
            .alpha(0f)
            .setDuration(animationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    changePinBottomConstraint(fadeIn)
                    updateViewMargins(fadeIn, binding.root.top)
                    fadeOut.visibility = View.GONE
                }
            })
    }

    private fun changePinBottomConstraint(destination: View){
        val params = binding.pin.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = destination.id
        binding.pin.layoutParams = params
    }

    private fun displayImage(){
        binding.apply {
            crossFade(soundImage, playerView)
            expand.text = getString(R.string.expand_player)
        }
        imageDisplayed = true
    }

    private fun displayWaveForm(){
        binding.apply {
            crossFade(playerView, soundImage)
            playerView.layoutParams.height = 0
            expand.text = getString(R.string.expand_image)
        }
        imageDisplayed = false
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseController()
        progressBarAnimation?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBarAnimation = null
        _binding = null
        viewModel.destroyController()
    }
}
