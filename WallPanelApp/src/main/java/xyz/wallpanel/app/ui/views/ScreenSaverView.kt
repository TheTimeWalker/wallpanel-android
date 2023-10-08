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

package xyz.wallpanel.app.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import xyz.wallpanel.app.R
import xyz.wallpanel.app.persistence.Configuration.Companion.WEB_SCREEN_SAVER
import timber.log.Timber
import xyz.wallpanel.app.databinding.DialogScreenSaverBinding
import java.util.*
import java.util.concurrent.TimeUnit

class ScreenSaverView : RelativeLayout {

    private lateinit var binding: DialogScreenSaverBinding
    private var timeHandler: Handler? = null
    private var wallPaperHandler: Handler? = null
    private var saverContext: Context? = null
    private var parentWidth: Int = 0
    private var parentHeight: Int = 0
    private var showWebPage: Boolean = false
    private var showWallpaper: Boolean = false
    private var showClock: Boolean = false
    private var rotationInterval = 900L
    private var webUrl = WEB_SCREEN_SAVER
    private var certPermissionsShown = false

    val calendar: Calendar = Calendar.getInstance()

    private val timeRunnable = object : Runnable {
        override fun run() {
            val date = Date()
            calendar.time = date
            val currentTimeString = DateUtils.formatDateTime(context, date.time, DateUtils.FORMAT_SHOW_TIME)
            val currentDayString = DateUtils.formatDateTime(context, date.time, DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_DATE)
            binding.screenSaverClock.text = currentTimeString
            binding.screenSaverDay.text = currentDayString

            val width = binding.screenSaverClockLayout.width
            val height = binding.screenSaverClockLayout.height

            parentWidth = binding.screenSaverView.width
            parentHeight = binding.screenSaverView.height

            try {
                if (width > 0 && height > 0 && parentWidth > 0 && parentHeight > 0) {
                    if (parentHeight - width > 0) {
                        val newX = Random().nextInt(parentWidth - width)
                        binding.screenSaverClockLayout.x = newX.toFloat()
                    }
                    if (parentHeight - height > 0) {
                        val newY = Random().nextInt(parentHeight - height)
                        binding.screenSaverClockLayout.y = newY.toFloat()
                    }
                }
            } catch (e: IllegalArgumentException) {
                Timber.e(e.message)
            }

            val offset = 60L - calendar.get(Calendar.SECOND)
            timeHandler?.postDelayed(this, TimeUnit.SECONDS.toMillis(offset))
        }
    }

    private val wallPaperRunnable = object : Runnable {
        override fun run() {
            setScreenSaverView()
            wallPaperHandler?.postDelayed(this, TimeUnit.MINUTES.toMillis(rotationInterval))
        }
    }

    constructor(context: Context) : super(context) {
        saverContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        saverContext = context
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = DialogScreenSaverBinding.bind(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timeHandler?.removeCallbacks(timeRunnable)
        wallPaperHandler?.removeCallbacks(wallPaperRunnable)
    }

    fun init(hasWeb: Boolean, urlWeb: String, hasWallpaper: Boolean, hasClock: Boolean, rotationDelay: Long) {
        rotationInterval = rotationDelay
        showWebPage = hasWeb
        webUrl = urlWeb
        showWallpaper = hasWallpaper
        showClock = hasClock

        // always allow the clock screensaver to be displayed
        if(showClock) {
            setClockViews()
            timeHandler = Handler(Looper.getMainLooper())
            timeHandler?.postDelayed(timeRunnable, 10)
            binding.screenSaverClockLayout.visibility = View.VISIBLE
            //screenSaverWebViewLayout.visibility = View.GONE
        } else {
            binding.screenSaverClockLayout.visibility = View.GONE
        }

        // show optional screensaver layers
        if (showWallpaper) {
            wallPaperHandler = Handler(Looper.getMainLooper())
            wallPaperHandler?.postDelayed(wallPaperRunnable, 10)
            binding.screenSaverImageLayout.visibility  = View.VISIBLE
            binding.screenSaverWebViewLayout.visibility = View.GONE
        } else if (showWebPage) {
            //c
            binding.screenSaverImageLayout.visibility  = View.GONE
            binding.screenSaverWebViewLayout.visibility = View.VISIBLE
            startWebScreenSaver(webUrl)
        }
    }

    // setup clock size based on screen and weather settings
    private fun setClockViews() {
        val initialRegular = binding.screenSaverClock.textSize
        binding.screenSaverClock.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialRegular + 100)
    }

    private fun setScreenSaverView() {
        Glide.with(this.context.applicationContext)
                .load(String.format(UNSPLASH_IT_URL, binding.screenSaverView.width, binding.screenSaverView.height))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .skipMemoryCache(true)
                .into(binding.screenSaverImageLayout);
    }

    private fun closeView() {
        this.callOnClick()
    }

    private fun startWebScreenSaver(url: String) {
        Timber.d("startWebScreenSaver $url")
        loadWebPage(url)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadWebPage(url: String) {
        Timber.d("loadWebPage url ${url}")
        configureWebSettings("")
        clearCache()
        binding.screenSaverWebView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(view.context, R.style.CustomAlertDialog)
                        .setTitle(context.getString(R.string.dialog_title_ssl_error))
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                return true
            }
        }
        binding.screenSaverWebView.setOnTouchListener { v, _ ->
            v.performClick()
            closeView()
            false
        }
        binding.screenSaverWebView.webViewClient = object : WebViewClient() {
            //If you will not use this method url links are open in new browser not in webview
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                Toast.makeText(context, description, Toast.LENGTH_SHORT).show()
            }

            // TODO we need to load SSL certificates
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler?, error: SslError?) {
                if (!certPermissionsShown) {
                    var message = context.getString(R.string.dialog_message_ssl_generic)
                    when (error?.primaryError) {
                        SslError.SSL_UNTRUSTED -> message = context.getString(R.string.dialog_message_ssl_untrusted)
                        SslError.SSL_EXPIRED -> message = context.getString(R.string.dialog_message_ssl_expired)
                        SslError.SSL_IDMISMATCH -> message = context.getString(R.string.dialog_message_ssl_mismatch)
                        SslError.SSL_NOTYETVALID -> message = context.getString(R.string.dialog_message_ssl_not_yet_valid)
                    }
                    message += context.getString(R.string.dialog_message_ssl_continue)
                    AlertDialog.Builder(context, R.style.CustomAlertDialog)
                            .setTitle(context.getString(R.string.dialog_title_ssl_error))
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { _, _ ->
                                certPermissionsShown = true
                                handler?.proceed()
                            })
                            .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { _, _ ->
                                certPermissionsShown = false
                                handler?.cancel()
                            })
                            .show()
                } else {
                    // we have already shown permissions, no need to show again on page refreshes or when page auto-refreshes itself
                    handler?.proceed()
                }
            }
        }
        binding.screenSaverWebView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebSettings(userAgent: String) {
        val webSettings = binding.screenSaverWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        if (!TextUtils.isEmpty(userAgent)) {
            webSettings.userAgentString = userAgent
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        Timber.d(webSettings.userAgentString)
    }

    private fun clearCache() {
        binding.screenSaverWebView.clearCache(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
        }
    }

    companion object {
        const val UNSPLASH_IT_URL = "http://unsplash.it/%s/%s?random"
    }
}
