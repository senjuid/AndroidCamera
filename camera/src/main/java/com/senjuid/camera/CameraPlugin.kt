package com.senjuid.camera

import android.app.Activity
import android.content.Intent
import androidx.activity.invoke
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver

class CameraPlugin(private val activity: Activity) : LifecycleObserver {

    private val requestCode = 1909

    private var listener: CameraPluginListener? = null

    fun setCameraPluginListener(listener: CameraPluginListener?) {
        this.listener = listener
    }

    fun open(options: CameraPluginOptions) {
        val intent = getIntent(options)

        if (activity is AppCompatActivity) {
            val startForResult =
                    activity.prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                        onActivityResult(requestCode, result.resultCode, result.data)
                    }
            startForResult(intent)
        } else {
            activity.startActivityForResult(intent, requestCode)
        }
    }

    fun getIntent(options: CameraPluginOptions): Intent {
        val intent = Intent(activity, CaptureActivity::class.java)
        intent.putExtra("name", options.name)
        intent.putExtra("disable_back", options.disableFacingBack)
        intent.putExtra("disable_mirror", options.disableMirroring)
        intent.putExtra("max_size", options.maxSize)
        intent.putExtra("quality", options.quality)
        return intent
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != this.requestCode) {
            return
        }

        if (resultCode == Activity.RESULT_OK) {
            listener?.let {
                val photoPath = data?.getStringExtra("photo")
                if (photoPath != null) {
                    it.onSuccess(photoPath)
                } else {
                    it.onCancel()
                }
            }
        } else {
            listener?.onCancel()
        }
    }
}

interface CameraPluginListener {
    fun onSuccess(photoPath: String)
    fun onCancel()
}

class CameraPluginOptions private constructor(
        val maxSize: Int?,
        val quality: Int?,
        val name: String?,
        val disableFacingBack: Boolean?,
        val disableMirroring: Boolean?
) {
    data class Builder(
            private var maxSize: Int? = 1024,
            private var quality: Int? = 100,
            private var name: String? = "img_lite",
            private var disableFacingBack: Boolean? = false,
            private var disableMirroring: Boolean? = true
    ) {
        fun setMaxSize(maxSize: Int) = apply { this.maxSize = maxSize }
        fun setQuality(quality: Int) = apply { this.quality = quality }
        fun setName(name: String) = apply { this.name = name }
        fun setDisableFacingBack(disable: Boolean) = apply { this.disableFacingBack = disable }
        fun setDisableMirroring(disable: Boolean) = apply { this.disableMirroring = disable }
        fun build() = CameraPluginOptions(maxSize, quality, name, disableFacingBack, disableMirroring)
    }
}