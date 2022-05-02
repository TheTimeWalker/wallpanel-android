package com.thanksmister.iot.wallpanel.ext


fun String.convertStringToArray(str: String): Array<String> {
    val strSeparator = ","
    return str.split(strSeparator).toTypedArray()
}
