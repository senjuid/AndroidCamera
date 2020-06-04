package com.senjuid.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import kotlinx.android.synthetic.main.activity_capture.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var countDownTimer: CountDownTimer? = null
    private lateinit var muteController: MuteController
    private var bitmapResult: Bitmap? = null

    // MARK: Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        muteController = MuteController(this)

        // Add camera listener
        camera_view.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                previewResult(result)
            }
        })
        camera_view.cameraOptions

        // Add take picture button listener
        btn_take_picture.setOnClickListener {
            showProgressDialog(true)
            camera_view.playSounds = !muteController.isMute()
            val snapshot = intent.extras.getBoolean("is_snapshot", true)
            if (snapshot) {
                camera_view.takePictureSnapshot() // faster
            } else {
                camera_view.takePicture()
            }
        }

        // Add back button listener
        btn_back.setOnClickListener {
            finish()
        }

        // Add back button listener
        btn_retake.setOnClickListener {
            viewMode(true)
        }

        // Add back button listener
        btn_retake.setOnClickListener {
            viewMode(true)
        }

        // Add select picture button listener
        btn_select_picture.setOnClickListener {
            saveBitmapAndFinish()
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
                if (camera_view.facing == Facing.FRONT) {
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

        // init countdown timer
        countDownTimer?.cancel()
        val timerTime: Long = 10 * 1000 * 60 //10 minutes
        countDownTimer = object : CountDownTimer(timerTime, 1000) {
            override fun onFinish() {
                this@CaptureActivity.finish()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
        countDownTimer?.start()
    }

    public
    override fun onResume() {
        super.onResume()
        camera_view.open()
    }

    override fun onPause() {
        super.onPause()
        camera_view.close()
    }

    override fun onStop() {
        countDownTimer?.cancel()
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        runTimePermission?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //
    // MARK: Own methods
    //
    private fun previewResult(data: PictureResult) {
        var maxSize = intent.getIntExtra("max_size", 0)
        if (maxSize > 0) {
            data?.toBitmap(maxSize!!, maxSize!!) {
                iv_preview.setImageBitmap(it)
                showProgressDialog(false)
                viewMode(false)
                bitmapResult = it
            }
        } else {
            data?.toBitmap {
                iv_preview.setImageBitmap(it)
                showProgressDialog(false)
                viewMode(false)
                bitmapResult = it
            }
        }
    }

    private fun saveBitmapAndFinish() {
        bitmapResult?.let {
            var bmp = it
            CoroutineScope(Dispatchers.IO).launch {
                // Mirroring option
                val snapshot = intent.getBooleanExtra("is_snapshot", true)
                var disableMirror = intent.getBooleanExtra("disable_mirror", true)
                if (camera_view.facing == Facing.FRONT && disableMirror!! && !snapshot) {
                    bmp = it.flip(-1f, 1f, it.width / 2f, it.height / 2f)
                }

                // Save picture to sdcard
                var compress = intent.getIntExtra("quality", 100)
                val prefix = intent.getStringExtra("name")
                val fileName = createFileName(prefix)
                val file = File(folder, fileName)
                val fileOutputStream = FileOutputStream(file)
                bmp.compress(Bitmap.CompressFormat.JPEG, compress!!, fileOutputStream)

                withContext(Dispatchers.Main) {
                    // Finish
                    val returnIntent = Intent()
                    returnIntent.putExtra("photo", file?.absolutePath)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            }
        }
    }

    private fun Bitmap.flip(x: Float, y: Float, cx: Float, cy: Float): Bitmap {
        val matrix = Matrix().apply { postScale(x, y, cx, cy) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun showProgressDialog(show: Boolean) {
        layout_progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun viewMode(isCapture: Boolean) {
        if (isCapture) {
            btn_select_picture.visibility = View.GONE
            iv_preview.visibility = View.GONE
            btn_retake.visibility = View.GONE
        } else {
            btn_select_picture.visibility = View.VISIBLE
            iv_preview.visibility = View.VISIBLE
            btn_retake.visibility = View.VISIBLE
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
