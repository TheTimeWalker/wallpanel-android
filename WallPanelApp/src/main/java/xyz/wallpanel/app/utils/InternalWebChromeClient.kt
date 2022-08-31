package xyz.wallpanel.app.utils

import android.content.res.Resources
import android.os.Build
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import xyz.wallpanel.app.R
import xyz.wallpanel.app.ui.activities.BaseBrowserActivity
import xyz.wallpanel.app.ui.views.WebClientCallback

class InternalWebChromeClient(val resources: Resources, val callback: WebClientCallback) :
    WebChromeClient() {

    var snackbar: Snackbar? = null

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        if (newProgress == 100) {
            snackbar?.dismiss()
            if (view.url != null) {

            } else {
                Toast.makeText(
                    view.context,
                    resources.getString(R.string.toast_empty_url),
                    Toast.LENGTH_SHORT
                ).show()
                callback.complete()
            }
            return
        }
        if (callback.displayProgress()) {
            val text =
                resources.getString(R.string.text_loading_percent, newProgress.toString(), view.url)
            if (snackbar == null) {
                snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
            } else {
                snackbar?.setText(text)
            }
            snackbar?.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onPermissionRequest(request: PermissionRequest?) {
        super.onPermissionRequest(request)
        callback.setWebkitPermissionRequest(request)
        request?.resources?.forEach {
            when (it) {
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                    callback.askForWebkitPermission(
                        it,
                        BaseBrowserActivity.REQUEST_CODE_PERMISSION_AUDIO
                    )
                }
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                    callback.askForWebkitPermission(
                        it,
                        BaseBrowserActivity.REQUEST_CODE_PERMISSION_CAMERA
                    )
                }
            }
        }
    }

    override fun onJsAlert(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        if (view.context != null && !callback.isFinishing()) {
            AlertDialog.Builder(view.context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
        return true
    }

}