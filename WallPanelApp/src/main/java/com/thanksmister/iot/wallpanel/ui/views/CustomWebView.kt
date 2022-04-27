package com.thanksmister.iot.wallpanel.ui.views

import android.annotation.SuppressLint
import kotlin.jvm.JvmOverloads
import android.webkit.WebView
import android.webkit.JavascriptInterface
import timber.log.Timber
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.ViewGroup
import android.content.res.Resources.NotFoundException
import android.util.AttributeSet
import android.view.View
import android.webkit.WebChromeClient
import com.thanksmister.iot.wallpanel.utils.WebClientRenderWrapper
import android.webkit.WebViewClient
import com.thanksmister.iot.wallpanel.BuildConfig

@SuppressLint("SetJavaScriptEnabled")
class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    privateBrowsing: Boolean = false
) : WebView(context, attrs, defStyleAttr, privateBrowsing) {

    private var requestDisallow = false
    private val androidInterface: Any = object : Any() {
        @JavascriptInterface
        fun requestScrollEvents() {
            Timber.d("WebView.Android.requestScrollEvents")
            requestDisallow = true
        }
    }

    init {
        val settings = settings
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        isVerticalScrollBarEnabled = false
        isFocusable = true
        isFocusableInTouchMode = true
        webChromeClient = WebChromeClient()
        webViewClient = WebClientRenderWrapper(WebViewClient())
        setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    isNestedScrollingEnabled = true
        //}
        addJavascriptInterface(androidInterface, "Android")
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : this(
        context,
        attrs,
        defStyleAttr,
        false
    ) {
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (requestDisallow) {
            requestDisallowInterceptTouchEvent(true)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> requestDisallow = false
        }
        return super.onTouchEvent(event)
    }

    companion object {
        fun getView(context: Context, parent: ViewGroup?): View {
            return try {
                CustomWebView(context)
            } catch (e: NotFoundException) {
                Timber.e(e, "Caught Lollipop WebView error")
                CustomWebView(context.applicationContext)
            }
        }
    }


}