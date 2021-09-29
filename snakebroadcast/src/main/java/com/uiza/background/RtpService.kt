package com.uiza.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.pedro.encoder.input.gl.render.filters.BaseFilterRender
import com.pedro.encoder.input.video.Camera2ApiManager
import com.pedro.encoder.input.video.CameraCallbacks
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtplibrary.base.Camera2Base
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.rtsp.RtspCamera2
import com.pedro.rtplibrary.util.FpsListener
import com.pedro.rtplibrary.view.OpenGlView
import com.uiza.R
import com.uiza.broadcast.CameraSize
import com.uiza.display.*
import com.uiza.util.UZConstant
import org.greenrobot.eventbus.EventBus

/**
 * Basic RTMP/RTSP service streaming implementation with camera2
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class RtpService : Service() {

    companion object {
        private const val TAG = "RtpService"
        private const val channelId = "rtpStreamChannel"

        const val KEY_END_POINT = "KEY_END_POINT"
        const val KEY_VIDEO_WIDTH = "KEY_VIDEO_WIDTH"
        const val KEY_VIDEO_HEIGHT = "KEY_VIDEO_HEIGHT"
        const val KEY_VIDEO_FPS = "KEY_VIDEO_FPS"
        const val KEY_VIDEO_BITRATE = "KEY_VIDEO_BITRATE"

        const val KEY_AUDIO_BITRATE = "KEY_AUDIO_BITRATE"
        const val KEY_AUDIO_SAMPLE_RATE = "KEY_AUDIO_SAMPLE_RATE"
        const val KEY_AUDIO_IS_STEREO = "KEY_AUDIO_IS_STEREO"
        const val KEY_AUDIO_ECHO_CANCELER = "KEY_AUDIO_ECHO_CANCELER"
        const val KEY_AUDIO_NOISE_SUPPRESSOR = "KEY_AUDIO_NOISE_SUPPRESSOR"

        private const val notifyId = 123456
        private var notificationManager: NotificationManager? = null
        private var camera2Base: Camera2Base? = null
        private var openGlView: OpenGlView? = null
        private var contextApp: Context? = null

        fun setView(openGlView: OpenGlView) {
            Companion.openGlView = openGlView
            camera2Base?.replaceView(openGlView)
        }

        fun setView(context: Context) {
            contextApp = context
            openGlView = null
            camera2Base?.replaceView(context)
        }

        /**
         * Start camera preview. Ignored, if stream or preview is started.
         *
         * @param cameraFacing front or back camera. Like: {@link com.pedro.encoder.input.video.CameraHelper.Facing#BACK}
         * {@link com.pedro.encoder.input.video.CameraHelper.Facing#FRONT}
         * @param rotation camera rotation (0, 90, 180, 270). Recommended: {@link
         * com.pedro.encoder.input.video.CameraHelper#getCameraOrientation(Context)}
         */
        fun startPreview(
            facing: CameraHelper.Facing,
            videoWidth: Int,
            videoHeight: Int,
            rotation: Int
        ) {
//            Log.d(TAG, ">>>>>>>startPreview ${facing.name}")
//            camera2Base?.stopPreview()
            camera2Base?.startPreview(
                facing,
                videoWidth,
                videoHeight,
                rotation
            )
//            Log.d(TAG, ">>>>>>>startPreview getCameraFacing ${getCameraFacing()?.name}")
        }

        fun init(context: Context) {
            contextApp = context
            if (camera2Base == null) {
                camera2Base = RtmpCamera2(context, true, connectCheckerRtp)
            }
        }

        fun stopStream() {
            camera2Base?.let { cam ->
                if (cam.isStreaming) {
                    cam.stopStream()
                }
            }
        }

        fun stopPreview() {
            camera2Base?.let { cam ->
                if (cam.isOnPreview) {
                    cam.stopPreview()
                }
            }
        }

        private val connectCheckerRtp = object : ConnectCheckerRtp {
            override fun onConnectionStartedRtp(rtpUrl: String) {
//                Log.d(TAG, "onConnectionStartedRtp")
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_connection_started))
                    EventBus.getDefault().post(OnConnectionStartedRtp(rtpUrl))
                }
            }

            override fun onConnectionSuccessRtp() {
//                Log.d(TAG, "onConnectionSuccessRtp")
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_started))
                    EventBus.getDefault().post(OnConnectionSuccessRtp())
                }
            }

            override fun onNewBitrateRtp(bitrate: Long) {
//                Log.d(TAG, "onNewBitrateRtp")
                EventBus.getDefault().post(OnNewBitrateRtp(bitrate))

            }

            override fun onConnectionFailedRtp(reason: String) {
//                Log.d(TAG, "onConnectionFailedRtp")
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_connection_failed))
                    EventBus.getDefault().post(OnConnectionFailedRtp(reason))
                }
            }

            override fun onDisconnectRtp() {
//                Log.d(TAG, "onDisconnectRtp")
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_stopped))
                    EventBus.getDefault().post(OnDisconnectRtp())
                }
            }

            override fun onAuthErrorRtp() {
//                Log.d(TAG, "onAuthErrorRtp")
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_auth_error))
                    EventBus.getDefault().post(OnAuthErrorRtp())
                }
            }

            override fun onAuthSuccessRtp() {
//                Log.d(TAG, "onAuthSuccessRtp")
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_auth_success))
                    EventBus.getDefault().post(OnAuthSuccessRtp())
                }
            }
        }

        private fun showNotification(text: String) {
            contextApp?.let {
                val notification = NotificationCompat.Builder(it, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(it.getString(R.string.rtp_stream))
                    .setContentText(text).build()
                notificationManager?.notify(notifyId, notification)
            }
        }

        fun setFilter(baseFilterRender: BaseFilterRender) {
            camera2Base?.glInterface?.setFilter(baseFilterRender)
        }

        fun getStreamWidth(): Int {
            return camera2Base?.streamWidth ?: 0
        }

        fun getStreamHeight(): Int {
            return camera2Base?.streamHeight ?: 0
        }

        fun enableAA(AAEnabled: Boolean) {
            camera2Base?.glInterface?.enableAA(AAEnabled)
        }

        fun isAAEnabled(): Boolean {
            return camera2Base?.glInterface?.isAAEnabled ?: false
        }

        fun setCameraCallbacks(cameraCallbacks: CameraCallbacks) {
            camera2Base?.setCameraCallbacks(cameraCallbacks)
        }

        fun setFpsListener(callback: FpsListener.Callback) {
            camera2Base?.setFpsListener(callback)
        }

        fun enableFaceDetection(callback: Camera2ApiManager.FaceDetectorCallback) {
            camera2Base?.enableFaceDetection(callback)
        }

        fun disableFaceDetection() {
            camera2Base?.disableFaceDetection()
        }

        fun isFaceDetectionEnabled(): Boolean? {
            return camera2Base?.isFaceDetectionEnabled
        }

        fun enableVideoStabilization(): Boolean? {
            return camera2Base?.enableVideoStabilization()
        }

        fun disableVideoStabilization() {
            camera2Base?.disableVideoStabilization()
        }

        fun isVideoStabilizationEnabled(): Boolean? {
            return camera2Base?.isVideoStabilizationEnabled
        }

        fun getCameraFacing(): CameraHelper.Facing? {
            return camera2Base?.cameraFacing
        }

        @Throws(Exception::class)
        fun enableLantern() {
            camera2Base?.enableLantern()
        }

        fun disableLantern() {
            camera2Base?.disableLantern()
        }

        fun isLanternEnabled(): Boolean? {
            return camera2Base?.isLanternEnabled
        }

        fun isLanternSupported(): Boolean? {
            return camera2Base?.isLanternSupported
        }

        fun enableAutoFocus() {
            camera2Base?.enableAutoFocus()
        }

        fun disableAutoFocus() {
            camera2Base?.disableAutoFocus()
        }

        fun isAutoFocusEnabled(): Boolean? {
            return camera2Base?.isAutoFocusEnabled
        }

        fun setFocusDistance(distance: Float) {
            camera2Base?.setFocusDistance(distance)
        }

        fun setAuthorization(user: String, password: String) {
            camera2Base?.setAuthorization(user, password)
        }

        fun getResolutionsBack(): List<CameraSize> {
            val listResolutionsBack = camera2Base?.resolutionsBack ?: emptyList()
            val list = ArrayList<CameraSize>()
            //remove item square
            listResolutionsBack.forEach {
                val w = it.width
                val h = it.height
                if (w != h && w <= UZConstant.RES_1920) {
                    list.add(CameraSize(width = w, height = h))
                }
            }
            return list
        }

        fun getResolutionsFront(): List<CameraSize> {
            val listResolutionsFront = camera2Base?.resolutionsFront ?: emptyList()
            val list = ArrayList<CameraSize>()
            //remove item square
            listResolutionsFront.forEach {
                val w = it.width
                val h = it.height
                if (w != h && w <= UZConstant.RES_1920) {
                    list.add(CameraSize(width = w, height = h))
                }
            }
            return list
        }

        fun disableAudio() {
            camera2Base?.disableAudio()
        }

        fun enableAudio() {
            camera2Base?.enableAudio()
        }

        fun isAudioMuted(): Boolean? {
            return camera2Base?.isAudioMuted
        }

        fun getBitrate(): Int? {
            return camera2Base?.bitrate
        }

        fun getResolutionValue(): Int? {
            return camera2Base?.resolutionValue
        }

        @Throws(CameraOpenException::class)
        fun switchCamera() {
            camera2Base?.switchCamera()
        }

        fun setExposure(value: Int) {
            camera2Base?.exposure = value
        }

        fun getExposure(): Int? {
            return camera2Base?.exposure
        }

        fun getMaxExposure(): Int? {
            return camera2Base?.maxExposure
        }

        fun getMinExposure(): Int? {
            return camera2Base?.minExposure
        }

        /**
         * Set video bitrate of H264 in bits per second while stream.
         *
         * @param bitrate H264 in bits per second.
         */
        fun setVideoBitrateOnFly(bitrate: Int) {
            camera2Base?.setVideoBitrateOnFly(bitrate)
        }

        /**
         * Set limit FPS while stream. This will be override when you call to prepareVideo method. This
         * could produce a change in iFrameInterval.
         *
         * @param fps frames per second
         */
        fun setLimitFPSOnFly(fps: Int) {
            camera2Base?.setLimitFPSOnFly(fps)
        }

        /**
         * Get stream state.
         *
         * @return true if streaming, false if not streaming.
         */
        fun isStreaming(): Boolean? {
            return camera2Base?.isStreaming
        }

        /**
         * Get preview state.
         *
         * @return true if enabled, false if disabled.
         */
        fun isOnPreview(): Boolean? {
            return camera2Base?.isOnPreview
        }

        //        Retries to connect with the given delay.
//        You can pass an optional backupUrl if you'd like to connect to your backup server instead of the original one. Given backupUrl replaces the original one.
        fun retry(delay: Long, reason: String, backupUrl: String?): Boolean? {
            return camera2Base?.reTry(delay, reason, backupUrl)
        }
    }

    private var endpoint: String? = null
    private var videoWidth = UZConstant.VIDEO_WIDTH_DEFAULT
    private var videoHeight = UZConstant.VIDEO_HEIGHT_DEFAULT
    private var videoFps = UZConstant.VIDEO_FPS_DEFAULT
    private var videoBitrate = UZConstant.VIDEO_BITRATE_DEFAULT

    private var audioBitrate = UZConstant.AUDIO_BITRATE_64
    private var audioSampleRate = UZConstant.AUDIO_SAMPLE_RATE_32000
    private var audioIsStereo = UZConstant.AUDIO_IS_STEREO_DEFAULT
    private var audioEchoCanceler = UZConstant.AUDIO_ECHO_CANCELER_DEFAULT
    private var audioNoiseSuppressor = UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "RtpService onCreate")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }
        keepAliveTrick()
    }

    private fun keepAliveTrick() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notification = NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        } else {
            startForeground(1, Notification())
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "RtpService onStartCommand")
        intent?.extras?.let { bundle ->
            endpoint = bundle.getString(KEY_END_POINT)
            videoWidth = bundle.getInt(KEY_VIDEO_WIDTH, UZConstant.VIDEO_WIDTH_DEFAULT)
            videoHeight = bundle.getInt(KEY_VIDEO_HEIGHT, UZConstant.VIDEO_HEIGHT_DEFAULT)
            videoFps = bundle.getInt(KEY_VIDEO_FPS, UZConstant.VIDEO_FPS_DEFAULT)
            videoBitrate = bundle.getInt(KEY_VIDEO_BITRATE, UZConstant.VIDEO_BITRATE_DEFAULT)
            audioBitrate = bundle.getInt(KEY_AUDIO_BITRATE, UZConstant.AUDIO_BITRATE_64)
            audioSampleRate =
                bundle.getInt(KEY_AUDIO_SAMPLE_RATE, UZConstant.AUDIO_SAMPLE_RATE_32000)
            audioIsStereo =
                bundle.getBoolean(KEY_AUDIO_IS_STEREO, UZConstant.AUDIO_IS_STEREO_DEFAULT)
            audioEchoCanceler =
                bundle.getBoolean(KEY_AUDIO_ECHO_CANCELER, UZConstant.AUDIO_ECHO_CANCELER_DEFAULT)
            audioNoiseSuppressor = bundle.getBoolean(
                KEY_AUDIO_NOISE_SUPPRESSOR,
                UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT
            )
        }
        endpoint?.let {
            prepareStreamRtp()
            startStreamRtp(it)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStream()
    }

    private fun prepareStreamRtp() {
        stopStream()
        stopPreview()
        endpoint?.let { ep ->
            camera2Base = if (ep.startsWith("rtmp")) {
                if (openGlView == null) {
                    RtmpCamera2(baseContext, true, connectCheckerRtp)
                } else {
                    RtmpCamera2(openGlView, connectCheckerRtp)
                }
            } else {
                if (openGlView == null) {
                    RtspCamera2(baseContext, true, connectCheckerRtp)
                } else {
                    RtspCamera2(openGlView, connectCheckerRtp)
                }
            }
        }
    }

    private fun startStreamRtp(endpoint: String) {
        camera2Base?.let { cam ->
            if (!cam.isStreaming) {

                Log.d(TAG, "startStreamRtp endpoint $endpoint")
                Log.d(TAG, "startStreamRtp videoWidth $videoWidth")
                Log.d(TAG, "startStreamRtp videoHeight $videoHeight")
                Log.d(TAG, "startStreamRtp videoFps $videoFps")
                Log.d(TAG, "startStreamRtp videoBitrate $videoBitrate")
                Log.d(TAG, "startStreamRtp audioBitrate $audioBitrate")
                Log.d(TAG, "startStreamRtp audioSampleRate $audioSampleRate")
                Log.d(TAG, "startStreamRtp audioIsStereo $audioIsStereo")
                Log.d(TAG, "startStreamRtp audioEchoCanceler $audioEchoCanceler")
                Log.d(TAG, "startStreamRtp audioNoiseSuppressor $audioNoiseSuppressor")
                val rotation = CameraHelper.getCameraOrientation(this)
                Log.d(TAG, "startStreamRtp rotation $rotation")
//                val facing = getCameraFacing()
//                Log.d(TAG, "startStreamRtp facing ${facing?.name}")
                if (cam.prepareVideo(
                        videoWidth,
                        videoHeight,
                        videoFps,
                        videoBitrate * 1024,
                        rotation
                    )
                    && cam.prepareAudio(
                        audioBitrate * 1024,
                        audioSampleRate,
                        audioIsStereo,
                        audioEchoCanceler,
                        audioNoiseSuppressor
                    )
                ) {
//                    facing?.let {
//                        startPreview(
//                            facing = it,
//                            videoWidth = videoWidth,
//                            videoHeight = videoHeight,
//                            rotation = rotation
//                        )
//                    }
                    cam.startStream(endpoint)
                }
            } else {
                showNotification(getString(R.string.you_are_already_streaming))
            }
        }
    }
}
