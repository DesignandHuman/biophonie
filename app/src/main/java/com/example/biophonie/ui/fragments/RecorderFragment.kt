package com.example.biophonie.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.example.biophonie.BuildConfig
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentRecordingBinding
import com.example.biophonie.viewmodels.RecViewModel
import fr.haran.soundwave.controller.DefaultRecorderController
import kotlin.properties.Delegates

private const val MINIMUM_DURATION = 60000
class RecorderFragment : Fragment() {

    private var startTime by Delegates.notNull<Long>()
    private var duration by Delegates.notNull<Long>()
    private val viewModel: RecViewModel by activityViewModels{
        RecViewModel.ViewModelFactory(requireActivity().application!!)
    }
    private lateinit var binding: FragmentRecordingBinding
    private var recorderController: DefaultRecorderController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recording,
            container,
            false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setClickListeners()
        setRecorderController()
        setDataObserver()
        return binding.root
    }

    private fun setDataObserver() {
        viewModel.goToNext.observe(viewLifecycleOwner, Observer {
            if (BuildConfig.DEBUG) {
                if (it){
                    binding.root.findNavController().navigate(R.id.action_recordingFragment_to_galleryFragment)
                    viewModel.onNextFragment()}
            } else {
                if (duration >= MINIMUM_DURATION)
                    binding.root.findNavController().navigate(R.id.action_recordingFragment_to_galleryFragment)
                else
                    Toast.makeText(
                        requireContext(),
                        "Une durée de plus de ${MINIMUM_DURATION /60000} minute est nécessaire",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        })
    }

    // The controller is inside the Fragment because it needs a reference to the recplayerview
    // The separation of concerns is not respected because of this. But I do not see another way
    // of using compound views in MVVM architecture.
    private fun setRecorderController() {
        viewModel.setRecorderController(binding.recPlayerView)
    }

    private fun setClickListeners() {
        binding.topPanel.previous.setOnClickListener { activity?.onBackPressed() }
        binding.topPanel.close.setOnClickListener { activity?.finish() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recorderController?.destroyController()
        recorderController = null
    }
}