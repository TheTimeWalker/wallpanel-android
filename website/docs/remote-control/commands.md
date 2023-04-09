---
title: MQTT and HTTP commands
---

Interact and control the application and device remotely using either MQTT or HTTP (REST) commands, including using your device as an announcer with Google Text-To-Speach.

## Commands

Key | Value | Example Payload | Description
-|-|-|-
clearCache | true | ```{"clearCache": true}``` | Clears the browser cache
eval | JavaScript | ```{"eval": "alert('Hello World!');"}``` | Evaluates Javascript in the dashboard
audio | URL | ```{"audio": "http://<url>"}``` | Play the audio specified by the URL immediately
relaunch | true | ```{"relaunch": true}``` | Relaunches the dashboard from configured launchUrl
reload | true | ```{"reload": true}``` | Reloads the current page immediately
url | URL | ```{"url": "http://<url>"}``` | Browse to a new URL immediately
wake | true | ```{"wake": true, "wakeTime": 180}``` | Wakes the screen if it is asleep. Optional wakeTime (in seconds). If no wake time provided, screen will wake but return to screensaver mode on user inactivity.  Sending false value will return app to normal screensaver mode and display screensaver on user inactivity.
wake | false | ```{"wake": false}``` | Release screen wake (Note: screen will not turn off before Androids Display Timeout finished)
speak | data | ```{"speak": "Hello!"}``` | Uses the devices TTS to speak the message
settings | data | ```{"settings": true}``` | Opens the settings screen remotely.
brightness | data | ```{"brightness": 1}``` | Changes the screens brightness, value 1-255.
camera | data | ```{"camera": true}``` | Turns on/off camera, this will also disable streaming, motion, QRCode, and face detection.
volume | data | ```{"volume": 100}``` | Changes the audio volume, value 0-100 (in %. Does not effect TTS volume).

* The base topic value (default is "mywallpanel") should be unique to each device running the application unless you want all devices to receive the same command. The base topic and can be changed in the applications ```MQTT settings```.
* Commands are constructed via valid JSON. It is possible to string multiple commands together:
  * eg, ```{"clearCache":true, "relaunch":true}```
* For REST
  * POST the JSON to URL ```http://<the.device.ip.address>:2971/api/command```
* For MQTT
  * WallPanel subscribes to topic ```wallpanel/[baseTopic]/command```
    * Default Topic: ```wallpanel/mywallpanel/command```
  * Publish a JSON payload to this topic (be mindful of quotes in JSON should be single quotes not double)

## Google Text-To-Speech (TTS) Command

You can send a command using either HTTP or MQTT to have the device speak a message using Google's Text-To-Speach. Note that the device must be running Android Lollipop or above.

Example format for the message topic and payload:

```json
{"topic":"wallpanel/mywallpanel/command", "payload":"{'speak':'Hello!'}"}
```

If you are using HTTP and sending text with special characters, such as those used in a Cyrillic or Spanish language, you would need to make sure your content type is set to utf-8, here is an example using curl to post a message in Spanish:

```sh
curl --location --request POST 'http://192.168.1.1:2971/api/command' \
--header 'Content-Type: application/json;charset=UTF-8' \
--data-raw '{
    "speak": "¡Aló mundo"                        
}'
```
