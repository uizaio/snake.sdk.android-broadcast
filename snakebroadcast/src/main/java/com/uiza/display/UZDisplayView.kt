package com.uiza.display

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.pedro.rtplibrary.util.RecordController
import com.uiza.R
import com.uiza.util.UZConstant
import com.uiza.util.UZUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UZDisplayView : FrameLayout, LifecycleObserver {
    companion object {
        const val REQUEST_CODE_STREAM = 179
        const val REQUEST_CODE_RECORD = 180
    }

    private val logTag = javaClass.simpleName
    private var notificationManager: NotificationManager? = null
    var onConnectionStartedRtp: ((rtpUrl: String?) -> Unit)? = null
    var onConnectionSuccessRtp: ((Unit) -> Unit)? = null
    var onNewBitrateRtp: ((bitrate: Long?) -> Unit)? = null
    var onConnectionFailedRtp: ((reason: String?) -> Unit)? = null
    var onDisconnectRtp: ((Unit) -> Unit)? = null
    var onAuthErrorRtp: ((Unit) -> Unit)? = null
    var onAuthSuccessRtp: ((Unit) -> Unit)? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onViewCreate() {
//        Log.d(logTag, "onViewCreate")
        EventBus.getDefault().register(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onViewDestroy() {
//        Log.d(logTag, "onViewDestroy")
        EventBus.getDefault().unregister(this)
    }

    private fun init() {
        notificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        DisplayService.init(context)
        View.inflate(context, R.layout.layout_uz_display, this)

        if (context is LifecycleOwner) {
            (context as LifecycleOwner).lifecycle.addObserver(this)
        }
    }

//    Get stream state.
//    Returns:
//    true if streaming, false if not streaming.
    fun isStreaming(): Boolean? {
        return DisplayService.isStreaming()
    }

//    Get record state.
//    Returns:
//    true if recording, false if not recoding.
    fun isRecording(): Boolean? {
        return DisplayService.isRecording()
    }

    /**
     * This notification is to solve MediaProjection problem that only render surface if something
     * changed.
     * It could produce problem in some server like in Youtube that need send video and audio all time
     * to work.
     */
    private fun initNotification() {
        val notificationBuilder = Notification.Builder(context)
            .setSmallIcon(R.drawable.notification_anim)
            .setContentTitle(context.getString(R.string.streaming))
            .setContentText(context.getString(R.string.display_mode_stream))
            .setTicker(context.getString(R.string.stream_in_progress))
        notificationBuilder.setAutoCancel(true)
        notificationManager?.notify(DisplayService.notifyId, notificationBuilder.build())
    }

    fun stopNotification() {
        notificationManager?.cancel(DisplayService.notifyId)
    }

    fun start(activity: Activity) {
        activity.startActivityForResult(
            DisplayService.sendIntent(),
            REQUEST_CODE_STREAM
        )
    }

    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        endPoint: String,
        videoWidth: Int,
        videoHeight: Int,
        videoFps: Int,
        videoBitrate: Int,
        videoRotation: Int,
        videoDpi: Int,
        audioBitrate: Int,
        audioSampleRate: Int,
        audioIsStereo: Boolean,
        audioEchoCanceler: Boolean,
        audioNoiseSuppressor: Boolean,
    ) {
        if (videoRotation != UZConstant.VIDEO_ROTATION_0
            && videoRotation != UZConstant.VIDEO_ROTATION_90
            && videoRotation != UZConstant.VIDEO_ROTATION_180
            && videoRotation != UZConstant.VIDEO_ROTATION_270
        ) {
            throw IllegalArgumentException("Rotation could be 90, 180, 270 or 0")
        }
        if (audioBitrate != UZConstant.AUDIO_BITRATE_32
            && audioBitrate != UZConstant.AUDIO_BITRATE_64
            && audioBitrate != UZConstant.AUDIO_BITRATE_128
            && audioBitrate != UZConstant.AUDIO_BITRATE_256
        ) {
            throw IllegalArgumentException("audioBitrate could be 32, 64, 128 or 256")
        }
        if (audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_8000
            && audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_16000
            && audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_22500
            && audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_32000
            && audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_44100
        ) {
            throw IllegalArgumentException("audioSampleRate could be 8000, 16000, 22500, 32000, 44100")
        }

        if (data != null &&
            (requestCode == REQUEST_CODE_STREAM
                    || requestCode == REQUEST_CODE_RECORD
                    && resultCode == AppCompatActivity.RESULT_OK)
        ) {
            initNotification()
            DisplayService.setData(resultCode = resultCode, data = data)
            val intent = Intent(context, DisplayService::class.java)
            intent.putExtra(DisplayService.END_POINT, endPoint)

            intent.putExtra(DisplayService.VIDEO_WIDTH, videoWidth)
            intent.putExtra(DisplayService.VIDEO_HEIGHT, videoHeight)
            intent.putExtra(DisplayService.VIDEO_FPS, videoFps)
            intent.putExtra(DisplayService.VIDEO_BITRATE, videoBitrate)
            intent.putExtra(DisplayService.VIDEO_ROTATION, videoRotation)
            intent.putExtra(DisplayService.VIDEO_DPI, videoDpi)

            intent.putExtra(DisplayService.AUDIO_BITRATE, audioBitrate)
            intent.putExtra(DisplayService.AUDIO_SAMPLE_RATE, audioSampleRate)
            intent.putExtra(DisplayService.AUDIO_IS_STEREO, audioIsStereo)
            intent.putExtra(DisplayService.AUDIO_ECHO_CANCELER, audioEchoCanceler)
            intent.putExtra(DisplayService.AUDIO_NOISE_SUPPRESSOR, audioNoiseSuppressor)

            context.startService(intent)
        }
    }

    fun stop() {
        context.stopService(Intent(context, DisplayService::class.java))
    }

    //        Starts recording an MP4 video. Needs to be called while streaming.
//        Params:
//        path – Where file will be saved.
//        Throws:
//        IOException – If initialized before a stream.
    fun startRecord(path: String, listener: RecordController.Listener?) {
        DisplayService.startRecord(path = path, listener = listener)
    }

    //Stop record MP4 video started with @startRecord. If you don't call it file will be unreadable.
    fun stopRecord() {
        DisplayService.stopRecord()
    }

    //Mute microphone, can be called before, while and after stream.
    fun disableAudio() {
        DisplayService.disableAudio()
    }

    //Enable a muted microphone, can be called before, while and after stream.
    fun enableAudio() {
        DisplayService.enableAudio()
    }

    //        Get mute state of microphone.
//        Returns:
//        true if muted, false if enabled
    fun isAudioMuted(): Boolean? {
        return DisplayService.isAudioMuted()
    }

    fun getBitrate(): Int? {
        return DisplayService.getBitrate()
    }

    fun getResolutionValue(): Int? {
        return DisplayService.getResolutionValue()
    }

    fun getStreamWidth(): Int? {
        return DisplayService.getStreamWidth()
    }

    fun getStreamHeight(): Int? {
        return DisplayService.getStreamHeight()
    }

    //    Set video bitrate of H264 in bits per second while stream.
//    Params:
//    bitrate – H264 in bits per second
    fun setVideoBitrateOnFly(bitrate: Int) {
        DisplayService.setVideoBitrateOnFly(bitrate)
    }

    /**
     * Set limit FPS while stream. This will be override when you call to prepareVideo method.
     * This could produce a change in iFrameInterval.
     *
     * @param fps frames per second
     */
    fun setLimitFPSOnFly(fps: Int) {
        DisplayService.setLimitFPSOnFly(fps)
    }

    fun pauseRecord() {
        DisplayService.pauseRecord()
    }

    fun resumeRecord() {
        DisplayService.resumeRecord()
    }

    fun getRecordStatus(): RecordController.Status? {
        return DisplayService.getRecordStatus()
    }

    fun toggleScreenOrientation() {
        if (context is Activity) {
            UZUtil.toggleScreenOrientation(context as Activity)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnConnectionStartedRtp?) {
        event?.let {
//            Log.d(logTag, "onMessageEvent OnConnectionStartedRtp ${it.rtpUrl}")
            onConnectionStartedRtp?.invoke(it.rtpUrl)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnConnectionSuccessRtp?) {
        event?.let {
//            Log.d(logTag, "onMessageEvent OnConnectionSuccessRtp")
            onConnectionSuccessRtp?.invoke(Unit)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnNewBitrateRtp?) {
        event?.let {
//            Log.d(logTag, "onMessageEvent OnNewBitrateRtp ${it.bitrate}")
            onNewBitrateRtp?.invoke(it.bitrate)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnConnectionFailedRtp?) {
        event?.let {
//            Log.d(logTag, "onMessageEvent OnConnectionFailedRtp ${it.reason}")
            stopNotification()
            stop()
            onConnectionFailedRtp?.invoke(it.reason)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnDisconnectRtp?) {
        event?.let {
//            Log.d(logTag, "onMessageEvent OnDisconnectRtp")
            onDisconnectRtp?.invoke(Unit)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnAuthErrorRtp?) {
        event?.let {
//            Log.d(logTag, "onMessageEvent OnAuthErrorRtp")
            onAuthErrorRtp?.invoke(Unit)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnAuthSuccessRtp?) {
        event?.let {
//            Log.d(logTag, "onMessageEvent OnAuthSuccessRtp")
            onAuthSuccessRtp?.invoke(Unit)
        }
    }
}