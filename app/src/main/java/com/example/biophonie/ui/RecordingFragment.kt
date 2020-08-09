package com.example.biophonie.ui

import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentRecordingBinding
import com.example.biophonie.viewmodels.RecViewModel
import fr.haran.soundwave.controller.DefaultRecorderController
import java.sql.Time
import java.time.Duration
import java.util.*
import kotlin.properties.Delegates

private const val MINIMUM_DURATION = 60000
class RecordingFragment : Fragment() {

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
        setClickListeners()
        setRecorderController()
        return binding.root
    }

    // The controller is inside the Fragment because it needs a reference to the recplayerview
    // The separation of concerns is not respected because of this. But I do not see another way
    // of using compound views in MVVM architecture.
    private fun setRecorderController() {
        recorderController = requireContext().externalCacheDir?.absolutePath?.let {
            DefaultRecorderController(binding.recPlayerView,
                it,
                viewModel
            ).apply { setListener(
                start = { startTime = SystemClock.uptimeMillis() },
                complete = {
                    duration = SystemClock.uptimeMillis() - startTime
                    binding.controlButtons.visibility = View.VISIBLE
                    binding.advice.text = "C'est tout bon !"
                })}
        }
        recorderController?.prepareRecorder()
    }

    private fun setClickListeners() {
        binding.ok.setOnClickListener { view: View ->
            if (duration >= MINIMUM_DURATION)
                view.findNavController().navigate(R.id.action_recordingFragment_to_galleryFragment)
            else
                Toast.makeText(
                    requireContext(),
                    "Une durée de plus de ${MINIMUM_DURATION/60000} minute est nécessaire",
                    Toast.LENGTH_SHORT
                ).show()
        }
        binding.topPanel.previous.setOnClickListener { activity?.onBackPressed() }
        binding.topPanel.close.setOnClickListener { activity?.finish() }
        binding.recordAgain.setOnClickListener {
            binding.controlButtons.visibility = View.GONE
            recorderController?.toggle()
        }
        binding.play.setOnClickListener { /*TODO(not implemented yet)*/ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recorderController?.destroyRecorder()
        recorderController = null
    }
}