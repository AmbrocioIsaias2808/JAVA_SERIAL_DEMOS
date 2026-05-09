# Paso a paso para copiar y pegar.


1.  
```JAVA


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

 static void sleep(int i){
        try{
             Thread.sleep(i);
        }catch(Exception e){
            System.out.println("Error al dormir");
        }
    }

```

```JAVA
static void lectura(SerialPort activePort){

        try{
                // Read response (assuming data is available)
            byte[] readBuffer = new byte[1024];
            int numBytesRead = activePort.readBytes(readBuffer, 1024);
            if (numBytesRead > 0) {
                String response = new String(readBuffer, 0, numBytesRead);
                textoRecibido=textoRecibido+response;
                //System.out.println(textoRecibido);
                if(textoRecibido.endsWith("*")==true){
                    textoRecibido=textoRecibido.substring(0, textoRecibido.indexOf("*"));
                    System.out.println(textoRecibido);
                    textoRecibido="";
                    
                }
            }
        }catch(Exception e){
            textoRecibido="";
            System.out.println("Error al recibir datos: "+e.getMessage());
        }
}

```

