package fr.labomg.biophonie.feature.addgeopoint

import android.net.Uri
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@BindingAdapter("uri")
fun setImageUri(view: AppCompatImageView, imageUri: Uri?) {
    imageUri?.let {
        Glide.with(view.context)
            .load(it)
            /*.placeholder(R.drawable.ic_pine)*/
            .transition(DrawableTransitionOptions.withCrossFade())
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
