package utils

import android.media.AudioFormat

object Constants {
    const val TAXA_AMOSTRAGEM = 88200
    const val THRESHOLD = 0.01
    const val PROBABILITY_THRESHOLD = 0.95
    const val CONFIGURACAO_CANAL = AudioFormat.CHANNEL_IN_MONO
    const val FORMATO_AUDIO = AudioFormat.ENCODING_PCM_16BIT
    const val PERMISSION_REQUEST_CODE = 123
    const val SECONDS_WINDOWS:Long = 500
    const val LOW_FREQUENCY:Double = 15.0
    const val HIGH_FREQUENCY:Double = 8200.0

    enum class Position {
        LEFT,
        RIGHT,
        CENTER
    }
}