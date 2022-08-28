package com.example.biophonie.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentBottomPlayerBinding
import com.example.biophonie.util.ScreenMetricsCompat
import com.example.biophonie.viewmodels.BottomPlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.geojson.Point
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
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_bottom_player,
            container,
            false
        )
        binding.viewModel = viewModel.apply {
            //TODO(run that somehow on another thread or not ?)
            setPlayerController(requireContext(), binding.playerView) }
        binding.lifecycleOwner = viewLifecycleOwner
        //TODO maybe a leak
        //https://stackoverflow.com/questions/57647751/android-databinding-is-leaking-memory

        bottomSheetBehavior = BottomSheetBehavior.from(binding.root)

        setUpClickListeners()
        addCallbackOnBottomSheet()
        setUpObservers()
        setPeekHeight()
        setProgressBarPosition()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        animationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val animated = AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.loader)
        animated?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                binding.progressBar.post { animated.start() }
            }
        })
        binding.progressBar.setImageDrawable(animated)
        animated?.start()
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

    private fun onNetworkError() {
        if(!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Vérifiez votre connection réseau", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }

    fun clickOnGeoPoint(id: String, name: String, coordinates: Point){
        viewModel.getGeoPoint(id, name, coordinates)
    }

    private fun setUpObservers() {
        viewModel.leftClickable.observe(viewLifecycleOwner, {
            binding.left.isEnabled = it
        })
        viewModel.rightClickable.observe(viewLifecycleOwner, {
            binding.right.isEnabled = it
        })
        viewModel.visibility.observe(viewLifecycleOwner, {
            changeWidgetsVisibility(it)
        })
        viewModel.bottomSheetState.observe(viewLifecycleOwner, {
            bottomSheetBehavior.state = it
        })
        viewModel.eventNetworkError.observe(viewLifecycleOwner, {
            if (it) onNetworkError()
        })
        viewModel.date.observe(viewLifecycleOwner, {
            viewModel.playerController.setTitle(SpannableStringBuilder()
                .bold { append("Le : ") }
                .append(it.split("\\s".toRegex())[0])
                .bold { append(" à ") }
                .append(it.split("\\s".toRegex())[1]))
        })
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
