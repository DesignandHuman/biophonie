package com.example.biophonie.ui.fragments

import android.app.Service
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentTitleBinding
import com.example.biophonie.util.setFiltersOnEditText
import com.example.biophonie.viewmodels.RecViewModel
import java.util.*


class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding
    private val viewModel: RecViewModel by activityViewModels{
        RecViewModel.ViewModelFactory(requireActivity().application!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_title,
            container,
            false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setClickListeners()
        setDataObservers()
        binding.titleEditText.setFiltersOnEditText()
        return binding.root
    }

    private fun setClickListeners() {
        binding.apply {
            topPanel.close.setOnClickListener { activity?.finish() }
            topPanel.previous.setOnClickListener {
                activity?.onBackPressed()
                //Hide keyboard
                val imm: InputMethodManager = requireContext().getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(root.windowToken, 0)
            }
        }
    }

    private fun setDataObservers() {
        viewModel.toast.observe(viewLifecycleOwner, {
            it?.let {
                binding.titleInputLayout.error = it.message
                viewModel.onToastDisplayed()
            }
        })
        viewModel.result.observe(viewLifecycleOwner, {
            val intent = Intent()
            val bundle = Bundle().apply {
                putString("title", it.title)
                putString("date", it.date)
                putIntegerArrayList("amplitudes", it.amplitudes as ArrayList<Int>)
                putDouble("latitude", it.coordinates.latitude())
                putDouble("longitude", it.coordinates.longitude())
                putString("soundPath", it.soundPath)
                putString("landscapePath", it.landscapePath)
            }
            intent.putExtras(bundle)
            requireActivity().apply {
                setResult(AppCompatActivity.RESULT_OK, intent)
                requireActivity().finish()
            }
        })
    }
}