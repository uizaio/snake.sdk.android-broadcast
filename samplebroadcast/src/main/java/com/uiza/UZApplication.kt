package com.uiza

import androidx.multidex.MultiDexApplication
import com.uiza.util.UZUtil

class UZApplication : MultiDexApplication() {

    companion object {
        const val URL_STREAM =
            "rtmp://a.rtmp.youtube.com/live2/ffy1-pap9-zdbf-qy2k-5faj"
    }

    override fun onCreate() {
        super.onCreate()
        UZUtil.setStrictMode()
    }
}
