package com.app.myapp.ui.register

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.myapp.R
import com.app.myapp.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RegisterViewModel @Inject constructor(
    var context: Context,
) : BaseViewModel<Any>(
) {
    var userName: String = ""
    var email: String = ""
    var password: String = ""

    private val validationMsg: MutableLiveData<String> =
        MutableLiveData()

    fun getValidationObservable(): LiveData<String> {
        return validationMsg
    }

    private val isValidationSuccess: MutableLiveData<Boolean> =
        MutableLiveData()

    fun getIsValidationSuccessObservable(): LiveData<Boolean> {
        return isValidationSuccess
    }

     fun checkValidation(pickedImgUri: Uri?, name: String, mail: String, password: String) {
        if (pickedImgUri == null) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.please_select_the_photo)
                }
            }
        } else if (name.isEmpty()) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.enter_name)
                }
            }
        } else if (mail.isEmpty()) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.enter_email)
                }
            }
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mail)
                .matches()
        ) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.enter_valid_email)
                }
            }
        } else if (password.isEmpty()) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.enter_password)
                }
            }
        } else if (password.length < 7) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.enter_valid_password)
                }
            }

        } else {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    isValidationSuccess.value = true
                }
            }
        }
    }
}