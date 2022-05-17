---
title: Screensaver and Brightness Control
---

On some older devices, there is no screensaver support such as Google Daydream that automatically dims the screen.  Therefore the application provides a screensaver feature.  This feature along with the screen brightness option, allows the screen to dim when the screensaver is active.  With the Camera and Motion feature, the device can be automatically awaken when motion is detected.  Optionally, you can send an MQTT command to wake the screen or just touch the screen to deactivate the screensaver.

There is setting to dim screen a set percentage when the screensaver is active, this requires the screen brightness setting be enabled. When set to a value above 0%, the screen will dim by the percent value set when the screensaver is active. So if the setting is 75%, the screen will dim to a value that is 75% of the default device brightness levels.

Using the screen brightness option requires some extra permissions.  This is because controlling the devices screen brightness is considered a dangerous permission by Google and so users have to manually turn this on.  When you first select the screen brightness option, you will be taken to the setting on your device to enable the permission.  The screen brightness feature behaves in the following manner:

- There is a general brightness setting that must be enabled (and permissions given) in order for the application to manually set the device brightness.  If at any time you revoke the permissions in the device settings for the application to control the brightness, this option will be disabled.

- The brightness mode can be disabled in the settings, returning the device back to its automatic brightness control. If brightness is disabled the application will no longer be able to change the devices brightness level including via MQTT commands.

- Brightness level is read at the time brightness control is enabled and permissions granted. However, there is also a new capture button in the settings to manually capture the devices current brightness level. To use this, first go into the app settings, then adjust your devices brightness level, then press the capture button to save the new brightness level.  The application will then use this brightness level to set the device brightness level.

- If you have brightness enabled, you can at any time manually set a new brightness level using the MQTT commands (see commands section above).  The device will then use the new setting as the default brightness level of the device.

