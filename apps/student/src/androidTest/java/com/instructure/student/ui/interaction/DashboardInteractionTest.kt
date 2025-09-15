/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.interaction

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAccountNotification
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Grades
import com.instructure.pandautils.di.NetworkStateProviderModule
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.espresso.fakes.FakeNetworkStateProvider
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test

@UninstallModules(NetworkStateProviderModule::class)
@HiltAndroidTest
class DashboardInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private val isOnlineLiveData = MutableLiveData<Boolean>()

    @BindValue
    @JvmField
    val networkStateProvider: NetworkStateProvider = FakeNetworkStateProvider(isOnlineLiveData)

    @Before
    fun setUp() {
        isOnlineLiveData.postValue(true)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testNavigateToDashboard() {
        // User should be able to tap and navigate to dashboard page
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        goToDashboard(data)
        dashboardPage.clickInboxTab()
        dashboardPage.goToDashboard()
        dashboardPage.assertDisplaysCourse(data.courses.values.first()) // disambiguates via isDisplayed()

        // These get confused by the existence of multiple DashboardPages in the layout
        //dashboardPage.waitForRender()
        //dashboardPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_emptyState() {
        // Empty state should be displayed with a 'Add Courses' button, when nothing is favorited (and courses are completed/concluded)
        // With the new DashboardCard api being used, if nothing is a favorite it will default to active enrollments
        val data = setUpData(courseCount = 0, pastCourseCount = 1)
        goToDashboard(data)
        dashboardPage.assertDisplaysAddCourseMessage()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_addFavorite() {
        // Starring should add course to favorite list

        val data = setUpData(courseCount = 2, favoriteCourseCount = 1)
        goToDashboard(data)
        val nonFavorite = data.courses.values.filter { x -> !x.isFavorite }.first()

        dashboardPage.assertCourseNotShown(nonFavorite)

        dashboardPage.editFavorites()
        allCoursesPage.assertCourseDisplayed(nonFavorite)
        allCoursesPage.assertCourseNotFavorited(nonFavorite)
        allCoursesPage.favoriteCourse(nonFavorite)
        allCoursesPage.assertCourseFavorited(nonFavorite)

        Espresso.pressBack()

        dashboardPage.assertDisplaysCourse(nonFavorite)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_removeFavorite() {
        // Un-starring should remove course from favorite list

        val data = setUpData(courseCount = 2, favoriteCourseCount = 2)
        goToDashboard(data)
        val favorite = data.courses.values.filter { x -> x.isFavorite }.first()

        dashboardPage.assertDisplaysCourse(favorite)

        dashboardPage.editFavorites()
        allCoursesPage.assertCourseDisplayed(favorite)
        allCoursesPage.assertCourseFavorited(favorite)
        allCoursesPage.unfavoriteCourse(favorite)
        allCoursesPage.assertCourseNotFavorited(favorite)

        Espresso.pressBack()

        dashboardPage.assertCourseNotShown(favorite)


    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_addAllToFavorites() {
        val data = setUpData(courseCount = 3, favoriteCourseCount = 0)
        goToDashboard(data)
        val toFavorite = data.courses.values

        data.courses.values.forEach { dashboardPage.assertDisplaysCourse(it) }

        dashboardPage.editFavorites()
        toFavorite.forEach { allCoursesPage.assertCourseNotFavorited(it) }
        allCoursesPage.selectAllCourses()
        toFavorite.forEach { allCoursesPage.assertCourseFavorited(it) }

        Espresso.pressBack()

        toFavorite.forEach { dashboardPage.assertDisplaysCourse(it) }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_removeAllFromFavorites() {
        val data = setUpData(courseCount = 3, favoriteCourseCount = 2)
        goToDashboard(data)
        val toRemove = data.courses.values.filter { it.isFavorite }

        toRemove.forEach { dashboardPage.assertDisplaysCourse(it) }

        dashboardPage.editFavorites()
        toRemove.forEach { allCoursesPage.assertCourseFavorited(it) }
        allCoursesPage.unselectAllCourses()
        toRemove.forEach { allCoursesPage.assertCourseNotFavorited(it) }

        Espresso.pressBack()

        data.courses.values.forEach { dashboardPage.assertDisplaysCourse(it) }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardAnnouncement_refresh() {
        // Pull to refresh loads new announcements
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1) // No announcements initially
        goToDashboard(data)
        dashboardPage.assertAnnouncementsGone()
        val announcement = data.addAccountNotification()
        dashboardPage.refresh()
        dashboardPage.assertAnnouncementShowing(announcement)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardAnnouncement_dismiss() {
        // Tapping dismiss should remove the announcement. Refresh should not display it again.
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1, announcementCount = 1)
        goToDashboard(data)
        val announcement = data.accountNotifications.values.first()

        dashboardPage.assertAnnouncementShowing(announcement)
        dashboardPage.refresh() //need this refresh because if there are such amount of elements and the screen is scrollable, first "interaction" will scroll down somehow a bit. It works on physical device, it's just an emulator-specific issue.
        dashboardPage.assertAnnouncementShowing(announcement)
        dashboardPage.dismissAnnouncement()
        dashboardPage.assertAnnouncementGoneAndCheckAfterRefresh()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardInvite_accept() {
        val data = setUpData(courseCount = 2, invitedCourseCount = 1)
        goToDashboard(data)
        val invitedCourse = data.courses.values.first { it.enrollments?.any { it.enrollmentState == EnrollmentAPI.STATE_INVITED } ?: false }

        dashboardPage.assertInviteShowing(invitedCourse.name)
        dashboardPage.refresh() //need this refresh because if there are such amount of elements and the screen is scrollable, first "interaction" will scroll down somehow a bit. It works on physical device, it's just an emulator-specific issue.
        dashboardPage.assertInviteShowing(invitedCourse.name)
        dashboardPage.acceptInvite()
        dashboardPage.assertInviteAccepted()
        dashboardPage.refresh()
        dashboardPage.assertInviteGone(invitedCourse.name)
        dashboardPage.assertDisplaysCourse(invitedCourse)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardInvite_decline() {
        val data = setUpData(courseCount = 2, invitedCourseCount = 1)
        goToDashboard(data)
        val invitedCourse = data.courses.values.first { it.enrollments?.any { it.enrollmentState == EnrollmentAPI.STATE_INVITED } ?: false }

        dashboardPage.assertInviteShowing(invitedCourse.name)
        dashboardPage.refresh() //need this refresh because if there are such amount of elements and the screen is scrollable, first "interaction" will scroll down somehow a bit. It works on physical device, it's just an emulator-specific issue.
        dashboardPage.assertInviteShowing(invitedCourse.name)
        dashboardPage.declineInvite()
        dashboardPage.assertInviteDeclined()
        dashboardPage.refresh()
        dashboardPage.assertInviteGone(invitedCourse.name)
        dashboardPage.assertCourseNotShown(invitedCourse)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardAnnouncement_view() {
        // Tapping global announcement displays the content
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1, announcementCount = 1)
        goToDashboard(data)
        val announcement = data.accountNotifications.values.first()

        dashboardPage.assertAnnouncementShowing(announcement)
        dashboardPage.refresh() //need this refresh because if there are such amount of elements and the screen is scrollable, first "interaction" will scroll down somehow a bit. It works on physical device, it's just an emulator-specific issue.
        dashboardPage.assertAnnouncementShowing(announcement)
        dashboardPage.tapAnnouncement()
        dashboardPage.assertAnnouncementDetailsDisplayed(announcement)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_tappingCourseCardDisplaysCourseBrowser() {
        // Tapping on a course card opens course browser page
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        goToDashboard(data)
        val course = data.courses.values.first()
        dashboardPage.selectCourse(course)

        courseBrowserPage.assertPageObjects()
        courseBrowserPage.assertTitleCorrect(course)
        var tabs = data.courseTabs[course.id]
        assertNotNull("Expected course tabs to be populated", tabs)
        for (tab in tabs!!) {
            courseBrowserPage.assertTabDisplayed(tab)
        }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_gradeIsDisplayedWhenShowGradesIsSelected() {
        // [Student] Grade is displayed when 'Show Grades' (located in navigation drawer) is selected
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        goToDashboard(data)
        leftSideNavigationDrawerPage.setShowGrades(true)
        dashboardPage.assertShowsGrades()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_gradeIsNotDisplayedWhenShowGradesIsDeSelected() {
        // [Student] Grade is NOT displayed when 'Show Grades' (located in navigation drawer) is de-selected
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        goToDashboard(data)
        leftSideNavigationDrawerPage.setShowGrades(false)
        dashboardPage.assertHidesGrades()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_gradeIsDisplayedWithGradeAndScoreWhenNotRestricted() {
        // [Student] Grade is displayed when 'Show Grades' (located in navigation drawer) is selected
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade("A", 100.0, data, false)
        goToDashboard(data)
        leftSideNavigationDrawerPage.setShowGrades(true)
        dashboardPage.assertGradeText("A 100%")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun testDashboardCourses_gradeIsDisplayedWithGradeOnlyWhenQuantitativeDataIsRestricted() {
        // [Student] Grade is displayed when 'Show Grades' (located in navigation drawer) is selected
        val data = setUpData(courseCount = 1, favoriteCourseCount = 1)
        setUpCustomGrade("A", 100.0, data, true)
        goToDashboard(data)
        leftSideNavigationDrawerPage.setShowGrades(true)
        dashboardPage.assertGradeText("A")
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testOfflineIndicatorDisplayedIfOffline() {
        goToDashboard(setUpData())

        isOnlineLiveData.postValue(false)

        dashboardPage.assertOfflineIndicatorDisplayed()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testOfflineIndicatorNotDisplayedIfOnline() {
        goToDashboard(setUpData())

        isOnlineLiveData.postValue(true)

        dashboardPage.assertOfflineIndicatorNotDisplayed()
    }

    private fun setUpData(
        courseCount: Int = 1,
        invitedCourseCount: Int = 0,
        pastCourseCount: Int = 0,
        favoriteCourseCount: Int = 0,
        announcementCount: Int = 0
    ): MockCanvas {
        return MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            invitedCourseCount = invitedCourseCount,
            pastCourseCount = pastCourseCount,
            favoriteCourseCount = favoriteCourseCount,
            accountNotificationCount = announcementCount
        )
    }

    private fun goToDashboard(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()
    }

    private fun setUpCustomGrade(grade: String, score: Double, data: MockCanvas, restrictQuantitativeData: Boolean) {
        val student = data.students[0]
        val course = data.courses.values.first()

        val enrollment = course.enrollments!!.first { it.userId == student.id }
            .copy(grades = Grades(currentGrade = grade, currentScore = score))

        val newCourse = course.copy(
            settings = CourseSettings(restrictQuantitativeData = restrictQuantitativeData),
            enrollments = mutableListOf(enrollment)
        )
        data.courses[course.id] = newCourse
    }
}
