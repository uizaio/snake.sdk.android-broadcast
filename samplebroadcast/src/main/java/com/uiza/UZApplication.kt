package com.uiza

import androidx.multidex.MultiDexApplication
import com.uiza.util.UZUtil

class UZApplication : MultiDexApplication() {

    companion object {
        const val URL_STREAM =
            "rtmp://rtmp.gcpsg.uizadev.io/live/live_AnafJmbQ6b"
    }

    override fun onCreate() {
        super.onCreate()
        UZUtil.setStrictMode()
    }
}
