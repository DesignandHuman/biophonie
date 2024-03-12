package fr.labomg.biophonie.ui.fragments

import android.app.Service
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fr.labomg.biophonie.R
import fr.labomg.biophonie.databinding.FragmentTitleBinding
import fr.labomg.biophonie.util.setFiltersOnEditText
import fr.labomg.biophonie.viewmodels.RecViewModel

class TitleFragment : Fragment() {
    private var _binding: FragmentTitleBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: RecViewModel by activityViewModels {
        RecViewModel.ViewModelFactory(requireActivity().application!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_title, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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
                // Hide keyboard
                val imm: InputMethodManager =
                    requireContext().getSystemService(Service.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                imm.hideSoftInputFromWindow(root.windowToken, 0)
            }
        }
    }

    private fun setDataObservers() {
        viewModel.toast.observe(viewLifecycleOwner) {
            it?.let {
                binding.titleInputLayout.error = it.message
                viewModel.onToastDisplayed()
            }
        }
        viewModel.result.observe(viewLifecycleOwner) {
            val intent = Intent()
            val bundle =
                Bundle().apply {
                    putString("title", it.title)
                    putString("date", it.date)
                    putFloatArray(
                        "amplitudes",
                        FloatArray(it.amplitudes.size) { index -> it.amplitudes[index].toFloat() }
                    )
                    putDouble("latitude", it.coordinates.latitude())
                    putDouble("longitude", it.coordinates.longitude())
                    putString("soundPath", it.soundPath)
                    putString("landscapePath", it.landscapePath)
                    putString("templatePath", it.templatePath)
                }
            intent.putExtras(bundle)
            requireActivity().apply {
                setResult(AppCompatActivity.RESULT_OK, intent)
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
