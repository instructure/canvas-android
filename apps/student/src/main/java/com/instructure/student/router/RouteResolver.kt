package com.instructure.student.router

import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.calendar.CalendarFragment
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventFragment
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoFragment
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment
import com.instructure.pandautils.features.dashboard.edit.EditDashboardFragment
import com.instructure.pandautils.features.discussion.create.CreateDiscussionWebViewFragment
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.features.notification.preferences.EmailNotificationPreferencesFragment
import com.instructure.pandautils.features.notification.preferences.PushNotificationPreferencesFragment
import com.instructure.pandautils.features.offline.offlinecontent.OfflineContentFragment
import com.instructure.pandautils.features.offline.sync.progress.SyncProgressFragment
import com.instructure.pandautils.features.offline.sync.settings.SyncSettingsFragment
import com.instructure.pandautils.features.settings.inboxsignature.InboxSignatureFragment
import com.instructure.pandautils.features.smartsearch.SmartSearchFragment
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.utils.Const
import com.instructure.student.AnnotationComments.AnnotationCommentListFragment
import com.instructure.student.activity.NothingToSeeHereFragment
import com.instructure.student.features.coursebrowser.CourseBrowserFragment
import com.instructure.student.features.discussion.details.DiscussionDetailsFragment
import com.instructure.student.features.discussion.list.DiscussionListFragment
import com.instructure.student.features.elementary.course.ElementaryCourseFragment
import com.instructure.student.features.files.details.FileDetailsFragment
import com.instructure.student.features.files.list.FileListFragment
import com.instructure.student.features.files.search.FileSearchFragment
import com.instructure.student.features.grades.GradesListFragment
import com.instructure.student.features.modules.list.ModuleListFragment
import com.instructure.student.features.modules.progression.CourseModuleProgressionFragment
import com.instructure.student.features.modules.progression.ModuleQuizDecider
import com.instructure.student.features.pages.details.PageDetailsFragment
import com.instructure.student.features.pages.list.PageListFragment
import com.instructure.student.features.people.details.PeopleDetailsFragment
import com.instructure.student.features.people.list.PeopleListFragment
import com.instructure.student.features.quiz.list.QuizListFragment
import com.instructure.student.features.todolist.ToDoListFragment
import com.instructure.student.fragment.AccountPreferencesFragment
import com.instructure.student.fragment.AnnouncementListFragment
import com.instructure.student.fragment.AssignmentBasicFragment
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.CourseSettingsFragment
import com.instructure.student.fragment.DashboardFragment
import com.instructure.student.fragment.EditPageDetailsFragment
import com.instructure.student.fragment.FeatureFlagsFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.NotificationListFragment
import com.instructure.student.fragment.ProfileSettingsFragment
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.fragment.OldToDoListFragment
import com.instructure.student.fragment.UnknownItemFragment
import com.instructure.student.fragment.UnsupportedFeatureFragment
import com.instructure.student.fragment.UnsupportedTabFragment
import com.instructure.student.fragment.ViewHtmlFragment
import com.instructure.student.fragment.ViewImageFragment
import com.instructure.student.fragment.ViewUnsupportedFileFragment
import com.instructure.student.mobius.assignmentDetails.submission.annnotation.AnnotationSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.SubmissionRubricDescriptionFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsRepositoryFragment
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceDetailsRepositoryFragment
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListRepositoryFragment
import com.instructure.student.mobius.elementary.ElementaryDashboardFragment
import com.instructure.student.mobius.settings.pairobserver.ui.PairObserverFragment
import com.instructure.student.mobius.syllabus.ui.SyllabusRepositoryFragment

object RouteResolver {

    fun getFragment(route: Route): Fragment? {
        return getFragment(route.canvasContext, route)
    }

    fun getFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        return if (canvasContext == null && route.canvasContext == null) { // Typically areas like inbox or notifications where a canvasContext may not apply
            if (route.primaryClass != null) {
                getFrag(route.primaryClass, route)
            } else {
                getFrag(route.secondaryClass, route)
            }
        } else {
            if (route.canvasContext == null) route.canvasContext = canvasContext
            if (!route.arguments.containsKey(Const.CANVAS_CONTEXT)) route.arguments.putParcelable(Const.CANVAS_CONTEXT, route.canvasContext) // When coming from an external url
            if (!route.arguments.containsKey(Const.URL)) route.arguments.putString(Const.URL, route.uri?.toString())

            // We have a route, load up the secondary class if there is one, otherwise primary
            if (route.secondaryClass != null) {
                // Load it up
                getFrag(route.secondaryClass, route)
            } else {
                // Load up the primary class
                getFrag(route.primaryClass, route)
            }
        }
    }

    private fun getFrag(cls: Class<out Fragment>?, route: Route): Fragment? {
        if (cls == null) return null

        // Divided up into two camps, those who need a valid CanvasContext and those who do not

        return when {
            cls.isA<DashboardFragment>() -> DashboardFragment.newInstance(route)
            cls.isA<ElementaryDashboardFragment>() -> ElementaryDashboardFragment.newInstance(route)
            cls.isA<OldToDoListFragment>() -> OldToDoListFragment.newInstance(route)
            cls.isA<ToDoListFragment>() -> ToDoListFragment.newInstance(route)
            cls.isA<NotificationListFragment>() -> NotificationListFragment.newInstance(route)
            cls.isA<InboxFragment>() -> InboxFragment.newInstance(route)
            cls.isA<CourseBrowserFragment>() -> CourseBrowserFragment.newInstance(route)
            cls.isA<ElementaryCourseFragment>() -> ElementaryCourseFragment.newInstance(route)
            cls.isA<EditDashboardFragment>() -> EditDashboardFragment.newInstance(route)
            cls.isA<ModuleQuizDecider>() -> ModuleQuizDecider.newInstance(route)
            cls.isA<EditPageDetailsFragment>() -> EditPageDetailsFragment.newInstance(route)
            cls.isA<InboxDetailsFragment>() -> InboxDetailsFragment.newInstance(route)
            cls.isA<InboxComposeFragment>() -> InboxComposeFragment.newInstance(route)
            cls.isA<QuizListFragment>() -> QuizListFragment.newInstance(route)
            cls.isA<BasicQuizViewFragment>() -> BasicQuizViewFragment.newInstance(route)
            cls.isA<AssignmentListFragment>() -> AssignmentListFragment.newInstance(route)
            cls.isA<AssignmentBasicFragment>() -> AssignmentBasicFragment.newInstance(route)
            cls.isA<PageDetailsFragment>() -> PageDetailsFragment.newInstance(route)
            cls.isA<LtiLaunchFragment>() -> LtiLaunchFragment.newInstance(route)
            cls.isA<SyllabusRepositoryFragment>() -> SyllabusRepositoryFragment.newInstance(route)
            cls.isA<GradesListFragment>() -> GradesListFragment.newInstance(route)
            cls.isA<ModuleListFragment>() -> ModuleListFragment.newInstance(route)
            cls.isA<CourseSettingsFragment>() -> CourseSettingsFragment.newInstance(route)
            cls.isA<AnnouncementListFragment>() -> AnnouncementListFragment.newInstance(route)
            cls.isA<ConferenceDetailsRepositoryFragment>() -> ConferenceDetailsRepositoryFragment.newInstance(route)
            cls.isA<ConferenceListRepositoryFragment>() -> ConferenceListRepositoryFragment.newInstance(route)
            cls.isA<UnsupportedTabFragment>() -> UnsupportedTabFragment.newInstance(route)
            cls.isA<PageListFragment>() -> PageListFragment.newInstance(route)
            cls.isA<UnsupportedFeatureFragment>() -> UnsupportedFeatureFragment.newInstance(route)
            cls.isA<UnknownItemFragment>() -> UnknownItemFragment.newInstance(route)
            cls.isA<PeopleListFragment>() -> PeopleListFragment.newInstance(route)
            cls.isA<PeopleDetailsFragment>() -> PeopleDetailsFragment.newInstance(route)
            cls.isA<FileListFragment>() -> FileListFragment.newInstance(route)
            cls.isA<FileSearchFragment>() -> FileSearchFragment.newInstance(route)
            cls.isA<ToDoFragment>() -> ToDoFragment.newInstance(route)
            cls.isA<CreateUpdateToDoFragment>() -> CreateUpdateToDoFragment.newInstance(route)
            cls.isA<CreateUpdateEventFragment>() -> CreateUpdateEventFragment.newInstance(route)
            cls.isA<EventFragment>() -> EventFragment.newInstance(route)
            cls.isA<CalendarFragment>() -> CalendarFragment.newInstance(route)
            cls.isA<FileDetailsFragment>() -> FileDetailsFragment.newInstance(route)
            cls.isA<ViewImageFragment>() -> ViewImageFragment.newInstance(route)
            cls.isA<ViewHtmlFragment>() -> ViewHtmlFragment.newInstance(route)
            cls.isA<ViewUnsupportedFileFragment>() -> ViewUnsupportedFileFragment.newInstance(route)
            cls.isA<ProfileSettingsFragment>() -> ProfileSettingsFragment.newInstance()
            cls.isA<AccountPreferencesFragment>() -> AccountPreferencesFragment.newInstance()
            cls.isA<CourseModuleProgressionFragment>() -> CourseModuleProgressionFragment.newInstance(route)
            cls.isA<AssignmentDetailsFragment>() -> AssignmentDetailsFragment.newInstance(route)
            cls.isA<SubmissionDetailsRepositoryFragment>() -> SubmissionDetailsRepositoryFragment.newInstance(route)
            cls.isA<SubmissionRubricDescriptionFragment>() -> SubmissionRubricDescriptionFragment.newInstance(route)
            cls.isA<DiscussionListFragment>() -> DiscussionListFragment.newInstance(route)
            cls.isA<DiscussionDetailsFragment>() -> DiscussionDetailsFragment.newInstance(route)
            cls.isA<CreateDiscussionWebViewFragment>() -> CreateDiscussionWebViewFragment.newInstance(route)
            cls.isA<StudioWebViewFragment>() -> StudioWebViewFragment.newInstance(route)
            cls.isA<TextSubmissionUploadFragment>() -> TextSubmissionUploadFragment.newInstance(route)
            cls.isA<UrlSubmissionUploadFragment>() -> UrlSubmissionUploadFragment.newInstance(route)
            cls.isA<PickerSubmissionUploadFragment>() -> PickerSubmissionUploadFragment.newInstance(route)
            cls.isA<UploadStatusSubmissionFragment>() -> UploadStatusSubmissionFragment.newInstance(route)
            cls.isA<AnnotationCommentListFragment>() -> AnnotationCommentListFragment.newInstance(route)
            cls.isA<NothingToSeeHereFragment>() -> NothingToSeeHereFragment.newInstance()
            cls.isA<AnnotationSubmissionUploadFragment>() -> AnnotationSubmissionUploadFragment.newInstance(route)
            cls.isA<PushNotificationPreferencesFragment>() -> PushNotificationPreferencesFragment.newInstance()
            cls.isA<EmailNotificationPreferencesFragment>() -> EmailNotificationPreferencesFragment.newInstance()
            cls.isA<DiscussionDetailsWebViewFragment>() -> DiscussionDetailsWebViewFragment.newInstance(route)
            cls.isA<DiscussionRouterFragment>() -> DiscussionRouterFragment.newInstance(route.canvasContext!!, route)
            cls.isA<OfflineContentFragment>() -> OfflineContentFragment.newInstance(route)
            cls.isA<SyncProgressFragment>() -> SyncProgressFragment.newInstance()
            cls.isA<PairObserverFragment>() -> PairObserverFragment.newInstance()
            cls.isA<SyncSettingsFragment>() -> SyncSettingsFragment.newInstance()
            cls.isA<FeatureFlagsFragment>() -> FeatureFlagsFragment()
            cls.isA<RemoteConfigParamsFragment>() -> RemoteConfigParamsFragment()
            cls.isA<SmartSearchFragment>() -> SmartSearchFragment.newInstance(route)
            cls.isA<InboxSignatureFragment>() -> InboxSignatureFragment()
            cls.isA<InternalWebviewFragment>() -> InternalWebviewFragment.newInstance(route) // Keep this at the end
            else -> null
        }
    }

    private inline fun <reified T> Class<*>.isA() = T::class.java.isAssignableFrom(this)
}
