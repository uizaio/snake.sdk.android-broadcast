package com.uiza.rtpstreamer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.uiza.rtpstreamer.backgroundAdvanced.BackgroundAdvancedActivity
import com.uiza.rtpstreamer.backgroundBasic.BackgroundBasicActivity
import com.uiza.rtpstreamer.broadcastAdvanced.BroadCastAdvancedActivity
import com.uiza.rtpstreamer.broadcastBasic.BroadCastBasicActivity
import com.uiza.rtpstreamer.displayAdvanced.DisplayAdvancedActivity
import com.uiza.rtpstreamer.displayBasic.DisplayBasicActivity
import com.uiza.rtpstreamer.player.PlayerBasicActivity
import com.uiza.util.UZUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 1
        private val PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        Glide.with(this).load(R.drawable.live).into(ivLive)
        tvVersion.text = "Version: " + BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE
        bBroadcastBasic.setOnClickListener {
            handleBroadcastBasic()
        }
        bBroadcastBackgroundBasic.setOnClickListener {
            handleBroadcastBackgroundBasic()
        }
        bDisplayBasic.setOnClickListener {
            handleDisplayBasic()
        }
        bBroadcastAdvanced.setOnClickListener {
            handleBroadcastAdvanced()
        }
        bBroadcastBackgroundAdvanced.setOnClickListener {
            handleBroadcastBackgroundAdvanced()
        }
        bDisplayAdvanced.setOnClickListener {
            handleDisplayAdvanced()
        }
        btPlayer.setOnClickListener {
            val intent = Intent(this, PlayerBasicActivity::class.java)
            startActivity(intent)
        }

        //hide background broadcast mode
        bBroadcastBackgroundBasic.isVisible = false
        bBroadcastBackgroundAdvanced.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
        }
    }

    private fun showPermissionsErrorAndRequest() {
        showToast("You need permissions before")
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun handleBroadcastBasic() {
        if (hasPermissions(this@MainActivity, *PERMISSIONS)) {
            val isStreamingDisplay = UZUtil.isStreamingDisplay()
            if (isStreamingDisplay == true) {
                showToast("You are still streaming with Display mode... Please stop it before broadcast in foreground mode")
                return
            }
            val isStreamingBroadcastBackground = UZUtil.isStreamingBroadcastBackground()
            if (isStreamingBroadcastBackground == true) {
                showToast("You are still streaming with broadcast background mode... Please stop it before broadcast in foreground mode")
                return
            }
            val intent = Intent(this, BroadCastBasicActivity::class.java)
            startActivity(intent)
        } else {
            showPermissionsErrorAndRequest()
        }
    }

    private fun handleBroadcastBackgroundBasic() {
        if (hasPermissions(this@MainActivity, *PERMISSIONS)) {
            val isStreamingDisplay = UZUtil.isStreamingDisplay()
            if (isStreamingDisplay == true) {
                showToast("You are still streaming with Display mode... Please stop it before broadcast in background mode")
                return
            }
            val intent = Intent(this, BackgroundBasicActivity::class.java)
            startActivity(intent)
        } else {
            showPermissionsErrorAndRequest()
        }
    }

    private fun handleDisplayBasic() {
        if (hasPermissions(this@MainActivity, *PERMISSIONS)) {
            val isStreamingBroadcastBackground = UZUtil.isStreamingBroadcastBackground()
            if (isStreamingBroadcastBackground == true) {
                showToast("You are still streaming with Broadcast background mode... Please stop it before display your screen")
                return
            }
            val intent = Intent(this, DisplayBasicActivity::class.java)
            startActivity(intent)
        } else {
            showPermissionsErrorAndRequest()
        }
    }

    private fun handleBroadcastAdvanced() {
        if (hasPermissions(this@MainActivity, *PERMISSIONS)) {
            val isStreamingDisplay = UZUtil.isStreamingDisplay()
            if (isStreamingDisplay == true) {
                showToast("You are still streaming with Display mode... Please stop it before broadcast in foreground mode")
                return
            }
            val isStreamingBroadcastBackground = UZUtil.isStreamingBroadcastBackground()
            if (isStreamingBroadcastBackground == true) {
                showToast("You are still streaming with broadcast background mode... Please stop it before broadcast in foreground mode")
                return
            }
            val intent = Intent(this, BroadCastAdvancedActivity::class.java)
            startActivity(intent)
        } else {
            showPermissionsErrorAndRequest()
        }
    }

    private fun handleBroadcastBackgroundAdvanced() {
        if (hasPermissions(this@MainActivity, *PERMISSIONS)) {
            val isStreamingDisplay = UZUtil.isStreamingDisplay()
            if (isStreamingDisplay == true) {
                showToast("You are still streaming with Display mode... Please stop it before broadcast in background mode")
                return
            }
            val intent = Intent(this, BackgroundAdvancedActivity::class.java)
            startActivity(intent)
        } else {
            showPermissionsErrorAndRequest()
        }
    }

    private fun handleDisplayAdvanced() {
        if (hasPermissions(this@MainActivity, *PERMISSIONS)) {
            val isStreamingBroadcastBackground = UZUtil.isStreamingBroadcastBackground()
            if (isStreamingBroadcastBackground == true) {
                showToast("You are still streaming with Broadcast background mode... Please stop it before display your screen")
                return
            }
            val intent = Intent(this, DisplayAdvancedActivity::class.java)
            startActivity(intent)
        } else {
            showPermissionsErrorAndRequest()
        }
    }
}
