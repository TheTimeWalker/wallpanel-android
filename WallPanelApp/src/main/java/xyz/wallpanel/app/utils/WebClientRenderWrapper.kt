package xyz.wallpanel.app.utils

import android.webkit.WebViewClient
import android.annotation.TargetApi
import android.os.Build
import android.webkit.WebView
import android.webkit.RenderProcessGoneDetail
import timber.log.Timber
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.graphics.Bitmap
import android.webkit.WebResourceResponse
import android.webkit.WebResourceError
import android.webkit.SslErrorHandler
import android.net.http.SslError
import android.os.Message
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.SafeBrowsingResponse

open class WebClientRenderWrapper(private val client: WebViewClient) : WebViewClient() {

    @TargetApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            client.onRenderProcessGone(view, detail)
            Timber.d("onRenderProcessGone %s %s", view, detail.didCrash())
            if (view.parent is ViewGroup) {
                (view.parent as ViewGroup).removeView(view)
                view.destroy()
            }
            return true
        }
        return super.onRenderProcessGone(view, detail)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return client.shouldOverrideUrlLoading(view, url)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return client.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        client.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        client.onPageFinished(view, url)
    }

    override fun onLoadResource(view: WebView, url: String) {
        client.onLoadResource(view, url)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onPageCommitVisible(view: WebView, url: String) {
        client.onPageCommitVisible(view, url)
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return client.shouldInterceptRequest(view, url)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return client.shouldInterceptRequest(view, request)
    }

    override fun onTooManyRedirects(view: WebView, cancelMsg: Message, continueMsg: Message) {
        client.onTooManyRedirects(view, cancelMsg, continueMsg)
    }

    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        client.onReceivedError(view, errorCode, description, failingUrl)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        client.onReceivedError(view, request, error)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        client.onReceivedHttpError(view, request, errorResponse)
    }



    override fun onFormResubmission(view: WebView, dontResend: Message, resend: Message) {
        client.onFormResubmission(view, dontResend, resend)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        client.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        client.onReceivedSslError(view, handler, error)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        client.onReceivedClientCertRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView,
        handler: HttpAuthHandler,
        host: String,
        realm: String
    ) {
        client.onReceivedHttpAuthRequest(view, handler, host, realm)
    }

    override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean {
        return client.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
        client.onUnhandledKeyEvent(view, event)
    }

    override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
        client.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(
        view: WebView,
        realm: String,
        account: String?,
        args: String
    ) {
        client.onReceivedLoginRequest(view, realm, account, args)
    }

    @TargetApi(Build.VERSION_CODES.O_MR1)
    override fun onSafeBrowsingHit(
        view: WebView,
        request: WebResourceRequest,
        threatType: Int,
        callback: SafeBrowsingResponse
    ) {
        client.onSafeBrowsingHit(view, request, threatType, callback)
    }
}