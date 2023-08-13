package com.app.myapp.util

import android.content.Context

fun Context.showLoaderDialog() {
    LoadingDialog.showLoadDialog(this)
}

fun Context.hideLoaderDialog() {
    LoadingDialog.hideLoadDialog()
}




