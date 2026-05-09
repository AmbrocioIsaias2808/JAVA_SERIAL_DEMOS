package pead_s1;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class PEAD_S1 {
    
    static SerialPort con_serial;  //<--- la declaramos como variable global
    static String textoRecibido=""; //<---- la declaramos como variable global
    
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

        try{
                // Read response (assuming data is available)
            byte[] readBuffer = new byte[1024];
            int numBytesRead = activePort.readBytes(readBuffer, 1024);
            if (numBytesRead > 0) {
                String response = new String(readBuffer, 0, numBytesRead);
                textoRecibido=textoRecibido+response;
                //System.out.println(response);
                if(textoRecibido.endsWith("*")==true){
                    
                    textoRecibido=textoRecibido.substring(0, textoRecibido.indexOf("*"));
                    System.out.println(textoRecibido);
                    
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
                    
                    JSONObject jsonApi = new JSONObject();
                    jsonApi.put("temperatura", temp);
                    jsonApi.put("humedad", humedad);
                    jsonApi.put("velocidad", velocidad);
                    jsonApi.put("equipo", "EQUIPO MASTER");
                    
                    enviarASupabaseAPI(jsonApi.toString());
                    
                    
                    textoRecibido="";
                    
                }
            }
        }catch(Exception e){
            textoRecibido="";
            System.out.println("Error al recibir datos: "+e.getMessage());
        }
}
    
    
     static void sleep(int i){
        try{
             Thread.sleep(i);
        }catch(Exception e){
            System.out.println("Error al dormir");
        }
    }
     
     
         static void enviarASupabaseAPI(String jsonParaEnviar) {
        
        String urlDestino = "https://uiggghnvmfnqkqdwvemg.supabase.co/rest/v1/mediciones";

        try {
            HttpClient client = HttpClient.newHttpClient();

            // 2. Construir la petición POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlDestino))
                    .header("Content-Type", "application/json") // Le avisamos que mandamos JSON
                    .header("apiKey", "sb_publishable_CuRsMNwLpF_C1-oLjOqxUA_X5PqeFGB") // Le avisamos que mandamos JSON
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
    
}
