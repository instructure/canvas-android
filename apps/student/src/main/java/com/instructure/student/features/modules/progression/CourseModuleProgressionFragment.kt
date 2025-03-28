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

package com.instructure.student.features.modules.progression

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.ModuleObject.State
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.isLocked
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_COURSE_MODULE_PROGRESSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelper
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.IntArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ParcelableArrayListArg
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.CourseModuleProgressionBinding
import com.instructure.student.events.ModuleUpdatedEvent
import com.instructure.student.events.post
import com.instructure.student.features.files.details.FileDetailsFragment
import com.instructure.student.features.modules.list.ModuleListFragment
import com.instructure.student.features.modules.util.ModuleProgressionUtility
import com.instructure.student.features.modules.util.ModuleUtility
import com.instructure.student.features.pages.details.PageDetailsFragment
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.Const
import com.instructure.student.util.CourseModulesStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@PageView(url = "courses/{canvasContext}/modules")
@ScreenView(SCREEN_VIEW_COURSE_MODULE_PROGRESSION)
@AndroidEntryPoint
class CourseModuleProgressionFragment : ParentFragment(), Bookmarkable {

    private val binding by viewBinding(CourseModuleProgressionBinding::bind)

    @Inject
    lateinit var discussionRouteHelper: DiscussionRouteHelper

    @Inject
    lateinit var repository: ModuleProgressionRepository

    private var moduleItemsJob: Job? = null
    private var markAsReadJob: WeaveJob? = null

    // Bundle Args
    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var groupPos: Int by IntArg(key = GROUP_POSITION)
    private var childPos: Int by IntArg(key = CHILD_POSITION)
    private var modules: ArrayList<ModuleObject> by ParcelableArrayListArg(key = MODULE_OBJECTS)
    private var items: ArrayList<ArrayList<ModuleItem>?> by SerializableArg(key = MODULE_ITEMS, default = ArrayList())
    private var assetId: String by StringArg(key = ASSET_ID)
    private var assetType: String by StringArg(key = ASSET_TYPE, default = ModuleItemAsset.MODULE_ITEM.assetType)
    // This is used in offline cases when the modules are not synced, but we have links with moduleId. This will never be module item asset type.
    private var secondaryAssetType: String? by NullableStringArg(key = SECONDARY_ASSET_TYPE)
    private var route: Route by ParcelableArg(key = ROUTE)
    private var navigatedFromModules: Boolean by BooleanArg(key = NAVIGATED_FROM_MODULES)

    // Default number will get reset
    private var itemsCount = 3

    // There's a case where we try to get the previous module and the previous module has a paginated list
    // of items.  A task will get those items and populate them in the background, but it throws off the
    // indexes because it adds the items to (possibly) the middle of the arrayList that backs the adapter.
    // The same case will happen if we don't have any information about the previous module.
    // This will keep track of where we need to be.
    private var currentPos = 0

    private var snycedTabs = emptySet<String>()
    private var syncedFileIds = emptyList<Long>()
    private var isOfflineEnabled = false

    val tabId: String
        get() = Tab.MODULES_ID

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.course_module_progression, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.prevItem.setImageDrawable(ColorKeeper.getColoredDrawable(requireActivity(), R.drawable.ic_chevron_left, canvasContext.color))
        binding.nextItem.setImageDrawable(ColorKeeper.getColoredDrawable(requireActivity(), R.drawable.ic_chevron_right, canvasContext.color))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.launch {
            isOfflineEnabled = repository.isOfflineEnabled()
            snycedTabs = repository.getSyncedTabs(canvasContext.id)
            syncedFileIds = repository.getSyncedFileIds(canvasContext.id)
            loadModuleProgression(savedInstanceState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        markAsReadJob?.cancel()
    }

    //endregion

    //region Fragment Overrides
    // This function is mostly for the internal web view fragments so we can go back in the webview
    override fun handleBackPressed(): Boolean = with(binding) {
        if (items.isNotEmpty()) {
            val pFrag = childFragmentManager.fragments.firstOrNull() as? ParentFragment
            if (pFrag?.handleBackPressed() == true) {
                return true
            }
        }
        return super.handleBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //save the position that we're currently on
        outState.putInt(MODULE_POSITION, currentPos)
    }

    //endregion

    //region Fragment Interaction Overrides
    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), canvasContext.color)
    }

    override fun title(): String = getString(R.string.modules)

    //endregion

    override fun getSelectedParamName(): String = RouterParams.MODULE_ID

    //region Setup

    private val prevItemClickCallback = View.OnClickListener {
        setupPrevModuleName(currentPos)
        setupPreviousModule(getModuleItemGroup(currentPos))
        if (currentPos >= 1) {
            showFragment(getItem(--currentPos))
        }

        updateBottomNavBarButtons()
    }

    private val nextItemClickCallback = View.OnClickListener {
        setupNextModuleName(currentPos)
        setupNextModule(getModuleItemGroup(currentPos))
        if (currentPos < itemsCount - 1) {
            showFragment(getItem(++currentPos))
        }
        updateBottomNavBarButtons()
    }

    private fun setButtonListeners() {
        binding.prevItem.setOnClickListener(prevItemClickCallback)
        binding.nextItem.setOnClickListener(nextItemClickCallback)

        binding.markDoneButton.onClickWithRequireNetwork {
            val moduleItem = getModelObject()
            if (moduleItem?.completionRequirement != null) {
                lifecycleScope.launch {
                    if (moduleItem.completionRequirement!!.completed) {
                        val result = repository.markAsNotDone(canvasContext, moduleItem)
                        if (result.isSuccess) setMarkDone(moduleItem, false)
                    } else {
                        val result = repository.markAsDone(canvasContext, moduleItem)
                        if (result.isSuccess) setMarkDone(moduleItem, true)
                    }
                }
            }
        }
    }

    private fun setMarkDone(moduleItem: ModuleItem, markDone: Boolean) {
        val currentModuleItem = getModelObject()
        if (isAdded && moduleItem == currentModuleItem) {
            binding.markDoneCheckbox.isChecked = markDone
        }
        moduleItem.completionRequirement?.completed = markDone
        notifyOfItemChanged(moduleItem)
    }

    private fun setModuleName(name: String) {
        // Set the label at the bottom
        binding.moduleName.text = name
    }

    private fun setViewInfo(bundle: Bundle?) {
        // Figure out the total size so the adapter knows how many items it will have
        var size = 0
        for (i in items.indices) { size += items[i]?.size.orDefault() }
        itemsCount = size

        currentPos = if (bundle != null && bundle.containsKey(Const.MODULE_POSITION)) {
            bundle.getInt(Const.MODULE_POSITION)
        } else {
            // Figure out which position in the overall adapter based on group and child position
            getCurrentModuleItemPos(groupPos, childPos)
        }

        showFragment(getItem(currentPos))

        updatePrevNextButtons(currentPos)

        // Check the previous and next_item modules when we first get into the module progression
        // Check if there are previous module items that we can view
        setupPreviousModule(getModuleItemGroup(currentPos))

        // Check if there are any next_item module items that we can view
        setupNextModule(getModuleItemGroup(currentPos))

        // Set the label at the bottom
        try {
            setModuleName(modules[groupPos].name!!)
        } catch (e: IndexOutOfBoundsException) {
            setModuleName("")
        }

        // Add the locked icon if needed
        addLockedIconIfNeeded(modules, items, groupPos, childPos)

        updateModuleMarkDoneView(getCurrentModuleItem(currentPos))
    }

    private fun showFragment(item: Fragment?) {
        item?.let {
            childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, it).commitAllowingStateLoss()
            applyFragmentTheme(it)
        }
    }

    private fun applyFragmentTheme(fragment: Fragment) {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                (fragment as? FragmentInteractions)?.applyTheme()
                fragment.lifecycle.removeObserver(this)
            }
        })
    }
    //endregion

    //region View Helpers
    // The bottom navigation bar has prev_item and next_item arrows that sometimes show up and sometimes don't depending
    // on where the user is (on the first module item there isn't a prev_item button).  We also may need to update these
    // dynamically when we use an async task to get the next_item group of module items
    private fun updateBottomNavBarButtons() {
        // Make them visible by default
        binding.prevItem.setVisible()
        binding.nextItem.setVisible()

        updatePrevNextButtons(currentPos)

        val currentModuleItem = getCurrentModuleItem(currentPos) ?: return

        val completionRequirement = currentModuleItem.completionRequirement
        if (completionRequirement != null && modules[groupPos].sequentialProgress) {
            // Reload the sequential module object to update the subsequent items that may now be unlocked
            // The user has viewed the item, and may have completed the contribute/submit requirements for a
            // discussion/assignment.
            addLockedIconIfNeeded(modules, items, groupPos, childPos)

            // Mark the item as viewed
            markAsRead(currentModuleItem)
        }

        updateModuleMarkDoneView(currentModuleItem)
    }

    private fun markAsRead(moduleItem: ModuleItem) {
        markAsReadJob = tryWeave {
            // mark the moduleItem as viewed if we have a valid module id and item id,
            // but not the files, because they need to open or download those to view them
            if (moduleItem.moduleId != 0L && moduleItem.id != 0L && getCurrentModuleItem(currentPos)!!.type != ModuleItem.Type.File.toString()) {
                repository.markAsRead(canvasContext, moduleItem)

                // Update the module item locally, needed to unlock modules as the user ViewPages through them
                getCurrentModuleItem(currentPos)?.completionRequirement?.completed = true

                setupNextModule(getModuleItemGroup(currentPos))

                // Update the module state to indicate in the list that the module is completed
                val module = modules.find { it.id == moduleItem.moduleId } ?: return@tryWeave
                val isModuleCompleted = items.filterNotNull().flatten().filter { it.moduleId == moduleItem.moduleId }.all { it.completionRequirement?.completed.orDefault() }
                val updatedState = if (isModuleCompleted) State.Completed.apiString else module.state

                // Update the module list fragment to show that these requirements are done,
                ModuleUpdatedEvent(module.copy(state = updatedState)).post()

                // Update the state of the next module to indicate in the list that it is unlocked
                modules.getOrNull(modules.indexOf(module) + 1)?.let {
                    if (isModuleCompleted && it.state != State.Completed.apiString) {
                        ModuleUpdatedEvent(it.copy(state = State.Unlocked.apiString)).post()
                    }
                }
            }
        } catch {
            Logger.e("Error marking module item as read. " + it.message)
        }
    }

    private fun updatePrevNextButtons(currentPosition: Int) {
        // Don't want to see the previous button if we're on the first item
        if (currentPosition == 0) {
            binding.prevItem.setInvisible()
        }
        // Don't show the next_item button if we're on the last item
        if (currentPosition >= itemsCount - 1) {
            binding.nextItem.visibility = View.INVISIBLE
        }
    }

    private fun updateModuleMarkDoneView(item: ModuleItem?) = with(binding) {
        // Sets up if the "mark done" view should be visible
        if (item == null) {
            markDoneWrapper.setGone()
        } else {
            val completionRequirement = item.completionRequirement
            if (completionRequirement != null && ModuleItem.MUST_MARK_DONE == completionRequirement.type && !item.isLocked()) {
                markDoneWrapper.setVisible()
                markDoneCheckbox.isChecked = completionRequirement.completed
            } else {
                markDoneWrapper.setGone()
            }
        }
    }

    private fun notifyOfItemChanged(item: ModuleItem?) {
        val navigation = navigation
        if (navigation != null) {
            val fragment = navigation.peekingFragment
            if (fragment is ModuleListFragment) {
                fragment.notifyOfItemChanged(modules[groupPos], item)
            }
        }
    }

    private fun getModuleItemData(moduleId: Long) {
        moduleItemsJob = lifecycleScope.tryLaunch {
            val moduleItems = repository.getAllModuleItems(canvasContext, moduleId, true)
            // Update ui here with results
            // Holds the position of the module the current module item belongs to
            var index = 0

            for (i in modules.indices) {
                // Loop through the modules for the course

                if (modules[i].id == moduleItems[0].moduleId) {
                    // We've found the module these items belong to, keep track of it's index
                    index = i
                    break
                }
            }

            var itemsAdded = 0
            //add the results in the correct place.  We need to get the index especially for pagination so we
            //add the items where they're supposed to be
            for (i in moduleItems.indices) {
                //check if we should add the moduleItem. Also, we don't want to add it if the view pager
                //already contains it. This could happen if they complete an item and pull to refresh
                //or if the teacher adds an item
                if (shouldAddModuleItem(requireContext(), moduleItems[i]) && !items[index]?.contains(moduleItems[i]).orDefault()) {
                    items[index]?.add(moduleItems[i])
                    itemsAdded++
                }
            }
            itemsCount += itemsAdded

            //only add to currentPos if we're adding to the module that is the previous module
            //Without this check it will modify the index of the array while we are progressing through
            //the a module which will cause it to jump around a lot because the index is changing.
            if (index < getModuleItemGroup(currentPos + itemsAdded)) {
                currentPos += itemsAdded
            }

            // When we tap on a module item it will try to load the previous and next_item modules, this can throw off the module item that was already loaded,
            // so load it to the current position
            showFragment(getItem(currentPos))

            //prev_item/next_item buttons may now need to be visible (if we were on a module item that was the last in its group but
            //now we have info about the next_item module, we want the user to be able to navigate there)
            binding.bottomBarModule.setVisible()
            updateBottomNavBarButtons()
        } catch { }
    }

    /**
     * Items could have a lot of modules with no data because we haven't retrieved it yet. So
     * we need to use the group position to get it
     *
     * @param groupPosition
     * @return
     */
    private fun setupNextModule(groupPosition: Int) {
        val nextUnlocked = groupPosition + 1
        // Check if the next_item module exists
        if (items.size > nextUnlocked && items[nextUnlocked] == null && moduleItemsJob?.isActive != true) {
            // Get the module items for the next_item module
            getModuleItemData(modules[nextUnlocked].id)
        }
    }

    /**
     * Items could have a lot of modules with no data because we haven't retrieved it yet. So
     * we need to use the group position to get it
     *
     * @param groupPosition
     * @return
     */
    private fun setupPreviousModule(groupPosition: Int) {
        val prevUnlocked = groupPosition - 1
        // Check if the prev_item module exists
        if (prevUnlocked >= 0 && items[prevUnlocked] == null && moduleItemsJob?.isActive != true) {
            // Get the module items for the previous module. The user could select the third module without expanding the second module, so we wouldn't
            // know what is in the second module.
            getModuleItemData(modules[prevUnlocked].id)
        }
    }

    /**
     * Setup the module name while progressing through the modules using the next_item button
     *
     * @param position
     * @return
     */
    private fun setupNextModuleName(position: Int) {
        var modulePos = 0
        var i = 0
        while (i < items.size) {
            if (position + 1 < items[i]?.size.orDefault() + modulePos) {
                // Set the label at the bottom
                setModuleName(modules[i].name!!)
                break
            }
            modulePos += items[i]?.size.orDefault()
            i++
        }
        // +1 because we're going to the next_item module item
        addLockedIconIfNeeded(modules, items, i, position - modulePos + 1)
    }

    /**
     * Setup the module name while progressing through the modules using the previous button
     *
     * @param position
     * @return
     */
    private fun setupPrevModuleName(position: Int) {
        var modulePos = 0
        var i = 0
        while (i < items.size) {
            if (position - 1 < items[i]?.size.orDefault() + modulePos) {
                // Set the label at the bottom
                setModuleName(modules[i].name!!)
                break
            }
            modulePos += items[i]?.size.orDefault()
            i++
        }
        // -1 from position because we're going to the previous module item
        addLockedIconIfNeeded(modules, items, i, position - modulePos - 1)
    }

    /**
     * Iterate through the items to find the module item at the given position.  The module items are
     * an arrayList of arrayLists, so there isn't a built in way to get the moduleItem that we need.
     *
     * We sometimes want the overall position of the module item, but the data structure that we have
     * is an arrayList of arrayLists, so we don't know which module item is the 9th module item without
     * going through the arrayLists and counting.
     * @param position
     * @return
     */
    private fun getCurrentModuleItem(position: Int): ModuleItem? {
        var moduleItem: ModuleItem? = null
        var modulePos = 0
        for (i in items.indices) {
            val item = items[i]
            if (item != null && position < item.size + modulePos) {
                moduleItem = item[position - modulePos]
                break
            }
            modulePos += items[i]?.size.orDefault()
        }
        return moduleItem
    }

    /**
     * Get the group position based on the overall position. When getting the next_item or previous group,
     * sometimes we just have the overall position in the adapter but we need the group number. This
     * helper function does that
     *
     * @param overallPos
     * @return
     */
    private fun getModuleItemGroup(overallPos: Int): Int {
        var modulePos = 0
        for (i in items.indices) {
            if (overallPos < items[i]?.size.orDefault() + modulePos) {
                // overallPos is the contained in the current group (i)
                return i
            }
            modulePos += items[i]?.size.orDefault()
        }
        return 0
    }

    /**
     * Iterate through the items to find the overall position at the given group and child position.
     * We are given the group and child info, but we need to know which position that is in the
     * items arrayList that is used by the adapter.
     *
     * @param groupPosition
     * @return
     */
    private fun getCurrentModuleItemPos(groupPosition: Int, childPosition: Int): Int {
        var modulePos = 0
        for (i in 0 until groupPosition) {
            modulePos += items[i]?.size.orDefault()
        }
        return modulePos + childPosition
    }

    /**
     * We add a locked icon on the bottom bar if the item is locked.  The item could be locked because of
     * a module that has sequential progression or because the module object is locked.
     *
     * @param objects
     * @param moduleItems
     * @param groupPosition
     * @param childPosition
     * @return true if icon is added, false otherwise
     */
    private fun addLockedIconIfNeeded(objects: ArrayList<ModuleObject>, moduleItems: ArrayList<ArrayList<ModuleItem>?>, groupPosition: Int, childPosition: Int): Boolean {
        if (objects.size <= groupPosition) {
            binding.moduleNotFound.setVisible()
            setLockedIcon()
            return true
        }

        binding.moduleNotFound.setGone()

        // If the group is locked, add locked icon
        if (ModuleUtility.isGroupLocked(objects[groupPosition])) {
            setLockedIcon()
            return true
        }

        // Check if it's sequential progress
        // If the module has a sequential progress and the module itself is either unlocked or started, we
        // need to see which items we add a locked icon to

        // Make sure the module is sequential progress
        if (objects[groupPosition].sequentialProgress
                // The state isn't always set, so we need a null check
                && (objects[groupPosition].state != null
                        // Modules can be locked by date or prerequisites. If we're unlocked then we will get just the first item, if the module is
                        // "started" then the user has done 0 or more items, we need to find out which ones are available to display.
                        && (objects[groupPosition].state == "unlocked" || objects[groupPosition].state == "started"))) {

            // Group is sequential, need to figure out which ones to display and not display. We don't want to display any locked items
            var index = -1 // Current behavior of sequential unlocking means that if you view the first item everything will unlock, so there won't be a 'first non-Completed item' and index will be -1
            for (i in 0 until moduleItems[groupPosition]?.size.orDefault()) {
                val moduleItem = moduleItems[groupPosition]
                if (moduleItem != null && moduleItem[i].completionRequirement != null && !moduleItem[i].completionRequirement!!.completed) {
                    // i is the first non Completed item (if it exists), so we don't include this item to show in the view pager.
                    index = i
                    break
                }
            }
            // Index is now the index of the first non Completed item (if exists). We show the first non Completed item, but not the next_item one, which is why this is a
            // greater than check instead of >=
            if (index != -1 && childPosition > index) {
                setLockedIcon()
                return true
            }
        }

        binding.moduleNameIcon.setGone()
        return false
    }

    private fun setLockedIcon() {
        binding.moduleNameIcon.setVisible()
    }

    private fun getItem(position: Int): Fragment {
        // Position is the overall position, and we could have multiple modules with their individual positions (if 2 modules have 3 items each, the last
        // item in the second module is position 5, not 2 (zero based)),
        // so we need to find the correct one overall
        val moduleItem = getCurrentModuleItem(position) ?: getCurrentModuleItem(0) // Default to the first item, band-aid for NPE

        val fragment = ModuleUtility.getFragment(
            moduleItem!!,
            canvasContext as Course,
            modules[groupPos],
            navigatedFromModules,
            repository.isOnline() || !isOfflineEnabled, // If the offline feature is disabled we always use the online behavior
            snycedTabs,
            syncedFileIds,
            requireContext()
        )
        var args: Bundle? = fragment!!.arguments
        if (args == null) {
            args = Bundle()
            fragment.arguments = args
        }

        // Add module item ID to bundle for PageView tracking.
        args.putLong(com.instructure.pandautils.utils.Const.MODULE_ITEM_ID, moduleItem.id)

        return fragment
        // Don't update the actionbar title here, we'll do it later. When we update it here the actionbar title sometimes
        // gets updated to the next_item fragment's title
    }
    //endregion

    private fun getModelObject(): ModuleItem? = getCurrentModuleItem(currentPos)

    //region Bookmarks
    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)
                .withUrl(getCurrentModuleItem(currentPos)?.url)
                .withParams(getParamForBookmark())
                .withQueryParams(getQueryParamForBookmark())

    private fun getParamForBookmark(): HashMap<String, String> {
        getCurrentModuleItem(currentPos)?.let {
            val uri = Uri.parse(it.url)

            if (uri != null) {
                val params = uri.pathSegments
                //get the last 2 segments for the type and type_id
                if (params.size > 2) {
                    val itemType = params[params.size - 2]
                    val itemTypeId = params[params.size - 1]
                    val map = HashMap<String, String>()

                    map[RouterParams.MODULE_TYPE_SLASH_ID] = "$itemType/$itemTypeId"
                    map[RouterParams.MODULE_ITEM_ID] = it.id.toString()

                    return map
                }
            }
        }

        return HashMap()
    }

    private fun getQueryParamForBookmark(): HashMap<String, String> {
        getCurrentModuleItem(currentPos)?.let {
            return HashMap<String, String>().apply { put(RouterParams.MODULE_ITEM_ID, it.id.toString()) }
        }
        return HashMap()
    }
    //endregion

    private fun loadModuleProgression(bundle: Bundle?) {
        if(assetId.isBlank()) {
            binding.bottomBarModule.setVisible()
            setViewInfo(bundle)
            setButtonListeners()
            updateBottomNavBarButtons()
            return
        }

        binding.progressBar.setVisible()
        lifecycleScope.tryLaunch {
            val moduleItemSequence = repository.getModuleItemSequence(canvasContext, assetType, assetId, true)
            // Make sure that there is a sequence
            val sequenceItems = moduleItemSequence.items ?: emptyArray()
            if (sequenceItems.isNotEmpty()) {
                // Get the current module item. we'll use the id of this down below
                val current = when {
                   assetType == ModuleItemAsset.MODULE_ITEM.assetType -> sequenceItems.firstOrNull { it.current!!.id == assetId.toLong() }?.current ?: sequenceItems[0].current
                   else -> sequenceItems[0].current
                }
                val moduleItems = repository.getAllModuleItems(canvasContext, current!!.moduleId, true)
                val unfilteredItems = ArrayList<ArrayList<ModuleItem>?>(1).apply { add(ArrayList(moduleItems)) }
                modules = ArrayList<ModuleObject>(1).apply { moduleItemSequence.modules!!.firstOrNull { it.id == current?.moduleId }?.let { add(it) } }
                val moduleHelper = ModuleProgressionUtility.prepareModulesForCourseProgression(requireContext(), current!!.id, modules, unfilteredItems)
                groupPos = moduleHelper.newGroupPosition
                childPos = moduleHelper.newChildPosition
                items = moduleHelper.strippedModuleItems
            } else {
                binding.progressBar.setGone()
                val moduleItemAsset = ModuleItemAsset.fromAssetType(assetType) ?: ModuleItemAsset.MODULE_ITEM
                val secondaryItemAsset = ModuleItemAsset.fromAssetType(secondaryAssetType)
                val routeOffline = !repository.isOnline() && isOfflineEnabled && secondaryItemAsset != null

                if (moduleItemAsset != ModuleItemAsset.MODULE_ITEM || routeOffline) {
                    // If the asset type is not a module item it means that the secondaryItemAsset will never be null.
                    val newRoute = route.copy(secondaryClass = secondaryItemAsset!!.routeClass, removePreviousScreen = true)
                    RouteMatcher.route(requireActivity(), newRoute)
                    return@tryLaunch
                }
            }

            binding.progressBar.setGone()
            binding.bottomBarModule.setVisible()
            setViewInfo(bundle)
            setButtonListeners()
        } catch {
            binding.progressBar.setGone()
            Logger.e("Error routing modules: " + it.message)
        }
    }

    companion object {

        private const val MODULE_ITEMS = "module_item"
        private const val MODULE_OBJECTS = "module_objects"
        private const val MODULE_POSITION = "module_position"
        private const val GROUP_POSITION = "group_position"
        private const val CHILD_POSITION = "child_position"
        private const val ASSET_ID = "asset_id"
        private const val ASSET_TYPE = "asset_type"
        private const val SECONDARY_ASSET_TYPE = "secondary_asset_type"
        private const val ROUTE = "route"
        private const val NAVIGATED_FROM_MODULES = "navigated_from_modules"


        //we don't want to add subheaders or external tools into the list. subheaders don't do anything and we
        //don't support external tools.
        fun shouldAddModuleItem(context: Context, moduleItem: ModuleItem): Boolean = when (moduleItem.type) {
            "UnlockRequirements" -> false
            "SubHeader" -> false
            else -> !moduleItem.title.equals(context.getString(R.string.loading), ignoreCase = true)
        }

        fun makeRoute(canvasContext: CanvasContext, groupPos: Int, childPos: Int): Route {
            return Route(null, CourseModuleProgressionFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                putInt(GROUP_POSITION, groupPos)
                putInt(CHILD_POSITION, childPos)
                putBoolean(NAVIGATED_FROM_MODULES, true)
            }))
        }

        fun newInstance(route: Route): CourseModuleProgressionFragment? {
            val fragment = if (validRoute(route)) CourseModuleProgressionFragment().apply {
                arguments = Bundle().apply {
                    putAll(route.arguments)
                    putSerializable(MODULE_ITEMS, CourseModulesStore.moduleListItems)
                    putParcelableArrayList(MODULE_OBJECTS, CourseModulesStore.moduleObjects)
                    putParcelable(ROUTE, route)
                }
                val asset = getAssetTypeAndId(route)
                assetType = asset.first.assetType
                secondaryAssetType = getSecondaryAssetType(route)?.assetType
                assetId = asset.second
            } else null

            CourseModulesStore.moduleListItems = null
            CourseModulesStore.moduleObjects = null

            return fragment
        }

        private fun getAssetTypeAndId(route: Route): Pair<ModuleItemAsset, String> {
            val queryParams = route.queryParamsHash
            val params = route.paramsHash
            if (queryParams.containsKey(RouterParams.MODULE_ITEM_ID)) {
                return Pair(ModuleItemAsset.MODULE_ITEM, queryParams[RouterParams.MODULE_ITEM_ID] ?: "")
            }

            ModuleItemAsset.values().forEach {
                if (params.containsKey(it.assetIdParamName)) {
                    return Pair(it, params[it.assetIdParamName] ?: "")
                }
            }

            return Pair(ModuleItemAsset.MODULE_ITEM, "")
        }

        private fun getSecondaryAssetType(route: Route): ModuleItemAsset? {
            val params = route.paramsHash

            ModuleItemAsset.values().forEach {
                if (params.containsKey(it.assetIdParamName)) {
                    return it
                }
            }

            return null
        }

        private fun validRoute(route: Route): Boolean = route.canvasContext != null
            && (CourseModulesStore.moduleObjects != null && CourseModulesStore.moduleListItems != null)
            || route.queryParamsHash.keys.any { it == RouterParams.MODULE_ITEM_ID }
            || route.paramsHash.keys.any { isModuleItemAsset(it) }

        private fun isModuleItemAsset(paramName: String) = ModuleItemAsset.values().find { it.assetIdParamName == paramName } != null
    }
}

enum class ModuleItemAsset(val assetType: String, val assetIdParamName: String, val routeClass: Class<out Fragment>? = null) {
    MODULE_ITEM("ModuleItem", RouterParams.MODULE_ITEM_ID),
    PAGE("Page", RouterParams.PAGE_ID, PageDetailsFragment::class.java),
    QUIZ("Quiz", RouterParams.QUIZ_ID, BasicQuizViewFragment::class.java),
    DISCUSSION("Discussion", RouterParams.MESSAGE_ID, DiscussionRouterFragment::class.java),
    ASSIGNMENT("Assignment", RouterParams.ASSIGNMENT_ID, AssignmentDetailsFragment::class.java),
    FILE("File", RouterParams.FILE_ID, FileDetailsFragment::class.java);

    companion object {
        fun fromAssetType(assetType: String?): ModuleItemAsset? = values().find { it.assetType == assetType }
    }
}
