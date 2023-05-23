package controller

import android.content.Context
import android.util.Log
import com.cosmos.atv.R
import com.cosmos.atv.controller.RealmController
import model.Frequency
import org.xmlpull.v1.XmlPullParser

class FrequencyController {

    var realmController = RealmController()

    fun addFrequenciesFromXml(context: Context) {
        val xmlResource = context.resources.getXml(R.xml.frequencias)

        try {
            var eventType = xmlResource.eventType
            var frequency = Frequency()

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    val tagName = xmlResource.name

                    if (tagName == "element") {
                        frequency = Frequency()
                    } else if (tagName == "frequencia") {
                        val value = xmlResource.nextText()
                        frequency.frequency = value.toDouble()
                    } else if (tagName == "nota") {
                        frequency.chord = xmlResource.nextText()
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    val tagName = xmlResource.name

                    if (tagName == "element") {
                        frequency.id = nextId()
                        Log.d("", "Id: " + frequency.id.toString() + "\n" + "Acorde: " + frequency.chord + "\n" + "Frequencia: " + frequency.frequency.toString() + "\n")
                        realmController.addFrequency(frequency)
                        frequency = Frequency()
                    }
                }

                eventType = xmlResource.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun nextId(): Long {
        val nextId: Long = realmController.getMaxIdFrequency()!!
        return nextId
    }

    fun getFrequency(id: Long): Frequency? {
        val frequency: Frequency? = realmController.getFrequencyById(id)
        return frequency;
    }

    fun removeFrequencyDatabase() {
        realmController.removeRealmOldVersion()
    }
}