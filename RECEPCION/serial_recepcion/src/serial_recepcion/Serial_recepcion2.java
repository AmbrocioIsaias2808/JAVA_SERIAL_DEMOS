
package serial_recepcion;

import java.util.Scanner;
import com.fazecast.jSerialComm.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

//librerias postgres y sql:
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Serial_recepcion2 {

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
        try{
                byte[] readBuffer = new byte[1024];
                int numBytesRead = activePort.readBytes(readBuffer, readBuffer.length);
                if (numBytesRead > 0) {
                    String response = new String(readBuffer, 0, numBytesRead);
                    textoRecibido=textoRecibido+response;
                    //System.out.println(textoRecibido);
                    if(textoRecibido.endsWith("*")==true){
                        textoRecibido=textoRecibido.substring(0, textoRecibido.indexOf("*"));
                        //System.out.println(textoRecibido);

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

                        //enviarALaNube(textoRecibido);
                         //guardarEnBD(temp, humedad);

                        JSONObject jsonToSend = new JSONObject();
                        jsonToSend.put("temp", temp);
                        jsonToSend.put("hum", humedad);

                        enviarASupabaseAPI(jsonToSend.toString());


                        //FIN DEL CÓDIGO NUEVO
                        textoRecibido="";
                        //enviar(activePort, "HOLA DESDE JAVA");
                    }
                }
        }catch(Exception e){
            textoRecibido="";
            System.out.println("Error al recibir datos"+e.getMessage());
        }
    }
    static void sleep(int i){
        try{
             Thread.sleep(i);
        }catch(Exception e){
            System.out.println("Error al dormir");
        }
    }
    
    static void enviarALaNube(String jsonParaEnviar) {
        // 1. Reemplaza con la URL que te dé Webhook.site
        String urlDestino = "https://[HOST_URL]";

        try {
            HttpClient client = HttpClient.newHttpClient();

            // 2. Construir la petición POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlDestino))
                    .header("Content-Type", "application/json") // Le avisamos que mandamos JSON
                    .POST(HttpRequest.BodyPublishers.ofString(jsonParaEnviar))
                    .build();

            // 3. Enviar de forma asíncrona (¡Importante para no bloquear el Serial!)
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                  .thenAccept(response -> {
                      System.out.println("Nube actualizada. Código: " + response.statusCode());
                  });

        } catch (Exception e) {
            System.err.println("Error al conectar con la API: " + e.getMessage());
        }
    }
    
    static void enviarASupabaseAPI(String jsonParaEnviar) {
        
        String urlDestino = "https://[HOST_URL]/rest/v1/[TABLA]";

        try {
            HttpClient client = HttpClient.newHttpClient();

            // 2. Construir la petición POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlDestino))
                    .header("Content-Type", "application/json") // Le avisamos que mandamos JSON
                    .header("apiKey", "[API_KEY]") // Le avisamos que mandamos JSON
                    .POST(HttpRequest.BodyPublishers.ofString(jsonParaEnviar))
                    .build();

            // 3. Enviar de forma asíncrona (¡Importante para no bloquear el Serial!)
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                  .thenAccept(response -> {
                      System.out.println("Nube actualizada. Código: " + response.statusCode());
                  });

        } catch (Exception e) {
            System.err.println("Error al conectar con la API: " + e.getMessage());
        }
    }
    
    
    public static void guardarEnBD(int t, int h) {
            // RECUERDA: jdbc:postgresql://[HOST]:[PUERTO]/[DB_NAME]
            String url = "jdbc:postgresql://[HOST]:[PUERTO]/[DB_NAME]";
            String user = "[USER]";
            String pass = "[PASSWORD]";
            
            try{
                Class.forName("org.postgresql.Driver");

                try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                    String query = "INSERT INTO muestras (temp, hum) VALUES (?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setDouble(1, t);
                    pstmt.setDouble(2, h);

                    pstmt.executeUpdate();
                    System.out.println(">>> [DB] Éxito: T=" + t + " H=" + h);
                    
                    conn.close();

                } catch (Exception e) {
                    System.err.println(">>> [DB] Error: " + e.getMessage());
                }
            }catch(Exception e){
                System.out.println("Error al conectar BD: "+e.getMessage());
            }
    }
}
