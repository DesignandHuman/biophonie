package com.example.biophonie

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.biophonie.api.ApiClient
import com.example.biophonie.api.ApiError
import com.example.biophonie.api.ApiInterface
import com.example.biophonie.api.ErrorUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class BottomSheetFragment(private var soundName: String) : Fragment() {

    private val TAG: String? = "BottomSheetFragment:"
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
        getSound()
        textView.text = id
    }

    private fun getSound() {
        val client: ApiClient =
            ApiClient()
        val api: ApiInterface = client.createService(ApiInterface::class.java)
        val call: Call<SoundResponse> = api.getSound("sound")
        call.enqueue(object: Callback<SoundResponse>{
            override fun onResponse(call: Call<SoundResponse>, response: Response<SoundResponse>) {
                if (response.isSuccessful){
                    val sound = response.body()
                    // TODO(not implemented yet)
                    textView.text = sound.toString()
                } else {
                    val error: ApiError? = ErrorUtils().parseError(response)
                    if (error == null)
                        Toast.makeText(context, "Erreur : " + response.code(), Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Erreur : " + error.statusCode + " " + error.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SoundResponse>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(context, "Problème réseau. Assurez-vous d'avoir une connection Internet suffisante", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "Erreur de conversion des données", Toast.LENGTH_SHORT).show();
                }
            }
        })
    }
}