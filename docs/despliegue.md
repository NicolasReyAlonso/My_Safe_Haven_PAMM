# Despliegue

Nuestra aplicación incluye un backend, contenerizado en Docker para poder compilar y ejecutar la aplicación es necesario disponer de los siguientes requisitos:
- Docker CLI o Docker Desktop.
- Android Studio.

De forma adicional, se recomienda contar con los siguientes elementos para facilitar las tareas de depuración y pruebas:
- Python.
- Un dispositivo Android físico o una máquina virtual (emulador de Android Studio).
- Los puertos 5432 y 5050 libres, destinados al backend y al servidor PostgreSQL, respectivamente.

Una vez verificados los requisitos previos, el proceso de compilación y despliegue se realiza siguiendo los pasos que se detallan a continuación:

1. Iniciar Docker Desktop o arrancar el daemon de Docker.
2. Navegar hasta la carpeta raíz del proyecto y ejecutar el siguiente comando: ```docker compose up```
3. Obtener la dirección IP de la máquina en la que se va a ejecutar la aplicación. Esta IP será utilizada por la aplicación móvil para comunicarse con el backend.
4. Abrir Android Studio.
5. Ejecutar el proceso de compilación mediante Gradle.
6. Sustituir la IP base configurada en el archivo com.nicojero.mysafehaven.remote.RetrofitClient.kt por la dirección IP obtenida previamente. Es necesario repetir este paso si la IP de la máquina cambia.
7. Ejecutar la aplicación en un dispositivo físico o en un emulador Android.

![Vista previa del despliegue](Captura%20de%20pantalla%202025-12-21%20114107.png)