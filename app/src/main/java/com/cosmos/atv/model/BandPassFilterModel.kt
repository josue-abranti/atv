package com.cosmos.atv.model

import kotlin.math.cos
import kotlin.math.sin

class BandPassFilterModel(lowFrequency: Double, private val highFrequency: Double, private val sampleRate: Int) {

    private var nyquistFrequency = sampleRate / 2.0
    private var normalizedLowFrequency = lowFrequency / nyquistFrequency
    private var normalizedHighFrequency = highFrequency / nyquistFrequency
    private var x1 = 0.0
    private var x2 = 0.0
    private var y1 = 0.0
    private var y2 = 0.0

    fun apply(input: Float): Double {

        val normalizedFrequency = (normalizedHighFrequency + normalizedLowFrequency) / 2.0
        val q = 0.5 / (normalizedHighFrequency - normalizedLowFrequency)

        val omega = 2.0 * Math.PI * normalizedFrequency
        val alpha = sin(omega) / (2.0 * q)

        val b1 = 0.0
        val b2 = -alpha
        val a0 = 1.0 + alpha
        val a1 = -2.0 * cos(omega)
        val a2 = 1.0 - alpha

        val output = (alpha / a0) * input + (b1 / a0) * x1 + (b2 / a0) * x2 - (a1 / a0) * y1 - (a2 / a0) * y2

        x2 = x1
        x1 = input.toDouble()
        y2 = y1
        y1 = output

        return output
    }
}