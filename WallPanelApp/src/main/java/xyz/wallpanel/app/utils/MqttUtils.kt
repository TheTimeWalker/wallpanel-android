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

package xyz.wallpanel.app.utils

import java.util.*

/**
 * Just a utility class to work with the specific settings of the MQTT
 */
class MqttUtils {
    companion object {

        const val PORT = 1883
        const val TOPIC_COMMAND = "command"
        const val COMMAND_STATE = "state"
        const val VALUE = "value"
        const val COMMAND_SENSOR_FACE = "sensor/face"
        const val COMMAND_SENSOR_QR_CODE = "sensor/qrcode"
        const val COMMAND_SENSOR_MOTION = "sensor/motion"
        const val STATE_CURRENT_URL = "currentUrl"
        const val STATE_SCREEN_ON = "screenOn"
        const val STATE_CAMERA = "camera"
        const val STATE_MOTION = "motion"
        const val STATE_BRIGHTNESS = "brightness"
        const val COMMAND_SENSOR = "sensor/"
        const val COMMAND_URL = "url"
        const val COMMAND_CAMERA = "camera"
        const val COMMAND_SETTINGS = "settings"
        const val COMMAND_RELAUNCH = "relaunch"
        const val COMMAND_WAKE = "wake"
        const val COMMAND_WAKETIME = "wakeTime"
        const val COMMAND_BRIGHTNESS = "brightness"
        const val COMMAND_NOTIFICATION = "notification"
        const val COMMAND_RELOAD = "reload"
        const val COMMAND_CLEAR_CACHE = "clearCache"
        const val COMMAND_EVAL = "eval"
        const val COMMAND_AUDIO = "audio"
        const val COMMAND_SPEAK = "speak"
        const val COMMAND_VOLUME = "volume"

        private val topicsList = ArrayList<String>()

        init {
            topicsList.add(TOPIC_COMMAND)
        }

        /**
         * We need to make an array of listeners to pass to the subscribe topics.
         * @param length
         * @return
         */
        /*fun getMqttMessageListeners(length: Int, listener: MQTTService.MqttManagerListener?): Array<IMqttMessageListener?> {
            val mqttMessageListeners = arrayOfNulls<IMqttMessageListener>(length)
            for (i in 0 until length) {
                val mqttMessageListener = IMqttMessageListener { topic, message ->
                    val payload = String(message.payload, Charset.forName("UTF-8"))
                    Timber.i("Subscribe Topic: " + topic + "  Payload: " + payload)
                    Timber.i("Subscribe Topic Listener: " + listener!!)
                    listener.subscriptionMessage(message.id.toString(), topic, payload)
                }
                mqttMessageListeners[i] = mqttMessageListener
            }
            return mqttMessageListeners
        }*/

        /**
         * Generate an array of QOS values for subscribing to multiple topics.
         * @param length
         * @return
         */
        fun getQos(length: Int): IntArray {
            val qos = IntArray(length)
            for (i in 0 until length) {
                qos[i] = 0
            }
            return qos
        }

    }
}