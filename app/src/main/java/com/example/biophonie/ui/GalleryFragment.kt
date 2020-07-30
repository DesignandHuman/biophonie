package com.example.biophonie.ui

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
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
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biophonie.R
import com.example.biophonie.databinding.FragmentGalleryBinding
import com.example.biophonie.domain.DialogAdapterItem
import com.example.biophonie.domain.Landscape
import com.example.biophonie.util.dpToPx

private const val TAG = "GalleryFragment"
private const val REQUEST_CAMERA = 0
private const val REQUEST_GALLERY = 1
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
            addItemDecoration(GridItemDecoration(requireActivity(), 20, 2))
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
        binding.importPicture.setOnClickListener {
            setUpDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageIntent: Intent?) {
        if (resultCode == RESULT_OK){
            when(requestCode){
                REQUEST_CAMERA -> {
                    val imageBitmap = imageIntent?.extras?.get("data") as Bitmap
                    binding.landscape.setImageBitmap(imageBitmap)
                }
                REQUEST_GALLERY -> binding.landscape.setImageURI(imageIntent?.data)
            }
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

    class ChooseMeanDialog(context: Context, items: Array<DialogAdapterItem>) : DialogFragment() {
        private val adapter: ListAdapter = DialogListAdapter(
            context,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            items)

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let { it ->
                val builder = AlertDialog.Builder(it, R.style.AlertDialogIBM)
                builder.setTitle("Importer depuis")
                builder.setAdapter(adapter){ _: DialogInterface, choice: Int ->
                    when(choice){
                        REQUEST_CAMERA -> Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                                startActivityForResult(takePictureIntent, REQUEST_CAMERA)
                            }
                        }
                        REQUEST_GALLERY -> Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        ).also { startActivityForResult(it, REQUEST_GALLERY) }
                    }
                }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }

    private fun setUpDialog() {
        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).also { startActivityForResult(it, REQUEST_GALLERY) }
        } else {
            val items = arrayOf(DialogAdapterItem("Appareil photo", R.drawable.photo_camera),
                DialogAdapterItem("Galerie", R.drawable.photo_library))
            activity?.supportFragmentManager?.let { ChooseMeanDialog(requireContext(),items).show(it, "dialog") }
        }
    }

    override fun onLandscapeClick(position: Int) {
        binding.landscape.setImageDrawable(listLandscapes[position].image)
    }

    class GridItemDecoration(context: Context, space: Int = 10, private val spanCount: Int) : RecyclerView.ItemDecoration() {

        private val spaceInDp = dpToPx(context, space)

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            outRect.left = spaceInDp
            outRect.right = spaceInDp
            outRect.bottom = 0
            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) < spanCount)
                outRect.top = 0
            else
                outRect.top = spaceInDp
            if(parent.getChildAdapterPosition(view) % spanCount == 0) {
                outRect.right = spaceInDp/2
                outRect.left = spaceInDp
            } else {
                outRect.right = spaceInDp
                outRect.left = spaceInDp/2
            }
        }
    }
}