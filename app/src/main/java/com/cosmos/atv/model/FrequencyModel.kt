package com.cosmos.atv.model

import android.content.Context
import android.util.Log
import com.cosmos.atv.R
import org.xmlpull.v1.XmlPullParser

class FrequencyController {

    var realmModel = RealmModel()

    fun addFrequenciesFromXml(context: Context?) {
        val xmlResource = context?.resources?.getXml(R.xml.frequencias)

        try {
            var eventType = xmlResource?.eventType
            var frequency = Frequency()

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    val tagName = xmlResource?.name

                    if (tagName == "element") {
                        frequency = Frequency()
                    } else if (tagName == "frequenciaMinima") {
                        val value = xmlResource?.nextText()
                        frequency.frequencyMin = value?.toDouble() ?: 0.0
                    } else if (tagName == "frequenciaNota") {
                        val value = xmlResource?.nextText()
                        frequency.frequencyPitch = value?.toDouble() ?: 0.0
                    } else if (tagName == "frequenciaMaxima") {
                        val value = xmlResource?.nextText()
                        frequency.frequencyMax = value?.toDouble() ?: 0.0
                    } else if (tagName == "nota") {
                        frequency.pitch = xmlResource?.nextText() ?: ""
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    val tagName = xmlResource?.name

                    if (tagName == "element") {
                        frequency.id = nextId()
                        Log.d("", "Id: " + frequency.id.toString() + "\n" + "Acorde: " + frequency.pitch + "\n" + "Frequencia minima: " + frequency.frequencyMin.toString() + "\n" + "Frequencia nota: " + frequency.frequencyPitch.toString() + "\n" +"Frequencia maxima: " + frequency.frequencyMax.toString() + "\n")
                        realmModel.addFrequency(frequency)
                        frequency = Frequency()
                    }
                }

                eventType = xmlResource?.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun nextId(): Long {
        val nextId: Long = realmModel.getMaxIdFrequency()!!
        return nextId
    }

    fun getPitchByFrequency(frequency: Double): Frequency? {
        return realmModel.getPitchByFrequency(frequency);
    }

    fun removeFrequencyDatabase() {
        realmModel.removeRealmOldVersion()
    }
}