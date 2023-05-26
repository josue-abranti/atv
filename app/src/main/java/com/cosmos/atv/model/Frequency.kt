package model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class Frequency : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var pitch: String = ""
    var frequencyMin: Double = 0.0
    var frequencyPitch: Double = 0.0
    var frequencyMax: Double = 0.0
}

