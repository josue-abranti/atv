package com.cosmos.atv.model

class YINModel {

    data class YinConfig(
        val threshold: Float,
        val sampleRate: Float,
        val probabilityThreshold: Float
    )

    private val defaultYinParams = YinConfig(0.1f, 44100f, 0.1f)

    fun YIN(params: YinConfig = defaultYinParams): PitchDetector {
        val config = defaultYinParams.copy(
            threshold = params.threshold,
            sampleRate = params.sampleRate,
            probabilityThreshold = params.probabilityThreshold
        )

            return fun(float32AudioBuffer: FloatArray): Float? {
            // Set buffer size to the highest power of two below the provided buffer's length.
            var bufferSize = 1
            while (bufferSize < float32AudioBuffer.size) {
                bufferSize *= 2
            }
            bufferSize /= 2

            // Set up the yinBuffer as described in step one of the YIN paper.
            val yinBufferLength = bufferSize / 2
            val yinBuffer = FloatArray(yinBufferLength)

            val probability = 0f

            // Compute the difference function as described in step 2 of the YIN paper.
            for (t in 0 until yinBufferLength) {
                yinBuffer[t] = 0f
            }
            for (t in 1 until yinBufferLength) {
                for (i in 0 until yinBufferLength) {
                    val delta = float32AudioBuffer[i] - float32AudioBuffer[i + t]
                    yinBuffer[t] += delta * delta
                }
            }

            // Compute the cumulative mean normalized difference as described in step 3 of the paper.
            yinBuffer[0] = 1f
            yinBuffer[1] = 1f
            var runningSum = 0f
            for (t in 1 until yinBufferLength) {
                runningSum += yinBuffer[t]
                yinBuffer[t] *= t.toFloat() / runningSum
            }

            // Compute the absolute threshold as described in step 4 of the paper.
            // Since the first two positions in the array are 1,
            // we can start at the third position.
            var tau = 2
            while (tau < yinBufferLength && yinBuffer[tau] >= config.threshold) {
                tau++
            }

            // if no pitch found, return null.
            if (tau == yinBufferLength || yinBuffer[tau] >= config.threshold) {
                return null
            }

            // If probability too low, return null.
            if (probability < config.probabilityThreshold) {
                return null
            }

            /**
             * Implements step 5 of the AUBIO_YIN paper. It refines the estimated tau
             * value using parabolic interpolation. This is needed to detect higher
             * frequencies more precisely. See http://fizyka.umk.pl/nrbook/c10-2.pdf and
             * for more background
             * http://fedc.wiwi.hu-berlin.de/xplore/tutorials/xegbohtmlnode62.html
             */
            val betterTau: Int
            val x0: Int = if (tau < 1) {
                tau
            } else {
                tau - 1
            }
            val x2: Int = if (tau + 1 < yinBufferLength) {
                tau + 1
            } else {
                tau
            }
            if (x0 == tau) {
                betterTau = if (yinBuffer[tau] <= yinBuffer[x2]) {
                    tau
                } else {
                    x2
                }
            } else if (x2 == tau) {
                betterTau = if (yinBuffer[tau] <= yinBuffer[x0]) {
                    tau
                } else {
                    x0
                }
            } else {
                val s0 = yinBuffer[x0]
                val s1 = yinBuffer[tau]
                val s2 = yinBuffer[x2]
                betterTau = tau + ((s2 - s0) / (2 * (2 * s1 - s2 - s0))).toInt()
            }

            return config.sampleRate / betterTau
        }
    }
}

typealias PitchDetector = (FloatArray) -> Float?