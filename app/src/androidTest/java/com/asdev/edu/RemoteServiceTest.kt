package com.asdev.edu

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.androidnetworking.AndroidNetworking
import com.asdev.edu.models.DGrade
import com.asdev.edu.models.DSchool
import com.asdev.edu.services.RemoteService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteServiceTest {

    private lateinit var context: Context

    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext()
        AndroidNetworking.initialize(context)
    }

    @Test
    fun userRegister() {
        val response = RemoteService.userRegister(
                authToken = "",
                profilePicRef = null,
                starredCourses = null,
                grade = DGrade.GRADE_9,
                school = DSchool("E.L. Crossley S.S.", "03cd4ad4")
        ).blockingFirst()

        Assert.assertNotNull("Must throw an error, invalid auth token", response.error)
    }
}
