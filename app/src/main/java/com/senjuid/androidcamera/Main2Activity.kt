package com.senjuid.androidcamera

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.senjuid.camera.CameraPlugin
import com.senjuid.camera.CameraPluginOptions
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    private lateinit var cameraPlugin: CameraPlugin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        cameraPlugin = CameraPlugin(this)
        cameraPlugin.setCameraPluginListener {
            Toast.makeText(this@Main2Activity, it, Toast.LENGTH_LONG).show()
        }

        button_click.setOnClickListener {
            val options = CameraPluginOptions.Builder().build()
            cameraPlugin.open(options)
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 1) {
//            if (resultCode == Activity.RESULT_OK) {
//                val extras: Bundle = data!!.extras
//                Toast.makeText(this, extras.getString("photo"), Toast.LENGTH_LONG).show()
//            }
//        }
//    }
}
