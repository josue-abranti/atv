package com.cosmos.atv.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import androidx.core.app.ActivityCompat
import com.cosmos.atv.interfaces.AudioServiceCallback
import com.cosmos.atv.model.FFTModel
import utils.Constants
import kotlin.concurrent.thread

class AudioService {

    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var fftModel = FFTModel()
    private var callback: AudioServiceCallback? = null

    fun setCallback(callback: AudioServiceCallback) {
        this.callback = callback
    }

    // Método de leitura contínua de áudio
    fun startAudioRecording(context: Context) {
        // Configurações da gravação de áudio
        val sampleRate = Constants.TAXA_AMOSTRAGEM // Taxa de amostragem em Hz
        val channelConfig = Constants.CONFIGURACAO_CANAL // Configuração de canal (mono)
        val audioFormat = Constants.FORMATO_AUDIO // Formato de áudio (16 bits por amostra)
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        // Inicialização do AudioRecord
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        val idSessaoAudio = audioRecord?.audioSessionId
        noiseSuppressor = idSessaoAudio?.let { NoiseSuppressor.create(it) }

        if (NoiseSuppressor.isAvailable() && noiseSuppressor != null) {
            // Habilitar o cancelamento de ruído
            noiseSuppressor?.enabled = true
        }

        thread {
            audioRecord?.startRecording()

            var seconds = 0

            while (true) {
                val buffer = ShortArray(bufferSize)
                audioRecord?.read(buffer, 0, bufferSize)

                // Calcula a frequência média com base no áudio capturado

                val frequency = fftModel.calculateFundamentalFrequency(buffer, sampleRate)

                callback!!.onAudioDataReceived(frequency)

                seconds++
                Thread.sleep(Constants.SECONDS_WINDOWS)
            }
        }
    }

    fun stopAudioRecording() {
        audioRecord?.stop()
        audioRecord?.release()

        // Liberar o NoiseSuppressor
        noiseSuppressor?.release()
    }
}