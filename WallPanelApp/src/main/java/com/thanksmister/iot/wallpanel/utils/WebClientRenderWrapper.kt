package com.thanksmister.iot.wallpanel.utils

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

class WebClientRenderWrapper(private val mClient: WebViewClient) : WebViewClient() {
    @TargetApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mClient.onRenderProcessGone(view, detail)
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
        return mClient.shouldOverrideUrlLoading(view, url)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return mClient.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        mClient.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        mClient.onPageFinished(view, url)
    }

    override fun onLoadResource(view: WebView, url: String) {
        mClient.onLoadResource(view, url)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onPageCommitVisible(view: WebView, url: String) {
        mClient.onPageCommitVisible(view, url)
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return mClient.shouldInterceptRequest(view, url)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return mClient.shouldInterceptRequest(view, request)
    }

    override fun onTooManyRedirects(view: WebView, cancelMsg: Message, continueMsg: Message) {
        mClient.onTooManyRedirects(view, cancelMsg, continueMsg)
    }

    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        mClient.onReceivedError(view, errorCode, description, failingUrl)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        mClient.onReceivedError(view, request, error)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        mClient.onReceivedHttpError(view, request, errorResponse)
    }

    override fun onFormResubmission(view: WebView, dontResend: Message, resend: Message) {
        mClient.onFormResubmission(view, dontResend, resend)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        mClient.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        mClient.onReceivedSslError(view, handler, error)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        mClient.onReceivedClientCertRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView,
        handler: HttpAuthHandler,
        host: String,
        realm: String
    ) {
        mClient.onReceivedHttpAuthRequest(view, handler, host, realm)
    }

    override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean {
        return mClient.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
        mClient.onUnhandledKeyEvent(view, event)
    }

    override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
        mClient.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(
        view: WebView,
        realm: String,
        account: String?,
        args: String
    ) {
        mClient.onReceivedLoginRequest(view, realm, account, args)
    }

    @TargetApi(Build.VERSION_CODES.O_MR1)
    override fun onSafeBrowsingHit(
        view: WebView,
        request: WebResourceRequest,
        threatType: Int,
        callback: SafeBrowsingResponse
    ) {
        mClient.onSafeBrowsingHit(view, request, threatType, callback)
    }
}