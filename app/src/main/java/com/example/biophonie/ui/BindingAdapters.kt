package com.example.biophonie.ui

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.text.bold
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.biophonie.R
import com.example.biophonie.domain.Resource
import com.example.biophonie.network.BASE_URL
import fr.haran.soundwave.ui.PlayerView
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

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

val templates: Map<String, Int> = mapOf(
    "forest" to R.drawable.forest,
    "swamp" to R.drawable.swamp,
    "sea" to R.drawable.sea,
    "mountain" to R.drawable.mountain
)

@BindingAdapter("uri")
fun setImageUri(view: AppCompatImageView, imageUri: Uri){
    Glide.with(view.context)
        .load(imageUri)
        /*.placeholder(R.drawable.ic_pine)*/
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)
}

@BindingAdapter("resource")
fun setImageResource(view: AppCompatImageView, picture: Resource?){
    picture?.let {
        if (templates.containsKey(picture.local)) {
            view.setImageResource(templates[picture.local]!!)
        } else {
            Drawable.createFromPath(picture.local)?.let { view.setImageDrawable(it) }
                ?.run {
                    Glide.with(view.context)
                        .load("$BASE_URL/api/v1/assets/picture/${picture.remote}")
                        .placeholder(R.drawable.loader) //unnecessary ?
                        //.transition(DrawableTransitionOptions.withCrossFade())
                        .error(android.R.drawable.stat_notify_error)
                        .into(view)
                }
        }
    }
}

@BindingAdapter("uri_thumbnail")
fun setImageUriThumbnail(view: AppCompatImageView, imageUri: Uri?){
    imageUri?.let { view.visibility = View.VISIBLE
        setImageUri(view, imageUri) }
}

@BindingAdapter("date")
fun setDate(view: TextView, date: Instant?) {
    if (date != null)
        view.text = DateTimeFormatter
            .ofPattern("MMM yyyy")
            .withLocale(Locale.getDefault())
            .withZone( ZoneId.of("UTC"))
            .format(date)
}

@BindingAdapter("title")
fun setTitle(view: PlayerView, date: Instant?) {
    if (date != null)
        with(DateTimeFormatter
            .ofPattern("d/MM/uuuu H:mm")
            .withLocale(Locale.getDefault())
            .withZone( ZoneId.of("UTC"))
            .format(date)) {
            view.setText(SpannableStringBuilder()
                .bold { append("Le : ") }
                .append(split("\\s".toRegex())[0])
                .bold { append(" à ") }
                .append(split("\\s".toRegex())[1]))
        }
}

@BindingAdapter("amplitudes")
fun setAmplitudes(view: PlayerView, amplitudes: List<Float>?) {
    amplitudes?.let { view.setAmplitudes(amplitudes) }
}