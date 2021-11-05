package com.uiza.broadcast

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import com.pedro.encoder.input.gl.SpriteGestureController
import com.pedro.encoder.input.gl.render.filters.BaseFilterRender
import com.pedro.encoder.input.video.Camera1ApiManager
import com.pedro.encoder.input.video.CameraCallbacks
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.util.BitrateAdapter
import com.pedro.rtplibrary.util.FpsListener
import com.pedro.rtplibrary.util.RecordController
import com.pedro.rtplibrary.view.TakePhotoCallback
import com.uiza.R
import com.uiza.util.UZConstant
import com.uiza.util.UZUtil
import kotlinx.android.synthetic.main.layout_uz_broadcast.view.*

class UZBroadCastView :
    FrameLayout,
    ConnectCheckerRtmp,
    SurfaceHolder.Callback,
    View.OnTouchListener {

    private val logTag = javaClass.simpleName
    private var rtmpCamera1: RtmpCamera1? = null
    val spriteGestureController = SpriteGestureController()
    var onAuthErrorRtmp: ((Unit) -> Unit)? = null
    var onAuthSuccessRtmp: ((Unit) -> Unit)? = null
    var onConnectionFailedRtmp: ((reason: String) -> Unit)? = null
    var onConnectionStartedRtmp: ((rtmpUrl: String) -> Unit)? = null
    var onConnectionSuccessRtmp: ((Unit) -> Unit)? = null
    var onDisconnectRtmp: ((Unit) -> Unit)? = null
    var onNewBitrateRtmp: ((bitrate: Long) -> Unit)? = null
    var onSurfaceChanged: ((holder: SurfaceHolder, format: Int, width: Int, height: Int) -> Unit)? =
        null
    var onSurfaceDestroyed: ((holder: SurfaceHolder) -> Unit)? = null

    // Adaptative video bitrate
    private var bitrateAdapter: BitrateAdapter? = null
    var isAdaptativeVideoBitrate = true

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

    private fun init() {
        View.inflate(context, R.layout.layout_uz_broadcast, this)
        rtmpCamera1 = RtmpCamera1(surfaceView, this)
        surfaceView.holder?.addCallback(this)
        surfaceView.setOnTouchListener(this)
    }

    override fun onAuthErrorRtmp() {
//        Log.d(logTag, "onAuthErrorRtmp")
        onAuthErrorRtmp?.invoke(Unit)
    }

    override fun onAuthSuccessRtmp() {
//        Log.d(logTag, "onAuthSuccessRtmp")
        onAuthSuccessRtmp?.invoke(Unit)
    }

    override fun onConnectionFailedRtmp(reason: String) {
//        Log.d(logTag, "Connection failed. $reason")
        rtmpCamera1?.stopStream()
        onConnectionFailedRtmp?.invoke(reason)
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
//        Log.d(logTag, "onConnectionStartedRtmp rtmpUrl $rtmpUrl")
        onConnectionStartedRtmp?.invoke(rtmpUrl)
    }

    override fun onConnectionSuccessRtmp() {
//        Log.d(logTag, "onConnectionSuccessRtmp")
        onConnectionSuccessRtmp?.invoke(Unit)

        if (isAdaptativeVideoBitrate) {
            bitrateAdapter = BitrateAdapter { bitrate ->
                setVideoBitrateOnFly(bitrate)
            }
            getBitrate()?.let { br ->
                bitrateAdapter?.setMaxBitrate(br)
            }
        }
    }

    override fun onDisconnectRtmp() {
//        Log.d(logTag, "onDisconnectRtmp")
        onDisconnectRtmp?.invoke(Unit)
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
//        Log.d(logTag, "onNewBitrateRtmp bitrate $bitrate")
        onNewBitrateRtmp?.invoke(bitrate)

        if (isAdaptativeVideoBitrate) {
            bitrateAdapter?.adaptBitrate(bitrate)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(logTag, "surfaceCreated")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(logTag, "surfaceChanged width $width, height $height")
        Log.d(logTag, "getStreamWidth ${getStreamWidth()}")
        Log.d(logTag, "getStreamHeight ${getStreamHeight()}")
        onSurfaceChanged?.invoke(holder, format, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        rtmpCamera1?.let { rc ->
            if (rc.isRecording) {
                rc.stopRecord()
            }
            if (rc.isStreaming) {
                rc.stopStream()
            }
            rc.stopPreview()
        }
        onSurfaceDestroyed?.invoke(holder)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (spriteGestureController.spriteTouched(v, event)) {
            spriteGestureController.moveSprite(v, event)
            spriteGestureController.scaleSprite(event)
            return true
        }
        return false
    }

    //    Enable or disable Anti aliasing (This method use FXAA).
//    Params:
//    AAEnabled – true is AA enabled, false is AA disabled. False by default.
    fun enableAA(AAEnabled: Boolean) {
        rtmpCamera1?.glInterface?.enableAA(AAEnabled)
    }

    fun isAAEnabled(): Boolean {
        return rtmpCamera1?.glInterface?.isAAEnabled ?: false
    }

    //    Set a filter to stream. You can select any filter from com.pedro.encoder.input.gl.render.filters or create your own filter if you extends from BaseFilterRender
//    Params:
//    baseFilterRender – filter to set. You can modify parameters to filter after set it to stream.
    fun setFilter(baseFilterRender: BaseFilterRender) {
        rtmpCamera1?.glInterface?.setFilter(baseFilterRender)
    }

    //    Capture an Image from Opengl.
//    Params:
//    takePhotoCallback – callback where you will get your image like a bitmap.
    fun takePhoto(takePhotoCallback: TakePhotoCallback) {
        rtmpCamera1?.glInterface?.takePhoto(takePhotoCallback)
    }

    fun getBitrate(): Int? {
        return rtmpCamera1?.bitrate
    }

    fun getResolutionValue(): Int? {
        return rtmpCamera1?.resolutionValue
    }

    fun getStreamWidth(): Int {
        return rtmpCamera1?.streamWidth ?: 0
    }

    fun getStreamHeight(): Int {
        return rtmpCamera1?.streamHeight ?: 0
    }

    fun stopListenerSpriteGestureController() {
        spriteGestureController.stopListener()
    }

    /**
     * Set video bitrate of H264 in bits per second while stream.
     *
     * @param bitrate H264 in bits per second.
     */
    fun setVideoBitrateOnFly(bitrate: Int) {
        rtmpCamera1?.setVideoBitrateOnFly(bitrate)
    }

    fun isStreaming(): Boolean {
        return rtmpCamera1?.isStreaming ?: false
    }

    fun isOnPreview(): Boolean {
        return rtmpCamera1?.isOnPreview ?: false
    }

    fun isRecording(): Boolean {
        return rtmpCamera1?.isRecording ?: false
    }

    fun pauseRecord() {
        rtmpCamera1?.pauseRecord()
    }

    fun resumeRecord() {
        rtmpCamera1?.resumeRecord()
    }

    fun getRecordStatus(): RecordController.Status? {
        return rtmpCamera1?.recordStatus
    }

    //    Start camera preview. Ignored, if stream or preview is started.
//    Params:
//    cameraFacing – front or back camera. Like: CameraHelper.Facing.BACK CameraHelper.Facing.FRONT
//    width – of preview in px.
//    height – of preview in px.
//    rotation – camera rotation (0, 90, 180, 270). Recommended: CameraHelper.getCameraOrientation(Context)
    fun startPreview(
        cameraFacing: CameraHelper.Facing,
        width: Int,
        height: Int,
        rotation: Int,
    ) {
//        Log.d(logTag, "startPreview width $width, height $height")
        rtmpCamera1?.startPreview(
            cameraFacing,
            width,
            height,
            rotation,
        )
    }

    fun startPreview(
        cameraFacing: CameraHelper.Facing,
        rotation: Int,
    ) {
        val cameraSize = getStableCameraSize()
        startPreview(
            cameraFacing,
            cameraSize.width,
            cameraSize.height,
            rotation,
        )
    }

    fun getStableCameraSize(): CameraSize {
        val resolutionCamera = if (isFrontCamera()) {
            getResolutionsFront()
        } else {
            getResolutionsBack()
        }
        return UZUtil.getStableCameraSize(resolutionCamera)
    }

    fun stopPreview() {
        rtmpCamera1?.stopPreview()
    }

    //    Call this method before use @startStream. If not you will do a stream without audio.
//    Params:
//    bitrate – AAC in kb.
//    sampleRate – of audio in hz. Can be 8000, 16000, 22500, 32000, 44100.
//    isStereo – true if you want Stereo audio (2 audio channels), false if you want Mono audio (1 audio channel).
//    echoCanceler – true enable echo canceler, false disable.
//    noiseSuppressor – true enable noise suppressor, false disable.
//    Returns:
//    true if success, false if you get a error (Normally because the encoder selected doesn't support any configuration seated or your device hasn't a AAC encoder).
    fun prepareAudio(
        audioBitrate: Int,
        audioSampleRate: Int,
        audioIsStereo: Boolean,
        audioEchoCanceler: Boolean,
        audioNoiseSuppressor: Boolean,
    ): Boolean {
        Log.d(
            logTag,
            "prepareAudio audioBitrate $audioBitrate, audioSampleRate $audioSampleRate, audioIsStereo $audioIsStereo, audioEchoCanceler $audioEchoCanceler, audioNoiseSuppressor $audioNoiseSuppressor"
        )
        if (audioBitrate != UZConstant.AUDIO_BITRATE_32 &&
            audioBitrate != UZConstant.AUDIO_BITRATE_64 &&
            audioBitrate != UZConstant.AUDIO_BITRATE_128 &&
            audioBitrate != UZConstant.AUDIO_BITRATE_256
        ) {
            throw IllegalArgumentException("audioBitrate could be 32, 64, 128 or 256")
        }
        if (audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_8000 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_16000 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_22500 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_32000 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_44100
        ) {
            throw IllegalArgumentException("audioSampleRate could be 8000, 16000, 22500, 32000, 44100")
        }
        return rtmpCamera1?.prepareAudio(
            audioBitrate * 1024,
            audioSampleRate,
            audioIsStereo,
            audioEchoCanceler,
            audioNoiseSuppressor
        ) ?: false
    }

    //    Call this method before use @startStream. If not you will do a stream without video. NOTE: Rotation with encoder is silence ignored in some devices.
//    Params:
//    width – resolution in px.
//    height – resolution in px.
//    fps – frames per second of the stream.
//    bitrate – H264 in bps.
//    rotation – could be 90, 180, 270 or 0. You should use CameraHelper.getCameraOrientation with SurfaceView or TextureView and 0 with OpenGlView or LightOpenGlView. NOTE: Rotation with encoder is silence ignored in some devices.
//    Returns:
//    true if success, false if you get a error (Normally because the encoder selected doesn't support any configuration seated or your device hasn't a H264 encoder).
    fun prepareVideo(
        videoWidth: Int,
        videoHeight: Int,
        videoFps: Int,
        videoBitrate: Int,
        videoRotation: Int? = null,
    ): Boolean {
        val mVideoRotation = videoRotation ?: CameraHelper.getCameraOrientation(context)
        Log.d(
            logTag,
            "prepareVideo videoWidth $videoWidth, videoHeight $videoHeight," +
                " videoFps $videoFps, videoBitrate $videoBitrate, videoRotation $videoRotation"
        )
        return rtmpCamera1?.prepareVideo(
            videoWidth,
            videoHeight,
            videoFps,
            videoBitrate * 1024,
            mVideoRotation,
        ) ?: false
    }

    /**
     * Need be called after @prepareVideo or/and @prepareAudio. This method override resolution of
     *
     * @param url of the stream like: protocol://ip:port/application/streamName
     *
     * RTSP: rtsp://192.168.1.1:1935/live/pedroSG94 RTSPS: rtsps://192.168.1.1:1935/live/pedroSG94
     * RTMP: rtmp://192.168.1.1:1935/live/pedroSG94 RTMPS: rtmps://192.168.1.1:1935/live/pedroSG94
     * @startPreview to resolution seated in @prepareVideo. If you never startPreview this method
     * startPreview for you to resolution seated in @prepareVideo.
     */
    fun startStream(url: String) {
        rtmpCamera1?.startStream(url)
    }

    // Stop stream started with @startStream.
    fun stopStream(
        delayStopStreamInMls: Long = UZConstant.DELAY_STOP_STREAM_IN_MLS,
        onStopPreExecute: ((Unit) -> Unit),
        onStopSuccess: ((Boolean) -> Unit)
    ) {
        if (delayStopStreamInMls <= 0) {
            throw IllegalArgumentException("Invalid value for parameter delayStopStreamInMls")
        }

        onStopPreExecute.invoke(Unit)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                rtmpCamera1?.stopStream()
                onStopSuccess.invoke(true)
            } catch (e: Exception) {
                onStopSuccess.invoke(false)
            }
        }, delayStopStreamInMls)
    }

    //    Retries to connect with the given delay. You can pass an optional backupUrl if
//    you'd like to connect to your backup server instead of the original one. Given backupUrl replaces the original one.
    fun retry(delay: Long, reason: String, backupUrl: String? = null): Boolean? {
        return rtmpCamera1?.reTry(delay, reason, backupUrl)
    }

    fun getResolutionsBack(): List<CameraSize> {
        val listResolutionsBack = rtmpCamera1?.resolutionsBack ?: emptyList()
        val list = ArrayList<CameraSize>()
        // remove item square
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
        val listResolutionsFront = rtmpCamera1?.resolutionsFront ?: emptyList()
        val list = ArrayList<CameraSize>()
        // remove item square
        listResolutionsFront.forEach {
            val w = it.width
            val h = it.height
            if (w != h && w <= UZConstant.RES_1920) {
                list.add(CameraSize(width = w, height = h))
            }
        }
        return list
    }

    /**
     * Mute microphone, can be called before, while and after stream.
     */
    fun disableAudio() {
        rtmpCamera1?.disableAudio()
    }

    //    Enable a muted microphone, can be called before, while and after stream.
    fun enableAudio() {
        rtmpCamera1?.enableAudio()
    }

    //    Get mute state of microphone.
//    Returns:
//    true if muted, false if enabled
    fun isAudioMuted(): Boolean? {
        return rtmpCamera1?.isAudioMuted
    }

    fun switchCamera() {
        try {
            rtmpCamera1?.switchCamera()
        } catch (e: CameraOpenException) {
            e.printStackTrace()
        }
    }

    fun setCameraFront() {
        try {
            if (!isFrontCamera()) {
                switchCamera()
            }
        } catch (e: CameraOpenException) {
            e.printStackTrace()
        }
    }

    fun setCameraBack() {
        try {
            if (isFrontCamera()) {
                switchCamera()
            }
        } catch (e: CameraOpenException) {
            e.printStackTrace()
        }
    }

    //    Starts recording an MP4 video. Needs to be called while streaming.
//    Params:
//    path – Where file will be saved.
//    Throws:
//    IOException – If initialized before a stream.
    fun startRecord(path: String) {
        rtmpCamera1?.startRecord(path)
    }

    // Stop record MP4 video started with @startRecord. If you don't call it file will be unreadable.
    fun stopRecord() {
        rtmpCamera1?.stopRecord()
    }

    /**
     * Basic auth developed to work with Wowza. No tested with other server
     *
     * @param user auth.
     * @param password auth.
     */
    fun setAuthorization(user: String, password: String) {
        rtmpCamera1?.setAuthorization(user, password)
    }

    // Change preview orientation can be called while stream.
    // Params:
    // orientation – of the camera preview. Could be 90, 180, 270 or 0.
    fun setPreviewOrientation(orientation: Int) {
        rtmpCamera1?.setPreviewOrientation(orientation)
    }

    fun setCameraCallbacks(cameraCallbacks: CameraCallbacks) {
        rtmpCamera1?.setCameraCallbacks(cameraCallbacks)
    }

    /**
     * @param callback get fps while record or stream
     */
    fun setFpsListener(callback: FpsListener.Callback) {
        rtmpCamera1?.setFpsListener(callback)
    }

    /**
     * @return true if success, false if fail (not supported or called before start camera)
     */
    fun enableFaceDetection(callback: Camera1ApiManager.FaceDetectorCallback): Boolean {
        return rtmpCamera1?.enableFaceDetection(callback) ?: false
    }

    fun disableFaceDetection() {
        rtmpCamera1?.disableFaceDetection()
    }

    fun isFaceDetectionEnabled(): Boolean {
        return rtmpCamera1?.isFaceDetectionEnabled ?: false
    }

    /**
     * @return true if success, false if fail (not supported or called before start camera)
     */
    fun enableVideoStabilization() {
        rtmpCamera1?.enableVideoStabilization()
    }

    fun disableVideoStabilization() {
        rtmpCamera1?.disableVideoStabilization()
    }

    fun isVideoStabilizationEnabled(): Boolean {
        return rtmpCamera1?.isVideoStabilizationEnabled ?: false
    }

    fun isFrontCamera(): Boolean {
        return getCameraFacing() == CameraHelper.Facing.FRONT
    }

    fun getCameraFacing(): CameraHelper.Facing? {
        return rtmpCamera1?.cameraFacing
    }

    fun isLanternEnabled(): Boolean? {
        return rtmpCamera1?.isLanternEnabled
    }

    fun enableLantern(): Boolean {
        return try {
            rtmpCamera1?.enableLantern()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun disableLantern() {
        rtmpCamera1?.disableLantern()
    }

    fun isAutoFocusEnabled(): Boolean {
        return rtmpCamera1?.isAutoFocusEnabled ?: false
    }

    fun enableAutoFocus() {
        rtmpCamera1?.enableAutoFocus()
    }

    fun disableAutoFocus() {
        rtmpCamera1?.disableAutoFocus()
    }

    fun toggleScreenOrientation() {
        if (context is Activity) {
            UZUtil.toggleScreenOrientation(context as Activity)
        }
    }
}
