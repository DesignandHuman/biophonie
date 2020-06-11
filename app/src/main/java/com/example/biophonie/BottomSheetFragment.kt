package com.example.biophonie

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.biophonie.api.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class BottomSheetFragment(private var soundName: String) : Fragment() {

    private val TAG: String? = "BottomSheetFragment:"
    private lateinit var mListener: SoundSheetListener
    private lateinit var location: TextView
    private lateinit var date: TextView
    private lateinit var coords: TextView
    private lateinit var close: ImageView
    private lateinit var waveForm: ImageView
    private lateinit var left: TextView
    private lateinit var datePicker: TextView
    private lateinit var right: TextView
    private lateinit var seePicture: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        bottomSheetBehavior = BottomSheetBehavior.from(view)

        location = view.findViewById(R.id.location)
        date = view.findViewById(R.id.date)
        coords = view.findViewById(R.id.coordinates)

        close = view.findViewById(R.id.close)
        close.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }

        waveForm = view.findViewById(R.id.wave_form)
        waveForm.setOnClickListener { Toast.makeText(view.context, "Lecture du son", Toast.LENGTH_SHORT).show() }

        left = view.findViewById(R.id.left)
        left.setOnClickListener {Toast.makeText(view.context,"Not implemented yet", Toast.LENGTH_SHORT).show() }

        datePicker = view.findViewById(R.id.date_picker)

        right = view.findViewById(R.id.right)
        right.setOnClickListener {Toast.makeText(view.context,"Not implemented yet", Toast.LENGTH_SHORT).show() }

        seePicture = view.findViewById(R.id.see_picture)
        seePicture.setOnClickListener { Toast.makeText(view.context, "Affichage de la photo", Toast.LENGTH_SHORT).show() }
        progressBar = view.findViewById(R.id.progress_bar)
        show(soundName)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        changeWidgetsVisibility(false)
        val api: ApiInterface = ApiClient().createService(ApiInterface::class.java)
        val call: Call<SoundResponse> = api.getSound(id)
        call.enqueue(object: Callback<SoundResponse>{
            override fun onResponse(call: Call<SoundResponse>, response: Response<SoundResponse>) {
                if (response.isSuccessful){
                    val sound = response.body()
                    // TODO not implemented yet)
                    //location.text = sound.toString()
                    changeWidgetsVisibility(true)
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

    private fun changeWidgetsVisibility(makeVisible: Boolean){
        if (makeVisible){
            location.visibility = View.VISIBLE
            date.visibility = View.VISIBLE
            coords.visibility = View.VISIBLE
            close.visibility = View.VISIBLE
            waveForm.visibility = View.VISIBLE
            left.visibility = View.VISIBLE
            datePicker.visibility = View.VISIBLE
            right.visibility = View.VISIBLE
            seePicture.visibility = View.VISIBLE

            progressBar.visibility = View.GONE
        }
        else{
            location.visibility = View.GONE
            date.visibility = View.GONE
            coords.visibility = View.GONE
            close.visibility = View.GONE
            waveForm.visibility = View.GONE
            left.visibility = View.GONE
            datePicker.visibility = View.GONE
            right.visibility = View.GONE
            seePicture.visibility = View.GONE

            progressBar.visibility = View.VISIBLE
        }
    }

}
