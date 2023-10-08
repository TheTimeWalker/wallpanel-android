package xyz.wallpanel.app.ui.views

import android.annotation.SuppressLint
import kotlin.jvm.JvmOverloads
import timber.log.Timber
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.ViewGroup
import android.content.res.Resources.NotFoundException
import android.util.AttributeSet
import android.view.View
import android.webkit.*

@SuppressLint("SetJavaScriptEnabled")
class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    privateBrowsing: Boolean = false
) : WebView(context, attrs, defStyleAttr, privateBrowsing) {


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : this(
        context,
        attrs,
        defStyleAttr,
        false
    )

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
        isVerticalScrollBarEnabled = false
        isFocusable = true
        isFocusableInTouchMode = true
        webChromeClient = WebChromeClient()
        //webViewClient = WebClientRenderWrapper(WebViewClient())
        //addJavascriptInterface(androidInterface, "Android")

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.saveFormData = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.allowFileAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowContentAccess = true
        settings.setSupportZoom(true)
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.pluginState = WebSettings.PluginState.ON
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // settings.cacheMode = WebSettings.LOAD_NO_CACHE;
        settings.mediaPlaybackRequiresUserGesture = false;

        /*if (userAgent.isNotEmpty()) {
            settings.userAgentString = userAgent
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
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