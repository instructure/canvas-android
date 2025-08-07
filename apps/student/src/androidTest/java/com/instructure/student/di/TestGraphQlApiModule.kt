/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */

package com.instructure.student.di

import com.instructure.canvas.espresso.mockCanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeStudentContextManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionCommentsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionRubricManager
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [GraphQlApiModule::class]
)
class TestGraphQlApiModule {

    @Provides
    fun provideCommentLibraryManager(): CommentLibraryManager {
        return FakeCommentLibraryManager()
    }

    @Provides
    fun provideInboxSettingsManager(): InboxSettingsManager {
        return FakeInboxSettingsManager()
    }

    @Provides
    fun provideStudentContextManager(): StudentContextManager {
        return FakeStudentContextManager()
    }

    @Provides
    fun provideAssignmentDetailsManager(): AssignmentDetailsManager {
        return FakeAssignmentDetailsManager()
    }

    @Provides
    fun provideSubmissionContentManager(): SubmissionContentManager {
        return FakeSubmissionContentManager()
    }

    @Provides
    fun provideSubmissionGradeManager(): SubmissionGradeManager {
        return FakeSubmissionGradeManager()
    }

    @Provides
    fun provideSubmissionDetailsManager(): SubmissionDetailsManager {
        return FakeSubmissionDetailsManager()
    }

    @Provides
    fun provideSubmissionRubricManager(): SubmissionRubricManager {
        return FakeSubmissionRubricManager()
    }

    @Provides
    fun provideSubmissionCommentManager(): SubmissionCommentsManager {
        return FakeSubmissionCommentsManager()
    }

    @Provides
    fun provideCustomGradeStatusesManager(): CustomGradeStatusesManager {
        return FakeCustomGradeStatusesManager()
    }
}
