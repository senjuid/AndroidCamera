package com.senjuid.androidcamera

import android.widget.Toast
import com.senjuid.camera.CameraActivity

class MainActivity: CameraActivity() {

    private fun MainActivity() {
//        val extras = intent.extras
//        if (extras == null) {
//            photo = "stan"
//        } else {
//            photo = extras.getString("name")
//        }
    }

    override fun onSetNamePhoto() {
        MainActivity()
    }

    override fun onYesButtonPressed(photo: String?) {
//        Toast.makeText(this, photo, Toast.LENGTH_SHORT).show()
    }

}
