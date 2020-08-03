package com.senjuid.camera

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.invoke
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class NativeCameraHelper(private val imageFileManager: ImageFileManager) : ContextWrapper(imageFileManager.baseContext) {

    var imageUri: Uri? = null

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 2310
    }

    fun open(cameraPluginOptions: CameraPluginOptions) {
        val file = File(imageFileManager.getDir(), imageFileManager.generateFileName(cameraPluginOptions.name))
        imageUri = FileProvider.getUriForFile(baseContext, "${baseContext.packageName}.provider", file)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        intent.resolveActivity(packageManager)?.let {
            if (baseContext is AppCompatActivity) {
                val startForResult =
                        (imageFileManager.baseContext as AppCompatActivity).prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                            onActivityResult(REQUEST_IMAGE_CAPTURE, result.resultCode, result.data)
                        }
                startForResult(intent)
            } else {
                (baseContext as Activity).startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        imageUri?.let {
            Toast.makeText(baseContext, it.path, Toast.LENGTH_LONG).show()
        }
    }
}