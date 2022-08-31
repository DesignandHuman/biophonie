package com.example.biophonie.ui

import android.text.SpannableStringBuilder
import android.widget.TextView
import androidx.core.text.bold
import androidx.databinding.BindingAdapter
import fr.haran.soundwave.ui.PlayerView
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("date")
fun date(view: TextView, date: Calendar?) {
    if (date != null)
        view.text = SimpleDateFormat("MMM yyyy", Locale.FRANCE).format(date.time)
}

@BindingAdapter("title")
fun title(view: PlayerView, date: Calendar?) {
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
fun amplitudes(view: PlayerView, amplitudes: Array<Double>?) {
    amplitudes?.let { view.setAmplitudes(it) }
}