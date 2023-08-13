package com.app.myapp.data.Response

import com.google.gson.annotations.SerializedName

data class UserDataResponse(

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    ) : BaseResponse() {
}