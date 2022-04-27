package com.thanksmister.iot.wallpanel.utils

import android.annotation.TargetApi
import android.os.Build
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import timber.log.Timber

open class InternalWebClient : WebViewClient() {

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