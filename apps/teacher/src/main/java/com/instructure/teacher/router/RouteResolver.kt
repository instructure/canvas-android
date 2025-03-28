package com.instructure.teacher.router

import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
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
import com.instructure.pandautils.features.settings.SettingsFragment
import com.instructure.pandautils.features.settings.inboxsignature.InboxSignatureFragment
import com.instructure.pandautils.fragments.HtmlContentFragment
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.teacher.PSPDFKit.AnnotationComments.AnnotationCommentListFragment
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.features.assignment.details.AssignmentDetailsFragment
import com.instructure.teacher.features.assignment.list.AssignmentListFragment
import com.instructure.teacher.features.assignment.submission.SubmissionListFragment
import com.instructure.teacher.features.files.search.FileSearchFragment
import com.instructure.teacher.features.modules.list.ui.ModuleListFragment
import com.instructure.teacher.features.modules.progression.ModuleProgressionFragment
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.features.syllabus.edit.EditSyllabusFragment
import com.instructure.teacher.features.syllabus.ui.SyllabusFragment
import com.instructure.teacher.fragments.AnnouncementListFragment
import com.instructure.teacher.fragments.AssigneeListFragment
import com.instructure.teacher.fragments.AttendanceListFragment
import com.instructure.teacher.fragments.ChooseRecipientsFragment
import com.instructure.teacher.fragments.CourseBrowserEmptyFragment
import com.instructure.teacher.fragments.CourseBrowserFragment
import com.instructure.teacher.fragments.CourseSettingsFragment
import com.instructure.teacher.fragments.CreateOrEditPageDetailsFragment
import com.instructure.teacher.fragments.DashboardFragment
import com.instructure.teacher.fragments.DiscussionsListFragment
import com.instructure.teacher.fragments.DueDatesFragment
import com.instructure.teacher.fragments.EditAssignmentDetailsFragment
import com.instructure.teacher.fragments.EditFileFolderFragment
import com.instructure.teacher.fragments.EditQuizDetailsFragment
import com.instructure.teacher.fragments.FeatureFlagsFragment
import com.instructure.teacher.fragments.FileListFragment
import com.instructure.teacher.fragments.FullscreenInternalWebViewFragment
import com.instructure.teacher.fragments.InternalWebViewFragment
import com.instructure.teacher.fragments.PageDetailsFragment
import com.instructure.teacher.fragments.PageListFragment
import com.instructure.teacher.fragments.PeopleListFragment
import com.instructure.teacher.fragments.ProfileEditFragment
import com.instructure.teacher.fragments.ProfileFragment
import com.instructure.teacher.fragments.QuizDetailsFragment
import com.instructure.teacher.fragments.QuizListFragment
import com.instructure.teacher.fragments.QuizPreviewWebviewFragment
import com.instructure.teacher.fragments.SpeedGraderQuizWebViewFragment
import com.instructure.teacher.fragments.ViewHtmlFragment
import com.instructure.teacher.fragments.ViewImageFragment
import com.instructure.teacher.fragments.ViewMediaFragment
import com.instructure.teacher.fragments.ViewPdfFragment
import com.instructure.teacher.fragments.ViewUnsupportedFileFragment

object RouteResolver {

    /**
     * Pass in a route and a course, get a fragment back!
     */
    fun getFullscreenFragment(canvasContext: CanvasContext?, route: Route): Fragment? {

        return if (canvasContext == null) {
            if (route.primaryClass != null) {
                getFrag(route.primaryClass, null, route)
            } else {
                getFrag(route.secondaryClass, null, route)
            }
        } else {
            //we have a route, load up the secondary class if there is one, otherwise primary
            if (route.secondaryClass != null) {
                //load it up
                getFrag(route.secondaryClass, canvasContext, route)
            } else {
                //load up the primary class
                getFrag(route.primaryClass, canvasContext, route)
            }
        }
    }

    fun getMasterFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        return getFrag(route.primaryClass, canvasContext, route)
    }

    fun getDetailFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        return getFrag(route.secondaryClass, canvasContext, route)
    }

    fun getBottomSheetFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        return getFrag(route.primaryClass, canvasContext, route)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Type : Fragment> getFrag(cls: Class<Type>?, canvasContext: CanvasContext?, route: Route): Type? {
        if (cls == null) return null

        var fragment: Fragment? = null

        if (ProfileFragment::class.java.isAssignableFrom(cls)) {
            fragment = ProfileFragment()
        } else if (CourseBrowserFragment::class.java.isAssignableFrom(cls)) {
            fragment = CourseBrowserFragment.newInstance(canvasContext!!)
        } else if (CourseBrowserEmptyFragment::class.java.isAssignableFrom(cls)) {
            fragment = CourseBrowserEmptyFragment.newInstance((canvasContext as Course?)!!)
        } else if (DashboardFragment::class.java.isAssignableFrom(cls)) {
            fragment = DashboardFragment.getInstance()
        } else if (EditDashboardFragment::class.java.isAssignableFrom(cls)) {
            fragment = EditDashboardFragment.newInstance(route)
        } else if (AssignmentListFragment::class.java.isAssignableFrom(cls)) {
            fragment = AssignmentListFragment.getInstance(canvasContext!!, route.arguments)
        } else if (AssignmentDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = getAssignmentDetailsFragment(canvasContext, route)
        } else if (DueDatesFragment::class.java.isAssignableFrom(cls)) {
            fragment = DueDatesFragment.getInstance((canvasContext as Course?)!!, route.arguments)
        } else if (SubmissionListFragment::class.java.isAssignableFrom(cls)) {
            fragment = SubmissionListFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else if (PostPolicyFragment::class.java.isAssignableFrom(cls)) {
            fragment = PostPolicyFragment.newInstance(route.argsWithContext)
        } else if (EditAssignmentDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = EditAssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else if (AssigneeListFragment::class.java.isAssignableFrom(cls)) {
            fragment = AssigneeListFragment.newInstance(route.arguments)
        } else if (CourseSettingsFragment::class.java.isAssignableFrom(cls)) {
            fragment = CourseSettingsFragment.newInstance((canvasContext as Course?)!!)
        } else if (QuizListFragment::class.java.isAssignableFrom(cls)) {
            fragment = QuizListFragment.newInstance(canvasContext!!)
        } else if (ModuleListFragment::class.java.isAssignableFrom(cls)) {
            fragment = getModuleListFragment(canvasContext, route)
        } else if (QuizDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = getQuizDetailsFragment(canvasContext, route)
        } else if (EditQuizDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = EditQuizDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else if (QuizPreviewWebviewFragment::class.java.isAssignableFrom(cls)) {
            fragment = QuizPreviewWebviewFragment.newInstance(route.arguments)
        } else if (EditQuizDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = EditQuizDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else if (AnnouncementListFragment::class.java.isAssignableFrom(cls)) {
            // This needs to be above DiscussionsListFragment because it extends it
            fragment = AnnouncementListFragment.newInstance(canvasContext!!)
        } else if (DiscussionsListFragment::class.java.isAssignableFrom(cls)) {
            fragment = DiscussionsListFragment.newInstance(canvasContext!!)
        } else if (DiscussionRouterFragment::class.java.isAssignableFrom(cls)) {
            fragment = DiscussionRouterFragment.newInstance(canvasContext!!, route)
        } else if(DiscussionDetailsWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = DiscussionDetailsWebViewFragment.newInstance(route)
        } else if(CreateDiscussionWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = CreateDiscussionWebViewFragment.newInstance(route)
        }  else if (InboxFragment::class.java.isAssignableFrom(cls)) {
            fragment = InboxFragment.newInstance(route)
        } else if (InboxComposeFragment::class.java.isAssignableFrom(cls)) {
            fragment = InboxComposeFragment.newInstance(route)
        } else if (InboxDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = InboxDetailsFragment.newInstance(route)
        } else if (ViewPdfFragment::class.java.isAssignableFrom(cls)) {
            fragment = ViewPdfFragment.newInstance(route.arguments)
        } else if (ViewImageFragment::class.java.isAssignableFrom(cls)) {
            fragment = ViewImageFragment.newInstance(route.arguments)
        } else if (ViewMediaFragment::class.java.isAssignableFrom(cls)) {
            fragment = ViewMediaFragment.newInstance(route.arguments)
        } else if (ViewHtmlFragment::class.java.isAssignableFrom(cls)) {
            fragment = ViewHtmlFragment.newInstance(route.arguments)
        } else if (ViewUnsupportedFileFragment::class.java.isAssignableFrom(cls)) {
            fragment = ViewUnsupportedFileFragment.newInstance(route.arguments)
        } else if (ChooseRecipientsFragment::class.java.isAssignableFrom(cls)) {
            fragment = ChooseRecipientsFragment.newInstance(route.arguments)
        } else if (SpeedGraderQuizWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = SpeedGraderQuizWebViewFragment.newInstance(route.arguments)
        } else if (AnnotationCommentListFragment::class.java.isAssignableFrom(cls)) {
            fragment = AnnotationCommentListFragment.newInstance(route.arguments)
        } else if (SettingsFragment::class.java.isAssignableFrom(cls)) {
            fragment = SettingsFragment.newInstance(route)
        } else if (ProfileEditFragment::class.java.isAssignableFrom(cls)) {
            fragment = ProfileEditFragment.newInstance(route.arguments)
        } else if (FeatureFlagsFragment::class.java.isAssignableFrom(cls)) {
            fragment = FeatureFlagsFragment()
        } else if (PushNotificationPreferencesFragment::class.java.isAssignableFrom(cls)) {
            fragment = PushNotificationPreferencesFragment()
        } else if (EmailNotificationPreferencesFragment::class.java.isAssignableFrom(cls)) {
            fragment = EmailNotificationPreferencesFragment()
        } else if (RemoteConfigParamsFragment::class.java.isAssignableFrom(cls)) {
            fragment = RemoteConfigParamsFragment()
        } else if (LtiLaunchFragment::class.java.isAssignableFrom(cls)) {
            fragment = LtiLaunchFragment.newInstance(route)
        } else if (PeopleListFragment::class.java.isAssignableFrom(cls)) {
            fragment = PeopleListFragment.newInstance(canvasContext!!)
        } else if (StudentContextFragment::class.java.isAssignableFrom(cls)) {
            fragment = StudentContextFragment.newInstance(route.arguments)
        } else if (AttendanceListFragment::class.java.isAssignableFrom(cls)) {
            fragment = AttendanceListFragment.newInstance(canvasContext!!, route.arguments)
        } else if (FileListFragment::class.java.isAssignableFrom(cls)) {
            fragment = FileListFragment.newInstance(canvasContext ?: route.canvasContext!!, route.arguments)
        } else if (FileSearchFragment::class.java.isAssignableFrom(cls)) {
            fragment = FileSearchFragment.newInstance(canvasContext ?: route.canvasContext!!)
        } else if (PageListFragment::class.java.isAssignableFrom(cls)) {
            fragment = PageListFragment.newInstance(canvasContext!!)
        } else if (PageDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = getPageDetailsFragment(canvasContext, route)
        } else if (EditFileFolderFragment::class.java.isAssignableFrom(cls)) {
            fragment = EditFileFolderFragment.newInstance(route.arguments)
        } else if (CreateOrEditPageDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = CreateOrEditPageDetailsFragment.newInstance(route.arguments)
        } else if (SyllabusFragment::class.java.isAssignableFrom(cls)) {
            fragment = SyllabusFragment.newInstance(canvasContext ?: route.canvasContext)
        } else if (EditSyllabusFragment::class.java.isAssignableFrom(cls)) {
            fragment = EditSyllabusFragment.newInstance(route.arguments)
        } else if (EventFragment::class.java.isAssignableFrom(cls)) {
            fragment = EventFragment.newInstance(route)
        } else if (ToDoFragment::class.java.isAssignableFrom(cls)) {
            fragment = ToDoFragment.newInstance(route)
        } else if (CreateUpdateToDoFragment::class.java.isAssignableFrom(cls)) {
            fragment = CreateUpdateToDoFragment.newInstance(route)
        } else if (CreateUpdateEventFragment::class.java.isAssignableFrom(cls)) {
            fragment = CreateUpdateEventFragment.newInstance(route)
        } else if (InboxSignatureFragment::class.java.isAssignableFrom(cls)) {
            fragment = InboxSignatureFragment()
        } else if (ModuleProgressionFragment::class.java.isAssignableFrom(cls)) {
            fragment = ModuleProgressionFragment.newInstance(route.copy(canvasContext = canvasContext))
        } else if (FullscreenInternalWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = FullscreenInternalWebViewFragment.newInstance(route.arguments)
        } else if (InternalWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = InternalWebViewFragment.newInstance(route.arguments)
        } else if (HtmlContentFragment::class.java.isAssignableFrom(cls)) {
            fragment = HtmlContentFragment.newInstance(route.arguments)
        }//NOTE: These should remain at or near the bottom to give fragments that extend InternalWebViewFragment the chance first

        return fragment as Type?
    }

    private fun getAssignmentDetailsFragment(canvasContext: CanvasContext?, route: Route): AssignmentDetailsFragment {
        return if (route.arguments.containsKey(AssignmentDetailsFragment.ASSIGNMENT)
            || route.arguments.containsKey(AssignmentDetailsFragment.ASSIGNMENT_ID)) {
            AssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else {
            //parse the route to get the assignment id
            val assignmentId = java.lang.Long.parseLong(route.paramsHash[RouterParams.ASSIGNMENT_ID]!!)
            val args = AssignmentDetailsFragment.makeBundle(assignmentId)
            AssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, args)
        }
    }

    private fun getModuleListFragment(canvasContext: CanvasContext?, route: Route): ModuleListFragment {
        return if (route.arguments.containsKey(Const.COURSE)) {
            ModuleListFragment.newInstance(route.arguments)
        } else {
            val moduleId = route.paramsHash[RouterParams.MODULE_ID]?.let { java.lang.Long.parseLong(it) }
            val args = ModuleListFragment.makeBundle(canvasContext as Course, moduleId)
            ModuleListFragment.newInstance(args)
        }
    }

    private fun getQuizDetailsFragment(canvasContext: CanvasContext?, route: Route): Fragment {
        return if (route.arguments.containsKey(QuizDetailsFragment.QUIZ)) {
            QuizDetailsFragment.newInstance(canvasContext as Course, route.arguments)
        } else {
            // Parse the route to get the quiz id
            var quizId = route.paramsHash[RouterParams.QUIZ_ID]?.toLongOrNull()
            // If we're routing from an assignment list to a quiz details fragment we'll have this quiz id
            if (quizId == null && route.arguments.containsKey(QuizDetailsFragment.QUIZ_ID)) {
                quizId = route.arguments[QuizDetailsFragment.QUIZ_ID] as Long
            }
            if (quizId != null) {
                val args = QuizDetailsFragment.makeBundle(quizId)
                QuizDetailsFragment.newInstance(canvasContext as Course, args)
            } else {
                // Could not get quiz ID; route to quiz list instead
                QuizListFragment.newInstance(canvasContext!!)
            }
        }
    }

    private fun getPageDetailsFragment(canvasContext: CanvasContext?, route: Route): PageDetailsFragment {
        return if (route.arguments.containsKey(PageDetailsFragment.PAGE)
            || route.arguments.containsKey(PageDetailsFragment.PAGE_ID)) {
            PageDetailsFragment.newInstance(canvasContext!!, route.arguments)
        } else {
            //parse the route to get the page id
            val pageId = route.paramsHash[RouterParams.PAGE_ID]
            val args = PageDetailsFragment.makeBundle(pageId ?: "")
            PageDetailsFragment.newInstance(canvasContext!!, args)
        }
    }
}
