/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */    package com.instructure.parentapp.features.dashboard

import androidx.annotation.ColorInt
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.parentapp.R
import com.instructure.parentapp.BR

data class AddStudentItemViewModel(
    @Bindable @ColorInt var color: Int,
    val onAddStudentClicked: () -> Unit
) : BaseObservable(), ItemViewModel {
    override val viewType: Int = StudentListViewType.ADD_STUDENT.viewType
    override val layoutId = R.layout.item_add_student

    fun updateColor(@ColorInt color: Int) {
        this.color = color
        notifyPropertyChanged(BR.color)
    }
}