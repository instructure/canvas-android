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
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.parentapp.util.navigation.Navigation
import com.instructure.parentapp.util.navigation.ParentWebViewRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

/**
 * Module for various common Fragment scope dependencies that are used in different Fragments.
 */
@Module
@InstallIn(FragmentComponent::class)
class FragmentModule {

    @Provides
    fun provideWebViewRouter(activity: FragmentActivity, navigation: Navigation): WebViewRouter {
        return ParentWebViewRouter(activity, navigation)
    }
}
