package com.zenflow.brain.detox

import android.app.Application
import com.zenflow.brain.detox.di.AppContainer

class BrainDetoxApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Load SQLCipher native library at the very beginning
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: Exception) {
            android.util.Log.e("BrainDetoxApp", "Failed to load sqlcipher", e)
        }
        container = AppContainer(this)
    }
}
