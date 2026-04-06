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
                    
                    System.out.println(jsonEnviar.toString());
                    
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
