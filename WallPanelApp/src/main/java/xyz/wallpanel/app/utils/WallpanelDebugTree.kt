package xyz.wallpanel.app.utils

import timber.log.Timber.DebugTree

class WallpanelDebugTree : DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        // returns a clickable Android Studio link
        return String.format(
            "%4\$s .%1\$s(%2\$s:%3\$s)",
            element.methodName,
            element.fileName,
            element.lineNumber,
            super.createStackElementTag(element)
        )
    }
}