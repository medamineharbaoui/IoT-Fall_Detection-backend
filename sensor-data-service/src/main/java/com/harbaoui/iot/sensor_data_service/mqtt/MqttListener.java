package com.harbaoui.iot.sensor_data_service.mqtt;

import com.harbaoui.iot.sensor_data_service.service.SensorDataService;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

@Component
public class MqttListener {

    private final SensorDataService service;
    private MqttClient client;

    public MqttListener(SensorDataService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId(), new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);

            client.subscribe("+/esp32/#", (topic, message) -> {
                String payload = new String(message.getPayload());
                service.processSensorData(topic, payload);
            });

            System.out.println("✅ MQTT connected and subscribed to +/esp32/#");
        } catch (MqttException e) {
            System.err.println("❌ MQTT connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
