package com.uiza.broadcast

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class CameraSize(
    val width: Int,
    val height: Int,
) : Serializable
