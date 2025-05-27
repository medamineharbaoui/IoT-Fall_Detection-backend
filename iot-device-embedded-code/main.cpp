#include <Arduino.h>
#include <Adafruit_NeoPixel.h>
#include <Adafruit_SSD1306.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <MyLD2410.h>
#include <Wire.h>
#include <WiFi.h>
#include <PubSubClient.h>

#define SERIAL_BAUD_RATE 115200U

#define LD2410_SERIAL Serial2
#define LD2410_RX_PIN 18
#define LD2410_TX_PIN 17

#define DHT_TYPE DHT22
#define DHT_PIN  8U

#define PIR_PIN 9U

#define OLED_SDA           6
#define OLED_SCL           7
#define OLED_SCREEN_WIDTH  128U
#define OLED_SCREEN_HEIGHT 64U

#define NEOPIXEL_PIN   48
#define NEOPIXEL_COUNT 1U

// Wi-Fi and MQTT
const char* ssid = "POCO X3 NFC";
const char* password = "11223344";
const char* mqttServer = "broker.hivemq.com";
const int mqttPort = 1883;
const char* clientID = "clientId-49XKOF7Dwl";

// Fall detection parameters
#define FALL_DISTANCE_THRESHOLD 30   // cm, distance increase to detect fall
#define FALL_TIME_WINDOW 2000       // ms, time window for distance change
#define FALL_HEIGHT_THRESHOLD 250   // cm, max distance for fall detection

WiFiClient espClient;
PubSubClient client(espClient);

MyLD2410 radar(LD2410_SERIAL);
DHT dht(DHT_PIN, DHT_TYPE);
Adafruit_SSD1306 display(OLED_SCREEN_WIDTH, OLED_SCREEN_HEIGHT);
Adafruit_NeoPixel led(NEOPIXEL_COUNT, NEOPIXEL_PIN, NEO_GRB + NEO_KHZ800);

// Fall detection variables
unsigned long lastDistance = 0;
unsigned long lastTime = 0;
bool fallDetected = false;
long fallDistance = 0;

void connectToWiFi() {
    WiFi.begin(ssid, password);
    Serial.println("Connecting to WiFi...");
    while (WiFi.status() != WL_CONNECTED) {
        delay(5000);
        Serial.print(".");
    }
    Serial.println("WiFi connected!");
}

void connectToMQTT() {
    while (!client.connected()) {
        Serial.print("Connecting to MQTT...");
        if (client.connect(clientID)) {
            Serial.println("MQTT connected!");
            delay(5000);
        } else {
            Serial.print("failed with state ");
            Serial.println(client.state());
            delay(5000);
        }
    }
}

void setup() {
    bool success = false;
    bool oledSuccess = false;

    Serial.begin(SERIAL_BAUD_RATE);
    Serial.println("Starting setup...");

    do {
        Serial.println("Initializing I2C...");
        success = Wire.begin(OLED_SDA, OLED_SCL);
        if (!success) {
            Serial.println("Failed to initialize I2C!");
            break;
        }

        Serial.println("Initializing OLED...");
        oledSuccess = success = display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
        if (!success) {
            Serial.println("Failed to initialize OLED!");
            break;
        }

        display.clearDisplay();
        display.setTextSize(1);
        display.setTextColor(SSD1306_WHITE);
        display.setCursor(0, 0);
        display.println("Initializing...");
        display.display();

        Serial.println("Initializing LD2410 Serial...");
        LD2410_SERIAL.begin(LD2410_BAUD_RATE, SERIAL_8N1, LD2410_RX_PIN, LD2410_TX_PIN);

        Serial.println("Initializing LD2410...");
        success = radar.begin();
        if (!success) {
            Serial.println("Failed to initialize LD2410!");
            break;
        }

        Serial.println("Configuring LD2410 enhanced mode...");
        success = radar.configMode(true);
        if (!success) {
            Serial.println("Failed to enter config mode!");
            break;
        }
        success = radar.enhancedMode(true);
        if (!success) {
            Serial.println("Failed to enable enhanced mode!");
            break;
        }
        success = radar.configMode(false);
        if (!success) {
            Serial.println("Failed to exit config mode!");
            break;
        }

        Serial.println("Initializing DHT22...");
        dht.begin();
        pinMode(PIR_PIN, INPUT);
        led.begin();
        led.show();

    } while (false);

    if (!success) {
        if (oledSuccess) {
            display.clearDisplay();
            display.setCursor(0, 0);
            display.println("Initialization failed!");
            display.display();
        }
        while (true) {
            Serial.println("Initialization failed!");
            delay(1000);
        }
    }

    Serial.println("Connecting to WiFi...");
    connectToWiFi();
    client.setServer(mqttServer, mqttPort);
    Serial.println("Connecting to MQTT...");
    connectToMQTT();

    lastTime = millis();
    Serial.println("Setup complete!");
    delay(1000);
}

void loop() {
    radar.check();
    const bool presence = radar.presenceDetected();
    const float temperature = dht.readTemperature();
    const float humidity = dht.readHumidity();
    const bool pirState = digitalRead(PIR_PIN);

    unsigned long currentDistance = radar.movingTargetDistance();
    unsigned long currentTime = millis();
    unsigned long timeDiff = currentTime - lastTime;

    Serial.printf("Debug: currentTime = %lu ms\n", currentTime);

    if (radar.movingTargetDetected() && lastDistance > 0) {
        long distanceChange = currentDistance - lastDistance;
        fallDistance = distanceChange;
        if (timeDiff <= FALL_TIME_WINDOW && 
            distanceChange >= FALL_DISTANCE_THRESHOLD && 
            currentDistance <= FALL_HEIGHT_THRESHOLD) {
            fallDetected = true;
        } else {
            fallDetected = false;
        }
    } else {
        fallDetected = false;
        fallDistance = 0;
    }

    lastDistance = currentDistance;
    lastTime = currentTime;

    Serial.printf(
        "LD2410: %s | PIR: %s | Temp: %.1f C | Hum: %.1f %% | Fall: %s | Fall Dist: %ld cm | Curr Dist: %lu cm | Time Diff: %lu ms\n",
        presence ? "YES" : "NO ",
        pirState ? "YES" : "NO ",
        temperature,
        humidity,
        fallDetected ? "DETECTED" : "NO FALL",
        fallDistance,
        currentDistance,
        timeDiff
    );

    display.clearDisplay();
    display.setCursor(0, 0);
    display.printf("LD2410: %s\n", presence ? "YES" : "NO");
    display.printf("PIR: %s\n", pirState ? "YES" : "NO");
    display.printf("Temp: %.1f C\n", temperature);
    display.printf("Hum: %.1f %%\n", humidity);
    display.printf("Fall: %s\n", fallDetected ? "DETECTED" : "NO FALL");
    display.printf("Fall Dist: %ld cm\n", fallDistance);
    display.display();

    led.setPixelColor(
        0u, (fallDetected ? led.Color(255u, 0u, 0u) : 
             (presence || pirState) ? led.Color(0u, 255u, 0u) : led.Color(255u, 0u, 0u)));
    led.show();

    if (!client.connected()) {
        connectToMQTT();
    }
    client.loop();

    // Publish booleans to separate topics as 1/0
    client.publish("harbaoui/esp32/presence", presence ? "1" : "0");
    client.publish("harbaoui/esp32/motion", pirState ? "1" : "0");
    client.publish("harbaoui/esp32/fall", fallDetected ? "1" : "0");

    // Publish other data as JSON to original topic
    String payload = String("{\"temperature\":") + temperature +
                     ",\"humidity\":" + humidity +
                     ",\"fall_distance\":" + fallDistance + "}";
    client.publish("harbaoui/esp32/sensors_data", payload.c_str());

    delay(1000);
}