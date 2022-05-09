package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.PagesApi
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.pages.WebViewTextCheck
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PagesE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testPagesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Create an unpublished page for course: ${course.name}.")
        val unpublishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = false,
                frontPage = false,
                token = teacher.token,
                body = "<h1 id=\"header1\">Unpublished Page Text</h1>"
        )

        Log.d(PREPARATION_TAG,"Create a published page for course: ${course.name}.")
        val publishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = false,
                token = teacher.token,
                body = "<h1 id=\"header1\">Regular Page Text</h1>"
        )

        Log.d(PREPARATION_TAG,"Create a front page for course: ${course.name}.")
        val frontPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = true,
                token = teacher.token,
                body = "<h1 id=\"header1\">Front Page Text</h1>"
        )

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Pages Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        Log.d(STEP_TAG,"Assert that ${unpublishedPage.title} page is displayed and it is really unpublished.")
        pageListPage.assertPageDisplayed(unpublishedPage.title)
        pageListPage.assertPageIsUnpublished(unpublishedPage.title)

        Log.d(STEP_TAG,"Assert that ${publishedPage.title} page is displayed and it is really published.")
        pageListPage.assertPageDisplayed(publishedPage.title)
        pageListPage.assertPageIsPublished(publishedPage.title)

        Log.d(STEP_TAG,"Assert that ${frontPage.title} page is displayed and it is really a front page and published.")
        pageListPage.assertPageDisplayed(frontPage.title)
        pageListPage.assertPageIsPublished(frontPage.title)
        pageListPage.assertFrontPageDisplayed(frontPage.title)

        Log.d(STEP_TAG,"Open ${publishedPage.title} page. Assert that it is really a regular published page via web view assertions.")
        pageListPage.openPage(publishedPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Regular Page Text"))
        Log.d(STEP_TAG,"Navigate back to Pages page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Open ${frontPage.title} page. Assert that it is really a front (published) page via web view assertions.")
        pageListPage.openPage(frontPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Front Page Text"))
        Log.d(STEP_TAG,"Navigate back to Pages page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Open ${unpublishedPage.title} page. Assert that it is really an unpublished page via web view assertions.")
        pageListPage.openPage(unpublishedPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Unpublished Page Text"))

    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testEditPageTitleE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Create an unpublished page for course: ${course.name}.")
        val unpublishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = false,
                frontPage = false,
                token = teacher.token
        )

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Pages Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        Log.d(STEP_TAG,"Assert that ${unpublishedPage.title} page is displayed and it is really unpublished.")
        pageListPage.assertPageDisplayed(unpublishedPage.title)
        pageListPage.assertPageIsUnpublished(unpublishedPage.title)

        Log.d(STEP_TAG,"Open ${unpublishedPage.title} page.")
        pageListPage.openPage(unpublishedPage.title)

        val editedUnpublishedPageName = "Page still unpublished"
        Log.d(STEP_TAG,"Edit the ${unpublishedPage.title} page and set $editedUnpublishedPageName page name as new value. Click on 'Save' and navigate back.")
        editPageDetailsPage.openEdit()
        editPageDetailsPage.editPageName(editedUnpublishedPageName)
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that the page name has been changed to $editedUnpublishedPageName.")
        pageListPage.assertPageIsUnpublished(editedUnpublishedPageName)
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testPublishFrontPageE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Create a published page for course: ${course.name}.")
        val publishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = false,
                token = teacher.token
        )

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Pages Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        Log.d(STEP_TAG,"Assert that ${publishedPage.title} page is displayed and it is really published.")
        pageListPage.assertPageDisplayed(publishedPage.title)
        pageListPage.assertPageIsPublished(publishedPage.title)

        Log.d(STEP_TAG,"Open ${publishedPage.title} page and Edit it. Set it as a front page and click on 'Save'. Navigate back.")
        pageListPage.openPage(publishedPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.toggleFrontPage()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that ${publishedPage.title} is displayed as a front page.")
        pageListPage.assertFrontPageDisplayed(publishedPage.title)
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testPublishUnpublishedPageE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Create an unpublished page for course: ${course.name}.")
        val unpublishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = false,
                frontPage = false,
                token = teacher.token
        )

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Pages Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        Log.d(STEP_TAG,"Open ${unpublishedPage.title} page and Edit it. Set it as a front page and click on 'Save'. Navigate back.")
        pageListPage.openPage(unpublishedPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that ${unpublishedPage.title} is published.")
        pageListPage.assertPageIsPublished(unpublishedPage.title)
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testUnableToUnpublishFrontPageE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Create a front page for course: ${course.name}.")
        val frontPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = false,
                token = teacher.token
        )

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Pages Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        Log.d(STEP_TAG,"Assert that ${frontPage.title} page is displayed and it is really a front page and published.")
        pageListPage.assertPageDisplayed(frontPage.title)
        pageListPage.assertPageIsPublished(frontPage.title)

        Log.d(STEP_TAG,"Open ${frontPage.title} page and Edit it. Unpublish it and remove 'Front page' from it.")
        pageListPage.openPage(frontPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.toggleFrontPage()

        Log.d(STEP_TAG,"Assert that a front page cannot be unpublished.")
        editPageDetailsPage.unableToSaveUnpublishedFrontPage()

        Log.d(STEP_TAG,"Publish ${frontPage.title} page again. Click on 'Save' and navigate back-")
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that ${frontPage.title} is displayed and published.")
        pageListPage.assertFrontPageDisplayed(frontPage.title)
        pageListPage.assertPageIsPublished(frontPage.title)
    }
}