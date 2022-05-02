package com.thanksmister.iot.wallpanel.ui.views

import android.webkit.PermissionRequest
import com.thanksmister.iot.wallpanel.persistence.Configuration
import com.thanksmister.iot.wallpanel.utils.DialogUtils

interface WebClientCallback {
    fun askForWebkitPermission(permission: String, requestCode: Int)

    fun complete()

    fun pageLoadComplete(url: String)

    fun setWebkitPermissionRequest(request: PermissionRequest?)

    var isConnected: Boolean

    fun isFinishing(): Boolean

    fun displayProgress(): Boolean

    fun startReloadDelay()

    fun stopReloadDelay()

    fun certPermissionsShown() : Boolean

}