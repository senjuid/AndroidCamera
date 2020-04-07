package com.senjuid.camera

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MuteControllerTest {

    lateinit var instrumentationContext: Context
    lateinit var muteController: MuteController

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        muteController = MuteController(instrumentationContext)
    }

    @Test
    fun isMuteTrueTest() {
        val isMute = muteController.isMute()
        assertTrue(isMute)
    }

    @Test
    fun isMuteFalseTest() {
        val isMute = muteController.isMute()
        assertFalse(isMute)
    }
}