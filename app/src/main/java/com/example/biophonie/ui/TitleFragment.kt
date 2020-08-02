package com.example.biophonie.ui

import android.app.Service
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
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
        binding.apply {
            ok.setOnClickListener { activity?.finish() }
            topPanel.close.setOnClickListener { activity?.finish() }
            topPanel.previous.setOnClickListener {
                activity?.onBackPressed()
                //Hide keyboard
                val imm: InputMethodManager = requireContext().getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(root.windowToken, 0)
            }
        }
    }
}