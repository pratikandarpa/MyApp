package com.app.myapp.util.alertDialog

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.myapp.R
import com.app.myapp.util.SnacyAlert

private var isShown = false

fun Context.alertDialog(
    title: String = resources.getString(R.string.app),
    message: String? = " ",
    isCancelable: Boolean = false,
    positiveBtnText: String = resources.getString(R.string.ok),
    negativeBtnText: String? = null,
    negativeClick: (() -> Unit)? = null,
    positiveClick: (() -> Unit)? = null,
    positiveBtnTextColor: Boolean = false,
    isInit: Boolean = false,
) {
    var builder = AlertDialog.Builder(this)
    builder.setTitle(title)
//    builder.setMessage(if (isInit) message else BaseActivity.language?.getLanguage(message) ?: "")
    builder.setMessage(if (isInit) message else message ?: "")
    builder.setCancelable(isCancelable)
    builder.setPositiveButton(if (isInit) positiveBtnText else positiveBtnText) { _, _ -> positiveClick?.invoke() }
    negativeBtnText?.let {
        builder.setNegativeButton(if (isInit) negativeBtnText else negativeBtnText) { _, _ -> negativeClick?.invoke() }
    }
    val alertDialog = builder.create()
    alertDialog.show()
    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        .setTextColor(ContextCompat.getColor(this, R.color.theme))
    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        .setTextColor(ContextCompat.getColor(this, R.color.grey))

}


fun Activity.showErrorAlert(
    message: String = " ",
    title: String? = getString(R.string.required),
    bgColor: Int = R.color.red,
    icon: Int = R.drawable.ic_baseline_required_24,
) {
    if (isShown){
        //do something
        return
    }
    isShown = true;

        SnacyAlert.create(this)
            .setText(message)
//        .setTitle(title.toString())
            .setBackgroundColorRes(bgColor)
            .setDuration(1500)
            .showIcon(true)
            .setIcon(icon)
            .show()
    Handler().postDelayed(
        Runnable {
            isShown = false
        },
        1500
    )
}

fun Activity.showSuccessAlertWithoutTitle(message: String = "") {
    SnacyAlert.create(this)
        .setText(message)
        .setTitle("")
        .setBackgroundColorRes(R.color.green)
        .setDuration(1500)
        .showIcon(true)
        .setIcon(R.drawable.ic_round_check_circle)
        .show()
}

fun Context.showGPermissionNeeded() {
    alertDialog(
        title = (getString(R.string.access_to_the_photo_gallery)),
        message = (getString(R.string.provide_access_to_photo_gallery)),
        positiveBtnText = (getString(R.string.settings)),
        negativeBtnText = (getString(R.string.cancel)),
        positiveClick = {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        },
        negativeClick = { null },
        isCancelable = true
    )
}

fun Context.checkImageGalleryPermission(): Boolean {
    return ActivityCompat.checkSelfPermission(
        applicationContext,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) != PackageManager.PERMISSION_GRANTED

}




