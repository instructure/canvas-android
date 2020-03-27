package com.instructure.teacher.router

import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.teacher.PSPDFKit.AnnotationComments.AnnotationCommentListFragment
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.features.files.search.FileSearchFragment
import com.instructure.teacher.features.modules.list.ui.ModuleListFragment
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.fragments.*
import instructure.rceditor.RCEFragment

object RouteResolver {

    /**
     * Pass in a route and a course, get a fragment back!
     */
    @JvmStatic
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

    @JvmStatic
    fun getMasterFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        return getFrag(route.primaryClass, canvasContext, route)
    }

    @JvmStatic
    fun getDetailFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        return getFrag(route.secondaryClass, canvasContext, route)
    }

    @JvmStatic
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
        } else if (CoursesFragment::class.java.isAssignableFrom(cls)) {
            fragment = CoursesFragment.getInstance()
        } else if (AssignmentListFragment::class.java.isAssignableFrom(cls)) {
            fragment = AssignmentListFragment.getInstance(canvasContext!!, route.arguments)
        } else if (AssignmentDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = getAssignmentDetailsFragment(canvasContext, route)
        } else if (DueDatesFragment::class.java.isAssignableFrom(cls)) {
            fragment = DueDatesFragment.getInstance((canvasContext as Course?)!!, route.arguments)
        } else if (AssignmentSubmissionListFragment::class.java.isAssignableFrom(cls)) {
            fragment = AssignmentSubmissionListFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else if (PostPolicyFragment::class.java.isAssignableFrom(cls)) {
            fragment = PostPolicyFragment.newInstance(route.argsWithContext)
        } else if (EditAssignmentDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = EditAssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else if (AssigneeListFragment::class.java.isAssignableFrom(cls)) {
            fragment = AssigneeListFragment.newInstance(route.arguments)
        } else if (EditFavoritesFragment::class.java.isAssignableFrom(cls)) {
            fragment = EditFavoritesFragment.newInstance(route.arguments)
        } else if (CourseSettingsFragment::class.java.isAssignableFrom(cls)) {
            fragment = CourseSettingsFragment.newInstance((canvasContext as Course?)!!)
        } else if (QuizListFragment::class.java.isAssignableFrom(cls)) {
            fragment = QuizListFragment.newInstance(canvasContext!!)
        } else if (ModuleListFragment::class.java.isAssignableFrom(cls)) {
            fragment = ModuleListFragment.newInstance(route.arguments)
        } else if (QuizDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = getQuizDetailsFragment(canvasContext, route)
        } else if (RCEFragment::class.java.isAssignableFrom(cls)) {
            fragment = RCEFragment.newInstance(route.arguments)
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
        } else if (DiscussionsDetailsFragment::class.java.isAssignableFrom(cls)) {
            fragment = getDiscussionDetailsFragment(canvasContext, route)
        } else if (InboxFragment::class.java.isAssignableFrom(cls)) {
            fragment = InboxFragment()
        } else if (AddMessageFragment::class.java.isAssignableFrom(cls)) {
            fragment = AddMessageFragment.newInstance(route.arguments)
        } else if (MessageThreadFragment::class.java.isAssignableFrom(cls)) {
            fragment = getMessageThreadFragment(route)
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
        } else if (cls.isAssignableFrom(DiscussionsReplyFragment::class.java)) {
            fragment = DiscussionsReplyFragment.newInstance(canvasContext!!, route.arguments)
        } else if (cls.isAssignableFrom(DiscussionsUpdateFragment::class.java)) {
            fragment = DiscussionsUpdateFragment.newInstance(canvasContext!!, route.arguments)
        } else if (ChooseRecipientsFragment::class.java.isAssignableFrom(cls)) {
            fragment = ChooseRecipientsFragment.newInstance(route.arguments)
        } else if (SpeedGraderQuizWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = SpeedGraderQuizWebViewFragment.newInstance(route.arguments)
        } else if (AnnotationCommentListFragment::class.java.isAssignableFrom(cls)) {
            fragment = AnnotationCommentListFragment.newInstance(route.arguments)
        } else if (CreateDiscussionFragment::class.java.isAssignableFrom(cls)) {
            fragment = CreateDiscussionFragment.newInstance(route.arguments)
        } else if (CreateOrEditAnnouncementFragment::class.java.isAssignableFrom(cls)) {
            fragment = CreateOrEditAnnouncementFragment.newInstance(route.arguments)
        } else if (SettingsFragment::class.java.isAssignableFrom(cls)) {
            fragment = SettingsFragment.newInstance(route.arguments)
        } else if (ProfileEditFragment::class.java.isAssignableFrom(cls)) {
            fragment = ProfileEditFragment.newInstance(route.arguments)
        } else if (FeatureFlagsFragment::class.java.isAssignableFrom(cls)) {
            fragment = FeatureFlagsFragment()
        } else if (RemoteConfigParamsFragment::class.java.isAssignableFrom(cls)) {
            fragment = RemoteConfigParamsFragment()
        } else if (LTIWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = LTIWebViewFragment.newInstance(route.arguments)
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
        } else if (FullscreenInternalWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = FullscreenInternalWebViewFragment.newInstance(route.arguments)
        } else if (InternalWebViewFragment::class.java.isAssignableFrom(cls)) {
            fragment = InternalWebViewFragment.newInstance(route.arguments)
        }//NOTE: These should remain at or near the bottom to give fragments that extend InternalWebViewFragment the chance first

        return fragment as Type?
    }

    private fun getMessageThreadFragment(route: Route): Fragment? {
        return if (route.paramsHash.containsKey(Const.CONVERSATION_ID)) {
            val args = MessageThreadFragment.createBundle(route.paramsHash[Const.CONVERSATION_ID]?.toLong()
                    ?: 0L)
            MessageThreadFragment.newInstance(args)
        } else {
            MessageThreadFragment.newInstance(route.arguments)
        }
    }

    private fun getAssignmentDetailsFragment(canvasContext: CanvasContext?, route: Route): AssignmentDetailsFragment {
        return if (route.arguments.containsKey(AssignmentDetailsFragment.ASSIGNMENT)
            || route.arguments.containsKey(AssignmentDetailsFragment.ASSIGNMENT_ID)) {
            AssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else {
            //parse the route to get the assignment id
            val assignmentId = java.lang.Long.parseLong(route.paramsHash[RouterParams.ASSIGNMENT_ID])
            val args = AssignmentDetailsFragment.makeBundle(assignmentId)
            AssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, args)
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

    private fun getDiscussionDetailsFragment(canvasContext: CanvasContext?, route: Route): DiscussionsDetailsFragment {
        return when {
            route.arguments.containsKey(DiscussionsDetailsFragment.DISCUSSION_TOPIC_HEADER) -> DiscussionsDetailsFragment.newInstance(canvasContext!!, route.arguments)
            route.arguments.containsKey(DiscussionsDetailsFragment.DISCUSSION_TOPIC_HEADER_ID) -> {
                val discussionTopicHeaderId = route.arguments.getLong(DiscussionsDetailsFragment.DISCUSSION_TOPIC_HEADER_ID)
                val args = DiscussionsDetailsFragment.makeBundle(discussionTopicHeaderId)
                DiscussionsDetailsFragment.newInstance(canvasContext!!, args)
            }
            else -> {
                //parse the route to get the discussion id
                val discussionTopicHeaderId = route.paramsHash[RouterParams.MESSAGE_ID]?.toLong()
                        ?: 0L
                val entryId = route.queryParamsHash[RouterParams.ENTRY_ID]?.toLong() ?: 0L
                val args = DiscussionsDetailsFragment.makeBundle(discussionTopicHeaderId, entryId)
                DiscussionsDetailsFragment.newInstance(canvasContext!!, args)
            }
        }
    }
}
