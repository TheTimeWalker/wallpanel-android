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

package xyz.wallpanel.app.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Browser
import android.view.*
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import xyz.wallpanel.app.databinding.ActivityBrowserBinding
import xyz.wallpanel.app.network.ConnectionLiveData
import xyz.wallpanel.app.ui.fragments.CodeBottomSheetFragment
import xyz.wallpanel.app.utils.InternalWebChromeClient
import xyz.wallpanel.app.ui.views.WebClientCallback
import xyz.wallpanel.app.utils.InternalWebClient
import xyz.wallpanel.app.BuildConfig
import xyz.wallpanel.app.R
import timber.log.Timber
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.TimeUnit


class BrowserActivityNative : BaseBrowserActivity(), LifecycleObserver, WebClientCallback {

    private lateinit var webView: WebView
    private lateinit var binding: ActivityBrowserBinding
    private var certPermissionsShown = false
    private var playlistHandler: Handler? = null
    private var codeBottomSheet: CodeBottomSheetFragment? = null
    private var webSettings: WebSettings? = null
    private val calendar: Calendar = Calendar.getInstance()
    private val reconnectionHandler = Handler(Looper.getMainLooper())
    private var connectionLiveData: ConnectionLiveData? = null
    override var isConnected = true
    private var webkitPermissionRequest: PermissionRequest? = null
    private var awaitingReconnect = false

    private val reloadPageRunnable = Runnable {
        initWebPageLoad()
    }

    // To save current index
    private var playlistIndex = 0

    private val playlistRunnable = object : Runnable {
        override fun run() {
            // TODO: allow users to set their own value in settings
            val offset = 60L - calendar.get(Calendar.SECOND)
            val urls: List<String> = configuration.appLaunchUrl.lines()
            // Avoid IndexOutOfBound
            playlistIndex = (playlistIndex + 1) % urls.size
            if (urls.isNotEmpty() && urls.size >= playlistIndex) {
                loadWebViewUrl(urls[playlistIndex])
                playlistHandler?.postDelayed(this, TimeUnit.SECONDS.toMillis(offset))
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            configuration.mqttBroker = BuildConfig.BROKER
            configuration.mqttUsername = BuildConfig.BROKER_USERNAME
            configuration.mqttPassword = BuildConfig.BROKER_PASS
            configuration.appLaunchUrl = BuildConfig.HASS_URL
            configuration.isFirstTime = false
            configuration.settingsCode = BuildConfig.CODE.toString()
            configuration.hasClockScreenSaver = true
        }

        binding = ActivityBrowserBinding.inflate(layoutInflater)
        try {
            setContentView(binding.root)
        } catch (e: Exception) {
            Timber.e(e.message)
            AlertDialog.Builder(this@BrowserActivityNative)
                .setMessage(getString(R.string.dialog_missing_webview_warning))
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        binding.launchSettingsFab.setOnClickListener {
            if (configuration.isFirstTime) {
                openSettings()
            } else {
                showCodeBottomSheet()
            }
        }

        configureConnection()
        configureWebView(binding.root)
        initWebPageLoad()
    }

    override fun onStart() {
        super.onStart()
        if (configuration.useDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            setLightTheme()
        }

        if (configuration.hardwareAccelerated) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        if (configuration.browserRefresh) {
            binding.swipeContainer.setOnRefreshListener {
                clearCache()
                initWebPageLoad()
            }
            mOnScrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
                binding.swipeContainer?.isEnabled = webView.scrollY == 0
            }
            binding.swipeContainer.viewTreeObserver.addOnScrollChangedListener(mOnScrollChangedListener)
        } else {
            binding.swipeContainer.isEnabled = false
        }

        setupSettingsButton()

        if (configuration.hasSettingsUpdates()) {
            initWebPageLoad()
        }
    }

    override fun onStop() {
        super.onStop()
        val view = binding.root
        if (mOnScrollChangedListener != null && configuration.browserRefresh) {
            binding.swipeContainer.viewTreeObserver.removeOnScrollChangedListener(mOnScrollChangedListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        codeBottomSheet?.dismiss()
    }

    override fun openSettings() {
        hideScreenSaver()
        // Stop our service for performance reasons and to pick up changes
        stopService(wallPanelService)
        val intent = SettingsActivity.createStartIntent(this)
        startActivity(intent)
    }

    override fun loadWebViewUrl(url: String) {
        Timber.d("loadUrl $url")
        if (url.startsWith("intent:")) {
            val launchIntent: Intent
            try {
                launchIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            } catch (ex: URISyntaxException) {
                Timber.e("Bad URI $url: $ex.message")
                dialogUtils.showAlertDialog(webView.context, resources.getString(R.string.dialog_message_invalid_intent))
                return
            }
            val selector = launchIntent.selector
            if (selector != null) {
                selector.addCategory(Intent.CATEGORY_BROWSABLE)
                selector.setComponent(null)
            }
            launchIntent.putExtra(Browser.EXTRA_APPLICATION_ID, webView.context.packageName);
            webView.context.startActivity(launchIntent)
        } else {
            webView.loadUrl(url)
        }
    }

    override fun evaluateJavascript(js: String) {
        webView.evaluateJavascript(js, null)
    }

    override fun clearCache() {
        webView.clearCache(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
        }
    }

    override fun reload() {
        webView.reload()
    }

    @SuppressLint("SetJavaScriptEnabled")
    // TODO handle deprecated web settings
    override fun configureWebSettings(userAgent: String) {
        if (webSettings == null) {
            webSettings = webView.settings
        }
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.databaseEnabled = true
        webSettings?.saveFormData = true
        webSettings?.javaScriptCanOpenWindowsAutomatically = true
        webSettings?.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings?.allowFileAccess = true
        webSettings?.allowFileAccessFromFileURLs = true
        webSettings?.allowContentAccess = true
        webSettings?.setSupportZoom(true)
        webSettings?.loadWithOverviewMode = true
        webSettings?.useWideViewPort = true
        webSettings?.pluginState = WebSettings.PluginState.ON
        webSettings?.setRenderPriority(WebSettings.RenderPriority.HIGH)
        // webSettings?.cacheMode = WebSettings.LOAD_NO_CACHE;
        webSettings?.mediaPlaybackRequiresUserGesture = false

        if (userAgent.isNotEmpty()) {
            webSettings?.userAgentString = userAgent
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    override fun complete() {
        if (binding.swipeContainer != null && binding.swipeContainer.isRefreshing && configuration.browserRefresh) {
            binding.swipeContainer.isRefreshing = false
        }
    }

    override fun setWebkitPermissionRequest(request: PermissionRequest?) {
        webkitPermissionRequest = request
    }

    override fun displayProgress(): Boolean {
        return displayProgress
    }

    override fun startReloadDelay() {
        awaitingReconnect = true
        playlistHandler?.removeCallbacksAndMessages(null)
        reconnectionHandler.postDelayed(reloadPageRunnable, 30000)
    }

    override fun certPermissionsShown(): Boolean {
        return certPermissionsShown
    }

    override fun stopReloadDelay() {
        awaitingReconnect = false
        reconnectionHandler.removeCallbacks(reloadPageRunnable)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun askForWebkitPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        } else {
            webkitPermissionRequest?.grant(webkitPermissionRequest?.resources)
        }
    }

    private fun configureConnection() {
        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData?.observe(this) { connected ->
            if (connected && isConnected.not()) {
                isConnected = true
                if (awaitingReconnect) { // reload the page if there was error initially loading page due to network disconnect
                    stopReloadDelay()
                    initWebPageLoad()
                } else if (configuration.browserRefreshDisconnect) { // reload page on network reconnect
                    initWebPageLoad()
                }
            } else if (connected.not()) {
                isConnected = false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configureWebView(view: ViewGroup) {
        webView = binding.activityBrowserWebviewNative
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // Force links and redirects to open in the WebView instead of in a browser
        configureWebChromeClient()
        configureWebViewClient()

        webView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    resetScreen()
                    if (!v.hasFocus()) {
                        v.requestFocus()
                    }
                }
                MotionEvent.ACTION_UP -> if (!v.hasFocus()) {
                    v.requestFocus()
                }
            }
            false
        }
    }

    private fun configureWebChromeClient() {
        webView.webChromeClient = InternalWebChromeClient(resources = resources, callback = this)
    }

    private fun configureWebViewClient() {
        webView.webViewClient = InternalWebClient(resources = resources, callback = this, configuration)
    }

    private fun initWebPageLoad() {
        binding.progressView.visibility = View.GONE
        webView.visibility = View.VISIBLE
        // set user agent
        configureWebSettings(configuration.browserUserAgent)
        // set zoom level
        if (zoomLevel != 0.0f) {
            val zoomPercent = (zoomLevel * 100).toInt()
            webView.setInitialScale(zoomPercent)
        }
        // check if we are using playlist
        if (configuration.appLaunchUrl.lines().size == 1) {
            loadWebViewUrl(configuration.appLaunchUrl)
        } else {
            startPlaylist()
        }
    }

    private fun startPlaylist() {
        playlistHandler = Handler(Looper.getMainLooper())
        playlistHandler?.postDelayed(playlistRunnable, 10)
    }



    private fun showCodeBottomSheet() {
        codeBottomSheet = CodeBottomSheetFragment.newInstance(configuration.settingsCode,
            object : CodeBottomSheetFragment.OnAlarmCodeFragmentListener {
                override fun onComplete(code: String) {
                    codeBottomSheet?.dismiss()
                    openSettings()
                }

                override fun onCodeError() {
                    Toast.makeText(
                        this@BrowserActivityNative,
                        R.string.toast_code_invalid,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancel() {
                    codeBottomSheet?.dismiss()
                }
            })
        codeBottomSheet?.show(supportFragmentManager, codeBottomSheet?.tag)
    }

    private fun setupSettingsButton() {
        // Set the location and transparency of the fab button
        val params: CoordinatorLayout.LayoutParams = CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            CoordinatorLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 16
        params.leftMargin = 16
        params.rightMargin = 16
        params.bottomMargin = 16
        when (configuration.settingsLocation) {
            0 -> {
                params.gravity = Gravity.BOTTOM or Gravity.END
            }
            1 -> {
                params.gravity = Gravity.BOTTOM or Gravity.START
            }
            2 -> {
                params.gravity = Gravity.TOP or Gravity.END
            }
            3 -> {
                params.gravity = Gravity.TOP or Gravity.START
            }
        }
        binding.launchSettingsFab.layoutParams = params
        when {
            configuration.settingsDisabled -> {
                binding.launchSettingsFab.visibility = View.GONE
            }
            configuration.settingsTransparent -> {
                binding.launchSettingsFab.visibility = View.VISIBLE
                binding.launchSettingsFab.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.transparent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    binding.launchSettingsFab.compatElevation = 0f
                }
                binding.launchSettingsFab.imageAlpha = 0
            }
            else -> {
                binding.launchSettingsFab.visibility = View.VISIBLE
                binding.launchSettingsFab.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorAccent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    binding.launchSettingsFab.compatElevation = 4f
                }
                binding.launchSettingsFab.imageAlpha = 180
            }
        }
    }

}
