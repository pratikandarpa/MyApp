package com.app.myapp.ui.register

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.app.myapp.BR
import com.app.myapp.R
import com.app.myapp.base.BaseActivity
import com.app.myapp.databinding.ActivityRegisterBinding
import com.app.myapp.ui.login.LoginActivity
import com.app.myapp.util.alertDialog.showErrorAlert
import com.app.myapp.util.alertDialog.showGPermissionNeeded
import com.app.myapp.util.alertDialog.showSuccessAlertWithoutTitle
import com.app.myapp.util.hideLoaderDialog
import com.app.myapp.util.showLoaderDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterViewModel>() {

    override val layoutId: Int
        get() = R.layout.activity_register
    override val bindingVariable: Int
        get() = BR.viewmodel
    private lateinit var auth: FirebaseAuth

    private lateinit var fStore: FirebaseFirestore
    val REQUEST_GALLARY = 1
    private var imageOptionSelect = -1

    private var pickedImgUri: Uri? = null

    override fun setupObservable() {
        //validation error
        mViewModel.getValidationObservable().observe(this) {
            showErrorAlert(
                message = it
            )
        }

        // signIn
        mViewModel.getIsValidationSuccessObservable().observe(this) {
            signIn()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        fStore = FirebaseFirestore.getInstance()

        binding.tvUpload.setOnClickListener {
            checkPermission()
        }

        binding.imageProfileLogin.setOnClickListener {
            checkPermission()
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(
                Intent(
                    this@RegisterActivity, LoginActivity::class.java
                )
            )
            finishAffinity()
        }

        binding.btnRegister.setOnClickListener {
            mViewModel.checkValidation(
                pickedImgUri, binding.etName.text.toString(),
                binding.etEmailRegister.text.toString(),
                binding.etPassLogin.text.toString()
            )
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activityResultLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
            activityResultLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun signIn() {
        activity.showLoaderDialog()
        auth.createUserWithEmailAndPassword(
            binding.etEmailRegister.text.toString(), binding.etPassLogin.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information

                val userID = auth.currentUser?.uid
                val documentReference = fStore.collection("user_profile").document(userID!!)
                val user: MutableMap<String, Any> = HashMap()
                user["Name"] = binding.etName.text.toString()
                user["Email"] = binding.etEmailRegister.text.toString()
                user["Password"] = binding.etPassLogin.text.toString()

                documentReference.set(user).addOnSuccessListener {

                }.addOnFailureListener { e ->

                }

                // after we created the user account we need to update his profile picture and name
                // check if the user photo is picked or not
                if (pickedImgUri != null) {
                    auth.currentUser?.let { it1 ->
                        updateUserInfo(
                            binding.etName.text.toString(), pickedImgUri!!, it1
                        )
                    }
                }
            } else {
                activity.hideLoaderDialog()
                showErrorAlert(
                    message = getString(R.string.authentication_failed) + task.exception?.message.toString()
                )
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun updateUserInfo(name: String, pickedImgUri: Uri, currentUser: FirebaseUser) {
        // First, upload user photo to Firebase storage and get URL
        val mStorage = FirebaseStorage.getInstance().reference.child("users_photos")
        val imageFilePath = mStorage.child(pickedImgUri.lastPathSegment!!)
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener { taskSnapshot ->
            // Image uploaded successfully, get image URL
            imageFilePath.downloadUrl.addOnSuccessListener { uri ->
                // URI contains the user image URL
                val profileUpdate =
                    UserProfileChangeRequest.Builder().setDisplayName(name).setPhotoUri(uri).build()

                currentUser.updateProfile(profileUpdate).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        currentUser.sendEmailVerification().addOnCompleteListener { task ->
                            activity.hideLoaderDialog()
                            if (task.isSuccessful) {
                                showSuccessAlertWithoutTitle(
                                    message = getString(R.string.mail_verification)
                                )

                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(
                                        Intent(
                                            this@RegisterActivity, LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }, 3000)
                            } else {
                                showErrorAlert(
                                    message = getString(R.string.authentication_failed) + task.exception?.message.toString()
                                )
                            }
                        }
                        /*showSuccessAlertWithoutTitle(
                            message = getString(R.string.registration_successful)
                        )

                        Handler().postDelayed(
                            Runnable {
                                startActivity(
                                    Intent(
                                        this@RegisterActivity, HomeActivity::class.java
                                    )
                                )
                                finishAffinity()
                            },
                            1000
                        )*/
                    } else {
                        activity.hideLoaderDialog()
                        showErrorAlert(
                            message = getString(R.string.authentication_failed) + task.exception?.message.toString()
                        )
                    }
                }
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (permissions["android.permission.READ_MEDIA_IMAGES"] == true) {
                    openGalery()
                } else if (permissions["android.permission.READ_MEDIA_IMAGES"] == false) {
                    showGPermissionNeeded()
                }
            } else {
                if (permissions["android.permission.READ_EXTERNAL_STORAGE"] == true) {
                    openGalery()
                } else if (permissions["android.permission.READ_EXTERNAL_STORAGE"] == false) {
                    showGPermissionNeeded()
                }
            }
        }

    private fun openGalery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            resultLauncher.launch(intent)
        } else {
            val intent = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            resultLauncherForBelowAndroid12.launch(intent)
//            startActivityForResult(intent, REQUEST_GALLARY)
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.tvUpload.isVisible = false
                pickedImgUri = result.data!!.data
                binding.imageProfileLogin.setImageURI(pickedImgUri)
            }
        }

    private var resultLauncherForBelowAndroid12 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.tvUpload.isVisible = false
                pickedImgUri = result.data!!.data
                binding.imageProfileLogin.setImageURI(pickedImgUri)
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == REQUEST_GALLARY && data != null) {

            pickedImgUri = data.data!!
            binding.imageProfileLogin.setImageURI(pickedImgUri)
        }
    }


    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {

        }
    }
}