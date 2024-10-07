package fr.labomg.biophonie.core.ui

import android.net.Uri
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.text.bold
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.textfield.TextInputLayout
import fr.haran.soundwave.ui.PlayerView
import fr.labomg.biophonie.core.assets.templates
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@BindingAdapter("uri")
fun setImageUri(view: AppCompatImageView, imageUri: Uri?) {
    imageUri?.let {
        Glide.with(view.context)
            .load(it)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(android.R.drawable.stat_notify_error)
            .into(view)
    }
}

@BindingAdapter("uri_thumbnail")
fun setImageUriThumbnail(view: AppCompatImageView, imageUri: Uri?) {
    imageUri?.let {
        view.visibility = View.VISIBLE
        setImageUri(view, imageUri)
    }
}

@BindingAdapter("date")
fun setDate(view: TextView, date: Instant?) {
    if (date != null)
        view.text =
            DateTimeFormatter.ofPattern("MMM yyyy")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.of("UTC"))
                .format(date)
}

@BindingAdapter("title")
fun setTitle(view: PlayerView, date: Instant?) {
    if (date != null)
        with(
            DateTimeFormatter.ofPattern("d/MM/uuuu H:mm")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.of("UTC"))
                .format(date)
        ) {
            view.setText(
                SpannableStringBuilder()
                    .bold { append("Le : ") }
                    .append(split("\\s".toRegex())[0])
                    .bold { append(" à ") }
                    .append(split("\\s".toRegex())[1])
            )
        }
}

@BindingAdapter("resource")
fun setImageResource(view: AppCompatImageView, picture: String?) {
    val baseName = picture?.substringBefore(".")
    picture?.let {
        if (templates.containsKey(baseName)) {
            view.setImageResource(templates[baseName]!!)
            return
        } else {
            var uri = it
            if (!uri.contains("/")) {
                uri = "${BuildConfig.BASE_URL}/api/v1/assets/picture/${uri}"
            }
            Glide.with(view.context)
                .load(uri)
                .placeholder(R.drawable.loader)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(android.R.drawable.stat_notify_error)
                .into(view)
        }
    }
}

@BindingAdapter("amplitudes")
fun setAmplitudes(view: PlayerView, amplitudes: List<Float>?) {
    amplitudes?.let { view.setAmplitudes(amplitudes) }
}

@BindingAdapter("errorText")
fun setErrorText(view: TextInputLayout, text: String?) {
    view.error = text
}
