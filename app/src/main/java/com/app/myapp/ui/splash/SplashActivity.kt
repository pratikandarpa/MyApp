package com.app.myapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.app.myapp.BR
import com.app.myapp.R
import com.app.myapp.base.BaseActivity
import com.app.myapp.databinding.ActivitySplashBinding
import com.app.myapp.extension.TAG
import com.app.myapp.ui.home.HomeActivity
import com.app.myapp.ui.login.LoginActivity
import com.app.myapp.util.*
import com.app.myapp.util.alertDialog.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {

    override val layoutId: Int
        get() = R.layout.activity_splash
    override val bindingVariable: Int
        get() = BR.viewmodel

    override fun setupObservable() {

    }

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance();
    }

    @SuppressLint("UnsafeOptInUsageError")
    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser

        lifecycleScope.launch {
            delay(3000L)
            if (currentUser != null) {
                if (currentUser.isEmailVerified) {
                    activity.hideLoaderDialog()

                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(
                            Intent(
                                this@SplashActivity,
                                HomeActivity::class.java
                            )
                        )
                        finishAffinity()
                    }, 3000)
                } else {
                    activity.hideLoaderDialog()
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            LoginActivity::class.java
                        )
                    )
                }
            } else {
                startActivity(
                    Intent(
                        this@SplashActivity,
                        LoginActivity::class.java
                    )
                )
            }
        }
    }
}