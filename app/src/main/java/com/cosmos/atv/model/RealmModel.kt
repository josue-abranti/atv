package com.cosmos.atv.model

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults

class RealmModel {

    var idFrequency: Long = 0

    /**

    Descrição do elemento documentado.
    @return Descrição do valor de retorno.
     */
    object RealmConfig {
        val config: RealmConfiguration = RealmConfiguration.Builder(setOf(Frequency::class)).build()
    }

    fun removeRealmOldVersion() {
        Realm.deleteRealm(RealmConfig.config)
    }

    fun addFrequency(frequencyObject: Frequency) {
        val realm = Realm.open(RealmConfig.config)
        realm.writeBlocking  {
            copyToRealm(frequencyObject.apply {
                id = frequencyObject.id
                pitch = frequencyObject.pitch
                frequencyMin = frequencyObject.frequencyMin
                frequencyPitch = frequencyObject.frequencyPitch
                frequencyMax = frequencyObject.frequencyMax
            })
        }
        realm.close()
    }

    fun getPitchByFrequency(frequency: Double): Frequency {

        val realm = Realm.open(RealmConfig.config)

        val frequencyResult: RealmResults<Frequency> = realm.query<Frequency>("frequencyMin < $0 AND frequencyMax >= $0", frequency).find()

        return if(frequencyResult.isEmpty()) Frequency()
        else frequencyResult[0]
    }

    fun getMaxIdFrequency(): Long {
        this.idFrequency++
        return this.idFrequency
    }
}