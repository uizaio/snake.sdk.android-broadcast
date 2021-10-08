package com.uiza

import androidx.multidex.MultiDexApplication
import com.uiza.util.UZUtil

class UZApplication : MultiDexApplication() {

    companion object {
        const val URL_STREAM =
            "rtmp://a.rtmp.youtube.com/live2/ybr5-0xxv-u9m0-3zaz-dss2"
    }

    override fun onCreate() {
        super.onCreate()
        UZUtil.setStrictMode()
    }
}
