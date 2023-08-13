package com.app.myapp.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.myapp.R
import com.app.myapp.base.BaseViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    var context: Context,
) : BaseViewModel<ImageListNavigator>(
) {

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

    private val allImageSelected: MutableLiveData<Boolean> =
        MutableLiveData()

    fun getSelectAllImagesObservable(): LiveData<Boolean> {
        return allImageSelected
    }

    private val imageListDataResponse: MutableLiveData<ImageModel> =
        MutableLiveData()

    fun getImageListObservable(): LiveData<ImageModel> {
        return imageListDataResponse
    }

    fun selectAllImages() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                allImageSelected.value = true
            }
        }
    }

    /* Reads image data from a JSON file in the assets folder and processes it. */
    fun getImageData() {
        lateinit var jsonString: String
        try {
            jsonString = context.assets.open("data.txt")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }

        val gson = Gson()
        val myModel: ImageModel = gson.fromJson(jsonString, ImageModel::class.java)
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                imageListDataResponse.value = myModel
            }
        }
    }

    fun filterDate(fromDate: String, toDate: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (fromDate.isEmpty()) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.select_from_date)
                }
            }
        } else if (toDate.isEmpty()) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.select_to_date)
                }
            }
        } else if (dateFormat.parse(toDate)!! <= dateFormat.parse(fromDate)) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    validationMsg.value = context.getString(R.string.to_date_from_date)
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