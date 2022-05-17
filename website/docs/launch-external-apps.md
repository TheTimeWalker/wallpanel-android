---
title: Launch External Applications
---

Using the url command `{"url": "http://<url>"}`, you can load other applications using the `intent: scheme URL` for that application. For exxample, to launch the Ring app, you would use this as the url schema:

```plain
intent:#Intent;launchFlags=0x10000000;component=com.ringapp/.ui.activities.LoginActivity;end
```

For a list of more intent schema urls, visit https://support.actiontiles.com/en/communities/12/topics/1255-open-android-app-or-app-activity-via-url-formatted-shortcut.
