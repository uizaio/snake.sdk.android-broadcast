package com.uiza.rtpstreamer.backgroundBasic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.UZApplication
import com.uiza.rtpstreamer.R
import com.uiza.util.UZConstant
import com.uiza.util.UZDialogUtil
import kotlinx.android.synthetic.main.activity_background_basic.*

class BackgroundBasicActivity : AppCompatActivity() {
    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private val logTag = javaClass.simpleName
    private var videoWidth = UZConstant.VIDEO_WIDTH_DEFAULT
    private var videoHeight = UZConstant.VIDEO_HEIGHT_DEFAULT
    private var videoFps = UZConstant.VIDEO_FPS_DEFAULT
    private var videoBitrate = UZConstant.VIDEO_BITRATE_DEFAULT
    private var audioBitrate = UZConstant.AUDIO_BITRATE_64
    private var audioSampleRate = UZConstant.AUDIO_SAMPLE_RATE_32000
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
        etRtpUrl.setText(UZApplication.URL_STREAM)
        setTextSetting()

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
        }
        uzBackgroundView.onConnectionFailedRtp = { reason ->
            tvStatus.text = "onConnectionFailedRtp reason $reason"
            handleUI()

            //reconnect if needed
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
                    bStartTop.visibility = View.GONE
                },
                onStopSuccess = {
                    bStartTop.visibility = View.VISIBLE
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
            bSwitchCamera.visibility = View.VISIBLE
        } else {
            bStartTop.setText(R.string.start_button)
            bSwitchCamera.visibility = View.GONE
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
        uzBackgroundView.startPreview(
            videoWidth = videoWidth,
            videoHeight = videoHeight,
        )
    }
}
