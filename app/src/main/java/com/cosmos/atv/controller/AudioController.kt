package controller

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import androidx.core.app.ActivityCompat
import utils.Constants
import kotlin.concurrent.thread

class AudioController {

    fun captureSound(activity: Activity) {

        // Configurações da gravação de áudio
        val taxaAmostragem = Constants.TAXA_AMOSTRAGEM // Taxa de amostragem em Hz
        val configuracaoCanal = Constants.CONFIGURACAO_CANAL // Configuração de canal (mono)
        val formatoAudio = Constants.FORMATO_AUDIO // Formato de áudio (16 bits por amostra)
        val tamanhoBuffer = AudioRecord.getMinBufferSize(taxaAmostragem, configuracaoCanal, formatoAudio)

        // Inicialização do AudioRecord
        val gravadorAudio = if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO),
                Constants.CODIGO_REQUISICAO_GRAVACAO)
            return
        } else {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                taxaAmostragem,
                configuracaoCanal,
                formatoAudio,
                tamanhoBuffer
            )
        }

        // Inicialização do NoiseSuppressor
        val idSessaoAudio = gravadorAudio.audioSessionId
        val supressorRuido = NoiseSuppressor.create(idSessaoAudio)

        // Verificar se o NoiseSuppressor foi criado com sucesso
        if (NoiseSuppressor.isAvailable() && supressorRuido != null) {
            // Habilitar o cancelamento de ruído
            supressorRuido.enabled = true
        }

        // Buffer de gravação
        val buffer = ShortArray(tamanhoBuffer)

        gravadorAudio.startRecording()

        thread {
            Thread.sleep(1000) // Captura de 1 segundo

            gravadorAudio.stop()
            gravadorAudio.release()
        }

        while (gravadorAudio.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            val readSize = gravadorAudio.read(buffer, 0, tamanhoBuffer)

        }

        // Liberar o NoiseSuppressor
        if (supressorRuido != null) {
            supressorRuido.release()
        }
    }
}