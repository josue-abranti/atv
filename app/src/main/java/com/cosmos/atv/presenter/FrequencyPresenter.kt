package com.cosmos.atv.presenter

import android.content.Context
import com.cosmos.atv.interfaces.AudioServiceCallback
import com.cosmos.atv.interfaces.FrequencyContract
import com.cosmos.atv.model.FrequencyController
import com.cosmos.atv.service.AudioService
import com.cosmos.atv.utils.Utils
import com.cosmos.atv.model.Frequency
import utils.Constants

class FrequencyPresenter: FrequencyContract.Presenter, AudioServiceCallback {

    private var frequencyModel: FrequencyController? = null
    private var context: Context? = null
    private var view: FrequencyContract.View? = null
    private val audioService = AudioService()

    init {
        audioService.setCallback(this)
    }

    constructor (view: FrequencyContract.View, context: Context) {
        this.context = context;
        this.view = view
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

    override fun receiveFrequencyData(frequencyValue: Double) {
        var frequency: Frequency? = frequencyModel?.getPitchByFrequency(frequencyValue)
        if (frequency != null) {
            if(frequencyValue < frequency.frequencyPitch) {
                view!!.updateFrequency(frequency, frequencyValue, fontColor(frequencyValue.toFloat()), Constants.Position.LEFT)
            } else if(frequencyValue > frequency.frequencyPitch) {
                view!!.updateFrequency(frequency, frequencyValue, fontColor(frequencyValue.toFloat()), Constants.Position.RIGHT)
            } else {
                view!!.updateFrequency(frequency, frequencyValue, 0, Constants.Position.CENTER)
            }
        }
    }

    fun fontColor(percentage: Float): Int {
        return Utils.rgbToColorHex(percentage, percentage, percentage)
    }

    override fun startRecording(context: Context) {
        audioService.startAudioRecording(context)
    }

    fun stopRecording() {
        audioService.stopAudioRecording()
    }

    fun stopRecording(context: Context) {
        audioService.startAudioRecording(context)
    }

    override fun onAudioDataReceived(frequencyValue: Double) {
        this.receiveFrequencyData(frequencyValue)
    }
}