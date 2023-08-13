package com.app.myapp.base

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import javax.inject.Inject

abstract class BaseActivity<T : ViewDataBinding, V : ViewModel> : AppCompatActivity() {
    abstract val layoutId: Int
    abstract val bindingVariable: Int

    @Inject
    lateinit var mViewModel: V
    lateinit var binding: T

    lateinit var activity: Activity

    abstract fun setupObservable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        activity = this
        performDataBinding()
    }

    private fun performDataBinding() {
        activity = this
        binding = DataBindingUtil.setContentView(this, layoutId)
        binding.setVariable(bindingVariable, mViewModel)
        binding.executePendingBindings()
        setupObservable()
    }
}