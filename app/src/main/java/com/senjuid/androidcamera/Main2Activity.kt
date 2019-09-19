package com.senjuid.androidcamera

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main2.*
import android.content.Intent
import com.senjuid.camera.CameraActivity
import com.senjuid.camera.CaptureActivity


class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        button_click.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            intent.putExtra("name","img_lite")
            startActivity(intent)

//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("name","img_lite")
//            startActivity(intent)
        }
    }


}
