package com.senjuid.camera

import android.app.Activity
import android.content.Intent
import androidx.activity.invoke
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver

class CameraPlugin(private val activity: AppCompatActivity) : LifecycleObserver {

    private var listener: ((String) -> Unit)? = null

    private val startForResult =
            activity.prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    listener?.let {
                        val photoPath = result.data?.getStringExtra("photo")
                        if (photoPath != null) {
                            it(photoPath)
                        }
                    }
                }
            }

    fun setCameraPluginListener(listener: ((String) -> Unit)?) {
        this.listener = listener
    }

    fun open(options: CameraPluginOptions) {
        val intent = Intent(activity, CaptureActivity::class.java)
        intent.putExtra("name", options.name)
        intent.putExtra("disable_back", options.disableFacingBack)
        intent.putExtra("disable_mirror", options.disableMirroring)
        intent.putExtra("max_size", options.maxSize)
        intent.putExtra("quality", options.quality)
        startForResult(intent)
    }
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