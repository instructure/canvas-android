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
 */

package com.instructure.parentapp.di

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.help.HelpDialogFragmentBehavior
import com.instructure.pandautils.features.help.HelpLinkFilter
import com.instructure.parentapp.features.help.ParentHelpDialogFragmentBehavior
import com.instructure.parentapp.features.help.ParentHelpLinkFilter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class HelpDialogModule {

    @Provides
    fun provideHelpLinkFilter(): HelpLinkFilter {
        return ParentHelpLinkFilter()
    }
}

@Module
@InstallIn(FragmentComponent::class)
class HelpDialogFragmentModule {

    @Provides
    fun provideHelpDialogFragmentBehavior(activity: FragmentActivity): HelpDialogFragmentBehavior {
        return ParentHelpDialogFragmentBehavior(activity)
    }
}
