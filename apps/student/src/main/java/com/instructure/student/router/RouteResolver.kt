package com.instructure.student.router

import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.student.features.files.search.FileSearchFragment
import com.instructure.student.fragment.*
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.mobius.syllabus.ui.SyllabusFragment

object RouteResolver {

    @JvmStatic
    fun getFragment(route: Route): Fragment? {
        return getFragment(route.canvasContext, route)
    }

    @JvmStatic
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
            cls.isA<ToDoListFragment>() -> ToDoListFragment.newInstance(route)
            cls.isA<NotificationListFragment>() -> NotificationListFragment.newInstance(route)
            cls.isA<SubmissionViewOnlineURLFragment>() -> SubmissionViewOnlineURLFragment.newInstance(route)
            cls.isA<InboxFragment>() -> InboxFragment.newInstance(route)
            cls.isA<CourseBrowserFragment>() -> CourseBrowserFragment.newInstance(route)
            cls.isA<AllCoursesFragment>() -> AllCoursesFragment.newInstance(route)
            cls.isA<EditFavoritesFragment>() -> EditFavoritesFragment.newInstance(route)
            cls.isA<ModuleQuizDecider>() -> ModuleQuizDecider.newInstance(route)
            cls.isA<EditPageDetailsFragment>() -> EditPageDetailsFragment.newInstance(route)
            cls.isA<InboxConversationFragment>() -> InboxConversationFragment.newInstance(route)
            cls.isA<InboxRecipientsFragment>() -> InboxRecipientsFragment.newInstance(route)
            cls.isA<InboxComposeMessageFragment>() -> InboxComposeMessageFragment.newInstance(route)
            cls.isA<QuizListFragment>() -> QuizListFragment.newInstance(route)
            cls.isA<BasicQuizViewFragment>() -> BasicQuizViewFragment.newInstance(route)
            cls.isA<AssignmentListFragment>() -> AssignmentListFragment.newInstance(route)
            cls.isA<AssignmentBasicFragment>() -> AssignmentBasicFragment.newInstance(route)
            cls.isA<QuizStartFragment>() -> QuizStartFragment.newInstance(route)
            cls.isA<QuizQuestionsFragment>() -> QuizQuestionsFragment.newInstance(route)
            cls.isA<PageDetailsFragment>() -> PageDetailsFragment.newInstance(route)
            cls.isA<AddSubmissionFragment>() -> AddSubmissionFragment.newInstance(route)
            cls.isA<LTIWebViewFragment>() -> LTIWebViewFragment.newInstance(route)
            cls.isA<CreateCalendarEventFragment>() -> CreateCalendarEventFragment.newInstance(route)
            cls.isA<CreateAnnouncementFragment>() -> CreateAnnouncementFragment.newInstance(route)
            cls.isA<SyllabusFragment>() -> SyllabusFragment.newInstance(route)
            cls.isA<GradesListFragment>() -> GradesListFragment.newInstance(route)
            cls.isA<ModuleListFragment>() -> ModuleListFragment.newInstance(route)
            cls.isA<CourseSettingsFragment>() -> CourseSettingsFragment.newInstance(route)
            cls.isA<AnnouncementListFragment>() -> AnnouncementListFragment.newInstance(route)
            cls.isA<ConferencesFragment>() -> ConferencesFragment.newInstance(route)
            cls.isA<UnsupportedTabFragment>() -> UnsupportedTabFragment.newInstance(route)
            cls.isA<PageListFragment>() -> PageListFragment.newInstance(route)
            cls.isA<UnsupportedFeatureFragment>() -> UnsupportedFeatureFragment.newInstance(route)
            cls.isA<UnknownItemFragment>() -> UnknownItemFragment.newInstance(route)
            cls.isA<PeopleListFragment>() -> PeopleListFragment.newInstance(route)
            cls.isA<PeopleDetailsFragment>() -> PeopleDetailsFragment.newInstance(route)
            cls.isA<FileListFragment>() -> FileListFragment.newInstance(route)
            cls.isA<FileSearchFragment>() -> FileSearchFragment.newInstance(route)
            cls.isA<CalendarEventFragment>() -> CalendarEventFragment.newInstance(route)
            cls.isA<CalendarListViewFragment>() -> CalendarListViewFragment.newInstance(route)
            cls.isA<FileDetailsFragment>() -> FileDetailsFragment.newInstance(route)
            cls.isA<ViewPdfFragment>() -> ViewPdfFragment.newInstance(route)
            cls.isA<ViewImageFragment>() -> ViewImageFragment.newInstance(route)
            cls.isA<ViewHtmlFragment>() -> ViewHtmlFragment.newInstance(route)
            cls.isA<ViewUnsupportedFileFragment>() -> ViewUnsupportedFileFragment.newInstance(route)
            cls.isA<ProfileSettingsFragment>() -> ProfileSettingsFragment.newInstance()
            cls.isA<AccountPreferencesFragment>() -> AccountPreferencesFragment.newInstance()
            cls.isA<CourseModuleProgressionFragment>() -> CourseModuleProgressionFragment.newInstance(route)
            cls.isA<AssignmentFragment>() -> AssignmentFragment.newInstance(route)
            cls.isA<AssignmentDetailsFragment>() -> AssignmentDetailsFragment.newInstance(route)
            cls.isA<SubmissionDetailsFragment>() -> SubmissionDetailsFragment.newInstance(route)
            cls.isA<DiscussionListFragment>() -> DiscussionListFragment.newInstance(route)
            cls.isA<DiscussionDetailsFragment>() -> DiscussionDetailsFragment.newInstance(route)
            cls.isA<DiscussionsReplyFragment>() -> DiscussionsReplyFragment.newInstance(route)
            cls.isA<CreateDiscussionFragment>() -> CreateDiscussionFragment.newInstance(route)
            cls.isA<DiscussionsUpdateFragment>() -> DiscussionsUpdateFragment.newInstance(route)
            cls.isA<ArcWebviewFragment>() -> ArcWebviewFragment.newInstance(route)
            cls.isA<TextSubmissionUploadFragment>() -> TextSubmissionUploadFragment.newInstance(route)
            cls.isA<UrlSubmissionUploadFragment>() -> UrlSubmissionUploadFragment.newInstance(route)
            cls.isA<InternalWebviewFragment>() -> InternalWebviewFragment.newInstance(route) // Keep this at the end
            else -> null
        }
    }

    private inline fun <reified T> Class<*>.isA() = T::class.java.isAssignableFrom(this)
}
