package com.uiza.display

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
import com.pedro.rtplibrary.base.DisplayBase
import com.pedro.rtplibrary.rtmp.RtmpDisplay
import com.pedro.rtplibrary.rtsp.RtspDisplay
import com.pedro.rtplibrary.util.RecordController
import com.uiza.R
import com.uiza.background.ConnectCheckerRtp
import com.uiza.util.UZConstant
import org.greenrobot.eventbus.EventBus

/**
 * Basic RTMP/RTSP service streaming implementation with camera2
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DisplayService : Service() {

    companion object {
        private const val TAG = "DisplayService"

        const val END_POINT = "endpoint"

        const val VIDEO_WIDTH = "VIDEO_WIDTH"
        const val VIDEO_HEIGHT = "VIDEO_HEIGHT"
        const val VIDEO_FPS = "VIDEO_FPS"
        const val VIDEO_BITRATE = "VIDEO_BITRATE"
        const val VIDEO_ROTATION = "VIDEO_ROTATION"
        const val VIDEO_DPI = "VIDEO_DPI"

        const val AUDIO_BITRATE = "AUDIO_BITRATE"
        const val AUDIO_SAMPLE_RATE = "AUDIO_SAMPLE_RATE"
        const val AUDIO_IS_STEREO = "AUDIO_IS_STEREO"
        const val AUDIO_ECHO_CANCELER = "AUDIO_ECHO_CANCELER"
        const val AUDIO_NOISE_SUPPRESSOR = "AUDIO_NOISE_SUPPRESSOR"

        private const val channelId = "rtpDisplayStreamChannel"
        const val notifyId = 123456
        private var notificationManager: NotificationManager? = null
        private var displayBase: DisplayBase? = null
        private var contextApp: Context? = null
        private var resultCode: Int? = null
        private var data: Intent? = null

        fun init(context: Context) {
            Log.d(TAG, "init")
            contextApp = context
            if (displayBase == null) {
                displayBase = RtmpDisplay(context, true, connectCheckerRtp)
            }
        }

        fun setData(resultCode: Int, data: Intent) {
            Companion.resultCode = resultCode
            Companion.data = data
        }

        fun sendIntent(): Intent? {
            return if (displayBase != null) {
                displayBase?.sendIntent()
            } else {
                null
            }
        }

        fun isStreaming(): Boolean? {
            return displayBase?.isStreaming
        }

        fun isRecording(): Boolean? {
            return displayBase?.isRecording
        }

        fun stopStream() {
            displayBase?.let {
                if (it.isStreaming) {
                    it.stopStream()
                }
            }
        }

//        fun setFpsListener(callback: FpsListener.Callback) {
//            displayBase?.setFpsListener(callback)
//        }

        fun setAuthorization(user: String, password: String) {
            displayBase?.setAuthorization(user, password)
        }

        private val connectCheckerRtp = object : ConnectCheckerRtp {

            override fun onConnectionStartedRtp(rtpUrl: String) {
                EventBus.getDefault().post(OnConnectionStartedRtp(rtpUrl = rtpUrl))
            }

            override fun onConnectionSuccessRtp() {
//                Log.e(TAG, "RTP service destroy")
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_started))
                }
                EventBus.getDefault().post(OnConnectionSuccessRtp())
            }

            override fun onNewBitrateRtp(bitrate: Long) {
//                Log.d(TAG, "onNewBitrateRtp bitrate $bitrate")
                EventBus.getDefault().post(OnNewBitrateRtp(bitrate))
            }

            override fun onConnectionFailedRtp(reason: String) {
//                Log.e(TAG, "RTP service destroy")
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_connection_failed))
                }
                EventBus.getDefault().post(OnConnectionFailedRtp(reason))
            }

            override fun onDisconnectRtp() {
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_stopped))
                }
                EventBus.getDefault().post(OnDisconnectRtp())
            }

            override fun onAuthErrorRtp() {
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_auth_error))
                }
                EventBus.getDefault().post(OnAuthErrorRtp())
            }

            override fun onAuthSuccessRtp() {
                contextApp?.let {
                    showNotification(it.getString(R.string.stream_auth_success))
                }
                EventBus.getDefault().post(OnAuthSuccessRtp())
            }
        }

        private fun showNotification(text: String) {
            contextApp?.let {
                val notification = NotificationCompat.Builder(it, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(it.getString(R.string.rtp_display_stream))
                    .setContentText(text).build()
                notificationManager?.notify(notifyId, notification)
            }
        }

        //        Starts recording an MP4 video. Needs to be called while streaming.
//        Params:
//        path – Where file will be saved.
//        Throws:
//        IOException – If initialized before a stream.
        fun startRecord(path: String, listener: RecordController.Listener? = null) {
            displayBase?.startRecord(path, listener)
        }

        //        Stop record MP4 video started with @startRecord. If you don't call it file will be unreadable.
        fun stopRecord() {
            displayBase?.stopRecord()
        }

        //Mute microphone, can be called before, while and after stream.
        fun disableAudio() {
            displayBase?.disableAudio()
        }

        //Enable a muted microphone, can be called before, while and after stream.
        fun enableAudio() {
            displayBase?.enableAudio()
        }

        //        Get mute state of microphone.
//        Returns:
//        true if muted, false if enabled
        fun isAudioMuted(): Boolean? {
            return displayBase?.isAudioMuted
        }

        fun getBitrate(): Int? {
            return displayBase?.bitrate
        }

        fun getResolutionValue(): Int? {
            return displayBase?.resolutionValue
        }

        fun getStreamWidth(): Int? {
            return displayBase?.streamWidth
        }

        fun getStreamHeight(): Int? {
            return displayBase?.streamHeight
        }

        /*Set video bitrate of H264 in bits per second while stream.
        Params:
        bitrate – H264 in bits per second*/
        fun setVideoBitrateOnFly(bitrate: Int) {
            displayBase?.setVideoBitrateOnFly(bitrate)
        }

        /**
         * Set limit FPS while stream. This will be override when you call to prepareVideo method.
         * This could produce a change in iFrameInterval.
         *
         * @param fps frames per second
         */
        fun setLimitFPSOnFly(fps: Int) {
            displayBase?.setLimitFPSOnFly(fps)
        }

        fun pauseRecord() {
            displayBase?.pauseRecord()
        }

        fun resumeRecord() {
            displayBase?.resumeRecord()
        }

        fun getRecordStatus(): RecordController.Status? {
            return displayBase?.recordStatus
        }

        /**
         * Retries to connect with the given delay. You can pass an optional backupUrl
         * if you'd like to connect to your backup server instead of the original one.
         * Given backupUrl replaces the original one.
         */
        fun retry(delay: Long, reason: String, backupUrl: String? = null): Boolean? {
            return displayBase?.reTry(delay, reason, backupUrl)
        }
    }

    private var endpoint: String? = null

    private var videoWidth: Int = UZConstant.VIDEO_WIDTH_DEFAULT
    private var videoHeight: Int = UZConstant.VIDEO_HEIGHT_DEFAULT
    private var videoFps: Int = UZConstant.VIDEO_FPS_DEFAULT
    private var videoBitrate: Int = UZConstant.VIDEO_BITRATE_DEFAULT
    private var videoRotation: Int = UZConstant.VIDEO_ROTATION_DEFAULT
    private var videoDpi: Int = UZConstant.VIDEO_DPI_DEFAULT

    private var audioBitrate: Int = UZConstant.AUDIO_BITRATE_DEFAULT
    private var audioSampleRate: Int = UZConstant.AUDIO_SAMPLE_RATE_DEFAULT
    private var audioIsStereo: Boolean = UZConstant.AUDIO_IS_STEREO_DEFAULT
    private var audioEchoCanceler: Boolean = UZConstant.AUDIO_ECHO_CANCELER_DEFAULT
    private var audioNoiseSuppressor: Boolean = UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "RTP Display service create")
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
//        Log.e(TAG, "RTP Display service started")

        intent?.extras?.let { bundle ->
            endpoint = bundle.getString(END_POINT)

            videoWidth = bundle.getInt(VIDEO_WIDTH, UZConstant.VIDEO_WIDTH_DEFAULT)
            videoHeight = bundle.getInt(VIDEO_HEIGHT, UZConstant.VIDEO_HEIGHT_DEFAULT)
            videoFps = bundle.getInt(VIDEO_FPS, UZConstant.VIDEO_FPS_DEFAULT)
            videoBitrate = bundle.getInt(VIDEO_BITRATE, UZConstant.VIDEO_BITRATE_DEFAULT)
            videoRotation = bundle.getInt(VIDEO_ROTATION, UZConstant.VIDEO_ROTATION_DEFAULT)
            videoDpi = bundle.getInt(VIDEO_DPI, UZConstant.VIDEO_DPI_DEFAULT)

            audioBitrate = bundle.getInt(AUDIO_BITRATE, UZConstant.AUDIO_BITRATE_DEFAULT)
            audioSampleRate = bundle.getInt(AUDIO_SAMPLE_RATE, UZConstant.AUDIO_SAMPLE_RATE_DEFAULT)
            audioIsStereo = bundle.getBoolean(AUDIO_IS_STEREO, UZConstant.AUDIO_IS_STEREO_DEFAULT)
            audioEchoCanceler =
                bundle.getBoolean(AUDIO_ECHO_CANCELER, UZConstant.AUDIO_ECHO_CANCELER_DEFAULT)
            audioNoiseSuppressor =
                bundle.getBoolean(AUDIO_NOISE_SUPPRESSOR, UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT)
        }
        endpoint?.let {
            prepareStreamRtp()
            startStreamRtp(it)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "RTP Display service destroy")
        stopStream()
    }

    private fun prepareStreamRtp() {
        stopStream()
        if (endpoint?.startsWith("rtmp") == true) {
            displayBase = RtmpDisplay(baseContext, true, connectCheckerRtp)
            resultCode?.let {
                displayBase?.setIntentResult(it, data)
            }
        } else {
            displayBase = RtspDisplay(baseContext, true, connectCheckerRtp)
            resultCode?.let {
                displayBase?.setIntentResult(it, data)
            }
        }
    }

    private fun startStreamRtp(endpoint: String) {
        displayBase?.let { db ->
            if (!db.isStreaming) {

                Log.d(TAG, "videoWidth $videoWidth")
                Log.d(TAG, "videoHeight $videoHeight")
                Log.d(TAG, "videoFps $videoFps")
                Log.d(TAG, "videoBitrate $videoBitrate")
                Log.d(TAG, "videoRotation $videoRotation")
                Log.d(TAG, "videoDpi $videoDpi")

                Log.d(TAG, "audioBitrate $audioBitrate")
                Log.d(TAG, "audioSampleRate $audioSampleRate")
                Log.d(TAG, "audioIsStereo $audioIsStereo")
                Log.d(TAG, "audioEchoCanceler $audioEchoCanceler")
                Log.d(TAG, "audioNoiseSuppressor $audioNoiseSuppressor")

                if (db.prepareVideo(
                        videoWidth,
                        videoHeight,
                        videoFps,
                        videoBitrate * 1024,
                        videoRotation,
                        videoDpi
                    )
                    && db.prepareAudio(
                        audioBitrate * 1024,
                        audioSampleRate,
                        audioIsStereo,
                        audioEchoCanceler,
                        audioNoiseSuppressor
                    )
                ) {
                    db.startStream(endpoint)
                } else {
                    Log.e(TAG, "startStreamRtp >>> do nothing")
                }
            } else {
                contextApp?.let {
                    showNotification(it.getString(R.string.you_are_already_streaming))
                }
            }
        }
    }
}
