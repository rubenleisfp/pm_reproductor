# Reproductor de Medios con Jetpack Compose y Media3

Este proyecto es una aplicación de Android de ejemplo que demuestra cómo construir un reproductor de vídeo utilizando las tecnologías más modernas de Android: Jetpack Compose para la interfaz de usuario y la biblioteca Media3 para la lógica de reproducción.

La aplicación sigue la arquitectura recomendada por Google, separando la interfaz de usuario (UI) de la lógica de reproducción, lo que permite una reproducción continua en segundo plano y una gestión del ciclo de vida robusta.

## Estructura del Proyecto

La estructura de los archivos clave del proyecto es la siguiente:

```
/app/src/main/java/com/example/pm_reproductor/
├── MainActivity.kt         # Actividad principal y la UI con Jetpack Compose
└── service/
    └── PlaybackService.kt  # Servicio para la reproducción en segundo plano
```

## Arquitectura y Componentes Clave

El proyecto se basa en la interacción de varios componentes de Media3, cada uno con una responsabilidad clara. Esta separación es fundamental para seguir las mejores prácticas y asegurar que la reproducción no se interrumpa si la UI es destruida por el sistema.

![Arquitectura Media3](https://developer.android.com/static/images/guide/topics/media/media3-session-arch.png)

### 1. La Capa de UI (MainActivity.kt)

- **`MainActivity.kt`**: Es la actividad principal y el punto de entrada a la aplicación. Su única responsabilidad es configurar el entorno de Jetpack Compose con `setContent` y mostrar el Composable principal: `VideoPlayerScreen`.

- **`VideoPlayerScreen` (Composable)**: Esta función Composable contiene toda la lógica de la interfaz de usuario.
    - **`PlayerView` y `AndroidView`**: Para mostrar el vídeo, utilizamos `PlayerView`, un componente de UI del sistema de Vistas tradicional. Lo integramos en nuestro diseño de Compose usando el Composable `AndroidView`, que actúa como un puente entre los dos mundos de UI.
    - **`remember { ... }`**: La instancia de `PlayerView` se crea dentro de un bloque `remember` para asegurar que no se recree en cada recomposición, manteniendo así su estado.
    - **`DisposableEffect`**: Es el componente más importante para gestionar el ciclo de vida. Lo usamos para crear y liberar el `MediaController` de forma segura:
        - El bloque de **configuración** se ejecuta cuando el Composable aparece en pantalla. Aquí es donde se construye el `MediaController` de forma asíncrona y se conecta al `PlaybackService`.
        - El bloque `onDispose` se ejecuta automáticamente cuando el Composable sale de la pantalla. Aquí es donde se llama a `controller.release()`, un paso **crítico** para liberar los recursos y evitar fugas de memoria.

### 2. La Capa de Reproducción (PlaybackService.kt)

- **`PlaybackService.kt` (`MediaSessionService`)**: Este es el corazón de la reproducción. Es un servicio que se ejecuta en segundo plano, independiente de la UI.
    - **Aloja la `MediaSession` y el `Player` (`ExoPlayer`)**: Su función es gestionar el reproductor real y exponer la información al resto del sistema operativo y a nuestra UI.
    - **Permite la reproducción en segundo plano**: Como es un servicio, puede continuar reproduciendo audio incluso si el usuario navega a otra aplicación o apaga la pantalla.

### 3. Los Componentes de Conexión (Media3)

Estos son los objetos que actúan como pegamento entre la UI y el servicio.

- **`MediaSession`**: Vive en el `PlaybackService`. Actúa como un "proyeccionista", gestionando qué se reproduce y su estado. No reproduce directamente, pero da órdenes al `ExoPlayer`.

- **`MediaController`**: Vive en la `VideoPlayerScreen` (UI). Es el "mando a distancia". Su trabajo es encontrar la `MediaSession` y enviarle comandos (play, pausa, etc.) y recibir actualizaciones de estado para que la UI se pueda redibujar correctamente.

- **`SessionToken`**: Es la "dirección". Es un objeto que el `MediaController` utiliza para saber exactamente a qué `MediaSessionService` debe conectarse.

## Dependencias Utilizadas

- **Jetpack Compose**: Para construir la interfaz de usuario de forma declarativa y moderna.
- **Media3**: El conjunto de bibliotecas de Android para la reproducción de medios.
    - `media3-exoplayer`: La implementación del reproductor de medios.
    - `media3-ui`: Proporciona los componentes de UI como `PlayerView`.
    - `media3-session`: Proporciona `MediaSession`, `MediaController` y `MediaSessionService` para la arquitectura de reproducción.
- **Lifecycle-Ktx**: Para gestionar ciclos de vida de forma más sencilla con corrutinas.
