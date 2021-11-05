package com.uiza.rtpstreamer.displayBasic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.uiza.UZApplication
import com.uiza.display.UZDisplayView
import com.uiza.rtpstreamer.R
import com.uiza.util.UZConstant
import com.uiza.util.UZUtil
import kotlinx.android.synthetic.main.activity_display_basic.*

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DisplayBasicActivity : AppCompatActivity() {
    private val logTag = DisplayBasicActivity::class.java.simpleName

    private var videoWidth = UZConstant.VIDEO_WIDTH_DEFAULT
    private var videoHeight = UZConstant.VIDEO_HEIGHT_DEFAULT
    private var videoFps = UZConstant.VIDEO_FPS_DEFAULT
    private var videoBitrate = UZConstant.VIDEO_BITRATE_DEFAULT
    private var videoRotation = UZConstant.VIDEO_ROTATION_DEFAULT
    private var videoDpi = UZConstant.VIDEO_DPI_DEFAULT

    private var audioBitrate = UZConstant.AUDIO_BITRATE_DEFAULT
    private var audioSampleRate = UZConstant.AUDIO_SAMPLE_RATE_DEFAULT
    private var audioIsStereo = UZConstant.AUDIO_IS_STEREO_DEFAULT
    private var audioEchoCanceler = UZConstant.AUDIO_ECHO_CANCELER_DEFAULT
    private var audioNoiseSuppressor = UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_display_basic)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        handleUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        setupTvSetting()
        Glide.with(this).load(UZUtil.URL_GIF_2).into(iv)
        etRtpUrl.setText(UZApplication.URL_STREAM)

        uzDisplayBroadCast.onConnectionStartedRtp = { rtpUrl ->
            tvStatus.text = "onConnectionStartedRtp $rtpUrl"
        }
        uzDisplayBroadCast.onConnectionSuccessRtp = {
            tvStatus.text = "onConnectionSuccessRtp"
            handleUI()
        }
        uzDisplayBroadCast.onNewBitrateRtp = { bitrate ->
            tvStatus.text = "onNewBitrateRtp bitrate $bitrate"
        }
        uzDisplayBroadCast.onConnectionFailedRtp = { reason ->
            handleUI()
            tvStatus.text = "onConnectionFailedRtp reason $reason"

            reason?.let {
                val retrySuccess = uzDisplayBroadCast.retry(delay = 1000, reason = reason)
                if (retrySuccess != true) {
                    runOnUiThread {
                        showToast("onConnectionFailedRtmp reason $reason, cannot retry connect, pls check you connection")
                    }
                }
            }
        }
        uzDisplayBroadCast.onDisconnectRtp = {
            tvStatus.text = "onDisconnectRtp"
            handleUI()
        }
        uzDisplayBroadCast.onAuthErrorRtp = {
            tvStatus.text = "onAuthErrorRtp"
        }
        uzDisplayBroadCast.onAuthSuccessRtp = {
            tvStatus.text = "onAuthSuccessRtp"
        }
        bStartTop.setOnClickListener {
            handleBStartTop()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && (
            requestCode == UZDisplayView.REQUEST_CODE_STREAM ||
                requestCode == UZDisplayView.REQUEST_CODE_RECORD &&
                resultCode == RESULT_OK
            )
        ) {
            val endPoint = etRtpUrl.text.toString()
            uzDisplayBroadCast.onActivityResult(
                requestCode = requestCode,
                resultCode = resultCode,
                data = data,
                endPoint = endPoint,
                videoWidth = videoWidth,
                videoHeight = videoHeight,
                videoFps = videoFps,
                videoBitrate = videoBitrate,
                videoRotation = videoRotation,
                videoDpi = videoDpi,
                audioBitrate = audioBitrate,
                audioSampleRate = audioSampleRate,
                audioIsStereo = audioIsStereo,
                audioEchoCanceler = audioEchoCanceler,
                audioNoiseSuppressor = audioNoiseSuppressor,
            )
        } else {
            showToast("No permissions available")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTvSetting() {
        tvSetting.text =
            "videoWidth: $videoWidth, videoHeight: $videoHeight, videoFps: $videoFps, videoBitrate: $videoBitrate, videoRotation: $videoRotation, videoDpi: $videoDpi" +
            "\naudioBitrate: $audioBitrate, audioSampleRate: $audioSampleRate, audioIsStereo: $audioIsStereo, audioEchoCanceler: $audioEchoCanceler, audioNoiseSuppressor: $audioNoiseSuppressor"
    }

    private fun handleBStartTop() {
        if (uzDisplayBroadCast.isStreaming() == false) {
            uzDisplayBroadCast.start(this)
        } else {
            uzDisplayBroadCast.stop(
                onStopPreExecute = {
                    bStartTop.isVisible = false
                    progressBar.isVisible = true
                },
                onStopSuccess = {
                    bStartTop.isVisible = true
                    progressBar.isVisible = false
                }
            )
            if (uzDisplayBroadCast.isStreaming() == false && uzDisplayBroadCast.isRecording() == false) {
                uzDisplayBroadCast.stopNotification()
            }
        }
    }

    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun handleUI() {
        if (uzDisplayBroadCast.isStreaming() == true) {
            bStartTop.setText(R.string.stop_button)
        } else {
            bStartTop.setText(R.string.start_button)
        }
    }
}
