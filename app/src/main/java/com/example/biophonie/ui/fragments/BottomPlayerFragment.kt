package com.example.biophonie.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "BottomPlayerFragment"
class BottomPlayerFragment : Fragment() {

    private var imageDisplayed: Boolean = false
    private var animationDuration: Int = 0

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
        addCallbackOnBottomSheet()
        setUpObservers()
        setPeekHeight()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        animationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        return binding.root
    }

    private fun setPeekHeight() {
        val screenHeight =
            DisplayMetrics().also { requireActivity().windowManager.defaultDisplay.getMetrics(it) }.heightPixels
        bottomSheetBehavior.peekHeight =
            if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) screenHeight / 2
            else screenHeight / 3
        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main){
                    pin.translationY = - root.top.toFloat()
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
                if (imageDisplayed)
                    updateViewMargins(binding.soundImage, bottomSheet.top)
                else
                    updateViewMargins(binding.playerView, bottomSheet.top)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    // Maybe try with a collapsing toolbar ? or a motion layout ?
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.close.setImageResource(R.drawable.ic_marker)
                        if (imageDisplayed)
                            displayWaveForm()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.close.setImageResource(R.drawable.arrow_down)
                        binding.playerView.apply { requestLayout() }.layoutParams.height = 0
                    }
                    else -> binding.close.setImageResource(R.drawable.ic_marker)
                }
            }
        })
    }

    private fun updateViewMargins(view: View, margin: Int) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.bottomMargin = margin
        view.layoutParams = params
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
            // Sets the content view to 0% opacity but visible
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity
            animate()
                .alpha(1f)
                .setDuration(animationDuration.toLong())
                .setListener(null)
        }
        // Animate the loading view to 0% opacity.
        // After the animation ends, set its visibility to GONE
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
            expand.text = "Voir le son"
        }
        imageDisplayed = true
    }

    private fun displayWaveForm(){
        binding.apply {
            crossFade(playerView, soundImage)
            playerView.layoutParams.height = 0
            expand.text = "Voir l'image"
        }
        imageDisplayed = false
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
