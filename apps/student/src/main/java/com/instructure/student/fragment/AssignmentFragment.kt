/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.ReflectField
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.activity.BaseRouterActivity
import kotlinx.android.synthetic.main.fragment_assignment.*
import kotlinx.android.synthetic.main.fragment_old_assignment_details.*
import retrofit2.Call
import retrofit2.Response
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.util.*

class AssignmentFragment : ParentFragment(), SubmissionDetailsFragment.SubmissionDetailsFragmentCallback, Bookmarkable {

    // Bundle Args
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var assignment by NullableParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    private var assignmentId: Long by LongArg(key = Const.ASSIGNMENT_ID)
    private var currentTab by IntArg(default = ASSIGNMENT_TAB_DETAILS, key = Const.TAB_ID)
    private var message: String by StringArg(key = Const.MESSAGE)

    private var fragmentPagerAdapter: FragmentPagerDetailAdapter? = null

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) = viewPager.setCurrentItem(tab.position, true)
        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    private val assignmentDetailsFragment: OldAssignmentDetailsFragment?
        get() = if (fragmentPagerAdapter == null) {
            null
        } else fragmentPagerAdapter!!.getRegisteredFragment(ASSIGNMENT_TAB_DETAILS) as OldAssignmentDetailsFragment?

    private val submissionDetailsFragment: SubmissionDetailsFragment?
        get() = if (fragmentPagerAdapter == null) {
            null
        } else fragmentPagerAdapter!!.getRegisteredFragment(ASSIGNMENT_TAB_SUBMISSION) as SubmissionDetailsFragment?

    private val assignmentCallback = object : StatusCallback<Assignment>() {
        override fun onResponse(response: Response<Assignment>, linkHeaders: LinkHeaders, type: ApiType) {
            if (!isAdded) return

            response.body().let {
                assignment = it
                populateFragments(assignment, true, APIHelper.isCachedResponse(response))
                assignmentDetailsFragment?.setAssignmentWithNotification(it, message)
                toolbar.title = title()
            }
        }

        override fun onFail(call: Call<Assignment>?, error: Throwable, response: Response<*>?) {
            // Unable to retrieve the assignment, likely was deleted at some point
            Toast.makeText(requireContext(), R.string.assignmentDeletedError, Toast.LENGTH_SHORT).show()
            navigation?.popCurrentFragment()
        }
    }

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // DO NOT USE setRetainInstance. It breaks the FragmentStatePagerAdapter.
        // The error is "Attempt to invoke virtual method 'int java.util.ArrayList.size()' on a null object reference"
        // setRetainInstance(this, true);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_assignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager.offscreenPageLimit = 2
        viewPager.isSaveFromParentEnabled = false // Prevents a crash with FragmentStatePagerAdapter, when the EditAssignmentFragment is dismissed
        fragmentPagerAdapter = FragmentPagerDetailAdapter(childFragmentManager, false)
        viewPager.adapter = fragmentPagerAdapter
        setupTabLayoutColors()
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.tabMode = if (!isTablet && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            TabLayout.MODE_SCROLLABLE else TabLayout.MODE_FIXED
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(tabSelectedListener)

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt(Const.TAB_ID, 0)
        }
        // currentTab can either be save on orientation change or handleIntentExtras (when someone opens a link from an email)
        viewPager.currentItem = currentTab
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AssignmentManager.getAssignment(assignmentId, canvasContext.id, true, assignmentCallback)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        tabLayout.tabMode = if (!isTablet && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            TabLayout.MODE_SCROLLABLE else TabLayout.MODE_FIXED
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Const.ASSIGNMENT, assignment)
        if (viewPager != null) {
            outState.putInt(Const.TAB_ID, viewPager.currentItem)
            currentTab = viewPager.currentItem
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        assignmentCallback.cancel()
    }
    //endregion

    //region Fragment Overrides
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        // Propagate userVisibleHint state to pager adapter for PageView tracking
        fragmentPagerAdapter?.setUserVisibleHint(isVisibleToUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == RequestCodes.EDIT_ASSIGNMENT && resultCode == Activity.RESULT_OK && intent != null) {
            if (intent.hasExtra(Const.ASSIGNMENT)) {
                this.assignment = intent.getParcelableExtra(Const.ASSIGNMENT)
                assignmentDetailsFragment?.setAssignmentWithNotification(assignment, message)
            }
        } else if(submissionDetailsFragment != null) {
            submissionDetailsFragment?.onActivityResult(requestCode, resultCode, intent)
        }
    }
    //endregion

    //region Parent Fragment Overrides
    override fun handleBackPressed(): Boolean {
        return if (assignmentDetailsFragment != null) {
            // Handles closing of fullscreen video (<sarcasm> Yay! nested fragments </sarcasm>)
            assignmentDetailsFragment!!.handleBackPressed()
        } else false
    }

    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = if (assignment != null) assignment!!.name!! else getString(R.string.assignment)

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    //endregion

    // Navigation is a course, but isn't in notification list.
    override val bookmark: Bookmarker
        get() = Bookmarker(canvasContext.isCourseOrGroup && navigation?.currentFragment !is NotificationListFragment, canvasContext, assignment?.htmlUrl)
                .withParam(RouterParams.ASSIGNMENT_ID, assignmentId.toString())

    //region Setup
    private fun populateFragments(assignment: Assignment?, isWithinAnotherCallback: Boolean, isCached: Boolean) {
        fragmentPagerAdapter ?: return

        if (assignment?.isLocked == true) {
            // Recreate the adapter, because of slidingTabLayout's assumption that viewpager won't change size.
            fragmentPagerAdapter = FragmentPagerDetailAdapter(childFragmentManager, true)
            viewPager.adapter = fragmentPagerAdapter
            applyLockedImage()
        }

        if(assignment != null) {
            (0 until NUMBER_OF_TABS)
                    .mapNotNull { fragmentPagerAdapter!!.getRegisteredFragment(it) }
                    .forEach {
                        when (it) {
                            is OldAssignmentDetailsFragment -> it.setupAssignment(assignment)
                            is SubmissionDetailsFragment -> {
                                it.setAssignmentFragment(WeakReference(this))
                                it.setAssignment(assignment, isWithinAnotherCallback, isCached)
                                it.setSubmissionDetailsFragmentCallback(this)
                            }
                            is RubricFragment -> it.setAssignment(assignment)
                        }
                    }
        }
    }

    private fun applyLockedImage() {
        emptyView.emptyViewImage?.setImageResource(R.drawable.vd_panda_locked)
        emptyView.setTitleText(R.string.locked)
        val unlockDate = assignment?.unlockDate
        if (unlockDate != null) {
            val dateString = DateFormat.getDateInstance().format(unlockDate)
            val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(unlockDate)
            emptyView.setMessageText(getString(R.string.lockedSubtext, dateString, timeString))
        } else {
            val name = assignment?.lockInfo?.lockedModuleName
            emptyView.setMessageText(getString(R.string.lockedModule, name))
        }
        emptyView.setListEmpty()
        canvasWebView?.setGone()
    }

    private fun setupTabLayoutColors() {
        val color = ColorKeeper.getOrGenerateColor(canvasContext)
        tabLayout.setBackgroundColor(color)
        tabLayout.setTabTextColors(ContextCompat.getColor(requireContext(), R.color.glassWhite), Color.WHITE)
    }
    //endregion

    override fun updateSubmissionDate(submissionDate: Date) {
        assignmentDetailsFragment?.updateSubmissionDate(submissionDate)
    }

    internal inner class FragmentPagerDetailAdapter(fm: FragmentManager, private val isLocked: Boolean) : FragmentStatePagerAdapter(fm) {
        // http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
        private var registeredFragments: SparseArray<WeakReference<Fragment>> = SparseArray()

        // region Modifications for PageView tracking sanity

        private var currentPrimaryItem by ReflectField<Fragment?>("mCurrentPrimaryItem", FragmentStatePagerAdapter::class.java)

        private var isVisible = parentFragment == null

        fun setUserVisibleHint(isVisibleToUser: Boolean) {
            if (!isVisibleToUser) {
                (0 until registeredFragments.size())
                    .mapNotNull { registeredFragments[it].get() }
                    .forEach { it.userVisibleHint = false }
            } else {
                currentPrimaryItem?.setMenuVisibility(true)
                currentPrimaryItem?.userVisibleHint = true
            }
            isVisible = isVisibleToUser
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            val fragment = `object` as? Fragment
            if (fragment !== currentPrimaryItem) {
                if (currentPrimaryItem != null) {
                    currentPrimaryItem?.setMenuVisibility(false)
                    currentPrimaryItem?.userVisibleHint = false
                }
                if (fragment != null && isVisible) {
                    fragment.setMenuVisibility(true)
                    fragment.userVisibleHint = true
                }
                currentPrimaryItem = fragment
            }
        }

        // endregion

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            registeredFragments.put(position, WeakReference(fragment))
            if (!isVisible) fragment.userVisibleHint = false
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
            registeredFragments.remove(position)
            super.destroyItem(container, position, item)
        }

        fun getRegisteredFragment(position: Int): Fragment? {
            val weakReference = registeredFragments.get(position)
            return weakReference?.get()
        }

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                ASSIGNMENT_TAB_DETAILS -> OldAssignmentDetailsFragment.newInstance(OldAssignmentDetailsFragment.makeRoute(canvasContext))
                ASSIGNMENT_TAB_SUBMISSION -> SubmissionDetailsFragment.newInstance(SubmissionDetailsFragment.makeRoute(canvasContext))
                ASSIGNMENT_TAB_GRADE -> RubricFragment.newInstance(RubricFragment.makeRoute(canvasContext))
                else -> OldAssignmentDetailsFragment.newInstance(OldAssignmentDetailsFragment.makeRoute(canvasContext))
            }
        }

        override fun getCount(): Int = if (isLocked) 1 else NUMBER_OF_TABS

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                ASSIGNMENT_TAB_DETAILS -> if (isLocked) getString(R.string.assignmentLocked) else getString(OldAssignmentDetailsFragment.tabTitle)
                ASSIGNMENT_TAB_SUBMISSION -> getString(SubmissionDetailsFragment.getTabTitle())
                ASSIGNMENT_TAB_GRADE -> getString(RubricFragment.tabTitle)
                else -> getString(OldAssignmentDetailsFragment.tabTitle)
            }
        }
    }

    companion object {
        const val ASSIGNMENT_TAB_DETAILS = 0
        const val ASSIGNMENT_TAB_SUBMISSION = 1
        const val ASSIGNMENT_TAB_GRADE = 2
        const val NUMBER_OF_TABS = 3

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, assignment: Assignment, tabId: Int): Route {
            return Route(null, AssignmentFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                putParcelable(Const.ASSIGNMENT, assignment)
                putLong(Const.ASSIGNMENT_ID, assignment.id)
                putInt(Const.TAB_ID, tabId)
            }))
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, assignment: Assignment): Route {
            return Route(null, AssignmentFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply { putParcelable(Const.ASSIGNMENT, assignment) }))
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, assignmentId: Long): Route {
            return Route(null, AssignmentFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply { putLong(Const.ASSIGNMENT_ID, assignmentId) }))
        }

        @JvmStatic
        fun makeRoute(context: Context, canvasContext: CanvasContext, assignmentId: Long, item: StreamItem?): Route {
            return Route(null, AssignmentFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                if(item != null) putString(Const.MESSAGE, item.getMessage(context))
            }))
        }

        @JvmStatic
        fun newInstance(route: Route) : AssignmentFragment? {
            return if(validRoute(route)) AssignmentFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)
                canvasContext = route.canvasContext!!

                assignment?.let { assignmentId = it.id }

                if (route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID)) {
                    assignmentId = route.paramsHash[RouterParams.ASSIGNMENT_ID]?.toLong() ?: -1

                    currentTab = when (route.paramsHash[RouterParams.SLIDING_TAB_TYPE]) {
                        BaseRouterActivity.SUBMISSIONS_ROUTE -> ASSIGNMENT_TAB_SUBMISSION
                        BaseRouterActivity.RUBRIC_ROUTE -> ASSIGNMENT_TAB_GRADE
                        else -> ASSIGNMENT_TAB_DETAILS
                    }
                }
            } else null
        }

        @JvmStatic
        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null &&
                    (route.arguments.containsKey(Const.ASSIGNMENT) ||
                            route.arguments.containsKey(Const.ASSIGNMENT_ID) ||
                            route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID))
        }
    }
}
