class YINModel() {
        private val threshold = 0.04
        private val probabilityThreshold = 0.99

        private var tau: Int = 0

        fun detectPitch(audioBuffer: ShortArray, sampleRate: Int, bufferSize: Int): Double {
            val yinBuffer = DoubleArray(bufferSize / 2)
            val floatBuffer = audioBuffer.map { it.toDouble() / Short.MAX_VALUE }.toDoubleArray()
            // val bandPassFilterModel = BandPassFilterModel(Constants.LOW_FREQUENCY, Constants.HIGH_FREQUENCY, sampleRate)

            // Apply band-pass filter to the input audio buffer
            // val filteredBuffer = FloatArray(bufferSize)
            // for (i in 0 until bufferSize) {
            //     filteredBuffer[i] = bandPassFilterModel.apply(floatBuffer[i])
            // }

            val bufferSizeDiv2 = bufferSize / 2
            val bufferSizeDiv2MinusTau = bufferSizeDiv2 - tau

            // Compute the difference function as described in step 2 of the YIN paper.
            for (t in 0 until bufferSizeDiv2) {
                yinBuffer[t] = 0.0
                for (j in 0 until bufferSizeDiv2MinusTau) {
                    // val delta = filteredBuffer[j] - filteredBuffer[j + t]
                    val delta = floatBuffer[j] - floatBuffer[j + t]
                    yinBuffer[t] += (delta * delta)
                }
            }

            // Compute the cumulative mean normalized difference as described in step 3 of the paper.
            yinBuffer[0] = 1.0
            var runningSum = 0.0
            for (t in 1 until bufferSizeDiv2) {
                runningSum += yinBuffer[t]
                yinBuffer[t] *= t.toDouble() / runningSum
            }

            // Compute the absolute threshold as described in step 4 of the paper.
            tau = 2
            while (tau < bufferSizeDiv2 && yinBuffer[tau] >= threshold) {
                tau++
            }

            // If no pitch found, return 0.0.
            if (tau == bufferSizeDiv2 || yinBuffer[tau] >= threshold) {
                return 0.0
            }

            // Calculate the probability based on the data in yinBuffer.
            val probability = calculateProbability(yinBuffer)

            // If probability too low, return 0.0.
            if (probability < probabilityThreshold) {
                return 0.0
            }

            // Implements step 5 of the AUBIO_YIN paper. It refines the estimated tau
            // value using parabolic interpolation.
            val betterTau: Int = if (tau < 1) {
                tau
            } else if (tau + 1 < bufferSizeDiv2) {
                val s0 = yinBuffer[tau - 1]
                val s1 = yinBuffer[tau]
                val s2 = yinBuffer[tau + 1]
                tau + ((s2 - s0) / (2 * (2 * s1 - s2 - s0))).toInt()
            } else {
                tau
            }

            return sampleRate.toDouble() / betterTau
        }

    private fun calculateProbability(yinBuffer: DoubleArray): Double {
        return yinBuffer.average()
    }
    }