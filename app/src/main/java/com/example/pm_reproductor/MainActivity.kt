package com.example.pm_reproductor

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.example.pm_reproductor.service.PlaybackService
import com.example.pm_reproductor.ui.theme.Pm_reproductorTheme
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pm_reproductorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoPlayerScreen()
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerScreen() {
    /**
     * En Compose, para obtener el Context de Android (necesario para crear vistas, acceder
     * a servicios, etc.), usamos LocalContext.current.
     */
    val context = LocalContext.current


    /**
     * PlayerView es un componente de UI del sistema de Vistas tradicional, no es un Composable
     * nativo.
     * Con la misma instancia del reproductor •apply { useController = true }:
     * Aquí configuras la PlayerView para que muestre los controles de reproducción
     * por defecto (botón de play/pausa, barra de progreso, etc.).
     */
    val playerView = remember {
        PlayerView(context).apply {
            useController = true
        }
    }

    /**
     * DisposableEffect(Unit): Es un "efecto secundario" de Compose atado al ciclo de vida.
     * Es el lugar perfecto para inicializar y liberar objetos que no son de Compose,
     * como nuestro MediaController.
     *
     * Bloque de configuración: El código principal se ejecuta cuando VideoPlayerScreen
     * aparece en pantalla. Aquí es donde creas el MediaController y lo conectas al PlaybackService
     * en segundo plano.
     *
     * onDispose { ... }: Este bloque es la clave. Se ejecuta automáticamente cuando
     * VideoPlayerScreen desaparece de la pantalla (por ejemplo, si navegas a otra pantalla o
     * cierras la app). Es el lugar ideal para limpiar y liberar recursos.
     *
     * MediaController.Builder(...).buildAsync(): Construir un MediaController es una operación
     * asíncrona porque necesita tiempo para encontrar y conectarse a tu PlaybackService.
     *
     * controllerFuture.addListener(...): Una vez que el controlador está listo, este "listener"
     * se dispara. Dentro de él, haces la conexión final: playerView.player = controller.
     * Esto le dice a la UI (playerView) qué controlador (controller) debe usar para mostrar el
     * estado del video y enviar comandos.•onDispose y .release(): Es fundamental llamar a
     * controller.release() para liberar los recursos multimedia y desconectarse correctamente
     * del servicio.
     *
     * onDispose garantiza que esto ocurra en el momento justo, evitando fugas de memoria.
     */
    DisposableEffect(Unit) {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            val controller = controllerFuture.get()
            playerView.player = controller

            // Configura un medio de ejemplo (reemplaza con tu propia URL)
            //val mediaItem = MediaItem.fromUri("https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3")
            val mediaItem = MediaItem.fromUri("https://cdn.pixabay.com/download/audio/2025/10/02/audio_06ac57a05b.mp3")
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.playWhenReady = true
        }, MoreExecutors.directExecutor())

        onDispose {
            // Libera los recursos de forma segura cuando el componente se desmonte
            controllerFuture.addListener({
                controllerFuture.get().release()
            }, MoreExecutors.directExecutor())
        }
    }

    /**
     *AndroidView: Este es el Composable que actúa como un puente para incluir cualquier Vista
     * de Android (como nuestra PlayerView) dentro de una UI de Compose.
     *
     * factory = { playerView }: En su factory, simplemente proporcionas la instancia de la
     * vista que quieres mostrar, que es la misma que creamos y remembered arriba.
     */
    // Muestra el reproductor en la interfaz
    AndroidView(
        factory = { playerView },
        modifier = Modifier.fillMaxSize()
    )
}
