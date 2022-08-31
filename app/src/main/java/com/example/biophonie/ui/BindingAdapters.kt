package com.example.biophonie.ui

import android.net.Uri
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.text.bold
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.biophonie.R
import com.example.biophonie.network.BASE_URL
import fr.haran.soundwave.ui.PlayerView
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
}*/

@BindingAdapter("uri")
fun setImageUri(view: AppCompatImageView, imageUri: Uri){
    Glide.with(view.context)
        .load(imageUri)
        /*.placeholder(R.drawable.ic_pine)*/
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)
}

@BindingAdapter("url")
fun setImageUrl(view: AppCompatImageView, imageUrl: String?){
    imageUrl?.let {
        Glide.with(view.context)
            .load("$BASE_URL/api/v1/assets/picture/$imageUrl")
            .placeholder(R.drawable.loader) //unnecessary ?
            //.transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}

@BindingAdapter("uri_thumbnail")
fun setImageUriThumbnail(view: AppCompatImageView, imageUri: Uri?){
    imageUri?.let { view.visibility = View.VISIBLE
        setImageUri(view, imageUri) }
}

@BindingAdapter("date")
fun setDate(view: TextView, date: Calendar?) {
    if (date != null)
        view.text = SimpleDateFormat("MMM yyyy", Locale.FRANCE).format(date.time)
}

@BindingAdapter("title")
fun setTitle(view: PlayerView, date: Calendar?) {
    if (date != null)
        with(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(date.time)) {
            view.setText(SpannableStringBuilder()
                .bold { append("Le : ") }
                .append(split("\\s".toRegex())[0])
                .bold { append(" à ") }
                .append(split("\\s".toRegex())[1]))
        }
}

@BindingAdapter("amplitudes")
fun setAmplitudes(view: PlayerView, amplitudes: Array<Double>?) {
    amplitudes?.let { view.setAmplitudes(it) }
}