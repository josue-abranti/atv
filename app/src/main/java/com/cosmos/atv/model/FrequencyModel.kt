package com.cosmos.atv.model

import android.content.Context
import android.util.Log
import com.cosmos.atv.R
import org.xmlpull.v1.XmlPullParser

class FrequencyController {

    private var realmModel = RealmModel()

    fun addFrequenciesFromXml(context: Context?) {
        val xmlResource = context?.resources?.getXml(R.xml.frequencias)

        try {
            var eventType = xmlResource?.eventType
            var frequency = Frequency()

            while (eventType != XmlPullParser.END_DOCUMENT) {

                when (eventType) {
                    XmlPullParser.START_TAG -> {

                        when (xmlResource?.name) {
                            "element" -> {
                                frequency = Frequency()
                            }
                            "frequenciaMinima" -> {
                                val value = xmlResource.nextText()
                                frequency.frequencyMin = value?.toDouble() ?: 0.0
                            }
                            "frequenciaNota" -> {
                                val value = xmlResource.nextText()
                                frequency.frequencyPitch = value?.toDouble() ?: 0.0
                            }
                            "frequenciaMaxima" -> {
                                val value = xmlResource.nextText()
                                frequency.frequencyMax = value?.toDouble() ?: 0.0
                            }
                            "nota" -> {
                                frequency.pitch = xmlResource.nextText() ?: ""
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tagName = xmlResource?.name

                        if (tagName == "element") {
                            frequency.id = nextId()
                            Log.d(
                                "",
                                "Id: ${frequency.id}\nAcorde: ${frequency.pitch}\nFrequencia minima: ${frequency.frequencyMin}\nFrequencia nota: ${frequency.frequencyPitch}\nFrequencia maxima: ${frequency.frequencyMax}\n"
                            )
                            realmModel.addFrequency(frequency)
                            frequency = Frequency()
                        }
                    }
                }

                eventType = xmlResource?.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun nextId(): Long {
        return realmModel.getMaxIdFrequency()
    }

    fun getPitchByFrequency(frequency: Double): Frequency {
        return realmModel.getPitchByFrequency(frequency)
    }

    fun removeFrequencyDatabase() {
        realmModel.removeRealmOldVersion()
    }
}