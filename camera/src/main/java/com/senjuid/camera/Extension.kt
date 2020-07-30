package com.senjuid.camera

import android.content.Context
import android.media.AudioManager
import java.io.File
import java.util.*

fun Context.isAudioServiceMute(): Boolean {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
}

fun Context.getStorage(): File {
    var rootPath = cacheDir.path
    if (rootPath.isNullOrEmpty()) {
        rootPath = filesDir.path
    }
    val dirPath = "${rootPath}/GreatDayHR"
    val folder = File(dirPath)
    if (!folder?.exists()) {
        folder?.mkdirs()
    }
    return folder
}

fun createFileName(prefixName: String?): String {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = if (calendar.get(Calendar.MONTH) + 1 < 10) {
        "0" + (calendar.get(Calendar.MONTH) + 1)
    } else {
        "" + (calendar.get(Calendar.MONTH) + 1)
    }
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return if (prefixName?.isNullOrEmpty() == true) {
        "${"img_default_"}$year$month$day${"_"}${System.currentTimeMillis()}.png"
    } else {
        "$prefixName${"_"}$year$month$day${"_"}${System.currentTimeMillis()}.png"
    }
}