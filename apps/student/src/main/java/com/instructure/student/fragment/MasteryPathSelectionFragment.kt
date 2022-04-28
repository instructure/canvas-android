/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.MasteryPath
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_MASTERY_PATH_SELECTION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import kotlinx.android.synthetic.main.fragment_assignment.*
import java.lang.ref.WeakReference
import java.util.*

@ScreenView(SCREEN_VIEW_MASTERY_PATH_SELECTION)
class MasteryPathSelectionFragment : ParentFragment() {

    // Bundle Args
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var masteryPath: MasteryPath by ParcelableArg(key = MASTERY_PATH)
    private var moduleObjectId: Long by LongArg(key = MODULE_OBJECT_ID)
    private var moduleItemId: Long by LongArg(key = MODULE_ITEM_ID)

    private var currentTab = 0

    // fragments
    private var fragmentPagerAdapter: FragmentPagerDetailAdapter? = null

    // model variables

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager.setCurrentItem(tab.position, true)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}

        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // DO NOT USE setRetainInstance. It breaks the FragmentStatePagerAdapter.
        // The error is "Attempt to invoke virtual method 'int java.util.ArrayList.size()' on a null object reference"
        // setRetainInstance(this, true);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_assignment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager.offscreenPageLimit = 2
        viewPager.isSaveFromParentEnabled = false // Prevents a crash with FragmentStatePagerAdapter
        fragmentPagerAdapter = FragmentPagerDetailAdapter(childFragmentManager)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupTabLayoutColors()

        with(viewPager) {
            adapter = fragmentPagerAdapter
            addOnPageChangeListener(com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }

        with(tabLayout) {
            tabMode = if (!isTablet && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                com.google.android.material.tabs.TabLayout.MODE_SCROLLABLE else com.google.android.material.tabs.TabLayout.MODE_FIXED
            setupWithViewPager(viewPager)
            addOnTabSelectedListener(tabSelectedListener)
        }

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt(Const.TAB_ID, 0)
        }

        // currentTab can either be save on orientation change or handleIntentExtras (when someone opens a link from an email)
        viewPager.currentItem = currentTab
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        tabLayout.tabMode = if (!isTablet && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            TabLayout.MODE_SCROLLABLE else TabLayout.MODE_FIXED
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
        outState.putParcelable(Const.MASTERY_PATH, masteryPath)

        if (viewPager != null) {
            outState.putInt(Const.TAB_ID, viewPager.currentItem)
            currentTab = viewPager!!.currentItem
        }
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = getString(R.string.choose_assignment_group)

    override fun applyTheme() {
        toolbar.let {
            it.title = getString(R.string.chooseAssignmentPath)
            it.setupAsBackButton(this)
            ViewStyler.themeToolbarColored(requireActivity(), it, canvasContext)
        }
    }

    //endregion

    //region Setup
    private fun setupTabLayoutColors() {
        val color = ColorKeeper.getOrGenerateColor(canvasContext)
        tabLayout.setBackgroundColor(color)
        tabLayout.setTabTextColors(ContextCompat.getColor(requireContext(), R.color.transparentWhite), Color.WHITE)
    }
    //endregion

    //region Adapter

    internal inner class FragmentPagerDetailAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        // http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
        var registeredFragments: SparseArray<WeakReference<Fragment>> = SparseArray()

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            registeredFragments.put(position, WeakReference(fragment))
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            registeredFragments.remove(position)
            super.destroyItem(container, position, `object`)
        }

        override fun getItem(position: Int): Fragment {
            val assignmentSet = masteryPath.assignmentSets!![position]
            val assignments = assignmentSet!!.assignments
            val route = MasteryPathOptionsFragment.makeRoute(canvasContext, assignments, assignmentSet, moduleObjectId, moduleItemId)
            return MasteryPathOptionsFragment.newInstance(route)
                ?: throw IllegalStateException("MasteryPathOptionsFragment is null!")
        }

        override fun getCount(): Int = masteryPath.assignmentSets!!.size

        override fun getPageTitle(position: Int): CharSequence = String.format(Locale.getDefault(), getString(R.string.choice_position), position + 1)
    }

    //endregion

    companion object {
        const val MASTERY_PATH = "mastery_path"
        const val MODULE_OBJECT_ID = "module_object_id"
        const val MODULE_ITEM_ID = "module_item_ud"

        fun makeRoute(canvasContext: CanvasContext, masteryPath: MasteryPath, moduleObjectId: Long, moduleItemId: Long): Route {
            val bundle = Bundle().apply {
                putParcelable(MASTERY_PATH, masteryPath)
                putLong(MODULE_OBJECT_ID, moduleObjectId)
                putLong(MODULE_ITEM_ID, moduleItemId)
            }
            return Route(MasteryPathSelectionFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) {
            MasteryPathSelectionFragment().withArgs(route.argsWithContext)
        } else null

        private fun validRoute(route: Route) = route.arguments.containsKey(MASTERY_PATH) &&
                route.arguments.containsKey(MODULE_OBJECT_ID) &&
                route.arguments.containsKey(MODULE_ITEM_ID)
    }
}
