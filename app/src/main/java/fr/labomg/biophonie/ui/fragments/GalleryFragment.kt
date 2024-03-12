package fr.labomg.biophonie.ui.fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.labomg.biophonie.R
import fr.labomg.biophonie.REQUEST_CAMERA
import fr.labomg.biophonie.REQUEST_GALLERY
import fr.labomg.biophonie.data.DialogAdapterItem
import fr.labomg.biophonie.data.Landscape
import fr.labomg.biophonie.databinding.FragmentGalleryBinding
import fr.labomg.biophonie.templates
import fr.labomg.biophonie.ui.LandscapesAdapter
import fr.labomg.biophonie.ui.MarginItemDecoration
import fr.labomg.biophonie.viewmodels.RecViewModel
import timber.log.Timber

class GalleryFragment :
    Fragment(), LandscapesAdapter.OnLandscapeListener, ChooseMeanDialog.ChooseMeanListener {

    private val viewModel: RecViewModel by activityViewModels {
        RecViewModel.ViewModelFactory(requireActivity().application!!)
    }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) viewModel.pictureResult() else Timber.i("onChoiceClick: could not take picture")
        }
    private val fetchGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            viewModel.pictureResult(it)
        }

    private var _binding: FragmentGalleryBinding? = null
    private val binding
        get() = _binding!!

    private var viewManager: LinearLayoutManager? = null
    private var viewAdapter: LandscapesAdapter? = null
    private lateinit var defaultLandscapes: List<Landscape>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setLiveDataObservers()
        setClickListeners()
        setUpRecyclerView()
        return binding.root
    }

    private fun setLiveDataObservers() {
        viewModel.toast.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(requireContext(), it.message, it.length).show()
                viewModel.onToastDisplayed()
            }
        }
        viewModel.fromDefault.observe(viewLifecycleOwner) {
            if (it) {
                viewAdapter?.apply {
                    selectedPosition = viewModel.currentId
                    notifyItemChanged(selectedPosition)
                }
                binding.thumbnail.isSelected = false
            } else {
                viewAdapter?.apply {
                    val previousPosition = selectedPosition
                    selectedPosition = RecyclerView.NO_POSITION
                    notifyItemChanged(previousPosition)
                }
                binding.thumbnail.isSelected = true
            }
        }
    }

    private fun setUpRecyclerView() {
        val templateNames = resources.getStringArray(R.array.template_names)
        defaultLandscapes =
            templateNames.mapIndexed { index, id ->
                Landscape(templates.values.elementAt(index), id)
            }
        viewManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        viewAdapter = LandscapesAdapter(defaultLandscapes, this)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(
                MarginItemDecoration(resources.getDimensionPixelOffset(R.dimen.gallery_simple_margin))
            )
        }
    }

    private fun setClickListeners() {
        binding.apply {
            okButton.ok.setOnClickListener { view: View ->
                view.findNavController().navigate(R.id.action_galleryFragment_to_titleFragment)
            }
            topPanel.close.setOnClickListener { activity?.finish() }
            topPanel.previous.setOnClickListener { activity?.onBackPressed() }
            importPicture.setOnClickListener { getOriginOfLandscape() }
            thumbnail.setOnClickListener { viewModel?.restorePreviewFromThumbnail() }
        }
    }

    private fun getOriginOfLandscape() {
        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            getPicture(REQUEST_GALLERY)
        } else {
            val items =
                arrayOf(
                    DialogAdapterItem("Appareil photo", R.drawable.ic_camera),
                    DialogAdapterItem("Galerie", R.drawable.ic_library)
                )
            activity?.supportFragmentManager?.let {
                ChooseMeanDialog(requireContext(), items, this).show(it, "dialog")
            }
        }
    }

    private fun getPicture(choice: Int) {
        when (choice) {
            REQUEST_CAMERA -> {
                val uri = viewModel.getCaptureUri()
                if (uri != null) takePicture.launch(uri)
            }
            REQUEST_GALLERY -> {
                fetchGallery.launch("image/*")
            }
        }
    }

    override fun onLandscapeClick(position: Int) {
        viewModel.onClickDefault(position)
    }

    override fun onChoiceClick(choice: Int) {
        getPicture(choice)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewManager = null
        viewAdapter = null
    }
}

class ChooseMeanDialog(
    context: Context,
    items: Array<DialogAdapterItem>,
    private var listener: ChooseMeanListener
) : DialogFragment() {

    interface ChooseMeanListener {
        fun onChoiceClick(choice: Int)
    }

    private val adapter: ListAdapter =
        DialogListAdapter(context, android.R.layout.select_dialog_item, android.R.id.text1, items)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.AlertDialogIBM)
            builder.setTitle("Importer depuis")
            builder.setAdapter(adapter) { _: DialogInterface, choice: Int ->
                listener.onChoiceClick(choice)
            }
            builder.create()
        } ?: error("Activity cannot be null")
    }
}

class DialogListAdapter(
    private val adapterContext: Context,
    dialogLayout: Int,
    private val textLayout: Int,
    private val items: Array<DialogAdapterItem>
) : ArrayAdapter<DialogAdapterItem>(adapterContext, dialogLayout, textLayout, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getView(position, convertView, parent)
        v.findViewById<TextView>(textLayout).apply {
            setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0)
            compoundDrawablePadding =
                (DENSITY_MULTIPLIER * adapterContext.resources.displayMetrics.density +
                        DENSITY_OFFSET)
                    .toInt()
            setTextAppearance(R.style.MyTextAppearance)
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                adapterContext.resources.getDimension(R.dimen.button_font_size)
            )
        }
        return v
    }

    companion object {
        private const val DENSITY_MULTIPLIER = 10
        private const val DENSITY_OFFSET = 0.5f
    }
}
