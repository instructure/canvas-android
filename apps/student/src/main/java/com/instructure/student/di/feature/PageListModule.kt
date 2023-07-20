package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.pages.list.PageListRepository
import com.instructure.student.features.pages.list.datasource.PageListLocalDataSource
import com.instructure.student.features.pages.list.datasource.PageListNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class PageListModule {

    @Provides
    fun provideLocalDataSource(pageFacade: PageFacade): PageListLocalDataSource {
        return PageListLocalDataSource(pageFacade)
    }

    @Provides
    fun provideNetworkDataSource(pageApi: PageAPI.PagesInterface): PageListNetworkDataSource {
        return PageListNetworkDataSource(pageApi)
    }

    @Provides
    fun providePageListRepository(
        localDataSource: PageListLocalDataSource,
        networkDataSource: PageListNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): PageListRepository {
        return PageListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }
}