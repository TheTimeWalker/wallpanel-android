package xyz.wallpanel.app.ext


fun String.convertStringToArray(str: String): Array<String> {
    val strSeparator = ","
    return str.split(strSeparator).toTypedArray()
}
