package com.instructure.parentapp.util.navigation

import android.app.Activity
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavType
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.fragment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.parentapp.R
import com.instructure.parentapp.features.alerts.list.AlertsFragment
import com.instructure.parentapp.features.calendar.CalendarFragment
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
    val inboxCompose = "$baseUrl/conversations/compose"
    val help = "$baseUrl/help"
    val manageStudents = "$baseUrl/manage-students"
    val settings = "$baseUrl/settings"

    fun courseDetailsRoute(id: Long) = "$baseUrl/courses/$id"

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
            fragment<InboxFragment>(inbox)
            fragment<InboxComposeFragment>(inboxCompose)
            fragment<ManageStudentsFragment>(manageStudents)
            fragment<SettingsFragment>(settings)
            fragment<CourseDetailsFragment>(courseDetails) {
                argument(courseId) {
                    type = NavType.LongType
                    nullable = false
                }
                deepLink {
                    uriPattern = courseDetails
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
            fragment<CalendarFragment>(calendar) {
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
