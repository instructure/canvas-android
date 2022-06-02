package com.instructure.student.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.ConferencesPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ConferencesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Fairly basic test that we can create and view a conference with the app.
    // I didn't attempt to actually start the conference because that goes through
    // an external web browser and would be really gross (if not impossible) to
    // test.
    //
    // Re-stubbing for now because the interface has changed from webview to native
    // and this test no longer passes.  MBL-14127 is being tracked to re-write this
    // test against the new native interface.
    @Stub
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CONFERENCES, TestCategory.E2E, true)
    fun testConferencesE2E() {

        // Seed basic student/teacher/course data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Sign the student in
        tokenLogin(student)
        dashboardPage.waitForRender()

        // Navigate to course conferences
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectConferences()

        // Some values to use/track
        val title = "Awesome Conference!"
        var description = "Awesome! Spectacular! Mind-blowing!"

        // Create a conference
        ConferencesPage.createConference(title, description)

        // Verify that your created conference is now displayed.
        ConferencesPage.assertConferenceTitlePresent(title)
        ConferencesPage.assertConferenceDescriptionPresent(description)
    }
}