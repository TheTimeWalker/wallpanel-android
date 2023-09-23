---
title: Limitations
---

Android devices use WebView to render webpages, This WebView does not have full feature parity with Chrome for Android and therefore pages that render in Chrome may not render nicely in Wall Panel. For example, WebView that shipped with Android 4.4 (KitKat) devices is based on the same code as Chrome for Android version 30.

This WebView does not have full feature parity with Chrome for Android and is given the version number 30.0.0.0. If you find that you cannot render a webpage, it is most likely that the version of WebView on your device does not support the CSS/HTML of that page. You have little recourse but to update the webpage, as there is nothing to be done to the WebView to make it compatible with your code.

Setting WallPanel as the default Home application will always load this application as your home. Removing this feature is difficult without uninstalling the application. So please do this is you wish to use the application as a "kiosk" type application.
