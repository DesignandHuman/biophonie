package com.example.biophonie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentTitleBinding

class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_title,
            container,
            false)
        setClickListeners()
        return binding.root
    }

    private fun setClickListeners() {
        binding.ok.setOnClickListener { activity?.finish() }
        binding.topPanel.close.setOnClickListener { activity?.finish() }
        binding.topPanel.previous.setOnClickListener { activity?.onBackPressed() }
    }
}