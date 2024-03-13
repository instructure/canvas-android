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

package com.instructure.student.features.modules.list

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.google.gson.Gson
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_MODULE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentModuleListBinding
import com.instructure.student.databinding.PandaRecyclerRefreshLayoutBinding
import com.instructure.student.events.ModuleUpdatedEvent
import com.instructure.student.features.ai.model.PageSummary
import com.instructure.student.features.ai.quiz.QuizFragment
import com.instructure.student.features.modules.list.adapter.ModuleAdapterToFragmentCallback
import com.instructure.student.features.modules.list.adapter.ModuleListRecyclerAdapter
import com.instructure.student.features.modules.progression.CourseModuleProgressionFragment
import com.instructure.student.features.modules.util.ModuleProgressionUtility
import com.instructure.student.features.modules.util.ModuleUtility
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.CourseModulesStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_MODULE_LIST)
@PageView(url = "modules")
@AndroidEntryPoint
class ModuleListFragment : ParentFragment(), Bookmarkable {

    private val binding by viewBinding(FragmentModuleListBinding::bind)
    private lateinit var recyclerBinding: PandaRecyclerRefreshLayoutBinding
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: ModuleListRecyclerAdapter? = null

    @Inject
    lateinit var repository: ModuleListRepository

    @Inject
    lateinit var openAi: OpenAI

    private val pageSummaryMap = mutableMapOf<Long, PageSummary>()

    val tabId: String
        get() = Tab.MODULES_ID

    //region Fragment Lifecycle Overrides

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        recyclerAdapter?.cancel()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = layoutInflater.inflate(R.layout.fragment_module_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerBinding = PandaRecyclerRefreshLayoutBinding.bind(binding.root)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupViews()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (recyclerAdapter?.size() == 0) {
            recyclerBinding.emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    recyclerBinding.emptyView.setGuidelines(.24f, .53f, .62f, .12f, .88f)
                } else {
                    recyclerBinding.emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    //change nothing, at least for now
                } else {
                    recyclerBinding.emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = getString(R.string.modules)

    override fun applyTheme() {
        with(binding) {
            toolbar.title = title()
            toolbar.setupAsBackButton(this@ModuleListFragment)
            setupToolbarMenu(toolbar)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
        }
    }

    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    override fun getSelectedParamName(): String = RouterParams.MODULE_ID

    fun setupViews() {
        val navigatingToSpecificModule = !arguments?.getString(MODULE_ID).isNullOrEmpty()

        recyclerAdapter = ModuleListRecyclerAdapter(canvasContext, requireContext(), navigatingToSpecificModule, repository, lifecycleScope, object :
            ModuleAdapterToFragmentCallback {
            override fun onRowClicked(moduleObject: ModuleObject, moduleItem: ModuleItem, position: Int, isOpenDetail: Boolean) {
                if (moduleItem.type == ModuleItem.Type.PracticeQuiz.toString()) {
                    showAiQuiz(moduleObject.id)
                    return
                }
                if (moduleItem.type != null && moduleItem.type == ModuleObject.State.UnlockRequirements.apiString) return

                // Don't do anything with headers if the user selects it
                if (moduleItem.type != null && moduleItem.type == ModuleItem.Type.SubHeader.toString()) return

                val isLocked = ModuleUtility.isGroupLocked(moduleObject)
                if (isLocked) return

                // Remove all the subheaders and stuff.
                val groups = recyclerAdapter?.groups ?: arrayListOf()

                val moduleItemsArray = groups.indices.mapTo(ArrayList()) { recyclerAdapter?.getItems(groups[it]) ?: arrayListOf() }
                val moduleHelper = ModuleProgressionUtility.prepareModulesForCourseProgression(
                    requireContext(), moduleItem.id, groups, moduleItemsArray
                )

                CourseModulesStore.moduleListItems = moduleHelper.strippedModuleItems
                CourseModulesStore.moduleObjects = groups
                RouteMatcher.route(
                    requireActivity(), CourseModuleProgressionFragment.makeRoute(
                        canvasContext,
                        moduleHelper.newGroupPosition,
                        moduleHelper.newChildPosition
                    )
                )
            }

            override fun onRefreshFinished(isError: Boolean) {
                setRefreshing(false)
                if(isError) {
                    // We need to force the empty view to be visible to use it for errors on refresh
                    recyclerBinding.emptyView.setVisible()
                    setEmptyView(recyclerBinding.emptyView, R.drawable.ic_panda_nomodules, R.string.modulesLocked, R.string.modulesLockedSubtext)
                } else if (recyclerAdapter?.size() == 0) {
                    setEmptyView(recyclerBinding.emptyView, R.drawable.ic_panda_nomodules, R.string.noModules, R.string.noModulesSubtext)
                } else if (!arguments?.getString(MODULE_ID).isNullOrEmpty()) {
                    // We need to delay scrolling until the expand animation has completed, otherwise modules
                    // that appear near the end of the list will not have the extra 'expanded' space needed
                    // to scroll as far as possible toward the top
                    recyclerBinding.listView.postDelayed({
                        val groupPosition = recyclerAdapter?.getGroupItemPosition(arguments!!.getString(
                            MODULE_ID
                        )!!.toLong()) ?: -1
                        if (groupPosition >= 0) {
                            val lm = recyclerBinding.listView.layoutManager as? LinearLayoutManager
                            lm?.scrollToPositionWithOffset(groupPosition, 0)
                            arguments?.remove(MODULE_ID)
                        }
                    }, 1000)
                }
            }
        })
        recyclerAdapter?.let {
            configureRecyclerView(requireView(), requireContext(), it, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView)
        }
    }

    private fun showAiQuiz(moduleId: Long) {
        lifecycleScope.launch {
            val pageSummary = pageSummaryMap[moduleId] ?: getAiSummary(moduleId)
            pageSummary?.let {
                RouteMatcher.route(
                    requireActivity(),
                    QuizFragment.makeRoute(it.questions, canvasContext)
                )
                recyclerBinding.listView.setVisible()
                recyclerBinding.emptyView.setGone()
            }
        }
    }

    private suspend fun getAiSummary(moduleId: Long): PageSummary? {
        try {
            recyclerBinding.emptyView.setLoadingWithAnimation(
                com.instructure.pandautils.R.string.offline_content_sync_loading_title,
                com.instructure.pandautils.R.string.offline_content_sync_loading_message,
                com.instructure.pandautils.R.raw.snail
            )
            recyclerBinding.emptyView.setVisible()
            recyclerBinding.listView.setGone()
            val pagesBodies = repository.getFirstPageModuleItems(canvasContext, moduleId, false)
                .depaginate {
                    repository.getNextPageModuleItems(it, false)
                }.dataOrNull
                ?.filter { it.type == ModuleItem.Type.Page.toString() }
                ?.filter { it.pageUrl != null }
                ?.map { repository.getPageDetails(canvasContext.id, it.pageUrl!!) }
                ?.map { it.dataOrNull }
                ?.filterNotNull()
                ?.map { it.body }
                ?.joinToString("\n")

            val request = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = "You're a helpful assistant."
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = "Can you create a tl;dr summary and at least 10 quiz questions with 2 possible choices of this text: '${pagesBodies}'. The result should be in the form of a JSON object. We are only expecting a json object, so exclude anything that is not the json itself." +
                                "{\"summary\":String\n" +
                                "\"questions\":[]\n" +
                                "}\n" +
                                "where the questions array is an array of objects with the following properties:\n" +
                                "{\"question\":String\n" +
                                "\"choices\":[String, String]\n" +
                                "\"answer\":String\n" +
                                "\"excerpt\":String\n" +
                                "}\n"
                    )
                )
            )
            val completion = openAi.chatCompletion(request)
            val message = completion.choices.firstOrNull()?.message?.content
            val pageSummary =
                message?.substring(message.indexOf("{"), message.lastIndexOf("}") + 1)?.let {
                    Gson().fromJson(it, PageSummary::class.java)
                }
            if (pageSummary != null) {
                pageSummaryMap[moduleId] = pageSummary
            }
            return pageSummary

        } catch (e: Exception) {
            e.printStackTrace()
            recyclerBinding.listView.setVisible()
            recyclerBinding.emptyView.setGone()
            return null
        }
    }

    fun notifyOfItemChanged(`object`: ModuleObject?, item: ModuleItem?) {
        if (item == null || `object` == null) return

        recyclerAdapter?.addOrUpdateItem(`object`, item)
    }

    fun refreshModuleList() = recyclerAdapter?.updateMasteryPathItems()

    /**
     * Update the list without clearing the data or collapsing headers. Used to update possibly updated
     * items (like a page that has now been viewed)
     */
    private fun updateList(moduleObject: ModuleObject) = recyclerAdapter?.updateWithoutResettingViews(moduleObject)


    // region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onModuleUpdated(event: ModuleUpdatedEvent) {
        event.once(javaClass.simpleName) {
            updateList(it)
            recyclerAdapter?.notifyDataSetChanged()
        }
    }
    // endregion

    companion object {
        private const val MODULE_ID = "module_id"

        fun newInstance(route: Route) =
                if (validateRoute(route)) {
                    ModuleListFragment().apply {
                        arguments = route.canvasContext!!.makeBundle(route.arguments) {
                            route.paramsHash[MODULE_ID]?.let { putString(MODULE_ID, it) }
                        }
                    }
                } else null

        fun makeRoute(canvasContext: CanvasContext?) =
                Route(ModuleListFragment::class.java, canvasContext)

        private fun validateRoute(route: Route) = route.canvasContext != null
    }
}
