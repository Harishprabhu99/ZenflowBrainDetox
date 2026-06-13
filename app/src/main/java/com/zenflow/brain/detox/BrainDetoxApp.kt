package com.zenflow.brain.detox

import android.app.Application
import com.zenflow.brain.detox.di.AppContainer

class BrainDetoxApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
