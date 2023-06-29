package com.cosmos.atv.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class Frequency : RealmObject {

    @PrimaryKey
    var id: Long = 0
    var pitch: String = ""
    var frequencyMin = 0.0
    var frequencyPitch: Double = 0.0
    var frequencyMax: Double = 0.0

    operator fun compareTo(other: Double): Int {
        val frequencyPitchRounded = Math.round(frequencyPitch * 10.0) / 10.0
        val otherRounded = Math.round(other * 10.0) / 10.0
        return when {
            frequencyPitchRounded < otherRounded -> -1
            frequencyPitchRounded > otherRounded -> 1
            else -> 0
        }
    }
}

