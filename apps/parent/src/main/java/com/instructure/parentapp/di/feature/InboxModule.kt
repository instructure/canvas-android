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

package com.instructure.parentapp.di.feature

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.pandautils.features.inbox.compose.InboxComposeBehavior
import com.instructure.pandautils.features.inbox.compose.InboxComposeRepository
import com.instructure.pandautils.features.inbox.details.InboxDetailsBehavior
import com.instructure.pandautils.features.inbox.list.InboxRepository
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.parentapp.features.inbox.compose.ParentInboxComposeRepository
import com.instructure.parentapp.features.inbox.coursepicker.ParentInboxCoursePickerRepository
import com.instructure.parentapp.features.inbox.list.ParentInboxRepository
import com.instructure.parentapp.features.inbox.list.ParentInboxRouter
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.navigation.Navigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class InboxFragmentModule {

    @Provides
    fun provideInboxRouter(activity: FragmentActivity, navigation: Navigation, fileDownloader: FileDownloader): InboxRouter {
        return ParentInboxRouter(activity, navigation, fileDownloader)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class InboxModule {

    @Provides
    fun provideInboxRepository(
        inboxApi: InboxApi.InboxInterface,
        coursesApi: CourseAPI.CoursesInterface,
        groupsApi: GroupAPI.GroupInterface,
        progressApi: ProgressAPI.ProgressInterface,
        inboxSettingsManager: InboxSettingsManager,
        featuresApi: FeaturesAPI.FeaturesInterface
    ): InboxRepository {
        return ParentInboxRepository(inboxApi, coursesApi, groupsApi, progressApi, inboxSettingsManager, featuresApi)
    }

    @Provides
    fun provideInboxComposeRepository(
        courseAPI: CourseAPI.CoursesInterface,
        recipientAPI: RecipientAPI.RecipientInterface,
        featuresApi: FeaturesAPI.FeaturesInterface,
        inboxAPI: InboxApi.InboxInterface,
        parentPrefs: ParentPrefs,
        inboxSettingsManager: InboxSettingsManager
    ): InboxComposeRepository {
        return ParentInboxComposeRepository(courseAPI, parentPrefs, featuresApi, recipientAPI, inboxAPI, inboxSettingsManager)
    }

    @Provides
    fun provideInboxCoursePickerRepository(
        courseAPI: CourseAPI.CoursesInterface,
        enrollmentAPI: EnrollmentAPI.EnrollmentInterface,
    ): ParentInboxCoursePickerRepository {
        return ParentInboxCoursePickerRepository(courseAPI, enrollmentAPI)
    }

    @Provides
    fun provideInboxDetailsBehavior(): InboxDetailsBehavior {
        return InboxDetailsBehavior()
    }

    @Provides
    fun provideInboxComposeBehavior(): InboxComposeBehavior {
        return InboxComposeBehavior()
    }
}
