package fr.labomg.biophonie.ui

import android.net.Uri
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.text.bold
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import fr.labomg.biophonie.R
import fr.labomg.biophonie.data.Resource
import fr.labomg.biophonie.templates
import fr.haran.soundwave.ui.PlayerView
import fr.labomg.biophonie.BuildConfig
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@BindingAdapter("uri")
fun setImageUri(view: AppCompatImageView, imageUri: Uri?){
    imageUri?.let {
        Glide.with(view.context)
            .load(it)
            /*.placeholder(R.drawable.ic_pine)*/
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}

@BindingAdapter("resource")
fun setImageResource(view: AppCompatImageView, picture: Resource?){
    picture?.let {
        val uri = if (it.local == null) {
            "${BuildConfig.BASE_URL}/api/v1/assets/picture/${picture.remote}"
        } else {
            if (templates.containsKey(picture.local)) {
                view.setImageResource(templates[picture.local]!!)
                return
            } else {
                "${picture.local}"
            }
        }
        Glide.with(view.context)
            .load(uri)
            .placeholder(R.drawable.loader)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(android.R.drawable.stat_notify_error)
            .into(view)
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