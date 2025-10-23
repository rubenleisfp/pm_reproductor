package com.example.pm_reproductor.service

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

// --- LA CABINA DE PROYECCIÓN ---
// Esta clase es el corazón de la reproducción. Es un servicio que se ejecuta en segundo plano,
// totalmente independiente de la interfaz de usuario. Es la "sala de máquinas" o
// "cabina de proyección" de nuestro cine.
class PlaybackService : MediaSessionService() {
    // El "proyeccionista" (MediaSession). Será el encargado de gestionar la película.
    private var mediaSession: MediaSession? = null

    // El método onCreate se llama cuando la cabina de proyección se construye por primera vez.
    override fun onCreate() {
        super.onCreate()
        // 1. Construimos nuestro "proyector" (ExoPlayer), que es el que realmente decodifica y
        //    reproduce el vídeo.
        val player = ExoPlayer.Builder(this).build()
        // 2. Contratamos a nuestro "proyeccionista" (MediaSession) y le entregamos el proyector
        //    para que lo controle.
        mediaSession = MediaSession.Builder(this, player).build()
    }

    // Este método se llama cada vez que un "mando a distancia" (MediaController) desde la UI
    // intenta conectarse. Simplemente le damos una referencia a nuestro proyeccionista.
    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    // El método onDestroy se llama cuando el cine cierra y la cabina de proyección se desmantela.
    override fun onDestroy() {
        // Es CRÍTICO despedir al proyeccionista y vender el proyector para liberar recursos.
        mediaSession?.run {
            player.release() // Apagamos y liberamos el proyector.
            release()        // Despedimos al proyeccionista.
            mediaSession = null
        }
        super.onDestroy()
    }
}
