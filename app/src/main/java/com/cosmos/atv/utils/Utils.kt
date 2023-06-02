package com.cosmos.atv.utils

object Utils {

    val rgbToColorHex: (Float, Float, Float) -> Int = { red, green, blue ->
        val redInt = (red * 255).toInt()
        val greenInt = (green * 255).toInt()
        val blueInt = (blue * 255).toInt()
        (255 shl 24) or (redInt shl 16) or (greenInt shl 8) or blueInt
    }
}