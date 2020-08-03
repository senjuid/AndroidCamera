package com.senjuid.camera

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ImageFileManagerTest {

    private lateinit var instrumentationContext: Context
    private lateinit var imageFileManager: ImageFileManager

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        imageFileManager = ImageFileManager(instrumentationContext)
    }

    @Test
    fun directory_created() {
        val isCreated = imageFileManager.createDir()
        Assert.assertTrue(isCreated)
    }

    @Test
    fun directory_exists() {
        val dir = imageFileManager.getDir()
        Assert.assertTrue(dir.exists())
    }
}