package com.cosmos.atv.model

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults

class RealmModel {

    var idFrequency: Long = 0

    object RealmConfig {
        val config: RealmConfiguration = RealmConfiguration.Builder(setOf(Frequency::class)).build()
    }

    fun removeRealmOldVersion() {
        Realm.deleteRealm(RealmConfig.config)
    }

    fun addFrequency(frequencyObject: Frequency) {
        var realm = Realm.open(RealmConfig.config)
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

    fun getPitchByFrequency(frequency: Double): Frequency? {

        var realm = Realm.open(RealmConfig.config)

        val frequency: RealmResults<Frequency> = realm.query<Frequency>("frequencyMin < $0 AND frequencyMax > $0", frequency).find()

        if(frequency == null) {
            return Frequency()
        } else {
            return frequency[0]
        }
    }

    fun getMaxIdFrequency(): Long? {
        this.idFrequency++
        return this.idFrequency
    }
}