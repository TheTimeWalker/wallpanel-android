/*
 * Copyright (c) 2022 WallPanel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.wallpanel.app.ui.activities

import android.Manifest
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import android.view.Gravity
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import xyz.wallpanel.app.R
import xyz.wallpanel.app.modules.CameraCallback
import xyz.wallpanel.app.persistence.Configuration
import xyz.wallpanel.app.ui.DetectionViewModel
import xyz.wallpanel.app.ui.views.CameraSourcePreview
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import xyz.wallpanel.app.databinding.ActivityLiveCameraBinding
import javax.inject.Inject

class LiveCameraActivity : DaggerAppCompatActivity() {

    private lateinit var binding: ActivityLiveCameraBinding
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: DetectionViewModel
    @Inject lateinit var configuration: Configuration

    private var updateHandler: Handler? = null
    private var removeTextCountdown: Int = 0
    private val interval = 1000/15L
    private var preview: CameraSourcePreview? = null
    private var toastShown = false
    private var toast: Toast? = null

    private val updatePicture = object : Runnable {
        override fun run() {
            if (removeTextCountdown > 0) {
                removeTextCountdown--
                if (removeTextCountdown == 0) {
                    toast!!.cancel()
                    toastShown = false
                }
            }
            updateHandler!!.postDelayed(this, interval)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLiveCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setContentView(R.layout.activity_cameratest)

        if (supportActionBar != null) {
            supportActionBar!!.show()
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title = getString(R.string.title_camera_test)
        }

        if(configuration.hardwareAccelerated && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DetectionViewModel::class.java)

        // Check for the camera permission before accessing the camera.
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            viewModel.startCameraPreview(cameraCallback, binding.imageViewPreview)
        } else {
            requestCameraPermission()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(toast != null) {
            toast!!.cancel()
        }
    }

    public override fun onStart() {
        super.onStart()
        startUpdatePicture()
    }

    public override fun onStop() {
        super.onStop()
        if(toast != null) {
            toast!!.cancel()
        }
        stopUpdatePicture()
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (toast != null) {
            toast!!.cancel()
        }
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CAMERA)
            return
        }
        ActivityCompat.requestPermissions(this@LiveCameraActivity, permissions, PERMISSIONS_REQUEST_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != PERMISSIONS_REQUEST_CAMERA) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.startCameraPreview(cameraCallback, preview!!)
            return
        }
        Toast.makeText(this, getString(R.string.toast_write_permissions_denied), Toast.LENGTH_LONG).show()
    }

    private fun startUpdatePicture() {
        updateHandler = Handler(Looper.getMainLooper())
        updateHandler!!.postDelayed(updatePicture, interval.toLong())
    }

    private fun stopUpdatePicture() {
        if (updateHandler != null) {
            updateHandler!!.removeCallbacks(updatePicture)
            updateHandler = null
        }
    }

    private val cameraCallback = object : CameraCallback {
        override fun onDetectorError() {
            if(configuration.cameraFaceEnabled || configuration.cameraQRCodeEnabled) {
                Toast.makeText(this@LiveCameraActivity,getString(R.string.error_missing_vision_lib), Toast.LENGTH_LONG).show()
            }
        }
        override fun onCameraError() {
            Toast.makeText(this@LiveCameraActivity, getString(R.string.toast_camera_source_error), Toast.LENGTH_LONG).show()
        }
        override fun onMotionDetected() {
            runOnUiThread {
                if(removeTextCountdown == 0) {
                    setStatusText(getString(R.string.toast_motion_detected))
                    removeTextCountdown = 10
                }
            }
        }
        override fun onTooDark() {
            runOnUiThread {
                if(removeTextCountdown == 0) {
                    setStatusText(getString(R.string.toast_too_dark_motion))
                    removeTextCountdown = 10
                }
            }
        }

        override fun onFaceDetected() {
            runOnUiThread {
                if(removeTextCountdown == 0) {
                    setStatusText(getString(R.string.toast_face_detected))
                    removeTextCountdown = 10
                }
            }
        }

        override fun onQRCode(data: String) {
            runOnUiThread {
                if(removeTextCountdown == 0) {
                    setStatusText(getString(R.string.toast_qrcode_read, data))
                    removeTextCountdown = 10
                }
            }
        }
    }

    private fun setStatusText(text: String) {
        Timber.d("statusTextView: $text")
        if(!toastShown) {
            toastShown = true
            toast = Toast.makeText(this@LiveCameraActivity, text, Toast.LENGTH_SHORT)
            toast!!.setGravity(Gravity.BOTTOM, 0, 40)
            toast!!.show()
        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_CAMERA = 201
    }
}