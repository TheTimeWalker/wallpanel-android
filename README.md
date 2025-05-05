# ARCHIVED

This project is now archived. As the maintainer of this project since 2022, I unfortunately didn't really live up to the standards to properly maintain this project. When taking over as maintainer, I was at the time motivated to work on it. However, in the coming years I have realized that with not having as much deep knowledge at native development, I wasn't able to work on it how I imagined to do. Additionally, I had more trouble really motivating myself, taking the necesssary free time and wrapping around developing this app. I then realized lately, that I personally haven't been using this app as extensively as I thought I would since I ended up only having one wall panel - which uses the Home Assistant companion app for this task. After ignoring issues and notifications all around, I've decided to archive this repository instead of disappointing additional people.

If anybody else wants to step up on continuing development and maintaining this project, you can contact me in the e-mail address inside the app and we can organize the transfer of the project, transfer for Google Play app, its signing keys, domain, and everything in-between.

Thank you!

# WallPanel

WallPanel is an Android application for Web Based Dashboards and Home Automation Platforms. You can either sideload the application to your Android device from the [release section](https://github.com/thetimewalker/wallpanel-android/releases) or install the application from [Google Play](https://play.google.com/store/apps/details?id=xyz.wallpanel.app).

<a href='https://play.google.com/store/apps/details?id=xyz.wallpanel.app&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width='240'/></a>

## Screenshots

<img src="img/dashboard2.png" width="640" />
<img src="img/dashboard3.png" width="640" />
<img src="img/dashboard1.png" width="640" />

## Support

For issues, feature requests, use the [Github issues tracker](https://github.com/thetimewalker/wallpanel-android/issues). For examples and to learn how to use each feature, visit [WallPanel Documentation](https://wallpanel.xyz/).

### Common Issues

Rendering issues with the webpage you are trying to view. Android applications use a component to render webpages, it's called the WebView component. WebView is not the same as Google Chrome app, it does not render the pages the same. The biggest issue is that your version of WebView is not capable of rendering the webpage you are trying to view. The only way possible to fix this issue is to update the WebView component (from Google Play Store), use a different webpage, or update your device OS.

## Features

- Web Based Dashboards and Home Automation Platforms support.
- Set application as Android Home screen (optional)
- Use code to access the settings and make the settings button invisible.
- Camera support for streaming video, motion detection, face detection, and QR Code reading.
- Google Text-to-Speech support to speak notification messages using MQTT or HTTP.
- MQTT or HTTP commands to remotely control device and application (url, brightness, wake, etc.).
- Sensor data reporting for the device (temperature, light, pressure, battery).
- Streaming MJPEG server support using the device camera.
- Screensaver feature that can be dismissed with motion or face detection.
- Support for Android 4.4 (API level 19) and greater devices.
- Support for launching external applications using intent URL

## Hardware & Software

- Android Device running Android OS 4.4 or greater. Note: The WebView shipped with Android 4.4 (KitKat) is based on the same code as Chrome for Android version 30. This WebView does not have full feature parity with Chrome for Android and is given the version number 30.0.0.0.

**_ If you have need support for older Android 4.0 devices (those below Android 4.4), you want to use the [legacy](https://github.com/thanksmister/wallpanel-android-legacy) version of the application. Alternatively you can download an APK from the release section prior to release v0.8.8-beta.6 _**

## Quick Start

You can either side load the application to your device from the [release section](https://github.com/thetimewalker/wallpanel-android/releases) or install the application from [Google Play](https://play.google.com/store/apps/details?id=xyz.wallpanel.app). The application will open to the welcome page with a link to update the settings. Open the settings by clicking the dashboard floating icon. In the settings, set your web page or home automation platform url. Also set the code for accessing the settings, the default is 1234.

## Building the Application

To build the application locally, checkout the code from Github and load the project into Android Studio with Android API 31 or higher. You will need to remove the Firebase dependency in the build.gradle file, this is not required. Remove the following dependencies:

```
apply plugin: 'com.google.firebase.crashlytics'

implementation 'com.google.firebase:firebase-core:20.1.1'
implementation 'com.google.firebase:firebase-crashlytics-ktx'
implementation 'com.google.firebase:firebase-analytics-ktx'
```

Remove this if you are building the application for devices that do not support Google Services.

```
apply plugin: 'com.google.gms.google-services'

implementation 'com.google.android.gms:play-services-vision:20.1.3'
```

The project should compile normally.

## Limitations

Android devices use WebView to render webpages, This WebView does not have full feature parity with Chrome for Android and therefore pages that render in Chrome may not render nicely in Wall Panel. For example, WebView that shipped with Android 4.4 (KitKat) devices is based on the same code as Chrome for Android version 30.

This WebView does not have full feature parity with Chrome for Android and is given the version number 30.0.0.0. If you find that you cannot render a webpage, it is most likely that the version of WebView on your device does not support the CSS/HTML of that page. You have little recourse but to update the webpage, as there is nothing to be done to the WebView to make it compatible with your code.

Setting WallPanel as the default Home application will always load this application as your home. Removing this feature is difficutl without uninstalling the application. So please do this is you wish to use the application as a "kiosk" type application.

## Contribution

All are welcome to propose a feature request, report or bug, or contribute to the project by updating examples or with a PR for new features. Thanks to all the [contributes](https://github.com/thetimewalker/wallpanel-android/graphs/contributors) who have contributed to the project!

## Special Thanks

- [ThanksMister](https://github.com/thanksmister) for maintaining and continued development of [WallPanel](https://github.com/thanksmister/wallpanel-android/) for multiple years.
- [quadportnick](https://github.com/quadportnick) for starting [the original WallPanel (formerly HomeDash)](https://github.com/WallPanel-Project/wallpanel-android).
