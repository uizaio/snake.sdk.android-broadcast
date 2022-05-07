package com.uiza

import androidx.multidex.MultiDexApplication
import com.uiza.util.UZUtil

//TODO add splash screen

// firebase console https://console.firebase.google.com/u/0/project/broadcast-3de39/overview

class UZApplication : MultiDexApplication() {

    companion object {
        const val URL_STREAM =
            "rtmp://a.rtmp.youtube.com/live2/s0tx-t5ru-p009-38pw-8k1m"
//        const val URL_STREAM =
//            "rtmp://rtmp.vngvn.uiza.io/live/live_31i07kQNmI"
    }

    override fun onCreate() {
        super.onCreate()
        UZUtil.setStrictMode()
    }
}
