package com.senjuid.camera

import android.content.Context
import android.media.AudioManager
import java.io.File

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