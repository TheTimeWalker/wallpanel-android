---
title: MQTT Setup
sidebar_position: 1
---

MQTT can be used by the application to [publish application data, user presence, and device sensor data](./sensors.md).  You can also use MQTT to [send commands to the application](./commands.md).  To setup the application for MQTT, enable it under the setting.

![Settings MQTT](/img/mqtt.png)

You will need your MQTT broker IP address and port number if different from the default.  There is an option to use SSL with the port 8883. Usually enter the IP address without http/https (192.168.1.1) to use a TCP connection to the broker.  You can also use http/https by entering the fully qualified url (https://192.168.1.1).

The base topic `wallpanel/mywallpanel` allows the device to send and receive MQTT commands or messages.   The base topic should be unique to each device if you want the device to operate independently from other devices running the same application in your network.  If all devices have the same base topic, then sending a command will mean all devices receive the command.

If needed, add your MQTT username and password.  The client id is the unique identifier of this device with the MQTT broker.  It can be changed, but should be different from other applications on the same network.  

![Client ID](/img/mqtt_client.png)

Finally we have MQTT discovery. Enabling MQTT Discovery will publish device sensor data on the MQTT channel that can be discovered automatically by your home automation platform. Note that for sensor data, you must also enable sensor data publishing in the sensor settings.

![MQTT Discovery](/img/mqtt_discovery.png)
