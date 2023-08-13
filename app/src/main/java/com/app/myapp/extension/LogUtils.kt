package com.app.myapp.extension

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

const val TAG = "myApp"
@SuppressLint("LongLogTag")
fun Context.logMessage(tag: String = "myApp", msg: String) {
    Log.e(TAG + tag, "= $msg")
}
