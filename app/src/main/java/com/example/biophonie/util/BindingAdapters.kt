package com.example.biophonie.util

import android.net.Uri
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions


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

private const val TAG = "BindingAdapters"
@BindingAdapter("uri")
fun setImageUri(view: AppCompatImageView, imageUri: Uri){
    Glide.with(view.context)
        .load(imageUri)
        /*.placeholder(R.drawable.ic_pine)*/
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)
}

@BindingAdapter("uri_thumbnail")
fun setImageUriThumbnail(view: AppCompatImageView, imageUri: Uri?){
    imageUri?.let { view.visibility = View.VISIBLE
        setImageUri(view, imageUri) }
}