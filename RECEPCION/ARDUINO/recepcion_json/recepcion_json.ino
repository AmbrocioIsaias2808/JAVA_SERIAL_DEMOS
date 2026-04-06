#include <ArduinoJson.h>

void setup() {
  Serial.begin(9600);
  randomSeed(200);

}

JsonDocument datos;

void loop() {
  int vel           = random(0,100);
  int presion       = random(0,100);
  int temp          = random(0,100);
  String temp_type  = "C";
  int humedad       = random(0,100);

  datos["velocidad"] = vel;
  datos["presion"]   = presion;
  datos["temp"]      = temp;
  datos["temp_type"] = temp_type;
  datos["humedad"]   = humedad;

  serializeJson(datos, Serial);
  Serial.print("*");
  delay(5000);
}
