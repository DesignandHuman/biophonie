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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recording,
            container,
            false)
        binding.next.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_recordingFragment_to_galleryFragment) }
        // Inflate the layout for this fragment
        return binding.root
    }
}