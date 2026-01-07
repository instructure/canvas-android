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
 */
package com.instructure.canvasapi2.di

import com.apollographql.apollo.ApolloClient
import com.instructure.canvasapi2.JourneyGraphQLClientConfig
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.RedwoodGraphQLClientConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultApolloClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RedwoodApolloClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JourneyApolloClient

@Module
@InstallIn(SingletonComponent::class)
class GraphQlClientModule {
    @Provides
    @DefaultApolloClient
    fun provideDefaultApolloClient(): ApolloClient {
        val config = QLClientConfig()
        return config.buildClient()
    }

    @Provides
    @RedwoodApolloClient
    fun provideRedwoodApolloClient(redwoodGraphQLClientConfig: RedwoodGraphQLClientConfig): ApolloClient {
        val config = QLClientConfig()
        val block = redwoodGraphQLClientConfig.createClientConfigBlock()
        config.block()
        return config.buildClient()
    }

    @Provides
    @JourneyApolloClient
    fun provideJourneyApolloClient(journeyGraphQLClientConfig: JourneyGraphQLClientConfig): ApolloClient {
        val config = QLClientConfig()
        val block = journeyGraphQLClientConfig.createClientConfigBlock()
        config.block()
        return config.buildClient()
    }
}