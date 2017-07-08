package com.asdev.edu

import android.content.Context
import android.os.Environment
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.androidnetworking.AndroidNetworking
import com.asdev.edu.services.KuuvService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class KuuvTest {

    private lateinit var context: Context

    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext()
        AndroidNetworking.initialize(context)
    }

    @Test
    fun uploadImg() {
        val testFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "test.png")
        KuuvService.upload(testFile, {
            uploaded, total ->
            println((uploaded.toDouble() / total.toDouble()) * 100.0)
        }).blockingSubscribe({ // on next
            println(it)
        }, { // on error
            throw it
        })
    }
}
