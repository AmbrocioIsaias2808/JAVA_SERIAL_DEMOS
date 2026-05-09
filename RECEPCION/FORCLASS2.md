
1. Arduino code:
```c++
#include <ArduinoJson.h> //<--- LA LIBRERIA PARA MANEJAR JSON EN ARDUINO
//NOTA: Este ejemplo usa la versión 7.0.4 de la librería

void setup() {
  Serial.begin(9600);
  randomSeed(200); //GENERO UNA SEMILLA PARA DATOS ALEATORIOS
}

JsonDocument datos; //VARIABLE PARA MANIPULAR JSON

void loop() {

  //GENERO NUMEROS ALEATORIOS DE MUESTRA PARA MIS DATOS  
  int vel           = random(0,100);
  int presion       = random(0,100);
  int temp          = random(0,100);
  String temp_type  = "C";
  int humedad       = random(0,100);

  //ARMO EL JSON SEGÚN LAS ESPECIFICACIONES QUE PEDÍ:  
  datos["velocidad"] = vel;
  datos["presion"]   = presion;
  datos["temp"]      = temp;
  datos["temp_type"] = temp_type;
  datos["humedad"]   = humedad;

  //SERIALIZE toma los datos generados, los formatea en json y los manda por el puerto serie
  serializeJson(datos, Serial);
  Serial.print("*"); //<---- mando el caracter de finalización
  delay(500); //un delay de 0.5 segundos para no saturarlo.
}


```

2. JAVA
```JAVA
                    /*CODIGO NUEVO: */
                    JSONObject json = new JSONObject(textoRecibido);

                    int temp            = json.getInt("temp");
                    String temp_type    = json.getString("temp_type");
                    int presion         = json.getInt("presion");
                    int velocidad       = json.getInt("velocidad");
                    int humedad         = json.getInt("humedad");
                    
                    System.out.println("Temperatura: "+ temp+" "+temp_type);
                    System.out.println("Presion:"+ presion);
                    System.out.println("Velocidad:"+velocidad);
                    System.out.println("Humedad:"+humedad);
                    System.out.println("");
                    //FIN DEL CÓDIGO NUEVO
```


3.
MODIFIQUE EL CÓDIGO DEL ARDIUNO PARA QUE SEA EL SENSOR DHT11 EL QUE OBTENGA LOS DATOS Y LOS ENVIE A JAVA