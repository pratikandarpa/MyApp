package com.app.myapp.extension

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.ProgressBar
import com.app.myapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener

@SuppressLint("ResourceType")
fun ImageView.loadImage(
    path: String,
    placeholder: Int = R.drawable.ic_round_cancel,
    progressBar: ProgressBar
) {
    progressBar.visible()
    Glide.with(context)
        .load(path)
        .listener(object : RequestListener<Drawable?> {

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean,
            ): Boolean {
                progressBar.gone()
                return false
            }

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable?>?,
                isFirstResource: Boolean,
            ): Boolean {
                progressBar.gone()
                return false
            }
        })
        .error(placeholder)
        .into(this)

    /*Glide.with(context)
        .load(path)
        .placeholder(R.drawable.progressbar_animation)
        .error(placeholder)
        .into(this)*/



}
