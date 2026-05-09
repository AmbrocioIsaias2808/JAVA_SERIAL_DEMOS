1. JAVA
   FUNCION NUEVA
```java
/*librerias:*/
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

    static void enviarASupabaseAPI(String jsonParaEnviar) {
        
        String urlDestino = "https://[URL]/rest/v1/[tabla]";

        try {
            HttpClient client = HttpClient.newHttpClient();

            // 2. Construir la petición POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlDestino))
                    .header("Content-Type", "application/json") // Le avisamos que mandamos JSON
                    .header("apiKey", "[APY_KEY]") // Le avisamos que mandamos JSON
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

```