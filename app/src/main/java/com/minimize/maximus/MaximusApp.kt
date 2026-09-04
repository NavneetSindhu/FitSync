package com.minimize.maximus

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.minimize.maximus.data.remote.RemoteConfigManager
import com.minimize.maximus.util.MaximusHapticUtils
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MaximusApp : Application() {

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    @Inject
    lateinit var crashlytics: FirebaseCrashlytics

    override fun onCreate() {
        super.onCreate()
        MaximusHapticUtils.init(this)

        // Initialize Firebase Crashlytics custom keys
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)

        // Initialize Firebase Remote Config & real-time updates
        remoteConfigManager.initialize()
    }
}