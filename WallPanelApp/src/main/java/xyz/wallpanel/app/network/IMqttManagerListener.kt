package xyz.wallpanel.app.network

interface IMqttManagerListener {
    fun subscriptionMessage(id: String, topic: String, payload: String)
    fun handleMqttException(errorMessage: String)
    fun handleMqttDisconnected()
    fun handleMqttConnected()
}