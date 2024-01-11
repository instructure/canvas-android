package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.PageApiModel
import com.instructure.teacher.ui.pages.WebViewTextCheck
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PagesE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.PAGES, TestCategory.E2E)
    fun testPagesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Create an unpublished page for course: ${course.name}.")
        val testPage1 = createCoursePage(course, teacher, published = false, frontPage = false, body = "<h1 id=\"header1\">Unpublished Page Text</h1>")

        Log.d(PREPARATION_TAG,"Create a published page for course: ${course.name}.")
        val testPage2 = createCoursePage(course, teacher, published = true, frontPage = false, body = "<h1 id=\"header1\">Regular Page Text</h1>")

        Log.d(PREPARATION_TAG,"Create a front page for course: ${course.name}.")
        val testPage3 = createCoursePage(course, teacher, published = true, frontPage = true, body = "<h1 id=\"header1\">Front Page Text</h1>")

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Pages Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        Log.d(STEP_TAG,"Assert that ${testPage1.title} page is displayed and it is really unpublished.")
        pageListPage.assertPageDisplayed(testPage1.title)
        pageListPage.assertPageIsUnpublished(testPage1.title)

        Log.d(STEP_TAG,"Assert that ${testPage2.title} page is displayed and it is really published.")
        pageListPage.assertPageDisplayed(testPage2.title)
        pageListPage.assertPageIsPublished(testPage2.title)

        Log.d(STEP_TAG,"Assert that ${testPage3.title} page is displayed and it is really a front page and published.")
        pageListPage.assertPageDisplayed(testPage3.title)
        pageListPage.assertPageIsPublished(testPage3.title)
        pageListPage.assertFrontPageDisplayed(testPage3.title)

        Log.d(STEP_TAG,"Open ${testPage2.title} page. Assert that it is really a regular published page via web view assertions.")
        pageListPage.openPage(testPage2.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Regular Page Text"))
        Log.d(STEP_TAG,"Navigate back to Pages page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Open ${testPage3.title} page. Assert that it is really a front (published) page via web view assertions.")
        pageListPage.openPage(testPage3.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Front Page Text"))

        Log.d(STEP_TAG,"Navigate back to Pages page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Open ${testPage1.title} page. Assert that it is really an unpublished page via web view assertions.")
        pageListPage.openPage(testPage1.title)
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Unpublished Page Text"))
        Espresso.pressBack()

        val editedUnpublishedPageName = "Page still unpublished"
        Log.d(STEP_TAG,"Open and edit the ${testPage1.title} page and set $editedUnpublishedPageName page name as new value. Click on 'Save' and navigate back.")
        pageListPage.openPage(testPage1.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.editPageName(editedUnpublishedPageName)
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that the page name has been changed to $editedUnpublishedPageName.")
        pageListPage.assertPageIsUnpublished(editedUnpublishedPageName)

        Log.d(STEP_TAG,"Open ${testPage2.title} page and Edit it. Set it as a front page and click on 'Save'. Navigate back.")
        pageListPage.openPage(testPage2.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.toggleFrontPage()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that ${testPage2.title} is displayed as a front page.")
        pageListPage.assertFrontPageDisplayed(testPage2.title)

        Log.d(STEP_TAG,"Open $editedUnpublishedPageName page and Edit it. Set it as a front page and click on 'Save'. Navigate back.")
        pageListPage.openPage(editedUnpublishedPageName)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that $testPage2 is published.")
        pageListPage.assertPageIsPublished(testPage2.title)

        Log.d(STEP_TAG,"Open ${testPage3.title} page and Edit it. Unpublish it and remove 'Front page' from it.")
        pageListPage.openPage(testPage3.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.toggleFrontPage()

        Log.d(STEP_TAG,"Assert that a front page cannot be unpublished.")
        editPageDetailsPage.unableToSaveUnpublishedFrontPage()

        Log.d(STEP_TAG,"Publish ${testPage3.title} page again. Click on 'Save' and navigate back-")
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that ${testPage2.title} is displayed as a front page.")
        pageListPage.assertFrontPageDisplayed(testPage2.title)

        Log.d(STEP_TAG,"Click on '+' icon on the UI to create a new page.")
        pageListPage.clickOnCreateNewPage()

        val newPageTitle = "Test Page Mobile UI"
        Log.d(STEP_TAG,"Set '$newPageTitle' as the page's title and set some description as well.")
        editPageDetailsPage.editPageName(newPageTitle)
        editPageDetailsPage.editDescription("Mobile UI Page description")

        Log.d(STEP_TAG,"Toggle Publish checkbox and save the page.")
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()

        Log.d(STEP_TAG,"Assert that '$newPageTitle' page is displayed and published.")
        pageListPage.assertPageIsPublished(newPageTitle)

        Log.d(STEP_TAG,"Click on the Search icon and type some search query string which matches only with the previously created page's title.")
        pageListPage.searchable.clickOnSearchButton()
        pageListPage.searchable.typeToSearchBar("Test")

        Log.d(STEP_TAG,"Assert that the '$newPageTitle' titled page is displayed and it is the only one.")
        pageListPage.assertPageIsPublished(newPageTitle)
        pageListPage.assertPageCount(1)

        Log.d(STEP_TAG, "Click on the clear search input button (X) on the toolbar. Assert that the default state, so all the 4 pages will be displayed.")
        pageListPage.searchable.clickOnClearSearchButton()
        pageListPage.assertPageCount(4)
    }

    private fun createCoursePage(
        course: CourseApiModel,
        teacher: CanvasUserApiModel,
        published: Boolean = true,
        frontPage: Boolean = false,
        body: String = EMPTY_STRING
    ): PageApiModel {
        return PagesApi.createCoursePage(
            courseId = course.id,
            published = published,
            frontPage = frontPage,
            token = teacher.token,
            body = body
        )
    }

}