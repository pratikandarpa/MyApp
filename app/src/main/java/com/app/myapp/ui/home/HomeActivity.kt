package com.app.myapp.ui.home

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.app.myapp.BR
import com.app.myapp.R
import com.app.myapp.base.BaseActivity
import com.app.myapp.databinding.ActivityHomeBinding
import com.app.myapp.databinding.NavDrawerHeaderBinding
import com.app.myapp.extension.loadImage
import com.app.myapp.ui.login.LoginActivity
import com.app.myapp.util.alertDialog.showErrorAlert
import com.app.myapp.util.alertDialog.showSuccessAlertWithoutTitle
import com.app.myapp.util.hideLoaderDialog
import com.app.myapp.util.showLoaderDialog
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@AndroidEntryPoint
@BuildCompat.PrereleaseSdkCheck
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(), ImageListNavigator {

    override val layoutId: Int
        get() = R.layout.activity_home
    override val bindingVariable: Int
        get() = BR.viewmodel

    private var doubleBackToExitPressedOnce = false
    var mAuth: FirebaseAuth? = null
    var currentUser: FirebaseUser? = null

    private lateinit var progressDialog: Dialog
    private lateinit var customTitle: TextView
    private lateinit var customProgressBar: ProgressBar
    private lateinit var customPercentage: TextView
    private lateinit var customText: TextView
    lateinit var imageAdapter: ImageListAdapter

    private var downloadID: Long = 0

    private val selectedImageList: ArrayList<ImageListCheckedModel> = ArrayList()
    private var allImageList = ArrayList<ImageModel>()
    private var imageList = ArrayList<ImageModel>()
    var selectAllImages: Boolean = false
    private var downlodedImage = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FirebaseAuth initialization
        mAuth = FirebaseAuth.getInstance()
        setDrawer()
        setRecyclerView()
        progressDialog()

        binding.homeView.btnDownload.setOnClickListener {
            downloadBtnClick()
        }

        binding.homeView.ivFilter.setOnClickListener {
            activity.showLoaderDialog()
            mViewModel.filterDate(
                binding.homeView.etFrom.text!!.toString(),
                binding.homeView.etTo.text!!.toString()
            )
        }

        binding.homeView.etFrom.setOnClickListener {
            selectFromAndToDate(isFromDate = true)
        }

        binding.homeView.etTo.setOnClickListener {
            selectFromAndToDate(isToDate = true)
        }
    }


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun setupObservable() {

        mViewModel.getValidationObservable().observe(this) {
            activity.hideLoaderDialog()
            showErrorAlert(
                message = it
            )
        }

        mViewModel.getIsValidationSuccessObservable().observe(this) {
            filterDate()
        }

        mViewModel.getImageListObservable().observe(this) {

            it.let {
                imageList.clear()
                allImageList.clear()
                imageList.add(it)
                allImageList.add(it)
                binding.homeView.rvList.adapter!!.notifyDataSetChanged()
            }
        }

        mViewModel.getSelectAllImagesObservable().observe(this) {
            it.let {
                selectedImageList.clear()
                selectAllImages = true
                for (i in imageList[0].myImagesList.indices) {
                    selectedImageList.add(
                        ImageListCheckedModel(
                            i, true, imageList[0].myImagesList[i].url
                        )
                    )
                }
                binding.homeView.rvList.adapter!!.notifyDataSetChanged()

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterDate() {
        // Create date formatter for date comparison
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Filter the dates based on specified range
        val filteredDates = allImageList[0].myImagesList.filter { dateString ->
            val dateParts = dateString.date.split(" ")[0].replace("‑", "-")
            val date = dateFormat.parse(dateParts)
            val fromDateObj = dateFormat.parse(binding.homeView.etFrom.text.toString())
            val toDateObj = dateFormat.parse(binding.homeView.etTo.text.toString())
            date in fromDateObj..toDateObj
        }

        val imageListForFilter = ArrayList<ImageData>()

        // Populate filtered dates into a new list
        if (filteredDates.isNotEmpty()) {
            binding.homeView.tvNoDataFound.isVisible = false
            filteredDates.forEach {
                println(it)
                imageListForFilter.add(it)
            }
        } else {
            binding.homeView.tvNoDataFound.isVisible = true
        }


        // Update UI with filtered image list
        selectedImageList.clear()
        imageList.clear()
        imageList.add(ImageModel(imageListForFilter))
        binding.homeView.rvList.adapter!!.notifyDataSetChanged()

        // Hide loading dialog
        activity.hideLoaderDialog()
    }

    @SuppressLint("SetTextI18n")
    private fun selectFromAndToDate(isFromDate: Boolean = false, isToDate: Boolean = false) {
        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)

        // Create and display the DatePickerDialog.
        val datePickerDialog = DatePickerDialog( // on below line we are passing context.
            this,
            { view, year, monthOfYear, dayOfMonth -> // on below line we are setting date to our text view.
                if (isFromDate) {
                    binding.homeView.etFrom.setText(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString())
                } else if (isToDate) {
                    binding.homeView.etTo.setText(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString())
                }
            },  // on below line we are passing year,
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    override fun onStart() {
        super.onStart()
        setProfileData()
    }

    private fun setProfileData() {
        try {
            currentUser = mAuth!!.currentUser

            val headerBinding = NavDrawerHeaderBinding.bind(binding.navigationView.getHeaderView(0))
            headerBinding.tvName.text = currentUser?.displayName.toString()
            headerBinding.tvEmail.text = currentUser?.email.toString()
            headerBinding.ivProfile.loadImage(
                currentUser?.photoUrl.toString(), progressBar = headerBinding.loader
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setRecyclerView() {
        val layoutManager = GridLayoutManager(this, 3)
        binding.homeView.rvList.layoutManager = layoutManager

        imageAdapter = ImageListAdapter(
            this, imageList, selectedImageList, imageListNavigator = this
        )

        binding.homeView.rvList.adapter = imageAdapter
        mViewModel.getImageData()
    }

    private fun setDrawer() {
        if (BuildCompat.isAtLeastT()) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                // Back is pressed... Finishing the activity
                closeDrawer()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this /* lifecycle owner */,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Back is pressed... Finishing the activity
                        closeDrawer()
                    }
                })
        }


        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.topAppBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.topAppBar.setNavigationIcon(R.drawable.ic_lines)
        binding.navigationView.setNavigationItemSelectedListener(object :
            OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                item.isChecked = !item.isChecked

                //Closing drawer on item click
                binding.drawerLayout.closeDrawers()

                // Handle navigation view item clicks here.
                val id = item.itemId
                if (id == R.id.logout) {
                    Firebase.auth.signOut()
                    unSelect(0)
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                    finishAffinity()
                    return true
                }
                return true
            }
        })
        binding.navigationView.itemIconTintList = null;
    }

    private fun downloadBtnClick() {
        if (selectAllImages) {
            // For select all image
            lifecycleScope.launch(Dispatchers.IO) {
                downlodedImage = 0
                beginDownload(selectedImageList[downlodedImage].downloadURL)
            }
        } else if (selectedImageList.size > 0) {
            // for only selected images
            lifecycleScope.launch(Dispatchers.IO) {
                downlodedImage = 0
                beginDownload(selectedImageList[downlodedImage].downloadURL)
            }
        } else {
            showErrorAlert(
                message = getString(R.string.please_select_the_images)
            )
        }
    }

    private fun unSelect(position: Int) {
        binding.navigationView.menu.getItem(position).isChecked = false
        binding.navigationView.menu.getItem(position).isCheckable = false
    }

    private fun select(position: Int) {
        binding.navigationView.menu.getItem(position).isChecked = true
    }

    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (doubleBackToExitPressedOnce) {
                finish()
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(
                this,
                resources.getString(R.string.Please_click_BACK_again_to_exit),
                Toast.LENGTH_SHORT
            ).show()
            Handler(Looper.getMainLooper()).postDelayed(
                { doubleBackToExitPressedOnce = false }, 2000
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navigation, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_select_all -> {
                mViewModel.selectAllImages()
                true
            }

            else -> true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onImageSelect(position: Int, isChecked: Boolean) {
        selectAllImages = false
        if (isChecked) {
            selectedImageList.add(
                ImageListCheckedModel(
                    position, true, imageList[0].myImagesList[position].url
                )
            )
        } else {
            selectedImageList.removeAll { it.index == position }
        }
        binding.homeView.rvList.adapter!!.notifyDataSetChanged()
    }

    private fun progressDialog() {
        // Create a custom dialog with appropriate layout and style.
        progressDialog = Dialog(this, R.style.Theme_Dialog)
        progressDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)

        // Initialize views from the custom dialog layout.
        customTitle = progressDialog.findViewById<TextView>(R.id.customTitle)
        customProgressBar = progressDialog.findViewById<ProgressBar>(R.id.customProgressBar)
        customPercentage = progressDialog.findViewById<TextView>(R.id.customPercentage)
        customText = progressDialog.findViewById<TextView>(R.id.customText)

        // Set default title for the progress dialog.
        customTitle.text = getString(R.string.downloading)
    }


    @SuppressLint("Range", "NotifyDataSetChanged", "SetTextI18n")
    fun beginDownload(downloadUrlOfImage: String) {
        // Create a directory for storing downloaded images.
        val imageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "images"
        )
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }

        // Create a unique image file in the directory.
        val imageFile = File(imageDir, System.currentTimeMillis().toString() + "image.jpg")

        // Build a DownloadManager request for the image.
        val request = DownloadManager.Request(Uri.parse(downloadUrlOfImage))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false).setTitle(downloadUrlOfImage)
            .setMimeType("image/jpeg") // Your file type. You can use this code to download other file types also.
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(imageFile))// Uri of the destination file

        val downloadManager =
            getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.

        // Track download progress and manage multiple downloads.
        var finishDownload = false
        var progress: Int
        while (!finishDownload) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                    }

                    DownloadManager.STATUS_PAUSED -> {
                    }

                    DownloadManager.STATUS_PENDING -> {
                    }

                    DownloadManager.STATUS_RUNNING -> {
                        try {
                            Thread.sleep(50) // Simulate some work being done
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                        val total =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloaded =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = (downloaded * 100L / total).toInt()

                            runOnUiThread {
                                progressDialog.show()
                                customProgressBar.progress = progress
                                customPercentage.text = "$progress%"
                                customText.text = "$downlodedImage / ${selectedImageList.size}"
                            }
                        }
                    }

                    DownloadManager.STATUS_SUCCESSFUL -> {
                        progress = 100
                        finishDownload = true

                        downlodedImage++

                        if (downlodedImage < selectedImageList.size) {

                            runOnUiThread {
                                customProgressBar.progress = 0
                                customPercentage.text = "0%"
                                customText.text = "$downlodedImage / ${selectedImageList.size}"
                            }

                            beginDownload(selectedImageList[downlodedImage].downloadURL)
                        } else {
                            runOnUiThread {
                                binding.homeView.etFrom.setText("")
                                binding.homeView.etTo.setText("")
                                selectedImageList.clear()
                                imageList.clear()
                                imageList.addAll(allImageList)
                                selectAllImages = false
                                binding.homeView.rvList.adapter!!.notifyDataSetChanged()
                                progressDialog.hide()
                            }

                            showSuccessAlertWithoutTitle(
                                message = getString(R.string.download_successful)
                            )
                        }
                    }
                }
            }
        }
    }
}