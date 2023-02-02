package com.example.biophonie.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.biophonie.R
import com.example.biophonie.util.setFiltersOnEditText
import com.google.android.material.textfield.TextInputLayout

class TutorialFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tutorial, container, false)
}

class NameFragment: Fragment(){
    lateinit var name: EditText
    lateinit var textInput: TextInputLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_name, container, false)
        textInput = view.findViewById(R.id.name)
        name = view.findViewById(R.id.name_edit_text)
        name.setFiltersOnEditText()
        return view
    }
}