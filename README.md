Proyecto: Recomendador de Películas
Este repositorio contiene una aplicación de recomendación de películas que desarrollé como un proyecto para explorar cómo un motor de recomendación, un concepto clave en el mundo del Big Data, puede ser implementado en diferentes plataformas.
La aplicación simula una base de datos de 50 usuarios con valoraciones de películas. Cuando tú, el usuario, valoras algunas películas, el sistema analiza tus gustos para encontrar a los usuarios con preferencias similares y te recomienda las películas que ellos han disfrutado.
1. Versión Web: HTML, CSS & JavaScript
Esta es la versión más sencilla del proyecto. Funciona completamente en tu navegador, sin necesidad de un servidor o de instalaciones complicadas. Es ideal para entender la lógica del algoritmo de recomendación de forma visual.
 * index.html: La estructura de la página web.
 * style.css: Los estilos que dan un diseño limpio y moderno a la aplicación.
 * script.js: El cerebro de la aplicación. Aquí se encuentra la lógica para generar los datos, el algoritmo de recomendación (filtrado colaborativo) y la gestión de la interfaz de usuario.
¿Cómo funciona?
Solo necesitas un navegador web.
 * Abre el archivo index.html en tu navegador.
 * Valora algunas de las películas en la lista.
 * Haz clic en el botón "Obtener Recomendaciones" para ver los resultados.
2. Versión Nativa Móvil: Kotlin con Android Studio
Esta es la versión nativa para el ecosistema de Android. Utiliza Kotlin y Jetpack Compose, el framework moderno de Google para construir interfaces de usuario. Esta versión es mucho más potente, rápida y ofrece una experiencia de usuario optimizada para dispositivos móviles.
 * MainActivity.kt: Contiene todo el código. Aquí se define la interfaz de usuario con Jetpack Compose, el modelo de datos, la simulación de la base de datos y el algoritmo de recomendación.
¿Cómo funciona?
Necesitas tener Android Studio instalado.
 * Abre Android Studio.
 * Crea un nuevo proyecto con la plantilla "Empty Compose Activity".
 * Copia y pega todo el código del archivo MainActivity.kt en el archivo principal de tu proyecto.
 * Ejecuta la aplicación en un emulador de Android o en un dispositivo físico conectado.
 * 
