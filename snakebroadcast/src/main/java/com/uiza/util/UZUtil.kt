package com.uiza.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.DisplayMetrics
import com.uiza.background.RtpService
import com.uiza.broadcast.CameraSize
import com.uiza.display.DisplayService

class UZUtil {

    companion object {
        fun setStrictMode() {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        const val URL_GIF =
            "https://i.pinimg.com/originals/90/28/92/902892347279519da4629d5177ac02d9.gif"

        const val URL_GIF_2 =
            "https://i.pinimg.com/originals/99/bb/50/99bb50c4218c35508a734a188a33ffcb.gif"

        fun getDpiOfCurrentScreen(context: Context?): Int {
            if (context == null) {
                return 0
            }
            val metrics: DisplayMetrics = context.resources.displayMetrics
            return (metrics.density * 160f).toInt()
        }

        fun getScreenWidth(): Int {
            return Resources.getSystem().displayMetrics.widthPixels
        }

        fun getScreenHeight(): Int {
            return Resources.getSystem().displayMetrics.heightPixels
        }

        fun getStableCameraSize(resolutionCamera: List<CameraSize>): CameraSize {

            //1280x960
            val list1280Width = resolutionCamera.filter {
                it.width == UZConstant.RESOLUTION_STABLE_FOR_SYSTEM_UIZA_1280
            }.sortedBy { it.height }
            if (list1280Width.isNotEmpty()) {
                return list1280Width.last()
            }

            val list1280Height = resolutionCamera.filter {
                it.height == UZConstant.RESOLUTION_STABLE_FOR_SYSTEM_UIZA_1280
            }.sortedBy { it.width }
            if (list1280Height.isNotEmpty()) {
                return list1280Height.last()
            }

            //720x480
            val list720Width = resolutionCamera.filter {
                it.width == UZConstant.RESOLUTION_STABLE_FOR_SYSTEM_UIZA_720
            }.sortedBy { it.height }
            if (list720Width.isNotEmpty()) {
                return list720Width.last()
            }

            //1640x720
            val list720Height = resolutionCamera.filter {
                it.height == UZConstant.RESOLUTION_STABLE_FOR_SYSTEM_UIZA_720
            }.sortedBy { it.width }
            if (list720Height.isNotEmpty()) {
                return list720Height.last()
            }

            return CameraSize(
                UZConstant.VIDEO_WIDTH_DEFAULT,
                UZConstant.VIDEO_HEIGHT_DEFAULT
            )
        }

        fun getBestCameraSize(resolutionCamera: List<CameraSize>): CameraSize {
            var cameraSize = CameraSize(
                UZConstant.VIDEO_WIDTH_DEFAULT,
                UZConstant.VIDEO_HEIGHT_DEFAULT
            )
            resolutionCamera.forEach {
                if (it.width > cameraSize.width && it.height > cameraSize.height) {
                    cameraSize = it
                }
            }
            return cameraSize
        }

        @JvmStatic
        fun toggleScreenOrientation(activity: Activity) {
            val s = getScreenOrientation(activity)
            if (s == Configuration.ORIENTATION_LANDSCAPE) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else if (s == Configuration.ORIENTATION_PORTRAIT) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        @JvmStatic
        fun changeScreenPortrait(activity: Activity) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        @JvmStatic
        fun changeScreenLandscape(activity: Activity) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        @JvmStatic
        fun getScreenOrientation(activity: Activity): Int {
            return activity.resources.configuration.orientation
        }

        @Suppress("DEPRECATION")
        fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun isStreamingBroadcastBackground(): Boolean? {
            return RtpService.isStreaming()
        }

        fun isStreamingDisplay(): Boolean? {
            return DisplayService.isStreaming()
        }
    }
}
