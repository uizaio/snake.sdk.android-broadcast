package com.uiza.rtpstreamer.broadcastAdvanced

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.* // ktlint-disable no-wildcard-imports
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.pedro.encoder.input.gl.render.filters.* // ktlint-disable no-wildcard-imports
import com.pedro.encoder.input.gl.render.filters.`object`.GifObjectFilterRender
import com.pedro.encoder.input.gl.render.filters.`object`.ImageObjectFilterRender
import com.pedro.encoder.input.gl.render.filters.`object`.SurfaceFilterRender
import com.pedro.encoder.input.gl.render.filters.`object`.TextObjectFilterRender
import com.pedro.encoder.input.video.CameraCallbacks
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.utils.gl.TranslateTo
import com.uiza.UZApplication
import com.uiza.rtpstreamer.R
import com.uiza.util.UZConstant
import com.uiza.util.UZDialogUtil
import com.uiza.util.UZPathUtils
import kotlinx.android.synthetic.main.activity_broadcast_advanced.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.* // ktlint-disable no-wildcard-imports

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BroadCastAdvancedActivity : AppCompatActivity() {
    private val logTag = javaClass.simpleName
    private var currentDateAndTime = ""
    private var folder: File? = null
    private val isCameraFrontDefault = true
    private var videoWidth = UZConstant.VIDEO_WIDTH_DEFAULT
    private var videoHeight = UZConstant.VIDEO_HEIGHT_DEFAULT
    private var videoFps = UZConstant.VIDEO_FPS_DEFAULT
    private var videoBitrate = UZConstant.VIDEO_BITRATE_DEFAULT
    private var audioBitrate = UZConstant.AUDIO_BITRATE_DEFAULT
    private var audioSampleRate = UZConstant.AUDIO_SAMPLE_RATE_DEFAULT
    private var audioIsStereo = UZConstant.AUDIO_IS_STEREO_DEFAULT
    private var audioEchoCanceler = UZConstant.AUDIO_ECHO_CANCELER_DEFAULT
    private var audioNoiseSuppressor = UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT
    private var autoStreamingAfterOnPause = true
    private var firstStartStream = true
    private var userWantToStopStream = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_broadcast_advanced)
        folder = UZPathUtils.getRecordPath(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupViews()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        etRtpUrl.setText(UZApplication.URL_STREAM)
        uzBroadCastView.onAuthErrorRtmp = {
            setTextStatus("onAuthErrorRtmp")
        }
        uzBroadCastView.onAuthSuccessRtmp = {
            setTextStatus("onAuthSuccessRtmp")
        }
        uzBroadCastView.onConnectionFailedRtmp = { reason ->
            setTextStatus("onConnectionFailedRtmp reason $reason")
            handleUI()

            val retrySuccess = uzBroadCastView.retry(delay = 1000, reason = reason)
            if (retrySuccess != true) {
                runOnUiThread {
                    showToast("onConnectionFailedRtmp reason $reason, cannot retry connect, pls check you connection")
                }
            }
        }
        uzBroadCastView.onConnectionStartedRtmp = { rtmpUrl ->
            setTextStatus("onConnectionStartedRtmp rtmpUrl $rtmpUrl")
            handleUI()
        }
        uzBroadCastView.onConnectionSuccessRtmp = {
            setTextStatus("onConnectionSuccessRtmp")
        }

        uzBroadCastView.onDisconnectRtmp = {
            setTextStatus("onDisconnectRtmp")
            handleUI()
        }
        uzBroadCastView.onNewBitrateRtmp = { bitrate ->
            setTextStatus("onNewBitrateRtmp bitrate $bitrate")
            updateDot()
        }
        uzBroadCastView.onSurfaceChanged =
            { _: SurfaceHolder, _: Int, _: Int, _: Int ->
                startPreview(true)
                if (autoStreamingAfterOnPause && !firstStartStream && !userWantToStopStream) {
                    start()
                }
            }
        uzBroadCastView.onSurfaceDestroyed = { _: SurfaceHolder ->
            if (uzBroadCastView.isRecording()) {
                bRecord.setText(R.string.start_record)
                showToast("file " + currentDateAndTime + ".mp4 saved in " + folder?.absolutePath)
                currentDateAndTime = ""
            }
            handleUI()
        }
        uzBroadCastView.setCameraCallbacks(object : CameraCallbacks {
            override fun onCameraChanged(facing: CameraHelper.Facing?) {
                showToast("onCameraChanged")
            }

            override fun onCameraError(error: String?) {
                showToast("onCameraError error $error")
            }
        })
        uzBroadCastView.setFpsListener { fps ->
            tvFps.post {
                tvFps.text = "$fps FPS"
            }
        }
        bScreenRotation.setOnClickListener {
            handleBScreenRotation()
        }
        bSetting.setOnClickListener {
            handleBSetting()
        }
        bStartTop.setOnClickListener {
            handleBStartTop()
        }
        bDisableAudio.setOnClickListener {
            handleBDisableAudio()
        }
        bEnableAudio.setOnClickListener {
            handleBEnableAudio()
        }
        bRecord.setOnClickListener {
            handleBRecord()
        }
        bRecordPause.setOnClickListener {
            handleBRecordPause()
        }
        bRecordResume.setOnClickListener {
            handleBRecordResume()
        }
        bSwitchCamera.setOnClickListener {
            handleBSwitchCamera()
        }
        bSwitchCameraFront.setOnClickListener {
            handleBSwitchCameraFront()
        }
        bSwitchCameraBack.setOnClickListener {
            handleBSwitchCameraBack()
        }
        bOnOffFlashFlight.setOnClickListener {
            handleBOnOffFlashFlight()
        }
        bOnOffAutoFocus.setOnClickListener {
            handleBOnOffAutoFocus()
        }
        bOrientation90.setOnClickListener {
            handleBOrientation90()
        }
        bOrientation180.setOnClickListener {
            handleBOrientation180()
        }
        bOrientation270.setOnClickListener {
            handleBOrientation270()
        }
        bOrientation0.setOnClickListener {
            handleBOrientation0()
        }
        bTakePhoto.setOnClickListener {
            handleBTakePhoto()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.gl_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        // Stop listener for image, text and gif stream objects.
        uzBroadCastView.stopListenerSpriteGestureController()
        return when (item.itemId) {
            R.id.e_d_fxaa -> {
                uzBroadCastView.enableAA(!uzBroadCastView.isAAEnabled())
                showToast("FXAA " + if (uzBroadCastView.isAAEnabled()) "enabled" else "disabled")
                true
            }
            R.id.no_filter -> {
                uzBroadCastView.setFilter(NoFilterRender())
                true
            }
            R.id.analog_tv -> {
                uzBroadCastView.setFilter(AnalogTVFilterRender())
                true
            }
            R.id.android_view -> {
                val androidViewFilterRender = AndroidViewFilterRender()
                androidViewFilterRender.view = bSwitchCamera
                uzBroadCastView.setFilter(androidViewFilterRender)
                true
            }
            R.id.basic_deformation -> {
                uzBroadCastView.setFilter(BasicDeformationFilterRender())
                true
            }
            R.id.beauty -> {
                uzBroadCastView.setFilter(BeautyFilterRender())
                true
            }
            R.id.black -> {
                uzBroadCastView.setFilter(BlackFilterRender())
                true
            }
            R.id.blur -> {
                uzBroadCastView.setFilter(BlurFilterRender())
                true
            }
            R.id.brightness -> {
                uzBroadCastView.setFilter(BrightnessFilterRender())
                true
            }
            R.id.cartoon -> {
                uzBroadCastView.setFilter(CartoonFilterRender())
                true
            }
            R.id.chroma -> {
                val chromaFilterRender = ChromaFilterRender()
                uzBroadCastView.setFilter(chromaFilterRender)
                chromaFilterRender.setImage(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.bg_chroma
                    )
                )
                true
            }
            R.id.circle -> {
                uzBroadCastView.setFilter(CircleFilterRender())
                true
            }
            R.id.color -> {
                uzBroadCastView.setFilter(ColorFilterRender())
                true
            }
            R.id.contrast -> {
                uzBroadCastView.setFilter(ContrastFilterRender())
                true
            }
            R.id.duotone -> {
                uzBroadCastView.setFilter(DuotoneFilterRender())
                true
            }
            R.id.early_bird -> {
                uzBroadCastView.setFilter(EarlyBirdFilterRender())
                true
            }
            R.id.edge_detection -> {
                uzBroadCastView.setFilter(EdgeDetectionFilterRender())
                true
            }
            R.id.exposure -> {
                uzBroadCastView.setFilter(ExposureFilterRender())
                true
            }
            R.id.fire -> {
                uzBroadCastView.setFilter(FireFilterRender())
                true
            }
            R.id.gamma -> {
                uzBroadCastView.setFilter(GammaFilterRender())
                true
            }
            R.id.glitch -> {
                uzBroadCastView.setFilter(GlitchFilterRender())
                true
            }
            R.id.gif -> {
                setGifToStream()
                true
            }
            R.id.grey_scale -> {
                uzBroadCastView.setFilter(GreyScaleFilterRender())
                true
            }
            R.id.halftone_lines -> {
                uzBroadCastView.setFilter(HalftoneLinesFilterRender())
                true
            }
            R.id.image -> {
                setImageToStream()
                true
            }
            R.id.image_70s -> {
                uzBroadCastView.setFilter(Image70sFilterRender())
                true
            }
            R.id.lamoish -> {
                uzBroadCastView.setFilter(LamoishFilterRender())
                true
            }
            R.id.money -> {
                uzBroadCastView.setFilter(MoneyFilterRender())
                true
            }
            R.id.negative -> {
                uzBroadCastView.setFilter(NegativeFilterRender())
                true
            }
            R.id.pixelated -> {
                uzBroadCastView.setFilter(PixelatedFilterRender())
                true
            }
            R.id.polygonization -> {
                uzBroadCastView.setFilter(PolygonizationFilterRender())
                true
            }
            R.id.rainbow -> {
                uzBroadCastView.setFilter(RainbowFilterRender())
                true
            }
            R.id.rgb_saturate -> {
                val rgbSaturationFilterRender = RGBSaturationFilterRender()
                uzBroadCastView.setFilter(rgbSaturationFilterRender)
                // Reduce green and blue colors 20%. Red will predominate.
                rgbSaturationFilterRender.setRGBSaturation(1f, 0.8f, 0.8f)
                true
            }
            R.id.ripple -> {
                uzBroadCastView.setFilter(RippleFilterRender())
                true
            }
            R.id.rotation -> {
                val rotationFilterRender = RotationFilterRender()
                uzBroadCastView.setFilter(rotationFilterRender)
                rotationFilterRender.rotation = 90
                true
            }
            R.id.saturation -> {
                uzBroadCastView.setFilter(SaturationFilterRender())
                true
            }
            R.id.sepia -> {
                uzBroadCastView.setFilter(SepiaFilterRender())
                true
            }
            R.id.sharpness -> {
                uzBroadCastView.setFilter(SharpnessFilterRender())
                true
            }
            R.id.snow -> {
                uzBroadCastView.setFilter(SnowFilterRender())
                true
            }
            R.id.swirl -> {
                uzBroadCastView.setFilter(SwirlFilterRender())
                true
            }
            R.id.surface_filter -> {
                val surfaceFilterRender = SurfaceFilterRender { surfaceTexture ->
                    // You can render this filter with other api that draw in a surface. for example you can use VLC
                    val mediaPlayer: MediaPlayer =
                        MediaPlayer.create(this@BroadCastAdvancedActivity, R.raw.big_bunny_240p)
                    mediaPlayer.setSurface(Surface(surfaceTexture))
                    mediaPlayer.start()
                }
                uzBroadCastView.setFilter(surfaceFilterRender)
                // Video is 360x240 so select a percent to keep aspect ratio (50% x 33.3% screen)
                surfaceFilterRender.setScale(50f, 33.3f)
                uzBroadCastView.spriteGestureController.setBaseObjectFilterRender(
                    surfaceFilterRender
                ) // Optional
                true
            }
            R.id.temperature -> {
                uzBroadCastView.setFilter(TemperatureFilterRender())
                true
            }
            R.id.text -> {
                setTextToStream()
                true
            }
            R.id.zebra -> {
                uzBroadCastView.setFilter(ZebraFilterRender())
                true
            }
            else -> false
        }
    }

    private fun setTextToStream() {
        val textObjectFilterRender = TextObjectFilterRender()
        uzBroadCastView.setFilter(textObjectFilterRender)
        textObjectFilterRender.setText("Hello world", 22f, Color.RED)
        textObjectFilterRender.setDefaultScale(
            uzBroadCastView.getStreamWidth(),
            uzBroadCastView.getStreamHeight()
        )
        textObjectFilterRender.setPosition(TranslateTo.CENTER)
        uzBroadCastView.spriteGestureController.setBaseObjectFilterRender(textObjectFilterRender) // Optional
    }

    private fun setImageToStream() {
        val imageObjectFilterRender = ImageObjectFilterRender()
        uzBroadCastView.setFilter(imageObjectFilterRender)
        imageObjectFilterRender.setImage(
            BitmapFactory.decodeResource(
                resources,
                R.mipmap.ic_launcher
            )
        )
        imageObjectFilterRender.setDefaultScale(
            uzBroadCastView.getStreamWidth(),
            uzBroadCastView.getStreamHeight()
        )
        imageObjectFilterRender.setPosition(TranslateTo.RIGHT)
        uzBroadCastView.spriteGestureController.setBaseObjectFilterRender(imageObjectFilterRender) // Optional
        uzBroadCastView.spriteGestureController.setPreventMoveOutside(false) // Optional
    }

    private fun setGifToStream() {
        try {
            val gifObjectFilterRender = GifObjectFilterRender()
            gifObjectFilterRender.setGif(resources.openRawResource(R.raw.banana))
            uzBroadCastView.setFilter(gifObjectFilterRender)
            gifObjectFilterRender.setDefaultScale(
                uzBroadCastView.getStreamWidth(),
                uzBroadCastView.getStreamHeight()
            )
            gifObjectFilterRender.setPosition(TranslateTo.BOTTOM)
            uzBroadCastView.spriteGestureController.setBaseObjectFilterRender(gifObjectFilterRender) // Optional
        } catch (e: IOException) {
            showToast(e.message)
        }
    }

    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun handleBScreenRotation() {
        fun reset() {
            videoWidth = UZConstant.VIDEO_WIDTH_DEFAULT
            videoHeight = UZConstant.VIDEO_HEIGHT_DEFAULT
            videoFps = UZConstant.VIDEO_FPS_DEFAULT
            videoBitrate = UZConstant.VIDEO_BITRATE_DEFAULT
            audioBitrate = UZConstant.AUDIO_BITRATE_DEFAULT
            audioSampleRate = UZConstant.AUDIO_SAMPLE_RATE_DEFAULT
            audioIsStereo = UZConstant.AUDIO_IS_STEREO_DEFAULT
            audioEchoCanceler = UZConstant.AUDIO_ECHO_CANCELER_DEFAULT
            audioNoiseSuppressor = UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT
            startPreview(false)
            setTextSetting()
        }
        UZDialogUtil.showDialog2(
            context = this,
            title = getString(R.string.warning),
            msg = getString(R.string.setting_msg),
            button1 = getString(R.string.confirm),
            button2 = getString(R.string.cancel),
            onClickButton1 = {
                uzBroadCastView.stopStream(
                    delayStopStreamInMls = 100,
                    onStopPreExecute = {
                        bStartTop.isVisible = false
                        progressBar.isVisible = true
                    },
                    onStopSuccess = {
                        bStartTop.isVisible = true
                        progressBar.isVisible = false
                        stopPreview()
                        uzBroadCastView.toggleScreenOrientation()
                        reset()
                    }
                )
            },
            onClickButton2 = null,
        )
    }

    private fun handleBSetting() {
        fun openSheet() {
            val openGlSettingDialog = BroadCastAdvancedSettingDialog(
                resolutionCamera = if (uzBroadCastView.isFrontCamera()) {
                    uzBroadCastView.getResolutionsFront()
                } else {
                    uzBroadCastView.getResolutionsBack()
                },
                videoWidth = videoWidth,
                videoHeight = videoHeight,
                videoFps = videoFps,
                videoBitrate = videoBitrate,
                audioBitrate = audioBitrate,
                audioSampleRate = audioSampleRate,
                audioIsStereo = audioIsStereo,
                audioEchoCanceler = audioEchoCanceler,
                audioNoiseSuppressor = audioNoiseSuppressor,
            )
            openGlSettingDialog.onOk = {
                videoWidth: Int,
                videoHeight: Int,
                videoFps: Int,
                videoBitrate: Int,
                audioBitrate: Int,
                audioSampleRate: Int,
                audioIsStereo: Boolean,
                audioEchoCanceler: Boolean,
                audioNoiseSuppressor: Boolean,
                ->
                this.videoWidth = videoWidth
                this.videoHeight = videoHeight
                this.videoFps = videoFps
                this.videoBitrate = videoBitrate
                this.audioBitrate = audioBitrate
                this.audioSampleRate = audioSampleRate
                this.audioIsStereo = audioIsStereo
                this.audioEchoCanceler = audioEchoCanceler
                this.audioNoiseSuppressor = audioNoiseSuppressor

                stopPreview()
                startPreview(false)
                setTextSetting()
            }
            openGlSettingDialog.show(supportFragmentManager, openGlSettingDialog.tag)
        }

        // stop streaming if exist
        if (uzBroadCastView.isStreaming()) {
            uzBroadCastView.stopStream(
                delayStopStreamInMls = 100,
                onStopPreExecute = {
                    bStartTop.isVisible = false
                    progressBar.isVisible = true
                },
                onStopSuccess = {
                    bStartTop.isVisible = true
                    progressBar.isVisible = false
                }
            )
        }
        openSheet()
    }

    private fun start() {
        if (!uzBroadCastView.isStreaming()) {
            if (uzBroadCastView.isRecording() || prepareAudio() && prepareVideo()) {
                uzBroadCastView.startStream(etRtpUrl.text.toString())
                if (firstStartStream) {
                    firstStartStream = false
                }
                userWantToStopStream = false
            } else {
                showToast("Error preparing stream, This device cant do it")
            }
        }
    }

    private fun stop() {
        if (uzBroadCastView.isStreaming()) {
            uzBroadCastView.stopStream(
                onStopPreExecute = {
                    bStartTop.isVisible = false
                    progressBar.isVisible = true
                },
                onStopSuccess = {
                    bStartTop.isVisible = true
                    progressBar.isVisible = false
                }
            )
            userWantToStopStream = true
        }
    }

    private fun handleBStartTop() {
        if (uzBroadCastView.isStreaming()) {
            stop()
        } else {
            start()
        }
    }

    private fun handleBDisableAudio() {
        uzBroadCastView.disableAudio()
        showToast("isAudioMuted ${uzBroadCastView.isAudioMuted()}")
    }

    private fun handleBEnableAudio() {
        uzBroadCastView.enableAudio()
        showToast("isAudioMuted ${uzBroadCastView.isAudioMuted()}")
    }

    private fun handleBSwitchCamera() {
        uzBroadCastView.switchCamera()
    }

    private fun handleBSwitchCameraFront() {
        uzBroadCastView.setCameraFront()
    }

    private fun handleBSwitchCameraBack() {
        uzBroadCastView.setCameraBack()
    }

    private fun handleBOnOffFlashFlight() {
        if (uzBroadCastView.isLanternEnabled() == true) {
            uzBroadCastView.disableLantern()
        } else if (uzBroadCastView.isLanternEnabled() == false) {
            val result = uzBroadCastView.enableLantern()
            if (result) {
                showToast("enableLantern success")
            } else {
                showToast("Lantern unsupported on your device")
            }
        }
    }

    private fun handleBOnOffAutoFocus() {
        if (uzBroadCastView.isAutoFocusEnabled()) {
            uzBroadCastView.disableAutoFocus()
            showToast("disableAutoFocus")
        } else {
            uzBroadCastView.enableAutoFocus()
            showToast("enableAutoFocus")
        }
    }

    private fun handleBRecord() {
        if (!uzBroadCastView.isRecording()) {
            try {
                folder?.let { f ->
                    if (!f.exists()) {
                        f.mkdir()
                    }
                }
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                currentDateAndTime = sdf.format(Date())
                if (!uzBroadCastView.isStreaming()) {
                    if (prepareAudio() && prepareVideo()) {
                        uzBroadCastView.startRecord(folder?.absolutePath + "/" + currentDateAndTime + ".mp4")
                        bRecord.setText(R.string.stop_record)
                        showToast("Recording... ")
                    } else {
                        showToast("Error preparing stream, This device cant do it")
                    }
                } else {
                    uzBroadCastView.startRecord(folder?.absolutePath + "/" + currentDateAndTime + ".mp4")
                    bRecord.setText(R.string.stop_record)
                    showToast("Recording...")
                }
            } catch (e: IOException) {
                uzBroadCastView.stopRecord()
                bRecord.setText(R.string.start_record)
                showToast(e.message)
            }
        } else {
            uzBroadCastView.stopRecord()
            bRecord.setText(R.string.start_record)
            showToast("file " + currentDateAndTime + ".mp4 saved in " + folder?.absolutePath)
            currentDateAndTime = ""
        }
    }

    private fun handleBRecordPause() {
        uzBroadCastView.pauseRecord()
        showToast(uzBroadCastView.getRecordStatus()?.toString())
    }

    private fun handleBRecordResume() {
        uzBroadCastView.resumeRecord()
        showToast(uzBroadCastView.getRecordStatus()?.toString())
    }

    private fun handleBOrientation90() {
        uzBroadCastView.setPreviewOrientation(UZConstant.PREVIEW_ROTATION_90)
    }

    private fun handleBOrientation180() {
        uzBroadCastView.setPreviewOrientation(UZConstant.PREVIEW_ROTATION_180)
    }

    private fun handleBOrientation270() {
        uzBroadCastView.setPreviewOrientation(UZConstant.PREVIEW_ROTATION_270)
    }

    private fun handleBOrientation0() {
        uzBroadCastView.setPreviewOrientation(UZConstant.PREVIEW_ROTATION_0)
    }

    private fun handleBTakePhoto() {
        uzBroadCastView.takePhoto {
            if (it != null) {
                runOnUiThread {
                    showToast("takePhoto success -> bitmap != null")
                }
            }
        }
    }

    private fun setTextStatus(msg: String) {
        tvStatus.post {
            tvStatus.text = msg
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTextSetting() {
        tvSetting.text =
            "videoWidth $videoWidth, videoHeight $videoHeight\nvideoFps $videoFps, videoBitrate $videoBitrate" +
            "\naudioBitrate $audioBitrate, audioSampleRate $audioSampleRate\naudioIsStereo $audioIsStereo" +
            ", audioEchoCanceler $audioEchoCanceler, audioNoiseSuppressor $audioNoiseSuppressor"
    }

    private fun startPreview(isInitFirst: Boolean) {
        Log.d(logTag, ">>>startPreview isFrontCamera ${uzBroadCastView.isFrontCamera()}")

        // Option 1: in case you want to customize width, height
        if (isInitFirst) {
            uzBroadCastView.startPreview(
                cameraFacing = if (isCameraFrontDefault) CameraHelper.Facing.FRONT else CameraHelper.Facing.BACK,
                width = videoWidth,
                height = videoHeight,
                rotation = CameraHelper.getCameraOrientation(this)
            )
        } else {
            if (uzBroadCastView.isFrontCamera()) {
                uzBroadCastView.startPreview(
                    cameraFacing = CameraHelper.Facing.FRONT,
                    width = videoWidth,
                    height = videoHeight,
                    rotation = CameraHelper.getCameraOrientation(this)
                )
            } else {
                uzBroadCastView.startPreview(
                    cameraFacing = CameraHelper.Facing.BACK,
                    width = videoWidth,
                    height = videoHeight,
                    rotation = CameraHelper.getCameraOrientation(this)
                )
            }
        }

        // Option 2: in case you want to SDK choose the width, height automatically
//        val cameraSize = uzBroadCastView.getStableCameraSize()
//        videoWidth = cameraSize.width
//        videoHeight = cameraSize.height
//        if (isInitFirst) {
//            uzBroadCastView.startPreview(
//                cameraFacing = if (isCameraFrontDefault) CameraHelper.Facing.FRONT else CameraHelper.Facing.BACK,
//                rotation = CameraHelper.getCameraOrientation(this)
//            )
//        } else {
//            if (uzBroadCastView.isFrontCamera()) {
//                uzBroadCastView.startPreview(
//                    cameraFacing = CameraHelper.Facing.FRONT,
//                    rotation = CameraHelper.getCameraOrientation(this)
//                )
//            } else {
//                uzBroadCastView.startPreview(
//                    cameraFacing = CameraHelper.Facing.BACK,
//                    rotation = CameraHelper.getCameraOrientation(this)
//                )
//            }
//        }

        setTextSetting()
    }

    private fun stopPreview() {
        uzBroadCastView.stopPreview()
    }

    private fun prepareAudio(): Boolean {
        return uzBroadCastView.prepareAudio(
            audioBitrate = audioBitrate,
            audioSampleRate = audioSampleRate,
            audioIsStereo = audioIsStereo,
            audioEchoCanceler = audioEchoCanceler,
            audioNoiseSuppressor = audioNoiseSuppressor
        )
    }

    private fun prepareVideo(): Boolean {
        return uzBroadCastView.prepareVideo(
            videoWidth = videoWidth,
            videoHeight = videoHeight,
            videoFps = videoFps,
            videoBitrate = videoBitrate,
        )
    }

    private fun handleUI() {
        runOnUiThread {
            if (uzBroadCastView.isStreaming()) {
                bStartTop.setText(R.string.stop_button)
                bDisableAudio.isVisible = true
                bEnableAudio.isVisible = true
            } else {
                bStartTop.setText(R.string.start_button)
                bDisableAudio.isVisible = false
                bEnableAudio.isVisible = false
            }
        }
    }

    private fun updateDot() {
        ivDot.post {
            ivDot.isVisible = true
            ivDot.postDelayed({ ivDot?.isVisible = false }, 100)
        }
    }
}
