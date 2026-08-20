package com.appdynamics.sampleandroidapplication

import android.app.Application
import com.appdynamics.eumagent.runtime.AgentConfiguration
import com.appdynamics.eumagent.runtime.Instrumentation

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Instrumentation of AppD
        Instrumentation.start(
            AgentConfiguration.builder()
                .withAppKey(getString(R.string.APP_KEY))
                .withContext(applicationContext)
                .withCollectorURL(getString(R.string.COLLECTOR_URL))
                .withScreenshotURL(getString(R.string.SCREENSHOT_URL))
                .withLoggingLevel(Instrumentation.LOGGING_LEVEL_VERBOSE)
                .build())
    }
}