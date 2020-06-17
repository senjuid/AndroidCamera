package com.senjuid.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.size.SizeSelectors
import kotlinx.android.synthetic.main.activity_capture.*


/**
 * Created by Hendi, 19 Sep 2019
 * */
class CaptureActivity : AppCompatActivity(), RunTimePermission.RunTimePermissionListener {

    private var runTimePermission: RunTimePermission? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var muteController: MuteController
    private lateinit var helper: CaptureActivityHelper

    // MARK: Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        helper = CaptureActivityHelper()
        muteController = MuteController(this)

        // Add camera listener
        camera_view.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                var maxSize = intent.getIntExtra("max_size", 0)
                helper.pictureResultHandler(result, maxSize) {
                    iv_preview.setImageBitmap(it)
                    showProgressDialog(false)
                    viewMode(false)
                }
            }
        })
        camera_view.setLifecycleOwner(this)
        camera_view.setPictureSize(SizeSelectors.smallest())

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
            helper.saveBitmapAndFinish(intent, camera_view.facing) {
                val returnIntent = Intent()
                returnIntent.putExtra("photo", it)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
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

        // check front disable front camera
        if (intent.getBooleanExtra("disable_back", false)) {
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

    override fun onStop() {
        countDownTimer?.cancel()
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        runTimePermission?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showProgressDialog(show: Boolean) {
        layout_progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun viewMode(isCapture: Boolean) {
        if (isCapture) {
            layout_preview.visibility = View.GONE
        } else {
            layout_preview.visibility = View.VISIBLE
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

    //
    // MARK: RunTimePermission.RunTimePermissionListener
    //
    override fun permissionGranted() {
        helper.createDirectory()
    }

    override fun permissionDenied() {
        finish()
    }
}
