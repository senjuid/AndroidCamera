package com.senjuid.androidcamera

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.senjuid.camera.CameraActivity

class MainActivity : CameraActivity("img_lite") {

    override fun onYesButtonPressed(photo: String?) {
        Toast.makeText(this, photo, Toast.LENGTH_SHORT).show()
    }

}
