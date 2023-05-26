package controller

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import androidx.core.app.ActivityCompat
import com.cosmos.atv.view.AudioCallback
import org.jtransforms.fft.DoubleFFT_1D
import utils.Constants
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos

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
                audioRecord?.read(buffer, 0, bufferSize)

                // Calcula a frequência média com base no áudio capturado

                val frequenciaMedia = calculateFundamentalFrequency(buffer, sampleRate)

                Log.d("", "frequenciaMedia: $frequenciaMedia\n")

                // Update
                audioCallback?.onFrequencyUpdated(frequenciaMedia)

                segundos++
                Thread.sleep(Constants.SECONDS_WINDOWS)
            }

            audioRecord?.stop()
            audioRecord?.release()

            // Liberar o NoiseSuppressor
            noiseSuppressor?.release()
        }
    }

    fun registerCallback(callback: AudioCallback) {
        audioCallback = callback
    }

    fun calculateFundamentalFrequency(audio: ShortArray, sampleRate: Int): Double {
        val fftSize = audio.size * 2
        val bufferSize = fftSize / 2
        val buffer = DoubleArray(bufferSize)

        // Pré-processamento do sinal
        val processedAudio = preprocessSignal(audio)

        // Converter os valores de áudio processado para double e aplicar a janela de Hamming
        for (i in 0 until bufferSize) {
            buffer[i] = processedAudio[i].toDouble() * hammingWindow(i, bufferSize)
        }

        // Realizar a transformada de Fourier
        val fft = DoubleFFT_1D(bufferSize.toLong())
        fft.realForward(buffer)

        // Encontrar o índice do pico dominante na resposta da FFT
        val peakIndex = findPeakIndex(buffer)

        // Calcular a frequência fundamental com base no índice do pico dominante e a taxa de amostragem
        val fundamentalFrequency = peakIndex * sampleRate.toDouble() / fftSize

        // Ajuste fino pela fase
        //  val phaseAdjustment = calculatePhaseAdjustment(buffer, peakIndex)
        // val adjustedFrequency = fundamentalFrequency + phaseAdjustment

        return fundamentalFrequency
    }

    private fun preprocessSignal(audio: ShortArray): ShortArray {
        // Aplicar técnicas de pré-processamento, como filtragem e normalização, ao sinal de áudio
        val filteredAudio = applyFilter(audio)
        val normalizedAudio = applyNormalization(filteredAudio)
        return normalizedAudio
    }

    private fun applyFilter(audio: ShortArray): ShortArray {
        // Aplicar o filtro desejado ao sinal de áudio
        // Implemente aqui a lógica do filtro que você deseja utilizar
        return audio // Retornar o áudio filtrado
    }

    private fun applyNormalization(audio: ShortArray): ShortArray {
        // Encontrar o valor máximo absoluto no sinal de áudio
        var maxAbsValue = 0

        for (value in audio) {
            val absValue = abs(value.toInt())
            if (absValue > maxAbsValue) {
                maxAbsValue = absValue
            }
        }

        // Normalizar o sinal de áudio dividindo cada valor pelo valor máximo absoluto
        val normalizedAudio = ShortArray(audio.size)

        for (i in audio.indices) {
            normalizedAudio[i] =
                (audio[i].toDouble() / maxAbsValue.toDouble() * Short.MAX_VALUE).toInt().toShort()
        }

        return normalizedAudio
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

    private fun hammingWindow(index: Int, size: Int): Double {
        return 0.54 - 0.46 * cos(2 * PI * index / (size - 1))
    }

    private fun calculatePhaseAdjustment(buffer: DoubleArray, peakIndex: Int): Double {
        val real = buffer[2 * peakIndex]
        val imag = buffer[2 * peakIndex + 1]
        return atan2(imag, real) / (2 * PI)
    }
}