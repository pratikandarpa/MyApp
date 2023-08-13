package com.app.myapp.ui.home

import com.google.gson.annotations.SerializedName

data class ImageModel(
    @SerializedName("myImages")
    val myImagesList: List<ImageData>
)

data class ImageData(
    @SerializedName("id")
    val id: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("download_url")
    val url: String,
    @SerializedName("date")
    val date: String,
)
