package com.cosmos.atv.model

//  import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos

class FFTModel() {

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
        //val fft = DoubleFFT_1D(bufferSize.toLong())
        //fft.realForward(buffer)

        // Encontrar o índice do pico dominante na resposta da FFT
        val peakIndex = findPeakIndex(buffer)

        // Calcular a frequência fundamental com base no índice do pico dominante e a taxa de amostragem
        val fundamentalFrequency = peakIndex * sampleRate.toDouble() / fftSize

        // Ajuste fino pela fase
        val phaseAdjustment = calculatePhaseAdjustment(buffer, peakIndex)
        val adjustedFrequency = fundamentalFrequency + phaseAdjustment

        return adjustedFrequency
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