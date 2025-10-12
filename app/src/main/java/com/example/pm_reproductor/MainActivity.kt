package com.example.pm_reproductor

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
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
    val context = LocalContext.current

    // Crea el PlayerView
    val playerView = remember {
        PlayerView(context).apply {
            useController = true
        }
    }

    // Crea el MediaController y lo conecta con el PlaybackService
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
            val mediaItem = MediaItem.fromUri("https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3")
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.playWhenReady = true
        }, MoreExecutors.directExecutor())

        onDispose {
            // Libera los recursos cuando el componente se desmonte
            playerView.player?.release()
            controllerFuture.get().release()
        }
    }

    // Muestra el reproductor en la interfaz
    AndroidView(
        factory = { playerView },
        modifier = Modifier.fillMaxSize()
    )
}