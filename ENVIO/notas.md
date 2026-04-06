# Puerto Serial en Java (ENVIO DE DATOS).

Como nota, este apartado es continuación de [Puerto Serial en Java (RECEPCIÓN DE DATOS)](../ENVIO/notas.md) el cual te invito a leer para no perderte en esta sección.

Continuando, la ocasión pasada vimos como usar java para conectarnos y recibir datos, aprendimos algo de como procesar json en ambas vias (java<-arduino). En este caso vamos a ver el otro lado de la moneda.

Repasemos nuestro código base de la ocasión pasada:

```java
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package serial_envio;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
import org.json.JSONObject;

/**
 *
 * @author isaias
 */
public class Serial_envio {

    static SerialPort con_serial;
    static String textoRecibido="";

    public static void main(String[] args) {
       int puerto=0;
       Scanner leer =  new Scanner(System.in);
       SerialPort[] portLists = SerialPort.getCommPorts();
       
       System.out.println("Hola, selecciona el puerto: ");
       for(int i=0; i<portLists.length;i++){
           System.out.println(i+". "+portLists[i].getSystemPortName());
       }
       puerto = leer.nextInt();
        con_serial =portLists[puerto];
        con_serial.setBaudRate(9600);
        con_serial.setNumDataBits(8);
        con_serial.setNumStopBits(1);
        con_serial.setParity(0);
        con_serial.openPort();
        
        if(con_serial.isOpen()){
            System.out.println("CONEXION EXITOSA");
            while(true){
                lectura(con_serial);
                sleep(1000);
            }
        }else{
            System.out.println("NO SE PUDO ESTABLECER UNA CONEXIÓN");
        }
        con_serial.closePort();
    }
    
    static void lectura(SerialPort activePort){
              // Read response (assuming data is available)
        byte[] readBuffer = new byte[1024];
        int numBytesRead = activePort.readBytes(readBuffer, 1024);
        if (numBytesRead > 0) {
            String response = new String(readBuffer, 0, numBytesRead);
            textoRecibido=textoRecibido+response;
            //System.out.println(textoRecibido);
            if(textoRecibido.endsWith("*")==true){
                textoRecibido=textoRecibido.substring(0, textoRecibido.indexOf("*"));
                //System.out.println(textoRecibido);
                
                
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
            
                textoRecibido="";
                
            }
        }
    }
    static void sleep(int i){
        try{
             Thread.sleep(i);
        }catch(Exception e){
            System.out.println("Error al dormir");
        }
    }
    
}

```

Vamos a modificarlo tantito, nadamas JAJAJAJAJAJ :v ok no. Empecemos agregando esta función al final de nuestra clase:

```java
    static void enviar(SerialPort activePort, String mensaje){
        OutputStream outputStream = con_serial.getOutputStream();
        
        try{
            outputStream.write(mensaje.getBytes());
            
        }catch(Exception e){
            System.out.println("Error inesperado");
        }
    
    }
```

Te imaginarás para que sirve, no?

La función recive dos parametros:
**activePort:** el objeto que en estos momentos nos esta ayudando a manicupar la conexión de nuestro puerto.
**mensaje:** pues el mensaje que queremos envías pues.

Aquí lo único que hacemos es que recibimos un string y:

```java 
//NOS COLGAMOS DEL PUERTO SERIAL:
OutputStream outputStream = con_serial.getOutputStream();
```
Para posteriormente intentar "**escribir**" un mensaje en el puerto serie.

```JAVA
        try{
            outputStream.write(mensaje.getBytes());
            
        }catch(Exception e){
            System.out.println("Error inesperado");
        }
```

**Notese:** que el mensaje tiene que ser convertido nuevamente a Bytes antes de ser enviado (.getBytes()).

Y ... y ... y ya.

Ahora, vamos a modificar esto un poco mas.

Deseo que al momento de recibir los datos de arduino, java los procese y me emita alertas. Por ejemplo:

* Si la temperatura pasa de 50 grados C debe activar una alarma (caso contrario la apaga).
* Si la humedad pasa de 70% debo activar otra alarma (caso contrario la apago).

Y deseo que java haga ese trabajo, arduino solo debe poder 

1. Enviarme los datos
2. Recibir ordenes de activación de alarmas

El punto 1 ya lo tenemos cubierto, el punto 2 es el que nos falta.

Empecemos desde el lado de JAVA. Modifiquemos la función lectura para hacer lo siguiente:

```java
static void lectura(SerialPort activePort){
              // Read response (assuming data is available)
        byte[] readBuffer = new byte[1024];
        int numBytesRead = activePort.readBytes(readBuffer, 1024);
        if (numBytesRead > 0) {
            String response = new String(readBuffer, 0, numBytesRead);
            textoRecibido=textoRecibido+response;
            //System.out.println(textoRecibido);
            if(textoRecibido.endsWith("*")==true){
                textoRecibido=textoRecibido.substring(0, textoRecibido.indexOf("*"));
                //System.out.println(textoRecibido);
                
               
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
                
                //INCIO: CÓDIGO NUEVO
                    JSONObject jsonEnviar = new JSONObject();
                
                    if(temp>50){
                        jsonEnviar.put("alarma_temp", "ON");
                    }else{
                        jsonEnviar.put("alarma_temp", "OFF");
                    }
                    
                    if(humedad>70){
                        jsonEnviar.put("alarma_hum", 1);
                    }else{
                        jsonEnviar.put("alarma_hum", 0);
                    }
                    
                    enviar(activePort, jsonEnviar.toString());
                    
                //FIN: CÓDIGO NUEVO
                
                textoRecibido="";
                //enviar(activePort, "HOLA DESDE JAVA");
            }
        }
    }
```

Analicemos el código nuevo:

```java
//INCIO: CÓDIGO NUEVO
                    JSONObject jsonEnviar = new JSONObject();
                
                    if(temp>50){
                        jsonEnviar.put("alarma_temp", "ON");
                    }else{
                        jsonEnviar.put("alarma_temp", "OFF");
                    }
                    
                    if(humedad>70){
                        jsonEnviar.put("alarma_hum", 1);
                    }else{
                        jsonEnviar.put("alarma_hum", 0);
                    }
                    
                    enviar(activePort, jsonEnviar.toString());
                    
                //FIN: CÓDIGO NUEVO
```

Empezamos creando una variable **jsonEnviar** para contener el json que deseo formar.
Posteriormente paso a evaluar la temperatura que anteriormente hemos recibido he indicamos que si supera nuestros rangos pemitidos debe enviar una instrucción de encendido u apagado según sea el caso.

Misma situación con la humedad.

> **Nota:** en la temperatura estoy enviando una palabra (**ON**/**OFF**) y en la humedad un número entero (**1**/**0**). Esto es simplemente por temas didacticos y para ver como se usa la librería json en distintos casos.

Finalmente convertirmos el json en un string y lo pasamos a la función enviar para enviar el json, el cual debe tener un lookandfeel como este:

```java
{
    "alarma_temp" : "ON",
    "alarma_hum"  :  0
}
```

Al final debemos tener un código como este:

```java

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package serial_envio;

import com.fazecast.jSerialComm.SerialPort;
import java.io.OutputStream;
import java.util.Scanner;
import org.json.JSONObject;

/**
 *
 * @author isaias
 */
public class Serial_envio {

    static SerialPort con_serial;
    static String textoRecibido="";

    public static void main(String[] args) {
       int puerto=0;
       Scanner leer =  new Scanner(System.in);
       SerialPort[] portLists = SerialPort.getCommPorts();
       
       System.out.println("Hola, selecciona el puerto: ");
       for(int i=0; i<portLists.length;i++){
           System.out.println(i+". "+portLists[i].getSystemPortName());
       }
       puerto = leer.nextInt();
        con_serial =portLists[puerto];
        con_serial.setBaudRate(9600);
        con_serial.setNumDataBits(8);
        con_serial.setNumStopBits(1);
        con_serial.setParity(0);
        con_serial.openPort();
        
        if(con_serial.isOpen()){
            System.out.println("CONEXION EXITOSA");
            while(true){
                lectura(con_serial);
                sleep(1000);
            }
        }else{
            System.out.println("NO SE PUDO ESTABLECER UNA CONEXIÓN");
        }
        con_serial.closePort();
    }
    
    static void lectura(SerialPort activePort){
              // Read response (assuming data is available)
        byte[] readBuffer = new byte[1024];
        int numBytesRead = activePort.readBytes(readBuffer, 1024);
        if (numBytesRead > 0) {
            String response = new String(readBuffer, 0, numBytesRead);
            textoRecibido=textoRecibido+response;
            //System.out.println(textoRecibido);
            if(textoRecibido.endsWith("*")==true){
                textoRecibido=textoRecibido.substring(0, textoRecibido.indexOf("*"));
                //System.out.println(textoRecibido);
                
               
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
                
                //INCIO: CÓDIGO NUEVO
                    JSONObject jsonEnviar = new JSONObject();
                
                    if(temp>50){
                        jsonEnviar.put("alarma_temp", "ON");
                    }else{
                        jsonEnviar.put("alarma_temp", "OFF");
                    }
                    
                    if(humedad>70){
                        jsonEnviar.put("alarma_hum", 1);
                    }else{
                        jsonEnviar.put("alarma_hum", 0);
                    }
                    
                    enviar(activePort, jsonEnviar.toString());
                    
                    
                //FIN: CÓDIGO NUEVO
                
                textoRecibido="";
                //enviar(activePort, "HOLA DESDE JAVA");
            }
        }
    }
    
     static void enviar(SerialPort activePort, String mensaje){
        OutputStream outputStream = con_serial.getOutputStream();
        
        try{
            outputStream.write(mensaje.getBytes());
            
        }catch(Exception e){
            System.out.println("Error inesperado");
        }
    
    }
     
    static void sleep(int i){
        try{
             Thread.sleep(i);
        }catch(Exception e){
            System.out.println("Error al dormir");
        }
    }
    
}

```


### Desde arduino:

Previamente teniamos este código:

```c++
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

  //INICIO: CÓDIGO NUEVO

  //FIN: CÓDIGO NUEVO:


  serializeJson(datos, Serial);
  Serial.print("*");
  delay(5000);
}

```

Vamos a modificarlo, en el bloque que deje marcado en el snippet anterior pegamos:

```c++
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
```

1. Este identifica si el puerto serial esta abierto recibiendo datos.
2. Si esto se cumple entonces extrae del buffer el texto recibido.
3. Entonces pasa a declarar una variable **jsonConDatos** con el cual vamos a procesar la información.
4. La función deserializeJson recibe 2 parametros. 
   1. Donde vamos a guardar los datos extraidos
   2. La fuente de datos (texto) original

La función hace el trabajo pesado por nosotros. Ahora, notose que con los corchetes accedemos a la **key** (**indice** si quieres verlo de ese modo)que deseamos devolviendonos el valor en esa posición del json.

Y pues ya solo comparamos.

> Nota: en arduino recordemos que la constante HIGH = 1 y LOW = 0, por lo que como alarma_hum tiene esos valores... podemos pasarlos directamente a digitalWrite.

Al final nos debería quedar un código como este tomando en cuenta el código que nos falta:

```c++
#include <ArduinoJson.h>

int led_temp=2; //<--- PARA EL CONTROL DE ALARMA
int led_hum=3; //<--- PARA EL CONTROL DE ALARMA

void setup() {
  Serial.begin(9600);
  randomSeed(200);
  pinMode(led_temp, OUTPUT); //<-- RECORDEMOS CONFIGURAR LOS PINES
  pinMode(led_hum, OUTPUT);  //<-- RECORDEMOS CONFIGURAR LOS PINES

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

```

<video controls src="../assets.img/VID_20260406_151443.mp4" title="Title"></video>