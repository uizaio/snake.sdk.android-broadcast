package com.uiza.background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.pedro.encoder.input.video.Camera2ApiManager
import com.pedro.encoder.input.video.CameraCallbacks
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.input.video.CameraOpenException
import com.uiza.R
import com.uiza.broadcast.CameraSize
import com.uiza.display.*
import com.uiza.util.UZUtil
import kotlinx.android.synthetic.main.layout_uz_background.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UZBackgroundView : FrameLayout, LifecycleObserver, SurfaceHolder.Callback {
    private val logTag = javaClass.simpleName
    var onSurfaceCreated: ((holder: SurfaceHolder) -> Unit)? = null
    var onSurfaceChanged: ((holder: SurfaceHolder, format: Int, width: Int, height: Int) -> Unit)? =
        null
    var onSurfaceDestroyed: ((holder: SurfaceHolder) -> Unit)? = null
    var onConnectionStartedRtp: ((rtpUrl: String?) -> Unit)? = null
    var onConnectionSuccessRtp: ((Unit) -> Unit)? = null
    var onNewBitrateRtp: ((bitrate: Long) -> Unit)? = null
    var onConnectionFailedRtp: ((reason: String) -> Unit)? = null
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
        View.inflate(context, R.layout.layout_uz_background, this)
        if (context is LifecycleOwner) {
            (context as LifecycleOwner).lifecycle.addObserver(this)
        }
        RtpService.init(context)
        surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        onSurfaceCreated?.invoke(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        RtpService.setView(surfaceView)
        onSurfaceChanged?.invoke(holder, format, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        RtpService.setView(context.applicationContext)
        stopPreview()
        onSurfaceDestroyed?.invoke(holder)
    }

    fun isServiceRunning(): Boolean {
        return UZUtil.isServiceRunning(context = context, serviceClass = RtpService::class.java)
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
        videoWidth: Int,
        videoHeight: Int,
    ) {
        RtpService.startPreview(
            facing = CameraHelper.Facing.BACK,
            videoWidth = videoWidth,
            videoHeight = videoHeight,
            rotation = CameraHelper.getCameraOrientation(context)
        )
    }

    //Stop camera preview. Ignored if streaming or already stopped. You need call it after
    fun stopPreview() {
        RtpService.stopPreview()
    }

    fun stopStream() {
        if (isServiceRunning()) {
            context.stopService(Intent(context.applicationContext, RtpService::class.java))
        }
    }

    fun startStream(
        endPoint: String,
        videoWidth: Int,
        videoHeight: Int,
        videoFps: Int,
        videoBitrate: Int,
        audioBitrate: Int,
        audioSampleRate: Int,
        audioIsStereo: Boolean,
        audioEchoCanceler: Boolean,
        audiNoiseSuppressor: Boolean
    ) {
        val intent = Intent(context.applicationContext, RtpService::class.java)
        intent.putExtra(RtpService.KEY_END_POINT, endPoint)
        intent.putExtra(RtpService.KEY_VIDEO_WIDTH, videoWidth)
        intent.putExtra(RtpService.KEY_VIDEO_HEIGHT, videoHeight)
        intent.putExtra(RtpService.KEY_VIDEO_FPS, videoFps)
        intent.putExtra(RtpService.KEY_VIDEO_BITRATE, videoBitrate)
        intent.putExtra(RtpService.KEY_AUDIO_BITRATE, audioBitrate)
        intent.putExtra(RtpService.KEY_AUDIO_SAMPLE_RATE, audioSampleRate)
        intent.putExtra(RtpService.KEY_AUDIO_IS_STEREO, audioIsStereo)
        intent.putExtra(RtpService.KEY_AUDIO_ECHO_CANCELER, audioEchoCanceler)
        intent.putExtra(RtpService.KEY_AUDIO_NOISE_SUPPRESSOR, audiNoiseSuppressor)
        context.startService(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnConnectionStartedRtp?) {
        event?.let {
            onConnectionStartedRtp?.invoke(it.rtpUrl)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnConnectionSuccessRtp?) {
        event?.let {
            onConnectionSuccessRtp?.invoke(Unit)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnNewBitrateRtp?) {
        event?.bitrate?.let {
            onNewBitrateRtp?.invoke(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnConnectionFailedRtp?) {
        event?.reason?.let {
            onConnectionFailedRtp?.invoke(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnDisconnectRtp?) {
        event?.let {
            onDisconnectRtp?.invoke(Unit)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnAuthErrorRtp?) {
        event?.let {
            onAuthErrorRtp?.invoke(Unit)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OnAuthSuccessRtp?) {
        event?.let {
            onAuthSuccessRtp?.invoke(Unit)
        }
    }

    //    Set a filter to stream. You can select any filter from com.pedro.encoder.input.gl.render.filters or create your own filter if you extends from BaseFilterRender
//    Params:
//    baseFilterRender – filter to set. You can modify parameters to filter after set it to stream.

    //didn't work when app is running in background
//    fun setFilter(baseFilterRender: BaseFilterRender) {
//        RtpService.setFilter(baseFilterRender)
//    }

//    fun getStreamWidth(): Int {
//        return RtpService.getStreamWidth()
//    }

//    fun getStreamHeight(): Int {
//        return RtpService.getStreamHeight()
//    }

//    fun enableAA(AAEnabled: Boolean) {
//        RtpService.enableAA(AAEnabled)
//    }
//
//    fun isAAEnabled(): Boolean {
//        return RtpService.isAAEnabled()
//    }

    fun setCameraCallbacks(cameraCallbacks: CameraCallbacks) {
        RtpService.setCameraCallbacks(cameraCallbacks)
    }

//    fun setFpsListener(callback: FpsListener.Callback) {
//        RtpService.setFpsListener(callback)
//    }

    /**
     * @return true if success, false if fail (not supported or called before start camera)
     */
    fun enableFaceDetection(callback: Camera2ApiManager.FaceDetectorCallback) {
        RtpService.enableFaceDetection(callback)
    }

    fun disableFaceDetection() {
        RtpService.disableFaceDetection()
    }

    fun isFaceDetectionEnabled(): Boolean? {
        return RtpService.isFaceDetectionEnabled()
    }

    /**
     * @return true if success, false if fail (not supported or called before start camera)
     */
    fun enableVideoStabilization(): Boolean? {
        return RtpService.enableVideoStabilization()
    }

    fun disableVideoStabilization() {
        RtpService.disableVideoStabilization()
    }

    fun isVideoStabilizationEnabled(): Boolean? {
        return RtpService.isVideoStabilizationEnabled()
    }

    fun isFrontCamera(): Boolean {
        return getCameraFacing() == CameraHelper.Facing.FRONT
    }

    fun getCameraFacing(): CameraHelper.Facing? {
        return RtpService.getCameraFacing()
    }

    @Throws(Exception::class)
    fun enableLantern(): Boolean {
        try {
            if (isLanternSupported() == true) {
                RtpService.enableLantern()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun disableLantern() {
        RtpService.disableLantern()
    }

    fun isLanternEnabled(): Boolean? {
        return RtpService.isLanternEnabled()
    }

    fun isLanternSupported(): Boolean? {
        return RtpService.isLanternSupported()
    }

    fun enableAutoFocus() {
        RtpService.enableAutoFocus()
    }

    fun disableAutoFocus() {
        RtpService.disableAutoFocus()
    }

    fun isAutoFocusEnabled(): Boolean? {
        return RtpService.isAutoFocusEnabled()
    }

    fun setFocusDistance(distance: Float) {
        RtpService.setFocusDistance(distance)
    }

    fun setAuthorization(user: String, password: String) {
        RtpService.setAuthorization(user, password)
    }

    /**
     * Get supported preview resolutions of back camera in px.
     *
     * @return list of preview resolutions supported by back camera
     */
    fun getResolutionsBack(): List<CameraSize> {
        return RtpService.getResolutionsBack()
    }

    /**
     * Get supported preview resolutions of front camera in px.
     *
     * @return list of preview resolutions supported by front camera
     */
    fun getResolutionsFront(): List<CameraSize> {
        return RtpService.getResolutionsFront()
    }

    /**
     * Mute microphone, can be called before, while and after stream.
     */
    fun disableAudio() {
        RtpService.disableAudio()
    }

    /**
     * Enable a muted microphone, can be called before, while and after stream.
     */
    fun enableAudio() {
        RtpService.enableAudio()
    }

    /**
     * Get mute state of microphone.
     *
     * @return true if muted, false if enabled
     */
    fun isAudioMuted(): Boolean? {
        return RtpService.isAudioMuted()
    }

    fun getBitrate(): Int? {
        return RtpService.getBitrate()
    }

    fun getResolutionValue(): Int? {
        return RtpService.getResolutionValue()
    }

    /**
     * Switch camera used. Can be called anytime
     *
     * @throws CameraOpenException If the other camera doesn't support same resolution.
     */
    @Throws(CameraOpenException::class)
    fun switchCamera() {
        RtpService.switchCamera()
    }

    fun setExposure(value: Int) {
        RtpService.setExposure(value)
    }

    fun getExposure(): Int? {
        return RtpService.getExposure()
    }

    fun getMaxExposure(): Int? {
        return RtpService.getMaxExposure()
    }

    fun getMinExposure(): Int? {
        return RtpService.getMinExposure()
    }

    /**
     * Set video bitrate of H264 in bits per second while stream.
     *
     * @param bitrate H264 in bits per second.
     */
    fun setVideoBitrateOnFly(bitrate: Int) {
        RtpService.setVideoBitrateOnFly(bitrate)
    }

    /**
     * Set limit FPS while stream. This will be override when you call to prepareVideo method. This
     * could produce a change in iFrameInterval.
     *
     * @param fps frames per second
     */
    fun setLimitFPSOnFly(fps: Int) {
        RtpService.setLimitFPSOnFly(fps)
    }

    /**
     * Get stream state.
     *
     * @return true if streaming, false if not streaming.
     */
    fun isStreaming(): Boolean? {
        return RtpService.isStreaming()
    }

    /**
     * Get preview state.
     *
     * @return true if enabled, false if disabled.
     */
    fun isOnPreview(): Boolean? {
        return RtpService.isOnPreview()
    }

    fun toggleScreenOrientation() {
        if (context is Activity) {
            UZUtil.toggleScreenOrientation(context as Activity)
        }
    }

    fun retry(delay: Long, reason: String, backupUrl: String? = null): Boolean? {
        return RtpService.retry(delay, reason, backupUrl)
    }
}
