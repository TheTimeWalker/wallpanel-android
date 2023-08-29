/*
 * Copyright (c) 2022 WallPanel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.wallpanel.app

import android.R.attr
import android.content.ComponentCallbacks2
import android.content.Context
import android.os.Process.myPid
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.multidex.MultiDex
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import xyz.wallpanel.app.di.DaggerApplicationComponent
import xyz.wallpanel.app.utils.CrashlyticsDebugTree
import xyz.wallpanel.app.utils.LauncherShortcuts
import xyz.wallpanel.app.utils.WallpanelDebugTree


class WallPanel : DaggerApplication() {


    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // Gives clickable links to the issue in the Android Studio Logcat
            Timber.plant(WallpanelDebugTree())
            // Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsDebugTree())
            // Timber.plant(CrashlyticsTree())
        }
        strictMode()
        LauncherShortcuts.createShortcuts(this)
    }

    private fun strictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork() // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                Timber.v("The user interface has moved to the background.")
                Runtime.getRuntime().gc()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                Timber.v("If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will begin killing background processes.")
                Runtime.getRuntime().gc()
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                Timber.v("If the event is TRIM_MEMORY_COMPLETE, the process will be one of the first to be terminated.")
                //val processId: Int = Process.myPid()
                //Process.killProcess(processId)
                Runtime.getRuntime().gc()
            }
            else -> {
                Timber.v("The app received an unrecognized memory level value from the system. Treat this as a generic low-memory message.")
                throw IllegalStateException("Unexpected value: $level")
            }
        }
    }


}
