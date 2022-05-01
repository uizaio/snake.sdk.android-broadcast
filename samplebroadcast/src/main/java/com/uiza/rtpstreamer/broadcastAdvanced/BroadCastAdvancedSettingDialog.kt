package com.uiza.rtpstreamer.broadcastAdvanced

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uiza.broadcast.CameraSize
import com.uiza.rtpstreamer.R
import com.uiza.util.UZConstant
import com.uiza.util.UZUtil
import kotlinx.android.synthetic.main.dialog_setting_broadcast_advanced.*

class BroadCastAdvancedSettingDialog(
    private val resolutionCamera: List<CameraSize>,
    private val videoWidth: Int,
    private val videoHeight: Int,
    private val videoFps: Int,
    private val videoBitrate: Int,
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
        return inflater.inflate(R.layout.dialog_setting_broadcast_advanced, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val listResolutionCameraString: MutableList<String> = ArrayList()
        resolutionCamera.forEach {
            listResolutionCameraString.add("${it.width} x ${it.height}")
        }
        context?.let { c ->
            val dataAdapter =
                ArrayAdapter(c, android.R.layout.simple_spinner_item, listResolutionCameraString)
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerResolutionCamera.adapter = dataAdapter
            setSelectionSpinnerResolutionCamera(w = videoWidth, h = videoHeight)
        }

        etVideoFps.setText("$videoFps")
        etVideoBitrate.setText("$videoBitrate")
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

        btGetStableResolutionCamera.setOnClickListener {
            handleBtGetStableResolutionCamera()
        }

        btGetBestSetting.setOnClickListener {
            handleBtGetBestSetting()
        }

        btOK.setOnClickListener {
            handleBtOK()
        }
    }

    private fun setSelectionSpinnerResolutionCamera(w: Int, h: Int) {
        var indexBack = 0
        resolutionCamera.forEachIndexed { index, item ->
            if (item.width == w && item.height == h) {
                indexBack = index
            }
        }
        spinnerResolutionCamera.setSelection(indexBack)
    }

    private fun handleBtGetStableResolutionCamera() {
        val cameraSize = UZUtil.getStableCameraSize(resolutionCamera)
        setSelectionSpinnerResolutionCamera(w = cameraSize.width, h = cameraSize.height)
    }

    @SuppressLint("SetTextI18n")
    private fun handleBtGetBestSetting() {
        val cameraSize = UZUtil.getBestCameraSize(resolutionCamera)
        setSelectionSpinnerResolutionCamera(w = cameraSize.width, h = cameraSize.height)
        etVideoFps.setText("60")
        etVideoBitrate.setText("5000")
        etAudioBitrate.setText("${UZConstant.AUDIO_BITRATE_256}")
        etAudioSampleRate.setText("${UZConstant.AUDIO_SAMPLE_RATE_44100}")
        switchAudioIsStereo.isChecked = true
        switchAudioEchoCanceler.isChecked = true
        switchAudioNoiseSuppressor.isChecked = true
        switchAutoRetry.isChecked = true
        etDelayRetryInS.setText("${UZConstant.RETRY_IN_S}")
        etNumberOfRetry.setText("${UZConstant.RETRY_COUNT}")
    }

    private fun handleBtOK() {
        val posBack = spinnerResolutionCamera.selectedItemPosition
        val cameraSize = resolutionCamera[posBack]
        val videoFps = etVideoFps.text.toString().toIntOrNull()
        val videoBitrate = etVideoBitrate.text.toString().toIntOrNull()
        val audioBitrate = etAudioBitrate.text.toString().toIntOrNull()
        val audioSampleRate = etAudioSampleRate.text.toString().toIntOrNull()
        val audioIsStereo = switchAudioIsStereo.isChecked
        val audioEchoCanceler = switchAudioEchoCanceler.isChecked
        val audioNoiseSuppressor = switchAudioNoiseSuppressor.isChecked
        val isAutoRetry = switchAutoRetry.isChecked
        val retryDelayInS = etDelayRetryInS.text.toString().toIntOrNull()
        val retryCount = etNumberOfRetry.text.toString().toIntOrNull()

        if (videoFps == null || videoBitrate == null || audioBitrate == null || audioSampleRate == null
            || retryDelayInS == null || retryCount == null || retryDelayInS <= 0 || retryCount <= 0
        ) {
            showToast("Invalid setting")
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
            cameraSize.width,
            cameraSize.height,
            videoFps,
            videoBitrate,
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

    private fun showToast(msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
