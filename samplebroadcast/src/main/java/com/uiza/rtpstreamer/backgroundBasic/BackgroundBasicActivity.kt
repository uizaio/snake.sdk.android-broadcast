package com.uiza.rtpstreamer.backgroundBasic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.uiza.UZApplication
import com.uiza.rtpstreamer.R
import com.uiza.util.UZConstant
import com.uiza.util.UZDialogUtil
import kotlinx.android.synthetic.main.activity_background_basic.*

class BackgroundBasicActivity : AppCompatActivity() {
    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

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
        setContentView(R.layout.activity_background_basic)
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
        Glide.with(this).load(R.drawable.dot).into(ivDot)
        etRtpUrl.setText(UZApplication.URL_STREAM)

        uzBackgroundView.onSurfaceChanged = { _: SurfaceHolder, _: Int, _: Int, _: Int ->
            startPreview()
        }
        uzBackgroundView.onConnectionStartedRtp = { rtpUrl ->
            tvStatus.text = "onConnectionStartedRtp rtpUrl $rtpUrl"
        }
        uzBackgroundView.onConnectionSuccessRtp = {
            tvStatus.text = "onConnectionSuccessRtp"
            handleUI()
        }
        uzBackgroundView.onNewBitrateRtp = { bitrate ->
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
        bStartTop.setOnClickListener {
            handleBStartTop()
        }
        bSwitchCamera.setOnClickListener {
            handleBSwitchCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        handleUI()
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
                },
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

    private fun handleBSwitchCamera() {
        try {
            uzBackgroundView.switchCamera()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleUI() {
        if (uzBackgroundView.isStreaming() == true) {
            bStartTop.setText(R.string.stop_button)
            bSwitchCamera.isVisible = true
        } else {
            bStartTop.setText(R.string.start_button)
            bSwitchCamera.isVisible = false
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
