package xyz.wallpanel.app.network

import android.content.Context
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5MessageException

interface MQTTServiceInterface {
    val isReady: Boolean

    fun publish(topic: String, payload: String, retain: Boolean)
    fun reconfigure(context: Context, options: MQTTOptions, listener: IMqttManagerListener)

    @Throws(Mqtt5MessageException::class)
    fun close()
}