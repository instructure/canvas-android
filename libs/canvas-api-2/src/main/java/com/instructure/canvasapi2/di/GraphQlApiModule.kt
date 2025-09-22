/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.canvasapi2.di

import com.apollographql.apollo.ApolloClient
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.CommentLibraryManagerImpl
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.InboxSettingsManagerImpl
import com.instructure.canvasapi2.managers.PostPolicyManager
import com.instructure.canvasapi2.managers.PostPolicyManagerImpl
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.StudentContextManagerImpl
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.managers.SubmissionRubricManagerImpl
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManagerImpl
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManagerImpl
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManagerImpl
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManagerImpl
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Provide classes for GraphQL related APIs. We need to separate this from the ApiModule if we want to overwrite these specific
 * providers in tests. Unfortunatelly Hilt doesn't allow overriding provider functions.
 */
@Module
@InstallIn(SingletonComponent::class)
class GraphQlApiModule {

    @Provides
    fun provideCommentLibraryManager(@DefaultApolloClient apolloClient: ApolloClient): CommentLibraryManager {
        return CommentLibraryManagerImpl(apolloClient)
    }

    @Provides
    fun provideInboxSettingsManager(@DefaultApolloClient apolloClient: ApolloClient): InboxSettingsManager {
        return InboxSettingsManagerImpl(apolloClient)
    }

    @Provides
    fun provideStudentContextManager(@DefaultApolloClient apolloClient: ApolloClient): StudentContextManager {
        return StudentContextManagerImpl(apolloClient)
    }

    @Provides
    fun provideAssignmentDetailsManager(@DefaultApolloClient apolloClient: ApolloClient): AssignmentDetailsManager {
        return AssignmentDetailsManagerImpl(apolloClient)
    }

    @Provides
    fun provideSubmissionContentManager(@DefaultApolloClient apolloClient: ApolloClient): SubmissionContentManager {
        return SubmissionContentManagerImpl(apolloClient)
    }

    @Provides
    fun provideSubmissionGradeManager(@DefaultApolloClient apolloClient: ApolloClient): SubmissionGradeManager {
        return SubmissionGradeManagerImpl(apolloClient)
    }

    @Provides
    fun provideSubmissionDetailsManager(@DefaultApolloClient apolloClient: ApolloClient): SubmissionDetailsManager {
        return SubmissionDetailsManagerImpl(apolloClient)
    }

    @Provides
    fun provideSubmissionRubricManager(@DefaultApolloClient apolloClient: ApolloClient): SubmissionRubricManager {
        return SubmissionRubricManagerImpl(apolloClient)
    }

    @Provides
    fun provideSubmissionCommentManager(@DefaultApolloClient apolloClient: ApolloClient): SubmissionCommentsManager {
        return SubmissionCommentsManagerImpl(apolloClient)
    }

    @Provides
    fun providePostPolicyManager(@DefaultApolloClient apolloClient: ApolloClient): PostPolicyManager {
        return PostPolicyManagerImpl(apolloClient)
    }
}
