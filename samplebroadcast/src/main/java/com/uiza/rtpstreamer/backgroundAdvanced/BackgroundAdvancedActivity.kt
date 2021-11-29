package com.uiza.rtpstreamer.backgroundAdvanced

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.pedro.encoder.input.video.CameraCallbacks
import com.pedro.encoder.input.video.CameraHelper
import com.uiza.UZApplication
import com.uiza.rtpstreamer.R
import com.uiza.rtpstreamer.broadcastAdvanced.BroadCastAdvancedSettingDialog
import com.uiza.util.UZConstant
import com.uiza.util.UZDialogUtil
import kotlinx.android.synthetic.main.activity_background_advanced.*

class BackgroundAdvancedActivity : AppCompatActivity() {

    private val logTag = javaClass.simpleName
    private var videoWidth = UZConstant.VIDEO_WIDTH_DEFAULT
    private var videoHeight = UZConstant.VIDEO_HEIGHT_DEFAULT
    private var videoFps = UZConstant.VIDEO_FPS_DEFAULT
    private var videoBitrate = UZConstant.VIDEO_BITRATE_DEFAULT
    private var audioBitrate = UZConstant.AUDIO_BITRATE_DEFAULT
    private var audioSampleRate = UZConstant.AUDIO_SAMPLE_RATE_DEFAULT
    private var audioIsStereo = UZConstant.AUDIO_IS_STEREO_DEFAULT
    private var audioEchoCanceler = UZConstant.AUDIO_ECHO_CANCELER_DEFAULT
    private var audioNoiseSuppressor = UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_advanced)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        etRtpUrl.setText(UZApplication.URL_STREAM)
        uzBackgroundView.onSurfaceCreated = {
            Log.d(logTag, "surfaceCreated")
        }
        uzBackgroundView.onSurfaceChanged = { _: SurfaceHolder, _: Int, width: Int, height: Int ->
            Log.d(logTag, "onSurfaceChanged width $width, height $height")
            startPreview()
        }
        uzBackgroundView.onSurfaceDestroyed = {
            Log.d(logTag, "onSurfaceDestroyed")
        }
        uzBackgroundView.onConnectionStartedRtp = { rtpUrl ->
            tvStatus.text = "onConnectionStartedRtp rtpUrl $rtpUrl"
        }
        uzBackgroundView.onConnectionSuccessRtp = {
            tvStatus.text = "onConnectionSuccessRtp"
            handleUI()
        }
        uzBackgroundView.onNewBitrateRtp = { bitrate ->
            Log.d(logTag, "onNewBitrateRtp $bitrate")
            tvStatus.text = "onNewBitrateRtp bitrate $bitrate"
            updateDot()
        }
        uzBackgroundView.onConnectionFailedRtp = { reason ->
            tvStatus.text = "onConnectionFailedRtp reason $reason"
            handleUI()
            // reconnect if needed
            val retrySuccess = uzBackgroundView.retry(delay = 1000, reason = reason)
            if (retrySuccess != true) {
                runOnUiThread {
                    showToast("onConnectionFailedRtmp reason $reason, cannot retry connect, pls check you connection")
                }
            }
        }
        uzBackgroundView.onDisconnectRtp = {
            tvStatus.text = "onDisconnectRtp"
            handleUI()
        }
        uzBackgroundView.onAuthErrorRtp = {
            tvStatus.text = "onAuthErrorRtp"
        }
        uzBackgroundView.onAuthSuccessRtp = {
            tvStatus.text = "onAuthSuccessRtp"
        }
        uzBackgroundView.setCameraCallbacks(object : CameraCallbacks {
            override fun onCameraChanged(facing: CameraHelper.Facing?) {
                Log.d(logTag, "onCameraChanged")
            }

            override fun onCameraError(error: String?) {
                Log.e(logTag, "onCameraError $error")
            }
        })
        bSetting.setOnClickListener {
            handleBSetting()
        }
        bStartTop.setOnClickListener {
            handleBStartTop()
        }
        bEnableFaceDetection.setOnClickListener {
            handleBEnableFaceDetection()
        }
        bDisableFaceDetection.setOnClickListener {
            handleBDisableFaceDetection()
        }
        bEnableVideoStabilization.setOnClickListener {
            handleBEnableVideoStabilization()
        }
        bDisableVideoStabilization.setOnClickListener {
            handleBDisableVideoStabilization()
        }
        bGetCameraFacing.setOnClickListener {
            handleBGetCameraFacing()
        }
        bEnableLantern.setOnClickListener {
            handleBEnableLantern()
        }
        bDisableLantern.setOnClickListener {
            handleBDisableLantern()
        }
        bEnableAutoFocus.setOnClickListener {
            handleBEnableAutoFocus()
        }
        bDisableAutoFocus.setOnClickListener {
            handleBDisableAutoFocus()
        }
        bDisableAudio.setOnClickListener {
            handleBDisableAudio()
        }
        bEnableAudio.setOnClickListener {
            handleBEnableAudio()
        }
        bSwitchCamera.setOnClickListener {
            handleBSwitchCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        handleUI()
    }

    private fun handleBSetting() {

        fun openSheet() {
            val dialog = BroadCastAdvancedSettingDialog(
                resolutionCamera = if (uzBackgroundView.isFrontCamera()) {
                    uzBackgroundView.getResolutionsFront()
                } else {
                    uzBackgroundView.getResolutionsBack()
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
            dialog.onOk = {
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

                uzBackgroundView.stopStream(
                    delayStopStreamInMls = 100,
                    onStopPreExecute = {
                        bStartTop.isVisible = false
                        progressBar.isVisible = true
                    },
                    onStopSuccess = {
                        bStartTop.isVisible = true
                        progressBar.isVisible = false
                        uzBackgroundView.stopPreview()
                        startPreview()
                        setTextSetting()
                    }
                )
            }
            dialog.show(supportFragmentManager, dialog.tag)
        }

        // stop streaming if exist
        uzBackgroundView.stopStream(
            delayStopStreamInMls = 100,
            onStopPreExecute = {
                bStartTop.isVisible = false
                progressBar.isVisible = true
            },
            onStopSuccess = {
                bStartTop.isVisible = true
                progressBar.isVisible = false
                openSheet()
            }
        )
    }

    private fun handleBStartTop() {
        if (uzBackgroundView.isServiceRunning()) {
            uzBackgroundView.stopStream(
                onStopPreExecute = {
                    bStartTop.isVisible = false
                    progressBar.isVisible = true
                },
                onStopSuccess = {
                    bStartTop.isVisible = true
                    progressBar.isVisible = false
                }
            )
        } else {
            UZDialogUtil.showDialog1(
                context = this,
                title = getString(R.string.warning),
                msg = getString(R.string.live_background_msg),
                button1 = getString(R.string.ok),
                onClickButton1 = {
                    uzBackgroundView.startStream(
                        endPoint = etRtpUrl.text.toString().trim(),
                        videoWidth = videoWidth,
                        videoHeight = videoHeight,
                        videoFps = videoFps,
                        videoBitrate = videoBitrate,
                        audioBitrate = audioBitrate,
                        audioSampleRate = audioSampleRate,
                        audioIsStereo = audioIsStereo,
                        audioEchoCanceler = audioEchoCanceler,
                        audiNoiseSuppressor = audioNoiseSuppressor
                    )
                }
            )
        }
    }

    private fun handleBEnableFaceDetection() {
        uzBackgroundView.enableFaceDetection { _, _, _ ->
            // do nothing
        }
        showToast("isFaceDetectionEnabled ${uzBackgroundView.isFaceDetectionEnabled()}")
    }

    private fun handleBDisableFaceDetection() {
        uzBackgroundView.disableFaceDetection()
        showToast("isFaceDetectionEnabled ${uzBackgroundView.isFaceDetectionEnabled()}")
    }

    private fun handleBEnableVideoStabilization() {
        val result = uzBackgroundView.enableVideoStabilization()
        if (result == true) {
            showToast("Enable success")
        } else {
            showToast("Hardware issue: Video Stabilization unsupported on your device")
        }
    }

    private fun handleBDisableVideoStabilization() {
        if (uzBackgroundView.isVideoStabilizationEnabled() == true) {
            uzBackgroundView.disableVideoStabilization()
            showToast("Disable success")
        } else {
            showToast("Hardware issue: Video Stabilization unsupported on your device or Video Stabilization is not enable currently")
        }
    }

    private fun handleBGetCameraFacing() {
        val facing = uzBackgroundView.getCameraFacing()
        if (facing == CameraHelper.Facing.BACK) {
            showToast("CameraHelper.Facing.BACK ")
        } else if (facing == CameraHelper.Facing.FRONT) {
            showToast("CameraHelper.Facing.FRONT ")
        }
    }

    private fun handleBEnableLantern() {
        UZDialogUtil.showDialog1(
            context = this,
            title = getString(R.string.warning),
            msg = getString(R.string.flash_light_background_msg),
            button1 = getString(R.string.ok),
            onClickButton1 = {
                val result = uzBackgroundView.enableLantern()
                if (result) {
                    showToast("handleBEnableLantern success")
                } else {
                    showToast("Lantern unsupported on your device")
                }
            }
        )
    }

    private fun handleBDisableLantern() {
        uzBackgroundView.disableLantern()
    }

    private fun handleBEnableAutoFocus() {
        uzBackgroundView.enableAutoFocus()
        showToast("isAutoFocusEnabled ${uzBackgroundView.isAutoFocusEnabled()}")
    }

    private fun handleBDisableAutoFocus() {
        uzBackgroundView.disableAutoFocus()
        showToast("isAutoFocusEnabled ${uzBackgroundView.isAutoFocusEnabled()}")
    }

    private fun handleBDisableAudio() {
        uzBackgroundView.disableAudio()
        showToast("isAudioMuted ${uzBackgroundView.isAudioMuted()}")
    }

    private fun handleBEnableAudio() {
        uzBackgroundView.enableAudio()
        showToast("isAudioMuted ${uzBackgroundView.isAudioMuted()}")
    }

    private fun handleBSwitchCamera() {
        try {
            uzBackgroundView.switchCamera()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        handleBGetCameraFacing()
    }

    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun handleUI() {
        if (uzBackgroundView.isStreaming() == true) {
            bStartTop.setText(R.string.stop_button)
            bDisableAudio.isVisible = true
            bEnableAudio.isVisible = true
        } else {
            bStartTop.setText(R.string.start_button)
            bDisableAudio.isVisible = false
            bEnableAudio.isVisible = false
        }
    }

    private fun updateDot() {
        ivDot.isVisible = true
        ivDot.postDelayed({ ivDot?.isVisible = false }, 100)
    }

    @SuppressLint("SetTextI18n")
    private fun setTextSetting() {
        tvSetting.text =
            "videoWidth $videoWidth, videoHeight $videoHeight\nvideoFps $videoFps, videoBitrate $videoBitrate" +
            "\naudioBitrate $audioBitrate, audioSampleRate $audioSampleRate\naudioIsStereo $audioIsStereo" +
            ", audioEchoCanceler $audioEchoCanceler, audioNoiseSuppressor $audioNoiseSuppressor"
    }

    private fun startPreview() {
        // Option 1: in case you want to customize width, height
        uzBackgroundView.startPreview(
            videoWidth = videoWidth,
            videoHeight = videoHeight,
        )

        // Option 2: in case you want to SDK choose the width, height automatically
//        val cameraSize = uzBackgroundView.getStableCameraSize()
//        videoWidth = cameraSize.width
//        videoHeight = cameraSize.height
//        uzBackgroundView.startPreview()

        setTextSetting()
    }
}
