package com.cosmos.atv.interfaces

import android.content.Context
import com.cosmos.atv.model.Frequency
import utils.Constants

interface FrequencyContract {

    interface View {
        fun updateFrequency(frequency: Frequency, frequencyValue: Double, color: Int, position: Constants.Position)
    }

    interface Presenter {
        fun onButtonClickOn()
        fun onButtonClickOff()
        fun receiveFrequencyData(frequency: Double)
        fun startRecording(context: Context)
    }
}