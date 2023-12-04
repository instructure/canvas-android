/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.teacher.features.modules.progression

import androidx.annotation.ColorInt
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.instructure.teacher.BR

data class ModuleProgressionViewData(
    val moduleItems: List<ModuleItemViewData>,
    val moduleNames: List<String>,
    val initialPosition: Int,
    @ColorInt val iconColor: Int
) : BaseObservable() {
    @Bindable
    var currentPosition = initialPosition

    val onPageChangeListener: OnPageChangeListener = object : SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            currentPosition = position
            notifyPropertyChanged(BR.currentPosition)
            notifyPropertyChanged(BR.previousVisible)
            notifyPropertyChanged(BR.nextVisible)
        }
    }

    @Bindable
    fun isPreviousVisible(): Boolean = currentPosition > 0

    @Bindable
    fun isNextVisible(): Boolean = currentPosition < moduleItems.lastIndex
}

sealed class ModuleProgressionAction {
    data class RedirectToAsset(val asset: ModuleItemAsset) : ModuleProgressionAction()
}

sealed class ModuleItemViewData {
    data class Page(val pageUrl: String) : ModuleItemViewData()
    data class Assignment(val assignmentId: Long) : ModuleItemViewData()
    data class Discussion(val isDiscussionRedesignEnabled: Boolean, val discussionTopicHeaderId: Long) : ModuleItemViewData()
    data class Quiz(val quizId: Long) : ModuleItemViewData()
    data class External(val url: String, val title: String) : ModuleItemViewData()
    data class File(val fileUrl: String) : ModuleItemViewData()
}
