/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.di

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.instructure.ngc.navigation.NgcAttachmentRouter
import com.instructure.ngc.navigation.NgcWebViewRouter
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.student.navigation.NgcStudentAttachmentRouter
import com.instructure.student.navigation.NgcStudentWebViewRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 * Provides the [WebViewRouter] at [ActivityComponent] scope (qualified with [NgcWebViewRouter]) so
 * Activity-hosted Compose screens (e.g. the NGC experience) can obtain it via a Hilt entry point.
 * The qualifier keeps it distinct from the unqualified FragmentComponent binding in [FragmentModule].
 */
@Module
@InstallIn(ActivityComponent::class)
class NGCRouterModule {

    @Provides
    @NgcWebViewRouter
    fun provideWebViewRouter(activity: Activity): WebViewRouter {
        return NgcStudentWebViewRouter(activity as FragmentActivity)
    }

    @Provides
    fun provideNgcAttachmentRouter(activity: Activity): NgcAttachmentRouter {
        return NgcStudentAttachmentRouter(activity as FragmentActivity)
    }
}
