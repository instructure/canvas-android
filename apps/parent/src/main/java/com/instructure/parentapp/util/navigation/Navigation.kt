package com.instructure.parentapp.util.navigation

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavType
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.fragment
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventFragment
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoFragment
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.settings.SettingsFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.fromJson
import com.instructure.pandautils.utils.toJson
import com.instructure.parentapp.R
import com.instructure.parentapp.features.addstudent.qr.QrPairingFragment
import com.instructure.parentapp.features.alerts.details.AnnouncementDetailsFragment
import com.instructure.parentapp.features.alerts.list.AlertsFragment
import com.instructure.parentapp.features.alerts.settings.AlertSettingsFragment
import com.instructure.parentapp.features.calendar.ParentCalendarFragment
import com.instructure.parentapp.features.courses.details.CourseDetailsFragment
import com.instructure.parentapp.features.courses.list.CoursesFragment
import com.instructure.parentapp.features.dashboard.DashboardFragment
import com.instructure.parentapp.features.lti.LtiLaunchFragment
import com.instructure.parentapp.features.managestudents.ManageStudentsFragment
import com.instructure.parentapp.features.notaparent.NotAParentFragment
import com.instructure.parentapp.features.splash.SplashFragment


class Navigation(apiPrefs: ApiPrefs) {

    private val baseUrl = apiPrefs.fullDomain

    private val courseDetails = "$baseUrl/courses/{$COURSE_ID}"

    private val announcementId = "announcement-id"
    private val courseAnnouncementDetails = "$baseUrl/courses/{$COURSE_ID}/discussion_topics/{$announcementId}"
    private val globalAnnouncementDetails = "$baseUrl/account_notifications/{$announcementId}"

    val splash = "$baseUrl/splash"
    val notAParent = "$baseUrl/not-a-parent"
    val courses = "$baseUrl/courses"
    val calendar = "$baseUrl/calendar"
    val alerts = "$baseUrl/alerts"
    val inbox = "$baseUrl/conversations"
    val manageStudents = "$baseUrl/manage-students"
    val qrPairing = "$baseUrl/qr-pairing"
    val settings = "$baseUrl/settings"

    private val inboxCompose = "$baseUrl/conversations/compose/{${InboxComposeOptions.COMPOSE_PARAMETERS}}"
    fun inboxComposeRoute(options: InboxComposeOptions) = "$baseUrl/conversations/compose/${InboxComposeOptionsParametersType.serializeAsValue(options)}"

    private val inboxDetails = "$baseUrl/conversations/{${InboxDetailsFragment.CONVERSATION_ID}}"
    fun inboxDetailsRoute(conversationId: Long) = "$baseUrl/conversations/$conversationId"

    private val calendarEvent =
        "$baseUrl/{${EventFragment.CONTEXT_TYPE}}/{${EventFragment.CONTEXT_ID}}/calendar_events/{${EventFragment.SCHEDULE_ITEM_ID}}"
    private val createEvent = "$baseUrl/create-event/{${CreateUpdateEventFragment.INITIAL_DATE}}"
    private val updateEvent = "$baseUrl/update-event/{${CreateUpdateEventFragment.SCHEDULE_ITEM}}"

    private val todo = "$baseUrl/todos/{${ToDoFragment.PLANNER_ITEM}}"
    private val createToDo = "$baseUrl/create-todo/{${CreateUpdateToDoFragment.INITIAL_DATE}}"
    private val updateToDo = "$baseUrl/update-todo/{${CreateUpdateToDoFragment.PLANNER_ITEM}}"
    private val alertSettings = "$baseUrl/alert-settings/{${Const.USER}}"

    private val ltiLaunch = "$baseUrl/lti-launch/{${LtiLaunchFragment.LTI_URL}}/{${LtiLaunchFragment.LTI_TITLE}}"

    fun courseDetailsRoute(id: Long) = "$baseUrl/courses/$id"

    fun calendarEventRoute(contextTypeString: String, contextId: Long, eventId: Long) = "$baseUrl/$contextTypeString/$contextId/calendar_events/$eventId"
    fun createEventRoute(initialDate: String?) = "$baseUrl/create-event/${Uri.encode(initialDate.orEmpty())}"
    fun updateEventRoute(scheduleItem: ScheduleItem) = "$baseUrl/update-event/${ScheduleItemParametersType.serializeAsValue(scheduleItem)}"

    fun toDoRoute(plannerItem: PlannerItem) = "$baseUrl/todos/${PlannerItemParametersType.serializeAsValue(plannerItem)}"
    fun createToDoRoute(initialDate: String?) = "$baseUrl/create-todo/${Uri.encode(initialDate.orEmpty())}"
    fun updateToDoRoute(plannerItem: PlannerItem) = "$baseUrl/update-todo/${PlannerItemParametersType.serializeAsValue(plannerItem)}"

    fun alertSettingsRoute(student: User) = "$baseUrl/alert-settings/${UserParametersType.serializeAsValue(student)}"

    fun globalAnnouncementRoute(alertId: Long) = "$baseUrl/account_notifications/$alertId"

    fun ltiLaunchRoute(url: String, title: String) = "$baseUrl/lti-launch/${Uri.encode(url)}/${Uri.encode(title)}"

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
            fragment<InboxComposeFragment>(inboxCompose) {
                argument(InboxComposeOptions.COMPOSE_PARAMETERS) {
                    type = InboxComposeOptionsParametersType
                    nullable = false
                }
            }
            fragment<InboxDetailsFragment>(inboxDetails) {
                argument(InboxDetailsFragment.CONVERSATION_ID) {
                    type = NavType.LongType
                    nullable = false
                }
                deepLink {
                    uriPattern = inboxDetails
                }
            }
            fragment<ManageStudentsFragment>(manageStudents)
            fragment<QrPairingFragment>(qrPairing)
            fragment<SettingsFragment>(settings)
            fragment<CourseDetailsFragment>(courseDetails) {
                argument(COURSE_ID) {
                    type = NavType.LongType
                    nullable = false
                }
                deepLink {
                    uriPattern = courseDetails
                }
            }
            fragment<AnnouncementDetailsFragment>(courseAnnouncementDetails) {
                argument(AnnouncementDetailsFragment.COURSE_ID) {
                    type = NavType.LongType
                    nullable = false
                }
                argument(AnnouncementDetailsFragment.ANNOUNCEMENT_ID) {
                    type = NavType.LongType
                    nullable = false
                }
            }
            fragment<AnnouncementDetailsFragment>(globalAnnouncementDetails) {
                argument(AnnouncementDetailsFragment.ANNOUNCEMENT_ID) {
                    type = NavType.LongType
                    nullable = false
                }
            }
            fragment<EventFragment>(calendarEvent) {
                argument(EventFragment.CONTEXT_TYPE) {
                    type = NavType.StringType
                    nullable = false
                }
                argument(EventFragment.CONTEXT_ID) {
                    type = NavType.LongType
                    nullable = false
                }
                argument(EventFragment.SCHEDULE_ITEM_ID) {
                    type = NavType.LongType
                    nullable = false
                }
                deepLink {
                    uriPattern = calendarEvent
                }
            }
            fragment<ToDoFragment>(todo) {
                argument(ToDoFragment.PLANNER_ITEM) {
                    type = PlannerItemParametersType
                    nullable = false
                }
            }
            fragment<CreateUpdateToDoFragment>(createToDo) {
                argument(CreateUpdateToDoFragment.INITIAL_DATE) {
                    type = NavType.StringType
                    nullable = true
                }
            }
            fragment<CreateUpdateToDoFragment>(updateToDo) {
                argument(CreateUpdateToDoFragment.PLANNER_ITEM) {
                    type = PlannerItemParametersType
                    nullable = false
                }
            }
            fragment<CreateUpdateEventFragment>(createEvent) {
                argument(CreateUpdateEventFragment.INITIAL_DATE) {
                    type = NavType.StringType
                    nullable = true
                }
            }
            fragment<CreateUpdateEventFragment>(updateEvent) {
                argument(CreateUpdateEventFragment.SCHEDULE_ITEM) {
                    type = ScheduleItemParametersType
                    nullable = false
                }
            }
            fragment<AlertSettingsFragment>(alertSettings) {
                argument(Const.USER) {
                    type = UserParametersType
                    nullable = false
                }
            }
            fragment<LtiLaunchFragment>(ltiLaunch) {
                argument(LtiLaunchFragment.LTI_URL) {
                    type = NavType.StringType
                    nullable = false
                }
                argument(LtiLaunchFragment.LTI_TITLE) {
                    type = NavType.StringType
                    nullable = false
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

    companion object {
        const val COURSE_ID = "course-id"
    }
}

private val PlannerItemParametersType = object : NavType<PlannerItem>(
    isNullableAllowed = false
) {
    override fun put(bundle: Bundle, key: String, value: PlannerItem) {
        bundle.putParcelable(key, value)
    }

    override fun get(bundle: Bundle, key: String): PlannerItem? {
        return bundle.getParcelable(key) as? PlannerItem
    }

    override fun serializeAsValue(value: PlannerItem): String {
        return Uri.encode(value.toJson())
    }

    override fun parseValue(value: String): PlannerItem {
        return value.fromJson()
    }
}

private val ScheduleItemParametersType = object : NavType<ScheduleItem>(
    isNullableAllowed = false
) {
    override fun put(bundle: Bundle, key: String, value: ScheduleItem) {
        bundle.putParcelable(key, value)
    }

    override fun get(bundle: Bundle, key: String): ScheduleItem? {
        return bundle.getParcelable(key) as? ScheduleItem
    }

    override fun serializeAsValue(value: ScheduleItem): String {
        return Uri.encode(value.toJson())
    }

    override fun parseValue(value: String): ScheduleItem {
        return value.fromJson()
    }
}

private val InboxComposeOptionsParametersType = object : NavType<InboxComposeOptions>(
    isNullableAllowed = false
) {
    override fun put(bundle: Bundle, key: String, value: InboxComposeOptions) {
        bundle.putParcelable(key, value)
    }

    override fun get(bundle: Bundle, key: String): InboxComposeOptions? {
        return bundle.getParcelable(key) as? InboxComposeOptions
    }

    override fun serializeAsValue(value: InboxComposeOptions): String {
        return Uri.encode(value.toJson())
    }

    override fun parseValue(value: String): InboxComposeOptions {
        return value.fromJson()
    }
}

private val UserParametersType = object : NavType<User>(isNullableAllowed = false) {
    override fun put(bundle: Bundle, key: String, value: User) {
        bundle.putParcelable(key, value)
    }

    override fun get(bundle: Bundle, key: String): User? {
        return bundle.getParcelable(key) as? User
    }

    override fun serializeAsValue(value: User): String {
        return Uri.encode(value.toJson())
    }

    override fun parseValue(value: String): User {
        return value.fromJson()
    }
}
