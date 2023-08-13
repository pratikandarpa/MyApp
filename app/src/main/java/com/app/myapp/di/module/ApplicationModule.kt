package com.app.myapp.di.module

import android.content.Context
import com.app.myapp.util.network.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    @Singleton
    fun provideContext(application: android.app.Application): Context {
        return application.applicationContext
    }
}