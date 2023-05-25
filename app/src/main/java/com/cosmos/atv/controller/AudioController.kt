package controller

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import androidx.core.app.ActivityCompat
import com.cosmos.atv.view.AudioCallback
import utils.Constants
import kotlin.concurrent.thread
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.FastFourierTransformer
import org.jtransforms.fft.DoubleFFT_1D

class AudioController {

    private var audioCallback: AudioCallback? = null
    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null

    fun startRecording(context: Activity) {
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

            var segundos = 0

            while (true) {
                val buffer = ShortArray(bufferSize)
                val audio = audioRecord?.read(buffer, 0, bufferSize)

                // Calcula a frequência média com base no áudio capturado

                val frequenciaMedia = calculateFundamentalFrequency(buffer, sampleRate)

                //val frequenciaMedia = calculateAverageFrequency(buffer)

                // Update
                audioCallback?.onFrequencyUpdated(frequenciaMedia)

                segundos++
                Thread.sleep(1000)
            }

            audioRecord?.stop()
            audioRecord?.release()

            // Liberar o NoiseSuppressor
            noiseSuppressor?.release()
        }
    }

    private fun calculateAverageFrequency(buffer: ShortArray): Double {
        var sum = 0.0
        for (value in buffer) {
            sum += value
        }
        val average = sum / buffer.size.toDouble()
        return average
    }

    fun registerCallback(callback: AudioCallback) {
        audioCallback = callback
    }

    fun calculateFundamentalFrequency(audio: ShortArray, sampleRate: Int): Double {
        val fftSize = audio.size * 2
        val bufferSize = fftSize / 2
        val buffer = DoubleArray(bufferSize)

        // Converter os valores de áudio para double
        for (i in 0 until bufferSize) {
            buffer[i] = audio[i].toDouble()
        }

        // Realizar a transformada de Fourier
        val fft = DoubleFFT_1D(bufferSize.toLong())
        fft.realForward(buffer)

        // Encontrar o índice do pico dominante na resposta da FFT
        val peakIndex = findPeakIndex(buffer)

        // Calcular a frequência fundamental com base no índice do pico dominante e a taxa de amostragem
        val fundamentalFrequency = peakIndex * sampleRate.toDouble() / fftSize

        return fundamentalFrequency
    }

    private fun findPeakIndex(buffer: DoubleArray): Int {
        var maxMagnitude = 0.0
        var peakIndex = 0

        for (i in 0 until buffer.size / 2) {
            val magnitude = Math.sqrt(buffer[2 * i] * buffer[2 * i] + buffer[2 * i + 1] * buffer[2 * i + 1])
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                peakIndex = i
            }
        }

        return peakIndex
    }
}