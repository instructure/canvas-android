package com.instructure.teacher.ui.e2e

import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.Stub
import com.instructure.dataseeding.api.PagesApi
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.pages.WebViewTextCheck
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

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
        val student = data.studentsList[0]
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

        pageListPage.assertPageDisplayed(pageTitle = unpublishedPage.title)
        pageListPage.assertPageIsUnpublished(pageTitle = unpublishedPage.title)

        pageListPage.assertPageDisplayed(pageTitle = publishedPage.title)
        pageListPage.assertPageIsPublished(pageTitle = publishedPage.title)

        pageListPage.assertPageDisplayed(pageTitle = frontPage.title)
        pageListPage.assertPageIsPublished(pageTitle = frontPage.title)
        pageListPage.assertFrontPageDisplayed(pageTitle = frontPage.title)

        pageListPage.openPage(pageTitle = publishedPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Regular Page Text"))
        editPageDetailsPage.navigateBack()

        pageListPage.openPage(pageTitle = frontPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Front Page Text"))
        editPageDetailsPage.navigateBack()

        pageListPage.openPage(pageTitle = unpublishedPage.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Unpublished Page Text"))
        editPageDetailsPage.navigateBack()

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

        pageListPage.assertPageDisplayed(pageTitle = unpublishedPage.title)
        pageListPage.assertPageIsUnpublished(unpublishedPage.title)

        pageListPage.openPage(pageTitle = unpublishedPage.title)
        editPageDetailsPage.openEdit()
        val editedUnpublishedPageName = "Page still unpublished"
        editPageDetailsPage.editPageName(editedPageName = editedUnpublishedPageName)
        editPageDetailsPage.savePage()
        editPageDetailsPage.navigateBack()
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

        pageListPage.assertPageDisplayed(pageTitle = publishedPage.title)
        pageListPage.assertPageIsPublished(publishedPage.title)

        pageListPage.openPage(pageTitle = publishedPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.toggleFrontPage()
        editPageDetailsPage.savePage()
        editPageDetailsPage.navigateBack()
        pageListPage.assertFrontPageDisplayed(pageTitle = publishedPage.title)
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
        pageListPage.openPage(pageTitle = unpublishedPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        editPageDetailsPage.navigateBack()
        pageListPage.assertPageIsPublished(pageTitle = unpublishedPage.title)
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E)
    fun testUnableToUnpublishFrontPage() {
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

        pageListPage.assertPageDisplayed(pageTitle = frontPage.title)
        pageListPage.assertPageIsPublished(pageTitle = frontPage.title)

        pageListPage.openPage(pageTitle = frontPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.toggleFrontPage()
        editPageDetailsPage.unableToSaveUnpublishedFrontPage()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        editPageDetailsPage.navigateBack()
        pageListPage.assertFrontPageDisplayed(pageTitle = frontPage.title)
        pageListPage.assertPageIsPublished(pageTitle = frontPage.title)
    }
}