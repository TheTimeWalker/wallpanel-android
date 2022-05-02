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

package xyz.wallpanel.app.persistence

import android.content.Context
import android.content.SharedPreferences
import xyz.wallpanel.app.R
import javax.inject.Inject

class Configuration @Inject
constructor(private val context: Context, private val sharedPreferences: SharedPreferences) {

    // APP
    var isFirstTime: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_FIRST_TIME, true)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_FIRST_TIME, value).apply()

    val appPreventSleep: Boolean
        get() = getBoolPref(R.string.key_setting_app_preventsleep,
                R.string.default_setting_app_preventsleep)

    // TODO we have to migrate this due to an error when entering codes with leading 0 value
    var settingsCode: String
        get() {
            val prev = this.sharedPreferences.getInt(PREF_SETTINGS_CODE, 0)
            val cur = this.sharedPreferences.getString(PREF_SETTINGS_CODE_STRING, "1234").orEmpty()
            return if(prev > 0) {
                val preStr = String.format("%04d", prev) // pad to 4 with 0's leading
                this.sharedPreferences.edit().putInt(PREF_SETTINGS_CODE, 0).apply()
                this.sharedPreferences.edit().putString(PREF_SETTINGS_CODE_STRING, preStr).apply()
                prev.toString()
            } else {
                cur
            }
        }
        set(value) = this.sharedPreferences.edit().putString(PREF_SETTINGS_CODE_STRING, value).apply()

    var fullScreen: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_FULL_SCREEN, true)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_FULL_SCREEN, value).apply()

    var useDarkTheme: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_DARK_THEME, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_DARK_THEME, value).apply()

    var settingsTransparent: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_SETTINGS_TRANSPARENT, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_SETTINGS_TRANSPARENT, value).apply()

    var settingsDisabled: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_SETTINGS_DISABLE, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_SETTINGS_DISABLE, value).apply()

    var writeScreenPermissionsShown: Boolean
        get() = sharedPreferences.getBoolean(PREF_WRITE_SCREEN_PERMISSIONS, false)
        set(value) {
            sharedPreferences.edit().putBoolean(PREF_WRITE_SCREEN_PERMISSIONS, value).apply()
        }

    var cameraPermissionsShown: Boolean
        get() = sharedPreferences.getBoolean(PREF_CAMERA_PERMISSIONS, false)
        set(value) {
            sharedPreferences.edit().putBoolean(PREF_CAMERA_PERMISSIONS, value).apply()
        }

    var cameraRotate: Float
        get() = this.sharedPreferences.getString(PREF_CAMERA_ROTATE, "0f")!!.toFloat()
        set(value) = this.sharedPreferences.edit().putString(PREF_CAMERA_ROTATE, value.toString()).apply()

    var appLaunchUrl: String
        get() = getStringPref(R.string.key_setting_app_launchurl,
                R.string.default_setting_app_launchurl)
        set(launchUrl) {
            sharedPreferences.edit().putString(context.getString(R.string.key_setting_app_launchurl), launchUrl).apply()
            settingsUpdated()
        }

    val appShowActivity: Boolean
        get() = getBoolPref(R.string.key_setting_app_showactivity,
                R.string.default_setting_app_showactivity)

    var cameraEnabled: Boolean
        get() = getBoolPref(R.string.key_setting_camera_enabled, R.string.default_setting_camera_enabled)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_enabled), value).apply()
        }

    var cameraId: Int
        get() = this.sharedPreferences.getInt(context.getString(R.string.setting_camera_cameraid), 0)
        set(value) {
            this.sharedPreferences.edit().putInt(context.getString(R.string.setting_camera_cameraid), value).apply()
        }

    var cameraMotionEnabled: Boolean
        get() = getBoolPref(R.string.key_setting_camera_motionenabled, R.string.default_setting_camera_motionenabled)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_motionenabled), value).apply()
        }

    var cameraMotionLeniency: Int
        get() = sharedPreferences.getInt(PREF_CAMERA_MOTION_LATENCY, 20)
        set(value) {
            sharedPreferences.edit().putInt(PREF_CAMERA_MOTION_LATENCY, value).apply()
        }

    val cameraMotionMinLuma: Int
        get() = Integer.valueOf(getStringPref(R.string.key_setting_camera_motionminluma, R.string.default_setting_camera_motionminluma).trim().toInt())


    val cameraMotionWake: Boolean
        get() = getBoolPref(R.string.key_setting_camera_motionwake,
                R.string.default_setting_camera_motionwake)

    val cameraMotionBright: Boolean
        get() = getBoolPref(R.string.key_setting_camera_motionbright,
                R.string.default_setting_camera_motionbright)

    var cameraFaceEnabled: Boolean
        get() = getBoolPref(R.string.key_setting_camera_faceenabled,
                R.string.default_setting_camera_faceenabled)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_faceenabled), value).apply()
        }

    val cameraFaceWake: Boolean
        get() = getBoolPref(R.string.key_setting_camera_facewake,
                R.string.default_setting_camera_facewake)

    var cameraFaceSize: Int
        get() = sharedPreferences.getInt(PREF_CAMERA_FACE_SIZE, 0)
        set(value) {
            sharedPreferences.edit().putInt(PREF_CAMERA_FACE_SIZE, value).apply()
        }

    val cameraFaceRotation: Boolean
        get() = getBoolPref(R.string.key_setting_camera_facerotation,
                R.string.default_setting_camera_facerotation)

    var cameraQRCodeEnabled: Boolean
        get() = getBoolPref(R.string.key_setting_camera_qrcodeenabled,
                R.string.default_setting_camera_qrcodeenabled)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_qrcodeenabled), value).apply()
        }

    val motionResetTime: Int
        get() = getStringPref(R.string.key_setting_motion_clear,
                R.string.default_motion_clear).trim().toInt()

    val httpEnabled: Boolean
        get() = httpRestEnabled || httpMJPEGEnabled

    val httpPort: Int
        get() =
            try {
                getStringPref(R.string.key_setting_http_port, R.string.default_setting_http_port).trim().toInt()
            } catch (e: NumberFormatException) {
                context.getString(R.string.default_setting_http_port).toInt()
            }

    val httpRestEnabled: Boolean
        get() = getBoolPref(R.string.key_setting_http_restenabled,
                R.string.default_setting_http_restenabled)

    val httpMJPEGEnabled: Boolean
        get() = getBoolPref(R.string.key_setting_http_mjpegenabled,
                R.string.default_setting_http_mjpegenabled)

    fun setHttpMJPEGEnabled(value: Boolean?) {
        sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_http_mjpegenabled), value!!).apply()
    }

    val httpMJPEGMaxStreams: Int
        get() = getStringPref(R.string.key_setting_http_mjpegmaxstreams, R.string.default_setting_http_mjpegmaxstreams).trim().toInt()

    val mqttEnabled: Boolean
        get() = getBoolPref(R.string.key_setting_mqtt_enabled, R.string.default_setting_mqtt_enabled)

    var mqttVersion: String
        get() = getStringPref(R.string.key_setting_mqtt_version, R.string.default_setting_mqtt_version)
        set(value) {
            sharedPreferences.edit().putString(context.getString(R.string.key_setting_mqtt_version), value).apply()
        }

    var mqttTlsEnabled: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.key_setting_mqtt_tls_enabled), false)
        set(value) =
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_mqtt_tls_enabled), value).apply()

    var mqttBroker: String
        get() = getStringPref(R.string.key_setting_mqtt_servername, R.string.default_setting_mqtt_servername)
        set(value) =
            sharedPreferences.edit().putString(context.getString(R.string.key_setting_mqtt_servername), value).apply()

    val mqttServerPort: Int
        get() = getStringPref(R.string.key_setting_mqtt_serverport, R.string.default_setting_mqtt_serverport).trim().toInt()

    val mqttBaseTopic: String
        get() = getStringPref(R.string.key_setting_mqtt_basetopic,
                R.string.default_setting_mqtt_basetopic)

    val mqttClientId: String
        get() = getStringPref(R.string.key_setting_mqtt_clientid,
                R.string.default_setting_mqtt_clientid)

    var mqttUsername: String
        get() = getStringPref(R.string.key_setting_mqtt_username,
                R.string.default_setting_mqtt_username)
        set(value) =
            sharedPreferences.edit().putString(context.getString(R.string.key_setting_mqtt_username), value).apply()

    var mqttPassword: String
        get() = getStringPref(R.string.key_setting_mqtt_password,
                R.string.default_setting_mqtt_password)
        set(value) =
            sharedPreferences.edit().putString(context.getString(R.string.key_setting_mqtt_password), value).apply()

    val mqttSensorFrequency: Int
        get() = getStringPref(R.string.key_setting_mqtt_sensorfrequency,
                R.string.default_setting_mqtt_sensorfrequency).trim().toInt()

    val mqttDiscovery: Boolean
        get() = getBoolPref(R.string.key_setting_mqtt_discovery, R.string.default_setting_mqtt_home_assistant_discovery)

    val mqttDiscoveryTopic: String
        get() = getStringPref(R.string.key_setting_mqtt_discovery_topic, R.string.default_setting_mqtt_discovery_topic)

    val mqttDiscoveryDeviceName: String
        get() = getStringPref(R.string.key_setting_mqtt_discovery_name, R.string.default_setting_mqtt_home_assistant_name)

    val sensorsEnabled: Boolean
        get() = getBoolPref(R.string.key_setting_sensors_enabled,
                R.string.default_setting_sensors_value)

    val hardwareAccelerated: Boolean
        get() = getBoolPref(R.string.key_hadware_accelerated_enabled,
                R.string.default_hardware_accelerated_value)

    var browserUserAgent: String
        get() = getStringPref(R.string.key_setting_browser_user_agent,
                R.string.default_browser_user_agent)
        set(value) {
            sharedPreferences.edit().putString(context.getString(R.string.key_setting_browser_user_agent), value).apply()
            settingsUpdated()
        }

    var browserRefreshDisconnect: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_BROWSER_REFRESH_DISCONNECT, true)
        set(value) {
            sharedPreferences.edit().putBoolean(PREF_BROWSER_REFRESH_DISCONNECT, value).apply()
        }

    var browserRefresh: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_pref_browser_refresh), true)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_pref_browser_refresh), value).apply()
        }

    val cameraFPS: Float
        get() = try {
            getStringPref(R.string.key_setting_camera_fps, R.string.default_camera_fps).trim().toFloat()
        } catch (e: Exception) {
            15.0F
        }

    val testZoomLevel: Float
        get() {
            val value = sharedPreferences.getString(context.getString(R.string.key_setting_test_zoomlevel), "1.0")
            return value?.toFloatOrNull()?: 1.0F
        }

    var inactivityTime: Long
        get() = sharedPreferences.getLong(context.getString(R.string.key_screensaver_inactivity_time), 30000)
        set(value) {
            sharedPreferences.edit().putLong(context.getString(R.string.key_screensaver_inactivity_time), value).apply()
        }

    var screenSaverDimValue: Int
        get() = sharedPreferences.getInt(context.getString(R.string.key_screensaver_dim_value), 25)
        set(value) {
            sharedPreferences.edit().putInt(context.getString(R.string.key_screensaver_dim_value), value).apply()
        }

    var settingsLocation: Int
        get() = sharedPreferences.getInt(PREF_SETTINGS_LOCATION, 0)
        set(value) {
            sharedPreferences.edit().putInt(PREF_SETTINGS_LOCATION, value).apply()
        }

    var imageRotation: Int
        get() = sharedPreferences.getInt(PREF_IMAGE_ROTATION, ROTATE_TIME_IN_MINUTES)
        set(value) = this.sharedPreferences.edit().putInt(PREF_IMAGE_ROTATION, value).apply()

    var hasBlankScreenSaver: Boolean
        get() = getBoolPref(R.string.key_screensaver_blank, R.string.default_screensaver_blank)
        set(value) =
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_screensaver_blank), value).apply()

    var hasDimScreenSaver: Boolean
        get() = getBoolPref(R.string.key_screensaver_dim, R.string.default_screensaver_dim)
        set(value) =
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_screensaver_dim), value).apply()

    var hasClockScreenSaver: Boolean
        get() = getBoolPref(R.string.key_screensaver, R.string.default_screensaver)
        set(value) =
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_screensaver), value).apply()

    var hasScreenSaverWallpaper: Boolean
        get() = getBoolPref(R.string.key_screensaver_wallpaper, R.string.default_screensaver_wallpaper)
        set(value) =
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_screensaver_wallpaper), value).apply()

    var webScreenSaver: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_WEB_SCREENSAVER, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_WEB_SCREENSAVER, value).apply()

    var webScreenSaverUrl: String
        get() = sharedPreferences.getString(PREF_WEB_SCREENSAVER_URL, WEB_SCREEN_SAVER).orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_WEB_SCREENSAVER_URL, value).apply()

    var screenBrightness: Int
        get() = sharedPreferences.getInt(context.getString(R.string.key_setting_screen_brightness), 150)
        set(value) {
            sharedPreferences.edit().putInt(context.getString(R.string.key_setting_screen_brightness), value).apply()
        }

    var screenScreenSaverBrightness: Int
        get() = sharedPreferences.getInt(context.getString(R.string.key_setting_screensaver_brightness), (255*.25).toInt())
        set(value) {
            sharedPreferences.edit().putInt(context.getString(R.string.key_setting_screensaver_brightness), value).apply()
        }

    var useScreenBrightness: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_SCREEN_BRIGHTNESS, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_SCREEN_BRIGHTNESS, value).apply()

    val ignoreSSLErrors: Boolean
        get() = getBoolPref(R.string.key_setting_ignore_ssl_errors,
                R.string.default_setting_ignore_ssl_errors)

    fun hasCameraDetections(): Boolean {
        return cameraEnabled && (cameraMotionEnabled || cameraQRCodeEnabled || cameraFaceEnabled || httpMJPEGEnabled)
    }

    private fun getStringPref(resId: Int, defId: Int): String {
        val def = context.getString(defId)
        val pref = sharedPreferences.getString(context.getString(resId), "")
        return if (pref!!.isEmpty()) def else pref
    }

    private fun getBoolPref(resId: Int, defId: Int): Boolean {
        return sharedPreferences.getBoolean(
                context.getString(resId),
                java.lang.Boolean.valueOf(context.getString(defId))
        )
    }

    fun hasSettingsUpdates(): Boolean {
        val updates = sharedPreferences.getBoolean(PREF_BROWSER_SETTINGS_UPDATED, false)
        sharedPreferences.edit().putBoolean(PREF_BROWSER_SETTINGS_UPDATED, false).apply()
        return updates
    }

    private fun settingsUpdated() {
        sharedPreferences.edit().putBoolean(PREF_BROWSER_SETTINGS_UPDATED, true).apply()
    }

    companion object {
        private const val PREF_BROWSER_SETTINGS_UPDATED = "pref_browser_settings_updated"
        private const val PREF_DARK_THEME = "pref_dark_theme"
        private const val PREF_FULL_SCREEN = "pref_full_screen"
        private const val PREF_SETTINGS_CODE = "pref_settings_code"
        private const val PREF_SETTINGS_CODE_STRING = "pref_settings_code_string"
        private const val PREF_SETTINGS_TRANSPARENT = "pref_settings_transparent"
        private const val PREF_SETTINGS_DISABLE = "pref_settings_disable"
        private const val PREF_SETTINGS_LOCATION = "pref_settings_location"
        const val PREF_FIRST_TIME = "pref_first_time"
        const val PREF_WRITE_SCREEN_PERMISSIONS = "pref_write_screen_permissions"
        const val PREF_CAMERA_PERMISSIONS = "pref_camera_permissions"
        const val PREF_CAMERA_ROTATE = "pref_camera_rotate"
        const val PREF_BROWSER_REFRESH_DISCONNECT = "pref_browser_refresh_disconnect"
        const val PREF_SCREEN_BRIGHTNESS = "pref_use_screen_brightness"
        const val PREF_SCREENSAVER_DIM_VALUE = "pref_screensaver_dim_value"
        private val ROTATE_TIME_IN_MINUTES = 15
        const val PREF_IMAGE_ROTATION = "pref_image_rotation"
        private val PREF_CAMERA_FACE_SIZE = "pref_camera_face_size"
        private val PREF_CAMERA_MOTION_LATENCY = "pref_camera_motion_latency"
        private val PREF_WEB_SCREENSAVER_URL = "pref_web_screensaver_url"
        private val PREF_WEB_SCREENSAVER = "pref_web_screensaver"
        const val WEB_SCREEN_SAVER = "https://wallpanel.xyz"
    }
}
