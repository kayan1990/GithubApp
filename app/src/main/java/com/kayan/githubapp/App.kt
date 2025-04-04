package com.kayan.githubapp

import android.app.Application
import com.kayan.githubapp.util.datastore.DataStoreUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DataStoreUtils.init(this)
    }
}