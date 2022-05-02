package xyz.wallpanel.app.ui.views

import android.webkit.PermissionRequest

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