package xyz.wallpanel.app.network

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5AuthException
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5MessageException
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
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

class MQTT5Service(
    private var context: Context, options: MQTTOptions,
    private var listener: IMqttManagerListener?
) : MQTTServiceInterface {

    private var mqtt5AsyncClient: Mqtt5AsyncClient? = null
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
        } catch (e: Mqtt5MessageException) {
            // empty
        }
        this.listener = listener
        this.context = context
        initialize(newOptions)
    }

    override val isReady: Boolean
        get() = mReady.get()

    @Throws(Mqtt5MessageException::class)
    override fun close() {
        Timber.d("close")
        mqtt5AsyncClient?.let {

            mqttOptions?.let {
                val offlineMessage =
                    Mqtt5Publish.builder().topic("${it.getBaseTopic()}${CONNECTION}")
                        .payload(OFFLINE.toByteArray()).retain(true).build()
                sendMessage(offlineMessage)
            }
            mqtt5AsyncClient = null
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
                        Mqtt5Publish.builder().topic(topic).payload(payload.toByteArray())
                            .retain(retain).build()
                    sendMessage(mqttMessage)
                }
            }
        } catch (e: Mqtt5MessageException) {
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
            Timber.i(
                "Subscribed to state topics: %s", mqttOptions!!.getStateTopics()
                    .convertArrayToString()
            )
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
        } catch (e: Mqtt5MessageException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        } catch (e: IOException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        } catch (e: GeneralSecurityException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        }
    }

    @SuppressLint("NewApi")
    @Throws(
        Mqtt5MessageException::class,
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
                        Mqtt5Publish.builder().topic("${mqttOptions.getBaseTopic()}${CONNECTION}")
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

                mqtt5AsyncClient = mqttBuilder.useMqttVersion5().build().toAsync()
                val clientConnect = mqtt5AsyncClient!!.connectWith()
                clientConnect.cleanStart(false)
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
        } catch (e: Mqtt5AuthException) {
            Timber.e(e)
            listener?.handleMqttException("Failed to authenticate: " + e.message)
        } catch (e: Mqtt5ConnAckException) {
            Timber.e(e)
            listener?.handleMqttException("Failed to connect: " + e.message)
        } catch (e: Mqtt5MessageException) {
            Timber.e(e)
            listener?.handleMqttException("" + e.message)
        }
    }

    @Throws(Mqtt5MessageException::class)
    private fun sendMessage(mqttMessage: Mqtt5Publish) {
        Timber.d("sendMessage")
        mqtt5AsyncClient?.let {
            if (it.state.isConnected) {
                try {
                    it.publish(mqttMessage)
                    Timber.d("Command Topic: ${mqttMessage.topic} Payload: ${mqttMessage.payload}")
                } catch (e: NullPointerException) {
                    Timber.e(e)
                } catch (e: Mqtt5MessageException) {
                    Timber.e(e, "Error Sending Command: %s", e.message)
                    listener?.handleMqttException("Couldn't send message to the MQTT broker for topic ${mqttMessage.topic}, check the MQTT client settings or your connection to the broker.")
                }
            }
        }
    }

    //@TargetApi(Build.VERSION_CODES.N)
    private fun subscribeToTopics(topicFilters: Array<String>?) {
        topicFilters?.let {
            Timber.d("Subscribe to Topics: %s", topicFilters.convertArrayToString())
            mqtt5AsyncClient?.let {
                try {
                    it.subscribeWith().topicFilter(topicFilters.convertArrayToString()).send()
                    it.publishes(MqttGlobalPublishFilter.ALL) { publish ->
                        listener?.subscriptionMessage(
                            it.config.clientIdentifier.get().toString(),
                            publish.topic.toString(),
                            Charsets.UTF_8.decode(publish.payload.get()).toString()
                        )
                    }
                } catch (e: NullPointerException) {
                    Timber.e(e)
                } catch (e: Mqtt5MessageException) {
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



