package fr.labomg.biophonie.feature.addgeopoint

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import fr.labomg.biophonie.core.ui.setFiltersOnEditText
import fr.labomg.biophonie.feature.addgeopoint.databinding.FragmentTitleBinding

class TitleFragment : Fragment() {
    private var _binding: FragmentTitleBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: AddViewModel by activityViewModels()

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
            topPanel.previous.setOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun setDataObservers() {
        viewModel.wasSubmitted.observe(viewLifecycleOwner) {
            if (it) {
                // using navigate here is not possible as it is only able to pop to nested_add_graph
                // and not host_nav_graph with deep link
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
