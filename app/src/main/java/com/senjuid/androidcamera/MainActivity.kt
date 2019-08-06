package com.senjuid.androidcamera

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.senjuid.camera.CameraActivity

class MainActivity : CameraActivity() {

    override fun onYesButtonPressed(photo: String?) {
        Toast.makeText(this, photo, Toast.LENGTH_SHORT).show()
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//    }
}
