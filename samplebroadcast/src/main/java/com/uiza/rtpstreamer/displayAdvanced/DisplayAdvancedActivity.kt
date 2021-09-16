package com.uiza.rtpstreamer.displayAdvanced

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.uiza.UZApplication
import com.uiza.display.UZDisplayView
import com.uiza.rtpstreamer.R
import com.uiza.util.UZConstant
import com.uiza.util.UZUtil
import kotlinx.android.synthetic.main.activity_display_advanced.*

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DisplayAdvancedActivity : AppCompatActivity() {
    private val logTag = DisplayAdvancedActivity::class.java.simpleName

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
        setContentView(R.layout.activity_display_advanced)
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
        Glide.with(this).load(UZUtil.URL_GIF).into(iv)
        etRtpUrl.setText(UZApplication.URL_STREAM)

        uzDisplayBroadCast.onConnectionStartedRtp = { rtpUrl ->
            Log.d(logTag, "onConnectionStartedRtp $rtpUrl")
            tvStatus.text = "onConnectionStartedRtp $rtpUrl"
        }
        uzDisplayBroadCast.onConnectionSuccessRtp = {
            Log.d(logTag, "onConnectionSuccessRtp")
            tvStatus.text = "onConnectionSuccessRtp"
            handleUI()
        }
        uzDisplayBroadCast.onNewBitrateRtp = { bitrate ->
            Log.d(logTag, "onNewBitrateRtp bitrate $bitrate")
            tvStatus.text = "onNewBitrateRtp bitrate $bitrate"
        }
        uzDisplayBroadCast.onConnectionFailedRtp = { reason ->
            Log.d(logTag, "onConnectionFailedRtp reason $reason")
            handleUI()
            tvStatus.text = "onConnectionFailedRtp reason $reason"
        }
        uzDisplayBroadCast.onDisconnectRtp = {
            Log.d(logTag, "onDisconnectRtp")
            tvStatus.text = "onDisconnectRtp"
            handleUI()
        }
        uzDisplayBroadCast.onAuthErrorRtp = {
            Log.d(logTag, "onAuthErrorRtp")
            tvStatus.text = "onAuthErrorRtp"
        }
        uzDisplayBroadCast.onAuthSuccessRtp = {
            Log.d(logTag, "onAuthSuccessRtp")
            tvStatus.text = "onAuthSuccessRtp"
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && (requestCode == UZDisplayView.REQUEST_CODE_STREAM
                    || requestCode == UZDisplayView.REQUEST_CODE_RECORD
                    && resultCode == RESULT_OK)
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

    private fun handleBScreenRotation() {
        uzDisplayBroadCast.toggleScreenOrientation()
    }

    private fun handleBSetting() {
        //stop streaming
        uzDisplayBroadCast.stop()
        if (uzDisplayBroadCast.isStreaming() == false && uzDisplayBroadCast.isRecording() == false) {
            uzDisplayBroadCast.stopNotification()
        }
        //setting config
        val displaySettingDialog = DisplayAdvancedSettingDialog(
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
        displaySettingDialog.onOk =
            {
                    videoWidth: Int, videoHeight: Int, videoFps: Int, videoBitrate: Int, videoRotation: Int, videoDpi: Int,
                    audioBitrate: Int, audioSampleRate: Int, audioIsStereo: Boolean, audioEchoCanceler: Boolean, audioNoiseSuppressor: Boolean,
                ->
                this.videoWidth = videoWidth
                this.videoHeight = videoHeight
                this.videoFps = videoFps
                this.videoBitrate = videoBitrate
                this.videoRotation = videoRotation
                this.videoDpi = videoDpi
                this.audioBitrate = audioBitrate
                this.audioSampleRate = audioSampleRate
                this.audioIsStereo = audioIsStereo
                this.audioEchoCanceler = audioEchoCanceler
                this.audioNoiseSuppressor = audioNoiseSuppressor
                setupTvSetting()
            }
        displaySettingDialog.show(supportFragmentManager, displaySettingDialog.tag)
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
            uzDisplayBroadCast.stop()
        }
        if (uzDisplayBroadCast.isStreaming() == false && uzDisplayBroadCast.isRecording() == false) {
            uzDisplayBroadCast.stopNotification()
        }
    }

    private fun handleBDisableAudio() {
        uzDisplayBroadCast.disableAudio()
        showToast("isAudioMuted ${uzDisplayBroadCast.isAudioMuted()}")
    }

    private fun handleBEnableAudio() {
        uzDisplayBroadCast.enableAudio()
        showToast("isAudioMuted ${uzDisplayBroadCast.isAudioMuted()}")
    }

    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun handleUI() {
        if (uzDisplayBroadCast.isStreaming() == true) {
            bStartTop.setText(R.string.stop_button)
            bDisableAudio.visibility = View.VISIBLE
            bEnableAudio.visibility = View.VISIBLE
        } else {
            bStartTop.setText(R.string.start_button)
            bDisableAudio.visibility = View.GONE
            bEnableAudio.visibility = View.GONE
        }
    }
}