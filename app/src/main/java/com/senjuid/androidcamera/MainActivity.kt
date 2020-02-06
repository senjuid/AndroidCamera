package com.senjuid.androidcamera

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.senjuid.camera.CameraActivity
import com.senjuid.camera.CameraActivity as a

class MainActivity: CameraActivity() {

    private fun MainActivity() {
        val extras = intent.extras
        if (extras == null) {
            photo = "stan"
        } else {
            photo = extras.getString("name")
        }
    }

    override fun onSetNamePhoto() {
        MainActivity()
    }

    override fun onYesButtonPressed(photo: String?) {
        Toast.makeText(this, photo, Toast.LENGTH_SHORT).show()
    }

}
