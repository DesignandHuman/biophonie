package fr.labomg.biophonie.feature.firstlaunch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.core.ui.fadeIn
import fr.labomg.biophonie.core.ui.setFiltersOnEditText
import fr.labomg.biophonie.feature.firstlaunch.databinding.FragmentTutoNameBinding

class TutoMapFragment : Fragment(), FirstLaunchFragments {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tuto_map, container, false)

    override fun animate() {
        (view?.findViewById<AppCompatImageView>(R.id.map_image)?.drawable as AnimatedVectorDrawable)
            .start()
    }
}

class TutoDetailsFragment : Fragment(), FirstLaunchFragments {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tuto_details, container, false)

    override fun animate() {
        val fab = view?.findViewById<FloatingActionButton>(R.id.play)
        fab?.visibility = View.INVISIBLE
        val clipDrawable = view?.findViewById<AppCompatImageView>(R.id.details_image)?.drawable
        ObjectAnimator.ofInt(clipDrawable, "level", 0, ANIMATION_DURATION).apply {
            duration = FADE_IN_DURATION
            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        fab?.fadeIn()
                    }
                }
            )
            start()
        }
    }

    companion object {
        private const val ANIMATION_DURATION = 10000
        private const val FADE_IN_DURATION = 2000L
    }
}

class TutoLocationFragment : Fragment(), FirstLaunchFragments {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tuto_location, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view.findViewById<AppCompatImageView>(R.id.background_location)?.background
                as AnimatedVectorDrawable)
            .registerAnimationCallback(
                object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        view.findViewById<View>(R.id.separator)?.fadeIn()
                        view.findViewById<View>(R.id.trip)?.fadeIn()
                        view.findViewById<View>(R.id.location)?.fadeIn()
                    }
                }
            )
    }

    override fun animate() {
        (view?.findViewById<View>(R.id.separator))?.visibility = View.INVISIBLE
        (view?.findViewById<View>(R.id.trip))?.visibility = View.INVISIBLE
        (view?.findViewById<View>(R.id.location))?.visibility = View.INVISIBLE
        (view?.findViewById<AppCompatImageView>(R.id.background_location)?.background
                as AnimatedVectorDrawable)
            .start()
    }
}

interface FirstLaunchFragments {
    fun animate()
}

class TutoRecordFragment : Fragment(), FirstLaunchFragments {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tuto_record, container, false)

    override fun animate() {
        (view?.findViewById<AppCompatImageView>(R.id.background_rec)?.drawable
                as AnimatedVectorDrawable)
            .start()
    }
}

@AndroidEntryPoint
class TutoNameFragment : Fragment() {
    private var _binding: FragmentTutoNameBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: TutorialViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tuto_name, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.nameEditText.setFiltersOnEditText(strict = true)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
