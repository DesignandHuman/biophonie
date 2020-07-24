package com.example.biophonie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentRecordingBinding

class RecordingFragment : Fragment() {

    private lateinit var binding: FragmentRecordingBinding

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
        return binding.root
    }

    private fun setClickListeners() {
        binding.okButton.ok.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_recordingFragment_to_galleryFragment)
        }
        binding.topPanel.previous.setOnClickListener { activity?.onBackPressed() }
        binding.topPanel.close.setOnClickListener { activity?.finish() }
    }
}