package com.example.biophonie.util

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.example.biophonie.domain.dateAsCalendar
import java.text.SimpleDateFormat
import java.util.*

//TODO(use?)
/*
@BindingAdapter("isNetworkError", "playlist")
fun hideIfNetworkError(view: View, isNetWorkError: Boolean, playlist: Any?) {
    view.visibility = if (playlist != null) View.GONE else View.VISIBLE

    if(isNetWorkError) {
        view.visibility = View.GONE
    }
}

@BindingAdapter("setDate")
fun TextView.setDate(date: String?) {
    Log.d(TAG, "setDate: $date")
    date?.let {
        val calendar: Calendar = dateAsCalendar(date)
        text = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(calendar.time)
        Log.d(TAG, "setDate: $text")
    }
}*/

@BindingAdapter("uri")
fun setImageUri(view: AppCompatImageView, imageUri: Uri){
    //TODO display a preview instead of the full picture
    view.setImageURI(imageUri)
}

@BindingAdapter("uri_preview")
fun setImageUriPreview(view: AppCompatImageView, imageUri: Uri){
    //TODO display a preview instead of the full picture
    view.setImageURI(imageUri)
}