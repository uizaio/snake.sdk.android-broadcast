package com.uiza.rtpstreamer.broadcastBasic

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.pedro.encoder.input.video.CameraHelper
import com.uiza.UZApplication
import com.uiza.rtpstreamer.R
import com.uiza.util.UZConstant
import kotlinx.android.synthetic.main.activity_broadcast_basic.*

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BroadCastBasicActivity : AppCompatActivity() {
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
    private var autoStreamingAfterOnPause = true
    private var firstStartStream = true
    private var userWantToStopStream = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_broadcast_basic)
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
        uzBroadCastView.onConnectionFailedRtmp = { reason ->
            Log.e(logTag, "onConnectionFailedRtmp reason ${System.nanoTime()}")
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
        uzBroadCastView.onSurfaceChanged = { _: SurfaceHolder, _: Int, w: Int, h: Int ->
            Log.d(logTag, "onSurfaceChanged $w x $h")
            startPreview()
            if (autoStreamingAfterOnPause && !firstStartStream && !userWantToStopStream) {
                start()
            }
        }
        uzBroadCastView.onSurfaceDestroyed = { _: SurfaceHolder ->
            handleUI()
        }
        uzBroadCastView.setFpsListener { fps ->
            tvFps.post {
                tvFps.text = "$fps FPS"
            }
        }
        bStartTop.setOnClickListener {
            handleBStartTop()
        }
        bSwitchCamera.setOnClickListener {
            handleBSwitchCamera()
        }
        bOnOffFlashFlight.setOnClickListener {
            handleBOnOffFlashFlight()
        }
    }

    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
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

    private fun handleBSwitchCamera() {
        uzBroadCastView.switchCamera()
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

    private fun startPreview() {
        Log.d(logTag, ">>>startPreview isFrontCamera ${uzBroadCastView.isFrontCamera()}")

        // Option 1: in case you want to customize width, height
        uzBroadCastView.startPreview(
            cameraFacing = CameraHelper.Facing.FRONT,
            width = videoWidth,
            height = videoHeight,
            rotation = CameraHelper.getCameraOrientation(this)
        )

        // Option 2: in case you want to SDK choose the width, height automatically
//        val cameraSize = uzBroadCastView.getStableCameraSize()
//        videoWidth = cameraSize.width
//        videoHeight = cameraSize.height
//        uzBroadCastView.startPreview(
//            cameraFacing = CameraHelper.Facing.FRONT,
//            rotation = CameraHelper.getCameraOrientation(this),
//        )

        setTextSetting()
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
            } else {
                bStartTop.setText(R.string.start_button)
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
