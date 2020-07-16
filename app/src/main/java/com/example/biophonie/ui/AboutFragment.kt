package com.example.biophonie.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.biophonie.R
import com.example.biophonie.databinding.AboutLayoutBinding


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
        setUpClickListeners()
        return binding.root
    }

    private fun setUpClickListeners() {
        binding.link.setOnClickListener {
            val uriUrl: Uri = Uri.parse("http://labo.mg")
            val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
            startActivity(launchBrowser)
        }
        binding.close.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.remove(this)?.commitAllowingStateLoss()
        }
    }
}
