package com.uiza.display

import androidx.annotation.Keep

@Keep
data class OnConnectionStartedRtp(
    var rtpUrl: String? = null
)

@Keep
class OnConnectionSuccessRtp

@Keep
data class OnNewBitrateRtp(
    var bitrate: Long? = null
)

@Keep
data class OnConnectionFailedRtp(
    var reason: String? = null
)

@Keep
class OnDisconnectRtp

@Keep
class OnAuthErrorRtp

@Keep
class OnAuthSuccessRtp
