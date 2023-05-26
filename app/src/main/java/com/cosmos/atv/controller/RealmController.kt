package com.cosmos.atv.controller

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import model.Frequency

class RealmController {

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

    fun getFrequencyById(id: Long): Frequency? {

        var realm = Realm.open(RealmConfig.config)

        val frequency: RealmResults<Frequency> = realm.query<Frequency>("id == $0", id).find()

        return frequency[0]
    }

    fun getMaxIdFrequency(): Long? {
        this.idFrequency++
        return this.idFrequency
    }
}