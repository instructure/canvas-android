/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.student.features.quiz.list

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.filterWithQuery
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.adapter.ExpandableRecyclerAdapter
import com.instructure.student.databinding.ViewholderQuizBinding
import com.instructure.student.holders.ExpandableViewHolder
import com.instructure.student.holders.QuizViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

class QuizListRecyclerAdapter(
        context: Context,
        private val canvasContext: CanvasContext,
        private val adapterToFragmentCallback: AdapterToFragmentCallback<Quiz>?,
        private val repository: QuizListRepository,
        private val lifecycleScope: LifecycleCoroutineScope,
) : ExpandableRecyclerAdapter<String, Quiz, RecyclerView.ViewHolder>(context, String::class.java, Quiz::class.java) {

    private var quizzes = emptyList<Quiz>()

    private var settings: CourseSettings? = null

    var searchQuery = ""
        set(value) {
            field = value
            clear()
            populateData()
            onCallbackFinished(ApiType.API)
        }

    init {
        isExpandedByDefault = true
        viewHolderHeaderClicked = object : ViewHolderHeaderClicked<String> {
            override fun viewClicked(view: View?, groupName: String) {
                expandCollapseGroup(groupName)
                // If this group is collapsed we want to try to load the data to avoid having a progress bar spin forever.
                if (!isGroupExpanded(groupName)) loadData()
            }

        }
        loadData()
    }

    private fun populateData() {
        val assignmentQuizzes = context.getString(R.string.assignmentQuizzes)
        val surveys = context.getString(R.string.surveys)
        val gradedSurveys = context.getString(R.string.gradedSurveys)
        val practiceQuizzes = context.getString(R.string.practiceQuizzes)

        quizzes.filterWithQuery(searchQuery, Quiz::title).forEach { quiz ->
            when (quiz.quizType) {
                Quiz.TYPE_ASSIGNMENT, Quiz.TYPE_NEW_QUIZZES -> addOrUpdateItem(assignmentQuizzes, quiz)
                Quiz.TYPE_SURVEY -> addOrUpdateItem(surveys, quiz)
                Quiz.TYPE_GRADED_SURVEY -> addOrUpdateItem(gradedSurveys, quiz)
                Quiz.TYPE_PRACTICE -> addOrUpdateItem(practiceQuizzes, quiz)
            }
        }

        adapterToFragmentCallback?.onRefreshFinished()
        if (size() == 0) adapterToRecyclerViewCallback?.setIsEmpty(true)
        notifyDataSetChanged()
    }

    override fun loadFirstPage() {
        lifecycleScope.tryLaunch {
            settings = repository.loadCourseSettings(canvasContext.id, isRefresh)
            quizzes = repository.loadQuizzes(canvasContext.type.apiString, canvasContext.id, isRefresh)
            isAllPagesLoaded = true
            populateData()
            onCallbackFinished(ApiType.API)
        } catch {
            context.toast(R.string.errorOccurred)
        }
    }

    override fun createViewHolder(v: View, viewType: Int) = when (viewType) {
        Types.TYPE_HEADER -> ExpandableViewHolder(v)
        else -> QuizViewHolder(ViewholderQuizBinding.bind(v))
    }

    override fun itemLayoutResId(viewType: Int) = when (viewType) {
        Types.TYPE_HEADER -> ExpandableViewHolder.HOLDER_RES_ID
        else -> QuizViewHolder.HOLDER_RES_ID
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, s: String, quiz: Quiz) {
        val restrictQuantitativeData = settings?.restrictQuantitativeData.orDefault()
        (holder as? QuizViewHolder)?.bind(quiz, adapterToFragmentCallback, context, canvasContext.color, restrictQuantitativeData)
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, s: String, isExpanded: Boolean) {
        (holder as ExpandableViewHolder).bind(context, s, s, isExpanded, viewHolderHeaderClicked)
    }

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<String> {
        return object : GroupSortedList.GroupComparatorCallback<String> {
            override fun compare(o1: String, o2: String) = o1.compareTo(o2)
            override fun areContentsTheSame(oldGroup: String, newGroup: String) = oldGroup == newGroup
            override fun areItemsTheSame(group1: String, group2: String) = group1 == group2
            override fun getUniqueGroupId(group: String) = group.hashCode().toLong()
            override fun getGroupType(group: String) = Types.TYPE_HEADER
        }
    }

    @Suppress("DEPRECATION")
    private val comparator = compareBy<Quiz>({ it.title }, { it.description }, { it.dueAt })

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<String, Quiz> {
        return object : GroupSortedList.ItemComparatorCallback<String, Quiz> {
            override fun compare(group: String, o1: Quiz, o2: Quiz) = o1.compareTo(o2)
            override fun areItemsTheSame(item1: Quiz, item2: Quiz) = item1.id == item2.id
            override fun getUniqueItemId(item: Quiz) = item.id
            override fun getChildType(group: String, item: Quiz) = Types.TYPE_ITEM
            override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz) = comparator.compare(oldItem, newItem) == 0
        }
    }

}
