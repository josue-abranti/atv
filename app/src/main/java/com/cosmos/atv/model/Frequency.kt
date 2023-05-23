package model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class Frequency : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var chord: String = ""
    var frequency: Double = 0.0
}

