package com.example.biophonie.ui.fragments

import android.app.Service
import android.content.Intent
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
import androidx.lifecycle.Observer
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentTitleBinding
import com.example.biophonie.viewmodels.RecViewModel
import java.util.ArrayList


class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding
    private val viewModel: RecViewModel by activityViewModels{
        RecViewModel.ViewModelFactory(requireActivity().application!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_title,
            container,
            false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setClickListeners()
        setDataObservers()
        setFiltersOnEditText()
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

    private fun setFiltersOnEditText() {
        val filter = InputFilter { source, start, end, _, _, _ ->
            return@InputFilter if (source is SpannableStringBuilder) {
                for (i in end - 1 downTo start) {
                    val currentChar: Char = source[i]
                    if (!Character.isLetterOrDigit(currentChar) && !Character.isSpaceChar(
                            currentChar
                        )
                    ) {
                        source.delete(i, i + 1)
                    }
                }
                source
            } else {
                val filteredStringBuilder = StringBuilder()
                for (i in start until end) {
                    val currentChar: Char = source[i]
                    if (Character.isLetterOrDigit(currentChar) || Character.isSpaceChar(
                            currentChar
                        )
                    ) {
                        filteredStringBuilder.append(currentChar)
                    }
                }
                filteredStringBuilder.toString()
            }
        }
        binding.titleEditText.filters += filter
    }

    private fun setDataObservers() {
        viewModel.toast.observe(viewLifecycleOwner, Observer{
            it?.let {
                binding.titleInputLayout.error = it.message
                viewModel.onToastDisplayed()
            }
        })
        viewModel.result.observe(viewLifecycleOwner, Observer {
            val intent = Intent()
            val bundle = Bundle().apply {
                putString("title", it.title)
                putIntegerArrayList("amplitudes", it.amplitudes as ArrayList<Int>)
                putDouble("latitude", it.coordinates.latitude)
                putDouble("longitude", it.coordinates.longitude)
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