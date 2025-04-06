package com.lairofpixies.choppity

object Constants {
    // Zoom controls
    const val RESET_ZOOM_ON_DOUBLETAP = true
    const val RESET_ZOOM_ON_RELEASE = false
    const val MINIMUM_ZOOM = 1f
    const val MAXIMUM_ZOOM = 20f

    // Mimetype
    const val MIMETYPE_IMAGE = "image/*"

    // Known aspect ratios
    val ASPECT_RATIOS = listOf(
        3 to 4,
        5 to 6,
        1 to 1,
        6 to 5,
        4 to 3,
        7 to 5,
        3 to 2,
        16 to 9,
        20 to 9,
    )
}