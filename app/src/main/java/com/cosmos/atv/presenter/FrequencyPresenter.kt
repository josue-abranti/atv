package com.cosmos.atv.presenter

import android.content.Context
import com.cosmos.atv.interfaces.AudioServiceCallback
import com.cosmos.atv.interfaces.FrequencyContract
import com.cosmos.atv.model.FrequencyController
import com.cosmos.atv.service.AudioService
import com.cosmos.atv.utils.Utils
import com.cosmos.atv.model.Frequency
import utils.Constants

class FrequencyPresenter(view: FrequencyContract.View, context: Context) : FrequencyContract.Presenter, AudioServiceCallback {

    private var frequencyModel: FrequencyController? = null
    private var context: Context? = context
    private var view: FrequencyContract.View? = view
    private val audioService = AudioService()

    init {
        audioService.setCallback(this)
    }

    init {
        this.frequencyModel = FrequencyController()
    }

    fun removeFrequencyDatabase (){
        frequencyModel?.removeFrequencyDatabase()
    }

    fun addFrequenciesFromXml() {
        frequencyModel?.addFrequenciesFromXml(this.context)
    }

    override fun onButtonClickOn() {
        this.startRecording(this.context!!)
    }

    override fun onButtonClickOff() {
        this.stopRecording()
    }

    override fun receiveFrequencyData(frequency: Double) {
        val frequencyResult: Frequency? = frequencyModel?.getPitchByFrequency(frequency)
        if (frequencyResult != null) {
            if(frequency.compareTo(frequency) == -1) {
                view!!.updateFrequency(frequencyResult, frequency, fontColor(frequency.toFloat()), Constants.Position.LEFT)
            } else if(frequency.compareTo(frequency) == 1) {
                view!!.updateFrequency(frequencyResult, frequency, fontColor(frequency.toFloat()), Constants.Position.RIGHT)
            } else {
                view!!.updateFrequency(frequencyResult, frequency, 0, Constants.Position.CENTER)
            }
        }
    }

    private fun fontColor(percentage: Float): Int {
        return Utils.rgbToColorHex(percentage, percentage, percentage)
    }

    override fun startRecording(context: Context) {
        audioService.startAudioRecording(context)
    }

    fun stopRecording() {
        audioService.stopAudioRecording()
    }

    override fun onAudioDataReceived(frequencyValue: Double) {
        this.receiveFrequencyData(frequencyValue)
    }
}