package com.senjuid.camera

import android.content.Context
import android.media.AudioManager

class MuteController(private val context: Context) {

    fun isMute(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            return false
        }
        return true
    }
}