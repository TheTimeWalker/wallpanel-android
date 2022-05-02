/*
 * Copyright (c) 2022 WallPanel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.wallpanel.app.modules

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.*
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3MessageException
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5MessageException
import xyz.wallpanel.app.network.IMqttManagerListener
import xyz.wallpanel.app.network.MQTT3Service
import xyz.wallpanel.app.network.MQTTOptions
import xyz.wallpanel.app.network.MQTT5Service

import timber.log.Timber

class MQTTModule (base: Context?, var mqttOptions: MQTTOptions, private val listener: MQTTListener) : ContextWrapper(base),
        LifecycleObserver,
        IMqttManagerListener, DefaultLifecycleObserver {

    private var mqtt3Service: MQTT3Service? = null
    private var mqtt5Service: MQTT5Service? = null

    override fun onStart(owner: LifecycleOwner) {
        Timber.d("start")
        startMqtt()
    }

    override fun onStop(owner: LifecycleOwner) {
        Timber.d("stop")
        stopMqtt()
    }

    private fun startMqtt() {
        if (mqttOptions.getVersion() == "3.1.1") {
            if (mqtt3Service == null) {
                try {
                    mqtt3Service = MQTT3Service(applicationContext, mqttOptions, this)
                } catch (t: Throwable) {
                    // TODO should we loop back and try again?
                    Timber.e("Could not create MQTTPublisher: " + t.message)
                }
            } else {
                try {
                    mqtt3Service?.reconfigure(applicationContext, mqttOptions, this)
                } catch (t: Throwable) {
                    // TODO should we loop back and try again?
                    Timber.e("Could not create MQTTPublisher: " + t.message)
                }
            }
        } else {
            if (mqtt5Service == null) {
                try {
                    mqtt5Service = MQTT5Service(applicationContext, mqttOptions, this)
                } catch (t: Throwable) {
                    // TODO should we loop back and try again?
                    Timber.e("Could not create MQTTPublisher: " + t.message)
                }
            } else {
                try {
                    mqtt5Service?.reconfigure(applicationContext, mqttOptions, this)
                } catch (t: Throwable) {
                    // TODO should we loop back and try again?
                    Timber.e("Could not create MQTTPublisher: " + t.message)
                }
            }
        }
    }

    private fun stopMqtt() {
        mqtt5Service?.let {
            try {
                it.close()
            } catch (e: Mqtt5MessageException) {
                e.printStackTrace()
            }
            mqtt5Service = null
        }
        mqtt3Service?.let {
            try {
                it.close()
            } catch (e: Mqtt3MessageException) {
                e.printStackTrace()
            }
            mqtt5Service = null
        }
    }

    fun restart() {
        Timber.d("restart")
        stopMqtt()
        startMqtt()
    }

    fun pause() {
        Timber.d("pause")
        stopMqtt()
    }

    fun publish(topic: String, message : String, retain: Boolean) {
        Timber.d("topic: $topic")
        Timber.d("message: $message")
        Timber.d("retain: $retain")
        mqtt5Service?.publish(topic, message, retain)
        mqtt3Service?.publish(topic, message, retain)
    }

    override fun subscriptionMessage(id: String, topic: String, payload: String) {
        Timber.d("topic: $topic")
        listener.onMQTTMessage(id, topic, payload)
    }

    override fun handleMqttException(errorMessage: String) {
        listener.onMQTTException(errorMessage)
    }

    override fun handleMqttDisconnected() {
        listener.onMQTTDisconnect()
    }

    override fun handleMqttConnected() {
        listener.onMQTTConnect()
    }

    interface MQTTListener {
        fun onMQTTConnect()
        fun onMQTTDisconnect()
        fun onMQTTException(message : String)
        fun onMQTTMessage(id: String, topic: String, payload: String)
    }
}