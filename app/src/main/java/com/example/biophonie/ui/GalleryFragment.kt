package com.example.biophonie.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.KeyEventDispatcher.dispatchKeyEvent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentGalleryBinding
import com.example.biophonie.domain.Landscape

class GalleryFragment : Fragment(), LandscapesAdapter.OnLandscapeListener {
    private lateinit var binding: FragmentGalleryBinding
    private lateinit var viewManager: GridLayoutManager
    private lateinit var viewAdapter: LandscapesAdapter
    private lateinit var listLandscapes: List<Landscape>

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
        setUpRecyclerView()
        return binding.root
    }

    private fun setUpRecyclerView(){
        listLandscapes = listOf(Landscape(resources.getDrawable(R.drawable.france, activity?.theme), "Forêt"),
            Landscape(resources.getDrawable(R.drawable.gabon, activity?.theme), "Plaine"),
                Landscape(resources.getDrawable(R.drawable.japon, activity?.theme), "Montagne"),
                Landscape(resources.getDrawable(R.drawable.russie, activity?.theme), "Rivière")
        )
        viewManager = GridLayoutManager(context,2)
        viewAdapter = LandscapesAdapter(listLandscapes, this)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
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

    override fun onLandscapeClick(position: Int) {
        binding.landscape.setImageDrawable(listLandscapes[position].image)
    }
}