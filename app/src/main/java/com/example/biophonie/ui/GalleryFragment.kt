package com.example.biophonie.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentGalleryBinding
import com.example.biophonie.domain.DialogAdapterItem
import com.example.biophonie.domain.Landscape
import com.example.biophonie.viewmodels.REQUEST_CAMERA
import com.example.biophonie.viewmodels.REQUEST_GALLERY
import com.example.biophonie.viewmodels.RecViewModel
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "GalleryFragment"
class GalleryFragment : Fragment(),
    LandscapesAdapter.OnLandscapeListener,
    ChooseMeanDialog.ChooseMeanListener {

    private val viewModel: RecViewModel by activityViewModels{
        RecViewModel.ViewModelFactory(requireActivity().application!!)
    }

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
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setLiveDataObservers()
        setClickListeners()
        setUpRecyclerView()
        return binding.root
    }

    private fun setLiveDataObservers() {
        viewModel.activityIntent.observe(viewLifecycleOwner, Observer {
            it?.let {
                startActivityForResult(it.intent, it.requestCode)
                viewModel.onRequestActivityStarted()
            }
        })
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
            addItemDecoration(GridItemDecoration(requireActivity(), 20, 2))
        }
    }

    private fun setClickListeners() {
        binding.apply {
            okButton.ok.setOnClickListener { view: View ->
                view.findNavController().navigate(R.id.action_galleryFragment_to_titleFragment)
            }
            topPanel.close.setOnClickListener {
                activity?.finish()
            }
            topPanel.previous.setOnClickListener {
                activity?.onBackPressed()
            }
            importPicture.setOnClickListener {
                getOriginOfLandscape()
            }
            /*thumbnail.setOnClickListener {
                //TODO fix selected photo inside recyclerview
                if (currentPhotoPath != null) {
                    landscape.setImageDrawable(Drawable.createFromPath(currentPhotoPath))
                    return@setOnClickListener
                }
                if (currentUri != null)
                    landscape.setImageURI(currentUri)
            }*/
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageIntent: Intent?) {
        Log.d(TAG, "onActivityResult: $resultCode")
        viewModel.activityResult(requestCode, resultCode, imageIntent) // Might be bad practice
    }

    private fun getOriginOfLandscape() {
        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            viewModel.dispatchTakePictureIntent(REQUEST_GALLERY)
        } else {
            val items = arrayOf(DialogAdapterItem("Appareil photo", R.drawable.photo_camera),
                DialogAdapterItem("Galerie", R.drawable.photo_library))
            activity?.supportFragmentManager?.let { ChooseMeanDialog(requireContext(),items,this).show(it, "dialog") }
        }
    }

    override fun onLandscapeClick(position: Int) {
        binding.landscape.setImageDrawable(listLandscapes[position].image)
    }

    override fun onChoiceClick(choice: Int) {
        viewModel.dispatchTakePictureIntent(choice)
    }
}

class ChooseMeanDialog(context: Context, items: Array<DialogAdapterItem>, private var listener: ChooseMeanListener) : DialogFragment() {

    interface ChooseMeanListener {
        fun onChoiceClick(choice: Int)
    }

    private val adapter: ListAdapter = DialogListAdapter(
        context,
        android.R.layout.select_dialog_item,
        android.R.id.text1,
        items
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it, R.style.AlertDialogIBM)
            builder.setTitle("Importer depuis")
            builder.setAdapter(adapter){ _: DialogInterface, choice: Int ->
                listener.onChoiceClick(choice)
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class DialogListAdapter(private val adapterContext: Context,
                        dialogLayout: Int,
                        private val textLayout: Int,
                        private val items: Array<DialogAdapterItem>) :
    ArrayAdapter<DialogAdapterItem>(adapterContext, dialogLayout, textLayout, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getView(position, convertView, parent)
        v.findViewById<TextView>(textLayout).apply{
            setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0)
            compoundDrawablePadding = (10 * adapterContext.resources.displayMetrics.density + 0.5f).toInt()
            setTextAppearance(R.style.MyTextAppearance)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, adapterContext.resources.getDimension(R.dimen.button_font_size))
        }
        return v
    }
}