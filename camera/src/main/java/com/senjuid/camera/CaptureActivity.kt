package com.senjuid.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import kotlinx.android.synthetic.main.activity_capture.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Created by Hendi, 19 Sep 2019
 * */
class CaptureActivity : AppCompatActivity(), RunTimePermission.RunTimePermissionListener {

    private var runTimePermission: RunTimePermission? = null
    private var folder: File? = null
    private var imageFileTemp: File? = null
    private var photo: String = "img_default"

    // MARK: Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Add camera listener
        camera_view.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                savePictureResult(result)
            }
        })
        camera_view.cameraOptions

        // Add take picture button listener
        btn_take_picture.setOnClickListener {
            showProgressDialog(true)
            camera_view.takePicture()
        }

        // Add back button listener
        btn_back.setOnClickListener {
            if (iv_preview.visibility == View.VISIBLE) {
                viewMode(true)
            } else {
                finish()
            }
        }

        // Add select picture button listener
        btn_select_picture.setOnClickListener {
            //            Toast.makeText(this, imageFileTemp?.absolutePath, Toast.LENGTH_LONG).show()
            val returnIntent = Intent()
            returnIntent.putExtra("photo", imageFileTemp?.absolutePath)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        btn_flash_on.setOnClickListener {
            camera_view.flash = Flash.OFF
            btn_flash_on.visibility = View.GONE;
            btn_flash_off.visibility = View.VISIBLE
        }

        btn_flash_off.setOnClickListener {
            camera_view.flash = Flash.ON
            btn_flash_on.visibility = View.VISIBLE
            btn_flash_off.visibility = View.GONE
        }


        // set view mode
        viewMode(true)

        // prepare (grant permission and make directory)
        prepare()

        // Get params
        val bundle: Bundle? = intent.extras
        photo = bundle?.getString("name")!!

        // check front disable front camera
        if (bundle.getBoolean("disable_back", false)) {
            btn_switch_camera.visibility = View.GONE
        } else {
            btn_switch_camera.visibility = View.VISIBLE
            btn_switch_camera.setOnClickListener {
                camera_view.toggleFacing()
                if (camera_view.facing.toString() == "FRONT") {
                    Log.d("facing camera", camera_view.facing.toString());
                    camera_view.flash = Flash.OFF
                    btn_flash_on.visibility = View.GONE
                    btn_flash_off.visibility = View.GONE
                } else {
                    camera_view.flash = Flash.OFF
                    btn_flash_on.visibility = View.GONE
                    btn_flash_off.visibility = View.VISIBLE
                }
            }
        }
    }

    fun savePictureResult(result: PictureResult) {
        var maxSize = intent.extras?.getInt("max_size")
        var compress = intent.extras?.getInt("quality")
        if (maxSize == null || maxSize == 0) {
            maxSize = 1024
        }
        if (compress == null || compress == 0) {
            compress = 100
        }

        result.toBitmap(maxSize!!, maxSize!!) {
            it?.let {
                // Save picture to sdcard
                val prefix = intent.getStringExtra("name")
                val fileName = createFileName(prefix)

                imageFileTemp = File(folder, fileName)
                val fileOutputStream = FileOutputStream(imageFileTemp)
                it?.compress(Bitmap.CompressFormat.JPEG, compress!!, fileOutputStream)

                iv_preview.setImageBitmap(it)
                viewMode(false)
            }

            showProgressDialog(false)
        }
    }

    public
    override fun onResume() {
        super.onResume()
        camera_view.open()
        camera_view.facing = Facing.BACK
    }

    override fun onPause() {
        super.onPause()
        camera_view.close()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        runTimePermission?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //
    // MARK: Own methods
    //
    private fun showProgressDialog(show: Boolean) {
        layout_progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun viewMode(isCapture: Boolean) {
        if (isCapture) {
            btn_select_picture.visibility = View.GONE
            iv_preview.visibility = View.GONE
        } else {
            btn_select_picture.visibility = View.VISIBLE
            iv_preview.visibility = View.VISIBLE

        }
    }

    private fun prepare() {
        runTimePermission = RunTimePermission(this)
        runTimePermission?.requestPermission(
                arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                this)
    }

    private fun createFileName(prefixName: String?): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = if (calendar.get(Calendar.MONTH) + 1 < 10) {
            "0" + (calendar.get(Calendar.MONTH) + 1)
        } else {
            "" + (calendar.get(Calendar.MONTH) + 1)
        }
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return if (prefixName?.isEmpty() == true) {
            "$photo${"_"}$year$month$day${"_"}${System.currentTimeMillis()}.png"
        } else {
            "$prefixName${"_"}$year$month$day${"_"}${System.currentTimeMillis()}.png"
        }
    }

    //
    // MARK: RunTimePermission.RunTimePermissionListener
    //
    override fun permissionGranted() {
        // Create directory
        val dirPath = "${Environment.getExternalStorageDirectory().path}/GreatDayHR"
        folder = File(dirPath)
        if (folder?.exists() == false) {
            folder?.mkdirs()

            // Create .nomedia file
            val noMediaPath = "${folder?.path}/.nomedia"
            val noMediaFile = File(noMediaPath)
            try {
                noMediaFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun permissionDenied() {
        finish()
    }
}
