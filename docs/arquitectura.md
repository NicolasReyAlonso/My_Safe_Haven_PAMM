# Arquitectura

Esta sección describe la arquitectura usada en la aplicación.

## MVVM
El patrón Model–View–ViewModel (MVVM) nos permite separar de forma efectiva la lógica de presentación de la lógica de la interfaz. Esto facilita el mantenimiento del código y reduce el acoplamiento entre componentes. Las principales ventajas por las que elegimos este patrón son:
- Separación clara de responsabilidades.
- Comunicación desacoplada entre UI y datos.
- Arquitectura escalable y mantenible.

## Clean Architecture
La Clean Architecture garantiza una separación clara entre las reglas de negocio, los datos y la interfaz de usuario. Esta división en capas mejora considerablemente la escalabilidad y mantenibilidad del sistema

## Jetpack (Compose/Views)
El ecosistema Jetpack nos proporciona herramientas modernas y altamente optimizadas para el desarrollo de interfaces y funcionalidades móviles. Dentro del proyecto empleamos tanto Compose como vistas tradicionales (Views), según las necesidades de cada pantalla.

## Origanización del proyecto
Para hacer un resumen organizamos el proyecto de la siguiente forma:
- Presentation: pantallas en Jetpack Compose y ViewModels.
- Domain: modelos que representan entidades del sistema.
- Data: acceso a datos locales, remotos y lógica de repositorios.