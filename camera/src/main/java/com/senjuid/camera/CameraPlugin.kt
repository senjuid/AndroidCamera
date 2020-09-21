package com.senjuid.camera

import android.app.Activity
import android.content.Intent
import androidx.activity.invoke
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver

class CameraPlugin(private val activity: Activity) : LifecycleObserver {

    companion object {
        const val REQUEST = 1909
    }

    private var listener: CameraPluginListener? = null
    private val imageFileManager = ImageFileManager(activity)
    private val nativeCameraHelper = NativeCameraHelper(imageFileManager)


    fun setCameraPluginListener(listener: CameraPluginListener?) {
        this.listener = listener
    }

    fun open(options: CameraPluginOptions) {
        val intent = getIntent(options)
        
        println("Babay open 1 $activity")
        println("Babay open 2 $imageFileManager")
        println("Babay open 3 $nativeCameraHelper")

        if (activity is AppCompatActivity) {
            val startForResult =
                    activity.prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                        onActivityResult(REQUEST, result.resultCode, result.data)
                    }
            startForResult(intent)
        } else {
            activity.startActivityForResult(intent, REQUEST)
        }
    }

    fun getIntent(options: CameraPluginOptions): Intent {
        val intent = Intent(activity, CaptureActivity::class.java)
        intent.putExtra("name", options.name)
        intent.putExtra("disable_back", options.disableFacingBack)
        intent.putExtra("disable_mirror", options.disableMirroring)
        intent.putExtra("max_size", options.maxSize)
        intent.putExtra("quality", options.quality)
        intent.putExtra("is_snapshot", options.snapshot)
        return intent
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val performNativeCamera = data?.getBooleanExtra("native", false)
                if (performNativeCamera!!) {
                    openNativeCamera()
                } else {
                    listener?.let {
                        val photoPath = data?.getStringExtra("photo")
                        if (photoPath != null) {
                            it.onSuccess(photoPath)
                        } else {
                            it.onCancel()
                        }
                    }
                }
            } else {
                listener?.onCancel()
            }
        } else if (requestCode == NativeCameraHelper.REQUEST_IMAGE_CAPTURE) {
            nativeCameraHelper.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openNativeCamera() {
        val options = CameraPluginOptions.Builder()
                .setName("native")
                .build()
        nativeCameraHelper.setCameraPluginListener(listener)
        nativeCameraHelper.open(options)
    }
}

interface CameraPluginListener {
    fun onSuccess(photoPath: String)
    fun onCancel()
}
