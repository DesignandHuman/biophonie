package com.example.biophonie

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior


class BottomSheetFragment(private var soundName: String) : Fragment() {

    private lateinit var mListener: SoundSheetListener
    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        textView = view.findViewById(R.id.textView)
        show(soundName)
        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(view)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val button1: Button = view.findViewById(R.id.button1)
        button1.setOnClickListener {
            mListener.onButtonClicked("Button 1 clicked")
        }
        return view
    }

    interface SoundSheetListener{
        fun onButtonClicked(text: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as SoundSheetListener
        } catch (e: ClassCastException){
            throw kotlin.ClassCastException("$context must implement BottomSheetListener")
        }

    }

    /**
     * Display the sound inside the fragment
     *
     * @param id name of the sound to be requested
     */
    fun show(id: String){
        textView.text = id
    }
}