package com.uiza

import androidx.multidex.MultiDexApplication
import com.uiza.util.UZUtil

class UZApplication : MultiDexApplication() {

    companion object {
        const val URL_STREAM =
            "rtmp://a.rtmp.youtube.com/live2/thzy-sxva-5wjf-k1hk-68rh"

//        const val URL_STREAM = "rtmp://worker-live.uizadev.io/live/test"
    }

    override fun onCreate() {
        super.onCreate()
        UZUtil.setStrictMode()
    }
}
