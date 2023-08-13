package com.app.myapp.ui.splash

import android.content.Context
import com.app.myapp.base.BaseViewModel
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    var context: Context,
) : BaseViewModel<Any>(
) {}