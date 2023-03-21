package com.example.biophonie.ui.fragments

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
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentNameBinding
import com.example.biophonie.util.fadeIn
import com.example.biophonie.util.setFiltersOnEditText
import com.example.biophonie.viewmodels.TutorialViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TutoMapFragment : Fragment(), FirstLaunchFragments {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tuto_map, container, false)

    override fun animate() {
        (view?.findViewById<AppCompatImageView>(R.id.map_image)?.drawable as AnimatedVectorDrawable).start()
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
        ObjectAnimator.ofInt(clipDrawable,"level",0,10000).apply {
            duration = 2000
            addListener(object: AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    fab?.fadeIn()
                }
            })
            start()
        }
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
        (view.findViewById<AppCompatImageView>(R.id.background_location)?.background as AnimatedVectorDrawable)
            .registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    view.findViewById<View>(R.id.separator)?.fadeIn()
                    view.findViewById<View>(R.id.trip)?.fadeIn()
                    view.findViewById<View>(R.id.location)?.fadeIn()
                }
            })
    }

    override fun animate() {
        (view?.findViewById<View>(R.id.separator))?.visibility = View.INVISIBLE
        (view?.findViewById<View>(R.id.trip))?.visibility = View.INVISIBLE
        (view?.findViewById<View>(R.id.location))?.visibility = View.INVISIBLE
        (view?.findViewById<AppCompatImageView>(R.id.background_location)?.background as AnimatedVectorDrawable).start()
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
        (view?.findViewById<AppCompatImageView>(R.id.background_rec)?.drawable as AnimatedVectorDrawable).start()
    }
}

class TutoNameFragment: Fragment(){
    private var _binding: FragmentNameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TutorialViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_name,
            container,
            false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.nameEditText.setFiltersOnEditText()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}