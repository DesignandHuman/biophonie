package com.example.biophonie.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.KeyEventDispatcher.dispatchKeyEvent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {
    private lateinit var binding: FragmentGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_gallery,
            container,
            false)
        setClickListeners()
        return binding.root
    }

    private fun setClickListeners() {
        binding.okButton.ok.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_galleryFragment_to_titleFragment)
        }
        binding.topPanel.close.setOnClickListener {
            activity?.finish()
        }
        binding.topPanel.previous.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}