package com.example.biophonie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.biophonie.R
import com.example.biophonie.databinding.AboutLayoutBinding
import com.example.biophonie.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class AboutFragment : Fragment() {

    private lateinit var binding: AboutLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.about_layout,
            container,
            false)
        binding.lifecycleOwner = this
        return binding.root
    }
}
