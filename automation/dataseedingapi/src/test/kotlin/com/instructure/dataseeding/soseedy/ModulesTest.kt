package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.ModulesApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.ModuleApiModel
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ModulesTest {
    private val course = CoursesApi.createCourse()
    private val teacher = UserApi.createCanvasUser()

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsTeacher(course.id, teacher.id)
    }

    @Test
    fun createModule() {
        val module = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = ""
        )
        assertThat(module, instanceOf(ModuleApiModel::class.java))
        assertTrue(module.id >= 1)
        assertTrue(module.name.isNotEmpty())
        assertTrue(module.unlockAt.isNullOrEmpty())
    }

    @Test
    fun createModule_withUnlockAt() {
        val date = "2020-01-01"
        val module = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = date
        )
        assertThat(module, instanceOf(ModuleApiModel::class.java))
        assertTrue(module.id >= 1)
        assertTrue(module.name.isNotEmpty())
        assertTrue(module.unlockAt?.startsWith(date) ?: false) // fail if unlockAt is null
    }

    @Test
    fun publishModule() {
        var module = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = ""
        )
        assertFalse(module.published)

        module = ModulesApi.updateModule(
                courseId = course.id,
                moduleId = module.id,
                published = true,
                teacherToken = teacher.token
        )
        assertTrue(module.published)
    }
}
