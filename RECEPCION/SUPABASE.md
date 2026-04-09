# SUPABASE:

Este readme es complementario al apartado de [Puerto Serial en Java (RECEPCIÓN DE DATOS)](notas.md).

No explicaré como crear una cuenta... es lo más fácil del mundo. Vamos directo a crear un proyecto y sacar las credenciales de conexión shall we?

## CREAR UN PROYECTO

Dentro de nuestra cuenta en el apartado de proyectos encontraremos el botón "New Proyect":

![alt text](../assets.img/image-10.png)

Llenamos los datos solicitados y camos en crear:

![alt text](../assets.img/image-11.png)

Dentro vamos a usar la opción SQL Editor para crear nuestra primer tabla:

![alt text](../assets.img/image-12.png)

Dentro escribirmos el comando deseado y damos clic en "RUN":

```sql
create table muestras(
  id serial primary key,
  temp numeric,
  hum numeric
)
```

![alt text](../assets.img/image-13.png)

# Obteniendo la cadena de conexión:

Damos clic en "Connect":

![alt text](../assets.img/image-14.png)

Seleccionamos la opción "Direct" y "Transaction pooler". El el typo de conexión seleccionamos "JDBC":

![alt text](../assets.img/image-15.png)

Y más abajo encontraremos la información que ocupamos:

![alt text](../assets.img/image-16.png)