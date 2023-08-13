package com.app.myapp.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import com.app.myapp.BR
import com.app.myapp.R
import com.app.myapp.base.BaseActivity
import com.app.myapp.databinding.ActivityLoginBinding
import com.app.myapp.extension.TAG
import com.app.myapp.ui.home.HomeActivity
import com.app.myapp.ui.register.RegisterActivity
import com.app.myapp.util.*
import com.app.myapp.util.alertDialog.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override val layoutId: Int
        get() = R.layout.activity_login
    override val bindingVariable: Int
        get() = BR.viewmodel

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener {
            mViewModel.checkValidation(
                binding.etEmailLogin.text.toString(),
                binding.etPassLogin.text.toString()
            )
        }

        binding.tvGoToRegistration.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    RegisterActivity::class.java
                )
            )
            finishAffinity()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun signIn(mail: String, password: String) {
        activity.showLoaderDialog()
        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val currentUser = mAuth.currentUser
                if (currentUser != null) {
                    if (currentUser.isEmailVerified) {
                        activity.hideLoaderDialog()

                        showSuccessAlertWithoutTitle(
                            message = getString(R.string.login_successful)
                        )

                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    HomeActivity::class.java
                                )
                            )
                            finishAffinity()
                        }, 1000)
                    } else {
                        activity.hideLoaderDialog()
                        showErrorAlert(
                            message = getString(R.string.login_verify_mail)
                        )
                    }
                }
            } else {
                activity.hideLoaderDialog()
                showErrorAlert(
                    message = getString(R.string.login_failed)
                )
            }
        }
    }

    override fun setupObservable() {
        //validation error
        mViewModel.getValidationObservable().observe(this) {
            showErrorAlert(
                message = it
            )
        }

        // signIn
        mViewModel.getIsValidationSuccessObservable().observe(this) {
            signIn(
                binding.etEmailLogin.text.toString(),
                binding.etPassLogin.text.toString()
            )
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        if (currentUser != null) {

        }
    }
}