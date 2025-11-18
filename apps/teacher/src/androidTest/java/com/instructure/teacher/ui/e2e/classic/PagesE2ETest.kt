package com.instructure.teacher.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.dataseeding.api.PagesApi
import com.instructure.teacher.ui.pages.classic.WebViewTextCheck
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.tokenLogin
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

        Log.d(PREPARATION_TAG, "Create an unpublished page for course: '${course.name}'.")
        val unpublishedPage = PagesApi.createCoursePage(course.id, teacher.token, published = false, frontPage = false, body = "<h1 id=\"header1\">Unpublished Page Text</h1>")

        Log.d(PREPARATION_TAG, "Create a published page for course: '${course.name}'.")
        val publishedPage = PagesApi.createCoursePage(course.id, teacher.token, published = true, frontPage = false, body = "<h1 id=\"header1\">Regular Page Text</h1>")

        Log.d(PREPARATION_TAG, "Create a front page for course: '${course.name}'.")
        val frontPage = PagesApi.createCoursePage(course.id, teacher.token, published = true, frontPage = true, body = "<h1 id=\"header1\">Front Page Text</h1>")

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Pages Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPagesTab()

        Log.d(ASSERTION_TAG, "Assert that '${unpublishedPage.title}' page is displayed and it is really unpublished.")
        pageListPage.assertPageDisplayed(unpublishedPage.title)
        pageListPage.assertPageIsUnpublished(unpublishedPage.title)

        Log.d(ASSERTION_TAG, "Assert that '${publishedPage.title}' page is displayed and it is really published.")
        pageListPage.assertPageDisplayed(publishedPage.title)
        pageListPage.assertPageIsPublished(publishedPage.title)

        Log.d(ASSERTION_TAG, "Assert that '${frontPage.title}' page is displayed and it is really a front page and published.")
        pageListPage.assertPageDisplayed(frontPage.title)
        pageListPage.assertPageIsPublished(frontPage.title)
        pageListPage.assertFrontPageDisplayed(frontPage.title)

        Log.d(STEP_TAG, "Open '${publishedPage.title}' page.")
        pageListPage.openPage(publishedPage.title)

        Log.d(ASSERTION_TAG, "Assert that it is really a regular published page via web view assertions.")
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Regular Page Text"))

        Log.d(STEP_TAG, "Navigate back to Pages page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open '${frontPage.title}' page.")
        pageListPage.openPage(frontPage.title)

        Log.d(ASSERTION_TAG, "Assert that it is really a front (published) page via web view assertions.")
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Front Page Text"))

        Log.d(STEP_TAG, "Navigate back to Pages page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open '${unpublishedPage.title}' page.")
        pageListPage.openPage(unpublishedPage.title)

        Log.d(ASSERTION_TAG, "Assert that it is really an unpublished page via web view assertions.")
        editPageDetailsPage.runTextChecks(WebViewTextCheck(Locator.ID, "header1", "Unpublished Page Text"))

        Log.d(STEP_TAG,"Navigate back to Pages page.")
        Espresso.pressBack()

        val editedUnpublishedPageName = "Page still unpublished"
        Log.d(STEP_TAG, "Open and edit the '${unpublishedPage.title}' page and set '$editedUnpublishedPageName' page name as new value. Click on 'Save' and navigate back.")
        pageListPage.openPage(unpublishedPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.editPageName(editedUnpublishedPageName)
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the page name has been changed to '$editedUnpublishedPageName'.")
        pageListPage.assertPageIsUnpublished(editedUnpublishedPageName)

        Log.d(STEP_TAG, "Open '${publishedPage.title}' page and Edit it. Set it as a front page and click on 'Save'. Navigate back.")
        pageListPage.openPage(publishedPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.toggleFrontPage()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that '${publishedPage.title}' is displayed as a front page.")
        pageListPage.assertFrontPageDisplayed(publishedPage.title)

        Log.d(STEP_TAG, "Open '$editedUnpublishedPageName' page and Edit it. Set it as a front page and click on 'Save'. Navigate back.")
        pageListPage.openPage(editedUnpublishedPageName)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that '$publishedPage' is published.")
        pageListPage.assertPageIsPublished(publishedPage.title)

        Log.d(STEP_TAG, "Open '${frontPage.title}' page and Edit it. Unpublish it and remove 'Front page' from it.")
        pageListPage.openPage(frontPage.title)
        editPageDetailsPage.openEdit()
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.toggleFrontPage()

        Log.d(ASSERTION_TAG, "Assert that a front page cannot be unpublished.")
        editPageDetailsPage.unableToSaveUnpublishedFrontPage()

        Log.d(STEP_TAG, "Publish '${frontPage.title}' page again. Click on 'Save' and navigate back-")
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that '${publishedPage.title}' is displayed as a front page.")
        pageListPage.assertFrontPageDisplayed(publishedPage.title)

        Log.d(STEP_TAG, "Click on '+' icon on the UI to create a new page.")
        pageListPage.clickOnCreateNewPage()

        val newPageTitle = "Test Page Mobile UI"
        Log.d(STEP_TAG, "Set '$newPageTitle' as the page's title and set some description as well.")
        editPageDetailsPage.editPageName(newPageTitle)
        editPageDetailsPage.editDescription("Mobile UI Page description")

        Log.d(STEP_TAG, "Toggle Publish checkbox and save the page.")
        editPageDetailsPage.togglePublished()
        editPageDetailsPage.savePage()

        Log.d(ASSERTION_TAG, "Assert that '$newPageTitle' page is displayed and published.")
        pageListPage.assertPageIsPublished(newPageTitle)

        Log.d(STEP_TAG, "Click on the Search icon and type some search query string which matches only with the previously created page's title.")
        pageListPage.searchable.clickOnSearchButton()
        pageListPage.searchable.typeToSearchBar("Test")

        Log.d(ASSERTION_TAG, "Assert that the '$newPageTitle' titled page is displayed and it is the only one.")
        pageListPage.assertPageIsPublished(newPageTitle)
        pageListPage.assertPageCount(1)

        Log.d(STEP_TAG, "Click on the clear search input button (X) on the toolbar.")
        pageListPage.searchable.clickOnClearSearchButton()

        Log.d(ASSERTION_TAG, "Assert that the default state, so all the 4 pages will be displayed.")
        pageListPage.assertPageCount(4)
    }

}