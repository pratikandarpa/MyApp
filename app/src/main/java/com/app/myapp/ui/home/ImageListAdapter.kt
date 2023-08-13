package com.app.myapp.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.myapp.R
import com.app.myapp.databinding.ItemImageBinding
import com.bumptech.glide.Glide

class ImageListAdapter(
    var activity: Activity,
    private val imageModelList: List<ImageModel>,
    private val selectedImageList: List<ImageListCheckedModel>,
    private var imageListNavigator: ImageListNavigator,
) : RecyclerView.Adapter<ImageListAdapter.ImageHolder>() {

    lateinit var context: Context
    lateinit var binding: ItemImageBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {

        context = parent.context
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_image, parent, false
        )

        return ImageHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: ImageHolder, @SuppressLint("RecyclerView") position: Int
    ) {

        if (selectedImageList.isNotEmpty()) {
            val foundImage = selectedImageList.find { it.index == position }
            holder.binding.myCheckBox.isChecked = foundImage != null && foundImage.isChecked
        } else {
            holder.binding.myCheckBox.isChecked = false
        }

        Glide.with(context).load(imageModelList[0].myImagesList[position].url)
            .placeholder(R.mipmap.ic_launcher).into(holder.binding.imageView)

        holder.binding.myCheckBox.setOnClickListener {
            imageListNavigator.onImageSelect(
                position = position, isChecked = holder.binding.myCheckBox.isChecked
            )
        }
    }

    override fun getItemCount(): Int {
        return imageModelList[0].myImagesList.size
    }

    class ImageHolder(var binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)
}