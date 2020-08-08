package com.senjuid.camera

import android.content.Context
import android.media.AudioManager

fun Context.isAudioServiceMute(): Boolean {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
}