package fr.labomg.biophonie.feature.exploregeopoints

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
import androidx.fragment.app.activityViewModels
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.core.ui.ScreenMetricsCompat
import fr.labomg.biophonie.feature.exploregeopoints.databinding.FragmentBottomPlayerBinding

@AndroidEntryPoint
class BottomPlayerFragment : Fragment() {

    private var imageDisplayed: Boolean = false
    private var animationDuration: Int = 0

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val viewModel: ExploreViewModel by activityViewModels()
    private var _binding: FragmentBottomPlayerBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var progressBarAnimation: AnimatedVectorDrawableCompat? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_bottom_player, container, false)
        binding.viewModel = viewModel.apply { setPlayerController(binding.playerView) }
        binding.lifecycleOwner = viewLifecycleOwner

        bottomSheetBehavior = BottomSheetBehavior.from(binding.root)

        setUpClickListeners()
        addCallbackOnBottomSheet()
        setUpObservers()
        setPeekHeight()
        setProgressBarPosition()

        hideBottomPlayer()
        animationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        progressBarAnimation =
            AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.loader).apply {
                this?.registerAnimationCallback(
                    object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            _binding?.progressBar?.post { this@apply.start() }
                        }
                    }
                )
            }
        return binding.root
    }

    private fun setProgressBarPosition() {
        if (
            activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            val params = binding.progressBar.layoutParams as ConstraintLayout.LayoutParams
            params.verticalBias = VERTICAL_CONSTRAINT_BIAS
            binding.progressBar.layoutParams = params
        }
    }

    private fun setPeekHeight() {
        val screenHeight = context?.let { ScreenMetricsCompat.getScreenSize(it) }?.height
        if (screenHeight != null) {
            bottomSheetBehavior.peekHeight =
                if (
                    activity?.resources?.configuration?.orientation ==
                        Configuration.ORIENTATION_LANDSCAPE
                )
                    screenHeight / HALF_SCREEN_RATIO
                else screenHeight / THIRD_SCREEN_RATIO
        }
        binding.apply {
            pin.translationY = -root.top.toFloat()
            errorMessage.translationY = -root.top.toFloat() / HALF_THIRD_SCREEN_RATIO
            retryFab.translationY = -root.top.toFloat() / HALF_THIRD_SCREEN_RATIO
            progressBar.translationY = -root.top.toFloat() / HALF_SCREEN_RATIO
            if (imageDisplayed) updateViewMargins(soundImage, root.top)
            else updateViewMargins(playerView, root.top)
        }
    }

    private fun setUpClickListeners() {
        binding.close.setOnClickListener { onClose() }
        binding.expand.setOnClickListener { onExpand() }
    }

    private fun onExpand() {
        expandBottomPlayer()
        if (!imageDisplayed) {
            displayImage()
        } else {
            displayWaveForm()
        }
    }

    private fun addCallbackOnBottomSheet() {
        bottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                // No other solution was found to pin a view to the bottom of the BottomSheet
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.pin.translationY = -bottomSheet.top.toFloat()
                    binding.errorMessage.translationY =
                        -bottomSheet.top.toFloat() / HALF_SCREEN_RATIO
                    binding.retryFab.translationY = -bottomSheet.top.toFloat() / HALF_SCREEN_RATIO
                    binding.progressBar.translationY = -bottomSheet.top.toFloat() / 2f
                    if (imageDisplayed) updateViewMargins(binding.soundImage, bottomSheet.top)
                    else updateViewMargins(binding.playerView, bottomSheet.top)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        // Maybe try with a collapsing toolbar ? or a motion layout ?
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            binding.close.setImageResource(R.drawable.ic_close)
                            if (imageDisplayed) displayWaveForm()
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            binding.close.setImageResource(R.drawable.ic_stripe_down)
                            binding.playerView.apply { requestLayout() }.layoutParams.height = 0
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            viewModel.unselect()
                        }
                        else -> binding.close.setImageResource(R.drawable.ic_close)
                    }
                }
            }
        )
    }

    private fun updateViewMargins(view: View, margin: Int) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.bottomMargin = margin
        view.layoutParams = params
    }

    private fun setUpObservers() {
        viewModel.leftClickable.observe(viewLifecycleOwner) { binding.left.isEnabled = it }
        viewModel.rightClickable.observe(viewLifecycleOwner) { binding.right.isEnabled = it }
        viewModel.event.observe(viewLifecycleOwner) {
            if (it == ExploreViewModel.Event.LOADING) {
                if (imageDisplayed) {
                    displayWaveForm()
                    imageDisplayed = false
                    binding.soundImage.visibility = View.GONE
                }
                binding.progressBar.setImageDrawable(progressBarAnimation)
                progressBarAnimation?.start()
            } else {
                progressBarAnimation?.stop()
            }
        }
        viewModel.geoPoint.observe(viewLifecycleOwner) {
            if (it != null) showBottomPlayer() else hideBottomPlayer()
        }
    }

    private fun onClose() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) collapseBottomPlayer()
        else hideBottomPlayer()
    }

    private fun crossFade(fadeIn: View, fadeOut: View) {
        fadeIn.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate().alpha(1f).setDuration(animationDuration.toLong()).setListener(null)
        }
        fadeOut
            .animate()
            .alpha(0f)
            .setDuration(animationDuration.toLong())
            .setListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        changePinBottomConstraint(fadeIn)
                        updateViewMargins(fadeIn, binding.root.top)
                        fadeOut.visibility = View.GONE
                    }
                }
            )
    }

    private fun changePinBottomConstraint(destination: View) {
        val params = binding.pin.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = destination.id
        binding.pin.layoutParams = params
    }

    private fun displayImage() {
        binding.apply {
            crossFade(soundImage, playerView)
            expand.text = getString(R.string.expand_player)
        }
        imageDisplayed = true
    }

    private fun displayWaveForm() {
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

    private fun collapseBottomPlayer() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun showBottomPlayer() {
        collapseBottomPlayer()
    }

    private fun expandBottomPlayer() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideBottomPlayer() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    companion object {
        private const val VERTICAL_CONSTRAINT_BIAS = 0.2f
        private const val HALF_SCREEN_RATIO = 2
        private const val HALF_THIRD_SCREEN_RATIO = 2.5f
        private const val THIRD_SCREEN_RATIO = 3
    }
}
