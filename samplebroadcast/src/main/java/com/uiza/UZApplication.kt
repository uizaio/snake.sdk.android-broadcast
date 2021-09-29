package com.uiza

import androidx.multidex.MultiDexApplication
import com.uiza.util.UZUtil

class UZApplication : MultiDexApplication() {

    companion object {
        const val URL_STREAM =
            "rtmp://a.rtmp.youtube.com/live2/8bdq-a4kv-q0k5-p2xm-2hqx"
    }

    override fun onCreate() {
        super.onCreate()
        UZUtil.setStrictMode()
    }
}
