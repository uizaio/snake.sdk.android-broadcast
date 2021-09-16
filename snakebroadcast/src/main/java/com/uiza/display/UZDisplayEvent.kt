package com.uiza.display

data class OnConnectionStartedRtp(
    var rtpUrl: String? = null
)

class OnConnectionSuccessRtp

data class OnNewBitrateRtp(
    var bitrate: Long? = null
)

data class OnConnectionFailedRtp(
    var reason: String? = null
)

class OnDisconnectRtp
class OnAuthErrorRtp
class OnAuthSuccessRtp
