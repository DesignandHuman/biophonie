package fr.labomg.biophonie.feature.exploregeopoints

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import fr.labomg.biophonie.feature.exploregeopoints.databinding.FragmentAboutBinding

class AboutDialog : DialogFragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        setUpClickListeners()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
    }

    private fun setUpClickListeners() {
        binding.link.setOnClickListener {
            val uriUrl: Uri = Uri.parse("http://labo.mg")
            val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
            startActivity(launchBrowser)
        }
        binding.close.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
