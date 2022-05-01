package com.uiza.rtpstreamer.displayAdvanced

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uiza.rtpstreamer.R
import com.uiza.util.UZConstant
import com.uiza.util.UZUtil
import kotlinx.android.synthetic.main.dialog_setting_display_advanced.*

class DisplayAdvancedSettingDialog(
    private val videoWidth: Int,
    private val videoHeight: Int,
    private val videoFps: Int,
    private val videoBitrate: Int,
    private val videoRotation: Int,
    private val videoDpi: Int,
    private val audioBitrate: Int,
    private val audioSampleRate: Int,
    private val audioIsStereo: Boolean,
    private val audioEchoCanceler: Boolean,
    private val audioNoiseSuppressor: Boolean,
    private val isAutoRetry: Boolean,
    private val retryDelayInS: Int,
    private val retryCount: Int,
) : BottomSheetDialogFragment() {
    var onOk: (
        (
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
        isAutoRetry: Boolean,
        retryDelayInS: Int,
        retryCount: Int,
    ) -> Unit
    )? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_setting_display_advanced, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        etVideoWidth.setText("$videoWidth")
        etVideoHeight.setText("$videoHeight")
        etVideoFps.setText("$videoFps")
        etVideoBitrate.setText("$videoBitrate")
        etVideoRotation.setText("$videoRotation")
        etVideoDpi.setText("$videoDpi")
        etAudioBitrate.setText("$audioBitrate")
        etAudioSampleRate.setText("$audioSampleRate")
        switchAudioIsStereo.isChecked = audioIsStereo
        switchAudioEchoCanceler.isChecked = audioEchoCanceler
        switchAudioNoiseSuppressor.isChecked = audioNoiseSuppressor

        switchAutoRetry.isChecked = isAutoRetry
        layoutRetrySetting.isVisible = isAutoRetry
        etDelayRetryInS.setText("$retryDelayInS")
        etNumberOfRetry.setText("$retryCount")
        switchAutoRetry.setOnCheckedChangeListener { _, b ->
            layoutRetrySetting.isVisible = b
        }

        btGetBestSetting.setOnClickListener {
            handleBtGetBestSetting()
        }

        btOK.setOnClickListener {
            handleBtOK()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleBtGetBestSetting() {
        val bestDisplaySize = UZUtil.getBestDisplaySize()
        etVideoWidth.setText("${bestDisplaySize.width}")
        etVideoHeight.setText("${bestDisplaySize.height}")
        etVideoFps.setText("60")
        etVideoBitrate.setText("5000")
        etVideoRotation.setText("0")
        etVideoDpi.setText("${UZUtil.getDpiOfCurrentScreen(context)}")
        etAudioBitrate.setText("${UZConstant.AUDIO_BITRATE_256}")
        etAudioSampleRate.setText("${UZConstant.AUDIO_SAMPLE_RATE_44100}")
        switchAudioIsStereo.isChecked = true
        switchAudioEchoCanceler.isChecked = true
        switchAudioNoiseSuppressor.isChecked = true
        switchAutoRetry.isChecked = true
        etDelayRetryInS.setText("${UZConstant.RETRY_IN_S}")
        etNumberOfRetry.setText("${UZConstant.RETRY_COUNT}")
    }

    /**
     * @param videoWidth resolution in px.
     * @param videoHeight resolution in px.
     * @param videoFps frames per second of the stream.
     * @param videoBitrate H264 in bps.
     * @param videoRotation could be 90, 180, 270 or 0 (Normally 0 if you are streaming in landscape or 90
     * if you are streaming in Portrait). This only affect to stream result. This work rotating with
     * encoder.
     * NOTE: Rotation with encoder is silence ignored in some devices.
     * @param videoDpi of your screen device.
     *
     * @param audioBitrate AAC in kb.
     * @param audioSampleRate of audio in hz. Can be 8000, 16000, 22500, 32000, 44100.
     * @param audioIsStereo true if you want Stereo audio (2 audio channels), false if you want Mono audio
     * (1 audio channel).
     */
    private fun handleBtOK() {
        val videoWidth = etVideoWidth.text.toString().toIntOrNull()
        val videoHeight = etVideoHeight.text.toString().toIntOrNull()
        val videoFps = etVideoFps.text.toString().toIntOrNull()
        val videoBitrate = etVideoBitrate.text.toString().toIntOrNull()
        val videoRotation = etVideoRotation.text.toString().toIntOrNull()
        val videoDpi = etVideoDpi.text.toString().toIntOrNull()
        val audioBitrate = etAudioBitrate.text.toString().toIntOrNull()
        val audioSampleRate = etAudioSampleRate.text.toString().toIntOrNull()
        val audioIsStereo = switchAudioIsStereo.isChecked
        val audioEchoCanceler = switchAudioEchoCanceler.isChecked
        val audioNoiseSuppressor = switchAudioNoiseSuppressor.isChecked
        val isAutoRetry = switchAutoRetry.isChecked
        val retryDelayInS = etDelayRetryInS.text.toString().toIntOrNull()
        val retryCount = etNumberOfRetry.text.toString().toIntOrNull()

        if (videoWidth == null || videoHeight == null || videoFps == null || videoBitrate == null || videoRotation == null || videoDpi == null ||
            audioBitrate == null || audioSampleRate == null
            || retryDelayInS == null || retryCount == null || retryDelayInS <= 0 || retryCount <= 0
        ) {
            showToast("Invalid setting")
            return
        }
        if (videoRotation != UZConstant.VIDEO_ROTATION_0 &&
            videoRotation != UZConstant.VIDEO_ROTATION_90 &&
            videoRotation != UZConstant.VIDEO_ROTATION_180 &&
            videoRotation != UZConstant.VIDEO_ROTATION_270
        ) {
            showToast("Rotation could be 90, 180, 270 or 0")
            return
        }
        if (audioBitrate != UZConstant.AUDIO_BITRATE_32 &&
            audioBitrate != UZConstant.AUDIO_BITRATE_64 &&
            audioBitrate != UZConstant.AUDIO_BITRATE_128 &&
            audioBitrate != UZConstant.AUDIO_BITRATE_256
        ) {
            showToast("audioBitrate could be 32, 64, 128 or 256")
            return
        }
        if (audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_8000 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_16000 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_22500 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_32000 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_44100 &&
            audioSampleRate != UZConstant.AUDIO_SAMPLE_RATE_48000
        ) {
            showToast("audioSampleRate could be 8000, 16000, 22500, 32000, 44100, 48000")
            return
        }
        onOk?.invoke(
            videoWidth,
            videoHeight,
            videoFps,
            videoBitrate,
            videoRotation,
            videoDpi,
            audioBitrate,
            audioSampleRate,
            audioIsStereo,
            audioEchoCanceler,
            audioNoiseSuppressor,
            isAutoRetry,
            retryDelayInS,
            retryCount,
        )
        dismiss()
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
