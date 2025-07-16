package com.lairofpixies.choppity.data

sealed class AspectRatio(val label: String, val readable: String) {
    data object Original : AspectRatio("Orig", "original")
    data object Auto : AspectRatio("Auto", "automatic")
    data class Value(val width: Int, val height: Int) :
        AspectRatio("$width:$height", "$width to $height")

    companion object {
        val All by lazy {
            listOf(
                Original,
                Auto,
                Value(3, 4),
                Value(5, 6),
                Value(1, 1),
                Value(6, 5),
                Value(4, 3),
                Value(7, 5),
                Value(3, 2),
                Value(16, 9),
            )
        }

        val Square = Value(1, 1)
    }
}