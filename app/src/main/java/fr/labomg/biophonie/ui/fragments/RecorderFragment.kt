package fr.labomg.biophonie.ui.fragments

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import fr.labomg.biophonie.R
import fr.labomg.biophonie.databinding.FragmentRecordingBinding
import fr.labomg.biophonie.viewmodels.RecViewModel

class RecorderFragment : Fragment() {

    private val viewModel: RecViewModel by activityViewModels()
    private var _binding: FragmentRecordingBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recording, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        setClickListeners()
        setRecorderController()
        setDataObserver()
        return binding.root
    }

    private fun setDataObserver() {
        viewModel.recordComplete.observe(viewLifecycleOwner) {
            if (it) {
                binding.root
                    .findNavController()
                    .navigate(R.id.action_recordingFragment_to_galleryFragment)
                viewModel.onValidateRecording()
            }
        }
    }

    // The controller is inside the Fragment because it needs a reference to the recplayerview
    // The separation of concerns is not respected because of this. But I do not see another way
    // of using compound views in MVVM architecture.
    private fun setRecorderController() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val recordedPreviously = viewModel.setRecorderController(binding.recPlayerView)
                    if (!recordedPreviously) viewModel.startRecording()
                    binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        )
    }

    private fun setClickListeners() {
        binding.topPanel.previous.setOnClickListener { activity?.onBackPressed() }
        binding.topPanel.close.setOnClickListener { activity?.finish() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopRecording()
        binding.recPlayerView.destroyCountdown()
        _binding = null
    }
}
