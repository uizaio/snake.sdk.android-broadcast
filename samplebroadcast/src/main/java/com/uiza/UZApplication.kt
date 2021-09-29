package com.uiza

import androidx.multidex.MultiDexApplication
import com.uiza.util.UZUtil

class UZApplication : MultiDexApplication() {

    companion object {
        const val URL_STREAM =
            "rtmp://rtmp.alyin.uiza.io/live/live_wbVJjJGD9u"
    }

    override fun onCreate() {
        super.onCreate()
        UZUtil.setStrictMode()
    }
}
