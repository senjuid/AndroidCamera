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

    // MARK: Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        muteController = MuteController(this)

        // Add camera listener
        camera_view.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                savePictureResult(result)
            }
        })
        camera_view.cameraOptions

        // Add take picture button listener
        btn_take_picture.setOnClickListener {
            showProgressDialog(true)
            camera_view.playSounds = !muteController.isMute()
            camera_view.takePicture()
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
    private fun savePictureResult(data: PictureResult?) {
        var maxSize = intent.extras?.getInt("max_size")
        if (maxSize == null || maxSize == 0) {

            // Picture doesn't resized
            data?.toBitmap {
                it?.let {
                    val bmpSaved = savePictureResultBitmap(it)
                    if (bmpSaved != null) {
                        iv_preview.setImageBitmap(bmpSaved)
                        viewMode(false)
                    }
                }

                // Dismiss progress
                showProgressDialog(false)
            }
        } else {

            // Picture resized
            data?.toBitmap(maxSize!!, maxSize!!) {
                it?.let {
                    val bmpSaved = savePictureResultBitmap(it)
                    if (bmpSaved != null) {
                        iv_preview.setImageBitmap(bmpSaved)
                        viewMode(false)
                    }
                }

                // Dismiss progress
                showProgressDialog(false)
            }
        }
    }

    private fun savePictureResultBitmap(it: Bitmap): Bitmap? {
        // Extras
        var disableMirror = intent.extras?.getBoolean("disable_mirror", true)
        var compress = intent.extras?.getInt("quality", 100)

        // Mirroring option
        val bmp = if (camera_view.facing == Facing.FRONT && disableMirror!!) {
            it.flip(-1f, 1f, it.width / 2f, it.height / 2f)
        } else {
            it
        }

        // Save picture to sdcard
        val prefix = intent.getStringExtra("name")
        val fileName = createFileName(prefix)
        imageFileTemp = File(folder, fileName)
        val fileOutputStream = FileOutputStream(imageFileTemp)
        bmp.compress(Bitmap.CompressFormat.JPEG, compress!!, fileOutputStream)

        return bmp
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
