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
package com.instructure.student.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.student.R
import com.instructure.student.databinding.AdapterCourseBrowserBinding
import com.instructure.student.databinding.AdapterCourseBrowserHomeBinding
import com.instructure.student.databinding.AdapterCourseBrowserWebViewBinding
import com.instructure.student.util.TabHelper

class CourseBrowserAdapter(val items: List<Tab>, val canvasContext: CanvasContext, private val homePageTitle: String? = null, private val isOnline: Boolean = true, val callback: (Tab) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HOME -> CourseBrowserHomeViewHolder(LayoutInflater.from(parent.context)
                    .inflate(CourseBrowserHomeViewHolder.HOLDER_RES_ID, parent, false), canvasContext, homePageTitle, isOnline)
            WEB_VIEW_ITEM -> CourseBrowserWebViewHolder(LayoutInflater.from(parent.context)
                    .inflate(CourseBrowserWebViewHolder.HOLDER_RES_ID, parent, false), canvasContext.color)
            else -> CourseBrowserViewHolder(LayoutInflater.from(parent.context)
                    .inflate(CourseBrowserViewHolder.HOLDER_RES_ID, parent, false), canvasContext.color)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tab = items[position]
        when {
            tab.tabId == Tab.HOME_ID && holder is CourseBrowserHomeViewHolder -> holder.bind(holder, tab, callback)
            holder is CourseBrowserViewHolder -> holder.bind(tab, callback)
            holder is CourseBrowserWebViewHolder -> holder.bind(tab, callback)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].tabId) {
            Tab.HOME_ID -> HOME
            Tab.COLLABORATIONS_ID, Tab.OUTCOMES_ID -> WEB_VIEW_ITEM
            else -> ITEM
        }
    }

    companion object {
        const val HOME = 0
        const val ITEM = 1
        const val WEB_VIEW_ITEM = 2
    }
}

class CourseBrowserHomeViewHolder(view: View, val canvasContext: CanvasContext, private val homePageTitle: String? = null, private val isOnline: Boolean = true) : RecyclerView.ViewHolder(view) {

    fun bind(holder: CourseBrowserHomeViewHolder, tab: Tab, clickedCallback: (Tab) -> Unit) {
        val binding = AdapterCourseBrowserHomeBinding.bind(holder.itemView)
        binding.homeLabel.text = tab.label

        if(canvasContext is Course && TabHelper.isHomeTabAPage(canvasContext)) binding.homeSubLabel.text = homePageTitle
        else binding.homeSubLabel.text = TabHelper.getHomePageDisplayString(canvasContext)

        val isRecentActivityHome = (canvasContext as? Course)?.homePageID == Tab.NOTIFICATIONS_ID
        val shouldDisable = !isOnline && isRecentActivityHome

        holder.itemView.isEnabled = !shouldDisable
        holder.itemView.alpha = if (shouldDisable) 0.5f else 1f

        if (!shouldDisable) {
            holder.itemView.setOnClickListener {
                clickedCallback(tab)
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_course_browser_home
    }
}

class CourseBrowserWebViewHolder(view: View, val color: Int) : RecyclerView.ViewHolder(view) {

    fun bind(tab: Tab, clickedCallback: (Tab) -> Unit) {

        val res: Int = when (tab.tabId) {
            Tab.OUTCOMES_ID -> R.drawable.ic_outcomes
            Tab.CONFERENCES_ID -> R.drawable.ic_conferences
            else /* Tab.COLLABORATIONS_ID */ -> R.drawable.ic_collaborations
        }

        var d = VectorDrawableCompat.create(itemView.context.resources, res, null)
        d = DrawableCompat.wrap(d!!) as VectorDrawableCompat?
        DrawableCompat.setTint(d!!, color)

        setupTab(tab, d, clickedCallback)
    }

    /**
     * Fill in the view with tabby goodness
     *
     * @param tab The tab (Assignment, Discussions, etc)
     * @param callback What we do when the user clicks this tab
     */
    private fun setupTab(tab: Tab, drawable: Drawable, callback: (Tab) -> Unit) {
        val binding = AdapterCourseBrowserWebViewBinding.bind(itemView)
        binding.unsupportedLabel.text = tab.label
        binding.unsupportedIcon.setImageDrawable(drawable)
        binding.unsupportedSubLabel.setText(R.string.opensInWebView)
        itemView.isEnabled = tab.enabled
        itemView.alpha = if (tab.enabled) 1f else 0.5f
        itemView.onClickWithRequireNetwork { callback(tab) }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_course_browser_web_view
    }
}

class CourseBrowserViewHolder(view: View, val color: Int) : RecyclerView.ViewHolder(view) {

    fun bind(tab: Tab, clickedCallback: (Tab) -> Unit) {
        val res: Int = when (tab.tabId) {
            Tab.ASSIGNMENTS_ID -> R.drawable.ic_assignment
            Tab.QUIZZES_ID -> R.drawable.ic_quiz
            Tab.DISCUSSIONS_ID -> R.drawable.ic_discussion
            Tab.ANNOUNCEMENTS_ID -> R.drawable.ic_announcement
            Tab.PEOPLE_ID -> R.drawable.ic_people
            Tab.FILES_ID -> R.drawable.ic_files
            Tab.PAGES_ID -> R.drawable.ic_pages
            Tab.MODULES_ID -> R.drawable.ic_modules
            Tab.SYLLABUS_ID -> R.drawable.ic_syllabus
            Tab.OUTCOMES_ID -> R.drawable.ic_outcomes
            Tab.GRADES_ID -> R.drawable.ic_grades
            Tab.HOME_ID -> R.drawable.ic_home
            Tab.CONFERENCES_ID -> R.drawable.ic_conferences
            Tab.COLLABORATIONS_ID -> R.drawable.ic_collaborations
            Tab.SETTINGS_ID -> R.drawable.ic_settings
            else -> {
                // Determine if it's the attendance tool
                if (tab.type == Tab.TYPE_EXTERNAL) {
                    R.drawable.ic_lti
                } else R.drawable.ic_canvas_logo
            }
        }

        var d = VectorDrawableCompat.create(itemView.context.resources, res, null)
        d = DrawableCompat.wrap(d!!) as VectorDrawableCompat?
        DrawableCompat.setTint(d!!, color)

        setupTab(tab, d, clickedCallback)
    }

    /**
     * Fill in the view with tabby goodness
     *
     * @param tab The tab (Assignment, Discussions, etc)
     * @param callback What we do when the user clicks this tab
     */
    private fun setupTab(tab: Tab, drawable: Drawable, callback: (Tab) -> Unit) {
        val binding = AdapterCourseBrowserBinding.bind(itemView)
        binding.label.text = tab.label
        binding.icon.setImageDrawable(drawable)
        itemView.isEnabled = tab.enabled
        itemView.alpha = if (tab.enabled) 1f else 0.5f
        if (tab.type == Tab.TYPE_EXTERNAL) {
            itemView.onClickWithRequireNetwork {
                callback(tab)
            }
        } else {
            itemView.setOnClickListener {
                callback(tab)
            }
        }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_course_browser
    }
}
