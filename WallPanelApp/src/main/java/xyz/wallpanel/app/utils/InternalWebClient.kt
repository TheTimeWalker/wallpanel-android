package xyz.wallpanel.app.utils

import android.annotation.TargetApi
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import timber.log.Timber
import xyz.wallpanel.app.R
import xyz.wallpanel.app.persistence.Configuration
import xyz.wallpanel.app.ui.views.WebClientCallback
import java.util.*
import javax.inject.Inject

open class InternalWebClient(val resources: Resources, private val callback: WebClientCallback, val configuration: Configuration) :
    WebViewClient() {

    @Inject
    lateinit var dialogUtils: DialogUtils

    private var pageLoaded = false
    private var currentUrl = ""

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (currentUrl.equals(url, ignoreCase = true)) {
            return false;
        }
        else if (!currentUrl.equals(url, ignoreCase = true)){
            currentUrl = url;
            view.loadUrl(currentUrl);
        }
        return true;
    }

    open fun isCurrentUrl(url: String): Boolean {
        return url.lowercase(Locale.getDefault()).contains(currentUrl.lowercase())
    }

    override fun onPageStarted(
        webView: WebView?, url: String,
        bitmap: Bitmap?
    ) {
        if (isCurrentUrl(url)) {
            pageLoaded = false
        }
    }

    // TODO load a special file here on disconnect and then reload page on timer
    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        if (!callback.isFinishing()) {
            view.loadUrl("about:blank")
            view.loadUrl("file:///android_asset/error_page.html")
            callback.isConnected = false
            callback.startReloadDelay()
        }
    }

    override fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: SslError
    ) {
        if (!callback.certPermissionsShown() && !callback.isFinishing() && configuration.ignoreSSLErrors) {
            var message = resources.getString(R.string.dialog_message_ssl_generic)
            when (error?.primaryError) {
                SslError.SSL_UNTRUSTED -> message =
                    resources.getString(R.string.dialog_message_ssl_untrusted)
                SslError.SSL_EXPIRED -> message =
                    resources.getString(R.string.dialog_message_ssl_expired)
                SslError.SSL_IDMISMATCH -> message =
                    resources.getString(R.string.dialog_message_ssl_mismatch)
                SslError.SSL_NOTYETVALID -> message =
                    resources.getString(R.string.dialog_message_ssl_not_yet_valid)
            }
            message += resources.getString(xyz.wallpanel.app.R.string.dialog_message_ssl_continue)
            dialogUtils.showAlertDialog(view.context,
                resources.getString(R.string.dialog_title_ssl_error),
                resources.getString(R.string.dialog_message_ssl_continue),
                resources.getString(R.string.button_continue),
                { _, _ -> handler.proceed() },
                { _, _ -> handler.proceed() }
            )
        } else {
            handler.proceed()
        }
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        callback.pageLoadComplete(view.url.toString())
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onPageFinished(view: WebView, url: String) {
        if (callback.isConnected) {
            callback.stopReloadDelay()
        }
        if (isCurrentUrl(url)) {
            pageLoaded = true
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onRenderProcessGone(view, detail)
            Timber.d("onRenderProcessGone %s %s", view, detail.didCrash())
            if (view.parent is ViewGroup) {
                (view.parent as ViewGroup).removeView(view)
                view.destroy()
            }
            return true
        }
        return super.onRenderProcessGone(view, detail)
    }


}