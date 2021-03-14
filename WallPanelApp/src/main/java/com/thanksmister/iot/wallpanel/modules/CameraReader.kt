/*
 * Copyright (c) 2019 ThanksMister LLC
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

package com.thanksmister.iot.wallpanel.modules

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.os.AsyncTask
import android.os.SystemClock
import android.renderscript.*
import android.view.Surface
import android.view.WindowManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.vision.*
import com.google.android.gms.vision.CameraSource.CAMERA_FACING_BACK
import com.google.android.gms.vision.CameraSource.CAMERA_FACING_FRONT
import com.google.android.gms.vision.Frame.ROTATION_180
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor
import com.thanksmister.iot.wallpanel.persistence.Configuration
import com.thanksmister.iot.wallpanel.ui.views.CameraSourcePreview
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*
import javax.inject.Inject


class CameraReader @Inject
constructor(private val context: Context) {

    private var cameraCallback: CameraCallback? = null
    private var faceDetector: FaceDetector? = null
    private var barcodeDetector: BarcodeDetector? = null
    private var motionDetector: MotionDetector? = null
    private var multiDetector: MultiDetector? = null
    private var streamDetector: StreamingDetector? = null
    private var cameraSource: CameraSource? = null
    private var slowCameraSource: SlowCameraSource? = null
    private var faceDetectorProcessor: LargestFaceFocusingProcessor? = null
    private var barCodeDetectorProcessor: MultiProcessor<Barcode>? = null
    private var motionDetectorProcessor: MultiProcessor<Motion>? = null
    private var streamDetectorProcessor: MultiProcessor<Stream>? = null
    private val byteArray = MutableLiveData<ByteArray>()
    private var bitmapComplete = true;
    private var byteArrayCreateTask: ByteArrayTask? = null
    private var cameraOrientation: Int = 0
    private var cameraPreview: CameraSourcePreview? = null

    fun getJpeg(): LiveData<ByteArray> {
        return byteArray
    }

    private fun setJpeg(value: ByteArray) {
        this.byteArray.value = value
    }

    fun stopCamera() {

        if (byteArrayCreateTask != null) {
            byteArrayCreateTask!!.cancel(true)
            byteArrayCreateTask = null
        }

        if(slowCameraSource != null) {
            slowCameraSource!!.stop()
            slowCameraSource = null
        }

        if (cameraSource != null) {
            cameraSource!!.release()
            cameraSource = null
        }

        if (faceDetector != null) {
            faceDetector!!.release()
            faceDetector = null
        }

        if (barcodeDetector != null) {
            barcodeDetector!!.release()
            barcodeDetector = null
        }

        if (motionDetector != null) {
            motionDetector!!.release()
            motionDetector = null
        }

        if (streamDetector != null) {
            streamDetector!!.release()
            streamDetector = null
        }

        if (multiDetector != null) {
            multiDetector!!.release()
            multiDetector = null
        }

        if (faceDetectorProcessor != null) {
            faceDetectorProcessor!!.release()
            faceDetectorProcessor = null
        }

        if (barCodeDetectorProcessor != null) {
            barCodeDetectorProcessor!!.release()
            barCodeDetectorProcessor = null
        }

        if (motionDetectorProcessor != null) {
            motionDetectorProcessor!!.release()
            motionDetectorProcessor = null
        }

        if (streamDetectorProcessor != null) {
            streamDetectorProcessor!!.release()
            streamDetectorProcessor = null
        }
    }

    @SuppressLint("MissingPermission")
    fun startCamera(callback: CameraCallback, configuration: Configuration) {
        Timber.d("startCamera")
        this.cameraCallback = callback
        if (configuration.cameraEnabled) {
            buildDetectors(configuration)
            if(multiDetector != null) {

                if(configuration.cameraFPS <= 5)  {
                    slowCameraSource = SlowCameraSource(context, multiDetector!!, configuration)
                }
                else
                {
                    try {
                        cameraSource = initCamera(configuration.cameraId, configuration.cameraFPS)
                        cameraSource!!.start()
                    } catch (e: Exception) {
                        Timber.e(e.message)
                        try {
                            if(configuration.cameraId == CAMERA_FACING_FRONT) {
                                cameraSource = initCamera(CAMERA_FACING_BACK, configuration.cameraFPS)
                                cameraSource!!.start()
                            } else {
                                cameraSource = initCamera(CAMERA_FACING_FRONT, configuration.cameraFPS)
                                cameraSource!!.start()
                            }
                        } catch (e: Exception) {
                            Timber.e(e.message)
                            cameraSource!!.stop()
                            cameraCallback?.onCameraError()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    fun startCameraPreview(callback: CameraCallback, configuration: Configuration, preview: CameraSourcePreview?) {
        Timber.d("startCameraPreview")
        if (configuration.cameraEnabled && preview != null) {
            this.cameraCallback = callback
            this.cameraPreview = preview
            buildDetectors(configuration)
            if(multiDetector != null) {
                cameraSource = initCamera(configuration.cameraId, configuration.cameraFPS)
                cameraPreview!!.start(cameraSource, object : CameraSourcePreview.OnCameraPreviewListener {
                    override fun onCameraError() {
                        Timber.e("Camera Preview Error")
                        cameraSource = if(configuration.cameraId == CAMERA_FACING_FRONT) {
                            initCamera(CAMERA_FACING_BACK, configuration.cameraFPS)
                        } else {
                            initCamera(CAMERA_FACING_FRONT, configuration.cameraFPS)
                        }
                        if(cameraPreview != null) {
                            try {
                                cameraPreview!!.start(cameraSource, object : CameraSourcePreview.OnCameraPreviewListener {
                                    override fun onCameraError() {
                                        Timber.e("Camera Preview Error")
                                        cameraCallback!!.onCameraError()
                                    }
                                })
                            } catch (e: Exception) {
                                Timber.e(e.message)
                                cameraPreview!!.stop()
                                cameraSource!!.stop()
                                cameraCallback!!.onCameraError()
                            }
                        }
                    }
                })
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    fun startCameraPreviewSolo(callback: CameraCallback, configuration: Configuration, preview: CameraSourcePreview?) {
        Timber.d("startCameraPreviewSolo")
        if (configuration.cameraEnabled && preview != null) {
            this.cameraCallback = callback
            this.cameraPreview = preview
            buildCameraDetector(configuration)
            if(multiDetector != null) {
                cameraSource = initCamera(configuration.cameraId, configuration.cameraFPS)
                cameraPreview!!.start(cameraSource, object : CameraSourcePreview.OnCameraPreviewListener {
                    override fun onCameraError() {
                        Timber.e("Camera Preview Error")
                        cameraSource = if(configuration.cameraId == CAMERA_FACING_FRONT) {
                            initCamera(CAMERA_FACING_BACK, configuration.cameraFPS)
                        } else {
                            initCamera(CAMERA_FACING_FRONT, configuration.cameraFPS)
                        }
                        if(cameraPreview != null) {
                            try {
                                cameraPreview!!.start(cameraSource, object : CameraSourcePreview.OnCameraPreviewListener {
                                    override fun onCameraError() {
                                        Timber.e("Camera Preview Error")
                                        cameraCallback!!.onCameraError()
                                    }
                                })
                            } catch (e: Exception) {
                                Timber.e(e.message)
                                cameraPreview!!.stop()
                                cameraSource!!.stop()
                                cameraCallback!!.onCameraError()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun buildCameraDetector(configuration: Configuration) {
        val info = Camera.CameraInfo()
        try{
            Camera.getCameraInfo(configuration.cameraId, info)
        } catch (e: RuntimeException) {
            Timber.e(e.message)
            cameraCallback!!.onCameraError()
            return
        }
        cameraOrientation = info.orientation
        val multiDetectorBuilder = MultiDetector.Builder()
        var detectorAdded = false
        if(configuration.cameraEnabled) {
            streamDetector = StreamingDetector.Builder().build()
            streamDetectorProcessor = MultiProcessor.Builder<Stream>(MultiProcessor.Factory<Stream> {
                object : Tracker<Stream>() {
                    override fun onUpdate(p0: Detector.Detections<Stream>?, stream: Stream?) {
                        super.onUpdate(p0, stream)
                    }
                }
            }).build()

            streamDetector!!.setProcessor(streamDetectorProcessor)
            multiDetectorBuilder.add(streamDetector)
            detectorAdded = true
        }

        if(detectorAdded) {
            multiDetector = multiDetectorBuilder.build()
        }
    }

    private fun buildDetectors(configuration: Configuration) {

        val info = Camera.CameraInfo()
        try{
            Camera.getCameraInfo(configuration.cameraId, info)
        } catch (e: RuntimeException) {
            Timber.e(e.message)
            cameraCallback!!.onCameraError()
            return
        }

        cameraOrientation = info.orientation
        val multiDetectorBuilder = MultiDetector.Builder()
        var detectorAdded = false

        if(configuration.cameraEnabled && configuration.httpMJPEGEnabled) {
            val renderScript = RenderScript.create(this.context)
            streamDetector = StreamingDetector.Builder().build()
            streamDetectorProcessor = MultiProcessor.Builder<Stream>(MultiProcessor.Factory<Stream> {
                object : Tracker<Stream>() {
                    override fun onUpdate(p0: Detector.Detections<Stream>?, stream: Stream?) {
                        super.onUpdate(p0, stream)
                        if (stream?.byteArray != null && bitmapComplete) {
                            byteArrayCreateTask = ByteArrayTask(context, renderScript, object : OnCompleteListener {
                                override fun onComplete(byteArray: ByteArray?) {
                                    bitmapComplete = true
                                    setJpeg(byteArray!!)
                                }
                            })
                            bitmapComplete = false
                            byteArrayCreateTask!!.execute(stream.byteArray, stream.width, stream.height, cameraOrientation, configuration.cameraRotate)
                        }
                    }
                }
            }).build()

            streamDetector!!.setProcessor(streamDetectorProcessor)
            multiDetectorBuilder.add(streamDetector)
            detectorAdded = true
        }

        if(configuration.cameraEnabled && configuration.cameraMotionEnabled) {
            motionDetector = MotionDetector.Builder(configuration.cameraMotionMinLuma, configuration.cameraMotionLeniency).build()
            motionDetectorProcessor = MultiProcessor.Builder<Motion>(MultiProcessor.Factory<Motion> {
                object : Tracker<Motion>() {
                    override fun onUpdate(p0: Detector.Detections<Motion>?, motion: Motion?) {
                        super.onUpdate(p0, motion)
                        if (cameraCallback != null && configuration.cameraMotionEnabled) {
                            if (Motion.MOTION_TOO_DARK == motion?.type) {
                                cameraCallback!!.onTooDark()
                            } else if (Motion.MOTION_DETECTED == motion?.type) {
                                Timber.d("motionDetected")
                                cameraCallback!!.onMotionDetected()
                            }
                        }
                    }
                }
            }).build()

            motionDetector!!.setProcessor(motionDetectorProcessor)
            multiDetectorBuilder.add(motionDetector)
            detectorAdded = true
        }

        if(configuration.cameraEnabled && configuration.cameraFaceEnabled) {
            try {
                faceDetector = FaceDetector.Builder(context)
                        .setProminentFaceOnly(true)
                        .setTrackingEnabled(false)
                        .setMode(FaceDetector.FAST_MODE)
                        .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .build()

                faceDetectorProcessor = LargestFaceFocusingProcessor(faceDetector, object : Tracker<Face>() {
                    override fun onUpdate(detections: Detector.Detections<Face>, face: Face) {
                        super.onUpdate(detections, face)

                        var faceSize = face.width / detections.frameMetadata.width * 100 > configuration.cameraFaceSize;
                        var faceRotation = if (configuration.cameraFaceRotation) face.eulerY > -12 && face.eulerY < 12 else true;

                        if (detections.detectedItems.size() > 0 && faceSize && faceRotation) {
                            if (cameraCallback != null && configuration.cameraFaceEnabled) {
                                Timber.d("faceDetected")
                                cameraCallback!!.onFaceDetected()
                            }
                        }
                    }
                })
                faceDetector!!.setProcessor(faceDetectorProcessor)
                multiDetectorBuilder.add(faceDetector)
                detectorAdded = true
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        if(configuration.cameraEnabled && configuration.cameraQRCodeEnabled) {
            barcodeDetector = BarcodeDetector.Builder(context)
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build()

            barCodeDetectorProcessor = MultiProcessor.Builder<Barcode>(MultiProcessor.Factory<Barcode> {
                object : Tracker<Barcode>() {
                    override fun onUpdate(p0: Detector.Detections<Barcode>?, p1: Barcode?) {
                        super.onUpdate(p0, p1)
                        if (cameraCallback != null && configuration.cameraQRCodeEnabled) {
                            Timber.d("Barcode: " + p1?.displayValue)
                            cameraCallback!!.onQRCode(p1?.displayValue)
                        }
                    }
                }
            }).build()

            barcodeDetector!!.setProcessor(barCodeDetectorProcessor);
            multiDetectorBuilder.add(barcodeDetector)
            detectorAdded = true
        }

        if(detectorAdded) {
            multiDetector = multiDetectorBuilder.build()
            if(!multiDetector!!.isOperational) {
                cameraCallback!!.onDetectorError()
                return
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun initCamera(camerId: Int, fsp: Float): CameraSource {
        Timber.d("initCamera camerId $camerId")
        Timber.d("initCamera fps $fsp")
        return CameraSource.Builder(context, multiDetector)
                .setRequestedFps(fsp)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(640, 480)
                .setFacing(camerId)
                .build()
    }

    interface OnCompleteListener {
        fun onComplete(byteArray: ByteArray?)
    }

    // For a very low FPS option, run single shot captures on a timer
    class SlowCameraSource(context: Context, private val detector: MultiDetector, private val configuration: Configuration) {

        private var cameraDevice: Camera
        private var cameraTexture: SurfaceTexture

        private var shotTimer: Timer

        private var frameId = 0
        private var startTimestamp = SystemClock.elapsedRealtime()
        private var lock = java.lang.Object()
        private var stop = false
        private var frameDelay : Long

        init {
            cameraDevice = Camera.open(configuration.cameraId)
            var parameters = cameraDevice.parameters
            parameters.setPreviewSize(640, 480)
            parameters.pictureFormat = ImageFormat.NV21
            //parameters.previewFrameRate = 5
            cameraDevice.setDisplayOrientation(configuration.cameraRotate.toInt())
            cameraDevice.parameters = parameters

            cameraTexture = SurfaceTexture(100)
            cameraDevice.setPreviewTexture(cameraTexture)

            frameDelay = (1000 / configuration.cameraFPS).toLong()

            shotTimer = Timer()

            scheduleSnapshot()
        }

        private fun scheduleSnapshot() {
            synchronized(lock) {
                if(stop)
                    return

                shotTimer.schedule(object : TimerTask() {
                    override fun run() {
                        cameraDevice.setOneShotPreviewCallback(object : Camera.PreviewCallback {
                            override fun onPreviewFrame(p0: ByteArray?, p1: Camera?) {
                                synchronized(lock) {
                                    if (stop)
                                        return

                                    cameraDevice.stopPreview()
                                }

                                val ts = SystemClock.elapsedRealtime() - startTimestamp
                                val byteBuffer = ByteBuffer.wrap(p0)
                                val frame = Frame.Builder().setImageData(byteBuffer, 640, 480, ImageFormat.NV21).setId(frameId).setTimestampMillis(ts).setRotation(ROTATION_180).build()
                                frameId++

                                detector.receiveFrame(frame)

                                // Schedule another snapshot..
                                scheduleSnapshot()
                            }
                        })

                        cameraDevice.startPreview()
                    }
                }, frameDelay)
            }
        }

        public fun stop() {
            synchronized(lock) {
                shotTimer.cancel()
                stop = true
            }
            cameraDevice.setOneShotPreviewCallback(null)
            cameraDevice.stopPreview()
            cameraDevice.release()
        }

    }

    class ByteArrayTask(context: Context, private val renderScript: RenderScript?, private val onCompleteListener: OnCompleteListener) : AsyncTask<Any, Void, ByteArray>() {

        private val contextRef: WeakReference<Context> = WeakReference(context)

        override fun doInBackground(vararg params: kotlin.Any): ByteArray? {
            if (isCancelled) {
                return null
            }
            val byteArray = params[0] as ByteArray
            val width = params[1] as Int
            val height = params[2] as Int
            val orientation = params[3] as Int
            val rotation = params[4] as Float

            val windowService = contextRef.get()!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val currentRotation = windowService.defaultDisplay.rotation
            val nv21Bitmap = nv21ToBitmap(renderScript, byteArray, width, height)
            var rotate = orientation

            when (currentRotation) {
                Surface.ROTATION_90 -> {
                    rotate -= 90
                }
                Surface.ROTATION_180 -> {
                    rotate -= 180
                }
                Surface.ROTATION_270 -> {
                    rotate -= 270
                }
            }

            rotate %= 360
            rotate += rotation.toInt()

            val matrix = Matrix()
            matrix.postRotate(rotate.toFloat())
            val bitmap =  Bitmap.createBitmap(nv21Bitmap, 0, 0, width, height, matrix, true)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArrayOut = stream.toByteArray()
            bitmap.recycle()

            return byteArrayOut
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
            if (isCancelled) {
                return
            }
            onCompleteListener.onComplete(result)
        }

        private fun nv21ToBitmap(rs: RenderScript?, yuvByteArray: ByteArray, width: Int, height: Int): Bitmap {

            val yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

            val yuvType = Type.Builder(rs, Element.U8(rs)).setX(yuvByteArray.size)
            val allocationIn = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT)

            val rgbaType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height)
            val allocationOut = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT)

            allocationIn.copyFrom(yuvByteArray)

            yuvToRgbIntrinsic.setInput(allocationIn)
            yuvToRgbIntrinsic.forEach(allocationOut)

            val bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            allocationOut.copyTo(bmpout)

            return bmpout
        }
    }

    companion object {

    }
}