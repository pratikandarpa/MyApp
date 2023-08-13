package com.app.myapp.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.app.myapp.R

object LoadingDialog {

    var dialog: Dialog? = null

    fun showLoadDialog(context: Context) {
        try {
            if (dialog != null && dialog!!.isShowing) {
                dialog!!.dismiss()
            }
            dialog = Dialog(context)

            dialog!!.apply {
                window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(R.layout.dialog_loading)
                show()
            }
        } catch (e: Exception) {
            dialog = null
            e.printStackTrace()
        }
    }

    fun hideLoadDialog() {
        dialog?.dismiss()
    }

    fun isDialogShowing(): Boolean {
        return dialog!!.isShowing()
    }

}