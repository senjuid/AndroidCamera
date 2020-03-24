package com.senjuid.androidcamera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.senjuid.camera.CaptureActivity
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        button_click.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)

            val maxSize = et_max_size.text.toString().toIntOrNull()
            val quality = et_quality.text.toString().toIntOrNull()

            intent.putExtra("name", "img_lite")
            intent.putExtra("disable_back", false)
            intent.putExtra("disable_mirror", true)
            intent.putExtra("max_size", maxSize)
            intent.putExtra("quality", quality)
            startActivityForResult(intent, 1)

//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("name","img_lite")
//            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val extras: Bundle = data!!.extras
                Toast.makeText(this, extras.getString("photo"), Toast.LENGTH_LONG).show()
            }
        }
    }


}
