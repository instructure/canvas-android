package com.instructure.teacher.ui.e2e

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
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testPagesE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val unpublishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = false,
                frontPage = false,
                token = teacher.token,
                body = "<h1 id=\"header1\">Unpublished Page Text</h1>"
        )

        val publishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = false,
                token = teacher.token,
                body = "<h1 id=\"header1\">Regular Page Text</h1>"
        )

        val frontPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = true,
                token = teacher.token,
                body = "<h1 id=\"header1\">Front Page Text</h1>"
        )

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        pageListPage.assertPageDisplayed(unpublishedPage.title)
        pageListPage.assertPageIsUnpublished(unpublishedPage.title)

        pageListPage.assertPageDisplayed(publishedPage.title)
        pageListPage.assertPageIsPublished(publishedPage.title)

        pageListPage.assertPageDisplayed(frontPage.title)
        pageListPage.assertPageIsPublished(frontPage.title)
        pageListPage.assertFrontPageDisplayed(frontPage.title)

        pageListPage.openPage(publishedPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Regular Page Text"))
        Espresso.pressBack()

        pageListPage.openPage(frontPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Front Page Text"))
        Espresso.pressBack()

        pageListPage.openPage(unpublishedPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Unpublished Page Text"))
        Espresso.pressBack()

    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testEditPageTitleE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val unpublishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = false,
                frontPage = false,
                token = teacher.token
        )
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        pageListPage.assertPageDisplayed(unpublishedPage.title)
        pageListPage.assertPageIsUnpublished(unpublishedPage.title)

        pageListPage.openPage(unpublishedPage.title)
        editPageDetailsPage.openEdit()
        val editedUnpublishedPageName = "Page still unpublished"
        editPageDetailsPage.editPageName(editedUnpublishedPageName)
        editPageDetailsPage.savePage()
        Espresso.pressBack()
        pageListPage.assertPageIsUnpublished(editedUnpublishedPageName)
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testPublishFrontPageE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val publishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = false,
                token = teacher.token
        )
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        pageListPage.assertPageDisplayed(publishedPage.title)
        pageListPage.assertPageIsPublished(publishedPage.title)

        pageListPage.openPage(publishedPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.toggleFrontPage()
        editPageDetailsPage.savePage()
        Espresso.pressBack()
        pageListPage.assertFrontPageDisplayed(publishedPage.title)
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testPublishUnpublishedPageE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val unpublishedPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = false,
                frontPage = false,
                token = teacher.token
        )

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()
        pageListPage.openPage(unpublishedPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        Espresso.pressBack()
        pageListPage.assertPageIsPublished(unpublishedPage.title)
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testUnableToUnpublishFrontPageE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val frontPage = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = false,
                token = teacher.token
        )
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        pageListPage.assertPageDisplayed(frontPage.title)
        pageListPage.assertPageIsPublished(frontPage.title)

        pageListPage.openPage(frontPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.toggleFrontPage()
        editPageDetailsPage.unableToSaveUnpublishedFrontPage()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        Espresso.pressBack()
        pageListPage.assertFrontPageDisplayed(frontPage.title)
        pageListPage.assertPageIsPublished(frontPage.title)
    }
}