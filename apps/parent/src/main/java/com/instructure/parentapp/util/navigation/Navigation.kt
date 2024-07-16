package com.instructure.parentapp.util.navigation

import android.app.Activity
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavType
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.dialog
import androidx.navigation.fragment.fragment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.help.HelpDialogFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.parentapp.R
import com.instructure.parentapp.features.alerts.list.AlertsFragment
import com.instructure.parentapp.features.calendar.ParentCalendarFragment
import com.instructure.parentapp.features.courses.details.CourseDetailsFragment
import com.instructure.parentapp.features.courses.list.CoursesFragment
import com.instructure.parentapp.features.dashboard.DashboardFragment
import com.instructure.parentapp.features.managestudents.ManageStudentsFragment
import com.instructure.parentapp.features.notaparent.NotAParentFragment
import com.instructure.parentapp.features.settings.SettingsFragment
import com.instructure.parentapp.features.splash.SplashFragment


class Navigation(apiPrefs: ApiPrefs) {

    private val baseUrl = apiPrefs.fullDomain

    private val courseId = "course-id"
    private val courseDetails = "$baseUrl/courses/{$courseId}"

    val splash = "$baseUrl/splash"
    val notAParent = "$baseUrl/not-a-parent"
    val courses = "$baseUrl/courses"
    val calendar = "$baseUrl/calendar"
    val alerts = "$baseUrl/alerts"
    val inbox = "$baseUrl/conversations"
    val help = "$baseUrl/help"
    val manageStudents = "$baseUrl/manage-students"
    val settings = "$baseUrl/settings"

    private val eventId = "event-id"
    private val calendarEvent = "$baseUrl/courses/{$courseId}/calendar_events/{$eventId}"

    fun courseDetailsRoute(id: Long) = "$baseUrl/courses/$id"
    fun calendarEventRoute(courseId: Long, eventId: Long) = "$baseUrl/courses/$courseId/calendar_events/$eventId"

    fun crateMainNavGraph(navController: NavController): NavGraph {
        return navController.createGraph(
            splash
        ) {
            fragment<SplashFragment>(splash)
            fragment<NotAParentFragment>(notAParent)
            fragment<DashboardFragment>(courses) {
                deepLink {
                    uriPattern = courses
                }
            }
            fragment<DashboardFragment>(calendar) {
                deepLink {
                    uriPattern = calendar
                }
            }
            fragment<DashboardFragment>(alerts) {
                deepLink {
                    uriPattern = alerts
                }
            }
            fragment<InboxFragment>(inbox) {
                deepLink {
                    uriPattern = inbox
                }
            }
            fragment<ManageStudentsFragment>(manageStudents)
            fragment<SettingsFragment>(settings)
            dialog<HelpDialogFragment>(help)
            fragment<CourseDetailsFragment>(courseDetails) {
                argument(courseId) {
                    type = NavType.LongType
                    nullable = false
                }
                deepLink {
                    uriPattern = courseDetails
                }
            }
            fragment<EventFragment>(calendarEvent) {
                argument(courseId) {
                    type = NavType.LongType
                    nullable = false
                }
                argument(eventId) {
                    type = NavType.LongType
                    nullable = false
                }
                deepLink {
                    uriPattern = calendarEvent
                }
            }
        }
    }

    fun createDashboardNavGraph(navController: NavController): NavGraph {
        return navController.createGraph(
            courses
        ) {
            fragment<CoursesFragment>(courses) {
                deepLink {
                    uriPattern = courses
                }
            }
            fragment<ParentCalendarFragment>(calendar) {
                deepLink {
                    uriPattern = calendar
                }
            }
            fragment<AlertsFragment>(alerts) {
                deepLink {
                    uriPattern = alerts
                }
            }
        }
    }

    fun navigate(activity: Activity?, route: String) {
        val navController = activity?.findNavController(R.id.nav_host_fragment) ?: return
        try {
            navController.navigate(route)
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, e.message.orEmpty())
        }
    }
}
