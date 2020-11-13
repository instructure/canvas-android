package com.instructure.teacher.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class DashboardE2ETest: TeacherTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testDashboardE2E() {

        val data = seedData(teachers = 1, courses = 2)
        val teacher = data.teachersList[0]

        tokenLogin(teacher)

        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()
        dashboardPage.assertDisplaysCourses()
        for(course in data.coursesList) {
            dashboardPage.assertDisplaysCourse(course)
        }

        dashboardPage.assertOpensCourse(data.coursesList[0])

        dashboardPage.clickTodoTab()
        dashboardPage.clickInboxTab()
        dashboardPage.clickCoursesTab()

    }
}