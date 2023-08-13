package com.app.myapp.extension


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.inputmethod.InputMethodManager

fun View.visible() {
    visibility = VISIBLE
}

fun View.gone() {
    visibility = GONE
}

fun View.invisible() {
    visibility = INVISIBLE
}

fun View.enabled() {
    isEnabled = true
}

fun View.disabled() {
    isEnabled = false
}

fun Context.hideKeyboard() {
    this as Activity
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun <T> Activity.startNewActivity(
    className: Class<T>,
    finish: Boolean = false,
    bundle: Bundle? = null
) {
    val intent = Intent(this, className)
    bundle?.let {
        intent.putExtras(it)
    }
    startActivity(intent)
    if (finish) {
        finish()
    }

}

fun <T> Activity.startNewActivityWithClearTop(
    className: Class<T>,
    finish: Boolean = false,
    bundle: Bundle? = null
) {
    val intent = Intent(this, className)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    bundle?.let {
        intent.putExtras(it)
    }
    startActivity(intent)
    if (finish) {
        finish()
    }

}




