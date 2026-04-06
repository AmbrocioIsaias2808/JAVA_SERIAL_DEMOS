#include <ArduinoJson.h>

int led_temp=2;
int led_hum=3;

void setup() {
  Serial.begin(9600);
  randomSeed(200);
  pinMode(led_temp, OUTPUT);
  pinMode(led_hum, OUTPUT);

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

  //INICIO: CÓDIGO NUEVO
    if(Serial.available()){

        String datosRecibidos = Serial.readStringUntil('\n');
        
        JsonDocument jsonConDatos;
        deserializeJson(jsonConDatos, datosRecibidos);

        if(jsonConDatos["alarma_temp"]=="ON"){
          digitalWrite(led_temp, HIGH);
        }else{
          digitalWrite(led_temp, LOW);
        }

        digitalWrite(led_hum, jsonConDatos["alarma_hum"]);


    }
  //FIN: CÓDIGO NUEVO


  serializeJson(datos, Serial);
  Serial.print("*");
  delay(1000);
}
