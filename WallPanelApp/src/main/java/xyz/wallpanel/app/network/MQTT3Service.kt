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

package xyz.wallpanel.app.network

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3MessageException
import com.hivemq.client.mqtt.mqtt3.message.disconnect.Mqtt3Disconnect
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.util.TypeSwitch
import xyz.wallpanel.app.R
import xyz.wallpanel.app.ext.convertArrayToString
import timber.log.Timber
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import kotlin.text.Charsets.UTF_8


class MQTT3Service(
    private var context: Context, options: MQTTOptions,
    private var listener: IMqttManagerListener?
) : MQTTServiceInterface {

    private var mqtt3AsyncClient: Mqtt3AsyncClient? = null
    private var mqttOptions: MQTTOptions? = null
    private val mReady = AtomicBoolean(false)

    init {
        initialize(options)
    }

    override fun reconfigure(
        context: Context,
        newOptions: MQTTOptions,
        listener: IMqttManagerListener
    ) {
        try {
            close()
        } catch (e: Mqtt3MessageException) {
            // empty
        }
        this.listener = listener
        this.context = context
        initialize(newOptions)
    }

    override val isReady: Boolean
        get() = mReady.get()

    @Throws(Mqtt3MessageException::class)
    override fun close() {
        Timber.d("close")

        mqtt3AsyncClient?.let {

            mqttOptions?.let {
                val offlineMessage =
                    Mqtt3Publish.builder().topic("${it.getBaseTopic()}${CONNECTION}")
                        .payload(OFFLINE.toByteArray()).retain(true).build()
                sendMessage(offlineMessage)
            }

            mqtt3AsyncClient = null
            listener = null
            mqttOptions = null
        }
        mReady.set(false)
    }

    override fun publish(topic: String, payload: String, retain: Boolean) {
        try {
            if (isReady) {
                mqttOptions?.let {
                    val mqttMessage =
                        Mqtt3Publish.builder().topic(topic).payload(payload.toByteArray())
                            .retain(retain).build()
                    sendMessage(mqttMessage)
                }
            }
        } catch (e: Mqtt3MessageException) {
            listener?.handleMqttException("Exception while publishing command $topic and it's payload to the MQTT broker.")
        }
    }

    /**
     * Initialize a Cloud IoT Endpoint given a set of configuration options.
     * @param options Cloud IoT configuration options.
     */
    private fun initialize(options: MQTTOptions) {
        Timber.d("initialize")
        try {
            mqttOptions = options
            Timber.i("Service Configuration:")
            Timber.i("Client ID: %s", mqttOptions!!.getClientId())
            Timber.i("Username: %s", mqttOptions!!.getUsername())
            Timber.i("Password: %s", mqttOptions!!.getPassword())
            Timber.i("TslConnect: %s", mqttOptions!!.getTlsConnection())
            Timber.i("MQTT Configuration:")
            Timber.i("Broker: %s", mqttOptions?.brokerUrl)
            Timber.i("Subscribed to state topics: %s", mqttOptions!!.getStateTopics().convertArrayToString())
            Timber.i("Publishing to base topic: %s", mqttOptions!!.getBaseTopic())
            mqttOptions?.let {
                if (it.isValid) {
                    initializeMqttClient()
                } else {
                    if (listener != null) {
                        listener!!.handleMqttDisconnected()
                    }
                }
            }
        } catch (e: Mqtt3MessageException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        } catch (e: IOException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        } catch (e: GeneralSecurityException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        }
    }

    @SuppressLint("NewApi")
    @Throws(
        Mqtt3MessageException::class,
        IOException::class,
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class
    )
    private fun initializeMqttClient() {
        Timber.d("initializeMqttClient")
        try {
            mqttOptions?.let { mqttOptions ->

                val mqttBuilder = MqttClient.builder().identifier(mqttOptions.getClientId())
                    .serverHost(mqttOptions.getBroker()).serverPort(mqttOptions.getPort())
                mqttBuilder.addConnectedListener { context: MqttClientConnectedContext? ->
                    Timber.d("connect to broker completed")
                    subscribeToTopics(mqttOptions.getStateTopics())

                    val onlineMessage =
                        Mqtt3Publish.builder().topic("${mqttOptions.getBaseTopic()}${CONNECTION}")
                            .payload(ONLINE.toByteArray()).retain(true).build()
                    sendMessage(onlineMessage)

                    // TODO: There needs to be a way to handle queues...
                    mReady.set(true)
                    listener?.handleMqttConnected()
                }
                mqttBuilder.addDisconnectedListener { context: MqttClientDisconnectedContext? ->
                    mReady.set(false)
                    listener?.handleMqttDisconnected()
                    mqttOptions.let {
                        Timber.e(
                            "Disconnected from: %s, exception: %s",
                            it.brokerUrl,
                            context?.cause?.message
                        )
                        listener?.handleMqttException("Error establishing MQTT connection to MQTT broker with address ${mqttOptions.brokerUrl}.")
                    }
                }

                mqtt3AsyncClient = mqttBuilder.useMqttVersion3().build().toAsync()
                val clientConnect = mqtt3AsyncClient!!.connectWith()
                clientConnect.cleanSession(false)
                clientConnect.willPublish().topic("${mqttOptions.getBaseTopic()}${CONNECTION}")
                    .payload(OFFLINE.toByteArray()).qos(
                    MqttQos.EXACTLY_ONCE
                ).retain(true).applyWillPublish()
                if (!TextUtils.isEmpty(mqttOptions.getUsername()) && !TextUtils.isEmpty(mqttOptions.getPassword())) {
                    clientConnect.simpleAuth().username(mqttOptions.getUsername())
                        .password(mqttOptions.getPassword().toByteArray()).applySimpleAuth()
                }
                val isConnected = clientConnect.send()
                if (isConnected.isDone) {
                    mReady.set(true)
                    return
                }
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        } catch (e: NullPointerException) {
            Timber.e(e)
        } catch (e: Mqtt3ConnAckException) {
            Timber.e(e)
            listener?.handleMqttException("Failed to connect: " + e.message)
        } catch (e: Mqtt3MessageException) {
            Timber.e(e)
            listener?.handleMqttException("" + e.message)
        }
    }

    @Throws(Mqtt3MessageException::class)
    private fun sendMessage(mqttMessage: Mqtt3Publish) {
        Timber.d("sendMessage")
        mqtt3AsyncClient?.let {
            if (it.state.isConnected) {
                try {
                    it.publish(mqttMessage)
                    Timber.d("Command Topic: ${mqttMessage.topic} Payload: ${mqttMessage.payload}")
                } catch (e: NullPointerException) {
                    Timber.e(e)
                } catch (e: Mqtt3MessageException) {
                    Timber.e("Error Sending Command: %s", e.message)
                    listener?.handleMqttException("Couldn't send message to the MQTT broker for topic ${mqttMessage.topic}, check the MQTT client settings or your connection to the broker.")
                }
            }
        }
    }

    //@TargetApi(Build.VERSION_CODES.N)
    private fun subscribeToTopics(topicFilters: Array<String>?) {
        topicFilters?.let {
            Timber.d("Subscribe to Topics: %s", topicFilters.convertArrayToString())
            mqtt3AsyncClient?.let {
                try {
                    it.subscribeWith().topicFilter(topicFilters.convertArrayToString()).send()
                    it.publishes(MqttGlobalPublishFilter.ALL) { publish ->
                        listener?.subscriptionMessage(
                            it.config.clientIdentifier.get().toString(),
                            publish.topic.toString(),
                            UTF_8.decode(publish.payload.get()).toString()
                        )
                    }
                } catch (e: NullPointerException) {
                    Timber.e(e)
                } catch (e: Mqtt3MessageException) {
                    Timber.e(e)
                    listener?.handleMqttException("Exception while subscribing: " + e.message)
                }
            }
        }
    }

    companion object {
        private const val ONLINE = "online"
        private const val OFFLINE = "offline"
        private const val CONNECTION = "connection"
    }
}