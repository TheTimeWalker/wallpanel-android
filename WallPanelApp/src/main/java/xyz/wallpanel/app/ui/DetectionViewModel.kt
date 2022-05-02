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

package xyz.wallpanel.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.hardware.Camera
import android.util.Log
import xyz.wallpanel.app.modules.CameraCallback
import xyz.wallpanel.app.modules.CameraReader
import xyz.wallpanel.app.persistence.Configuration
import xyz.wallpanel.app.ui.views.CameraSourcePreview
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by Michael Ritchie on 6/28/18.
 */
class DetectionViewModel @Inject
constructor(application: Application, private val configuration: Configuration,
            private val cameraReader: CameraReader) : AndroidViewModel(application) {

    private val cameras = MutableLiveData<ArrayList<String>>()

    fun getCameras(): LiveData<ArrayList<String>> {
        return cameras
    }

    private fun setCameras(cameras: ArrayList<String>) {
        this.cameras.value = cameras
    }

    init {
        Timber.d("init")
        getCameraList()
    }

    //prevents memory leaks by disposing pending observable objects
    public override fun onCleared() {
        cameraReader.stopCamera()
    }

    @Suppress("DEPRECATION")
    private fun getCameraList() {
        val cameraList: ArrayList<String> = ArrayList()
        for (i in 0 until Camera.getNumberOfCameras()) {
            var description: String
            try {
                val c = Camera.open(i)
                val p = c.parameters
                val previewSize = p.previewSize
                val width = previewSize.width
                val height = previewSize.height
                val info = Camera.CameraInfo()
                Camera.getCameraInfo(i, info)
                description = java.text.MessageFormat.format(
                        "{0}: {1} Camera {3}x{4} {2}º",
                        i,
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) "Front" else "Back",
                        info.orientation,
                        width,
                        height)
                c.release()
            } catch (e: Exception) {
                Log.e("CameraReader", "Had a problem reading camera $i")
                e.printStackTrace()
                description = java.text.MessageFormat.format("{0}: Error", i)
            }
            cameraList.add(description)
        }
        setCameras(cameraList)
    }

    fun startCameraPreview(callback: CameraCallback, preview: CameraSourcePreview?) {
        Timber.d("startCameraPreview")
        if (configuration.hasCameraDetections()) {
            cameraReader.startCameraPreview(callback, configuration, preview)
        } else if (configuration.cameraEnabled) {
            cameraReader.startCameraPreviewSolo(callback, configuration, preview)
        }
    }

    companion object {

    }
}