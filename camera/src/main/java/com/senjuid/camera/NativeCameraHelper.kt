package com.senjuid.camera

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.invoke
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class NativeCameraHelper(private val activity: Activity) : ContextWrapper(activity) {

    var imageUri: Uri? = null

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 2310
    }

    fun open(cameraPluginOptions: CameraPluginOptions) {
        val file = File(activity.getStorage(), createFileName(cameraPluginOptions.name))
        imageUri = FileProvider.getUriForFile(activity, "${activity.packageName}.provider", file)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        intent.resolveActivity(packageManager)?.let {
            if (activity is AppCompatActivity) {
                val startForResult =
                        activity.prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                            onActivityResult(REQUEST_IMAGE_CAPTURE, result.resultCode, result.data)
                        }
                startForResult(intent)
            } else {
                activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        imageUri?.let {
            Toast.makeText(activity, it.path, Toast.LENGTH_LONG).show()
        }
    }
}