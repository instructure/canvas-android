package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.pages.details.PageDetailsRepository
import com.instructure.student.features.pages.details.datasource.PageDetailsLocalDataSource
import com.instructure.student.features.pages.details.datasource.PageDetailsNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class PageDetailsModule {

    @Provides
    fun provideLocalDataSource(pageFacade: PageFacade): PageDetailsLocalDataSource {
        return PageDetailsLocalDataSource(pageFacade)
    }

    @Provides
    fun provideNetworkDataSource(pageApi: PageAPI.PagesInterface): PageDetailsNetworkDataSource {
        return PageDetailsNetworkDataSource(pageApi)
    }

    @Provides
    fun providePageDetailsRepository(
        localDataSource: PageDetailsLocalDataSource,
        networkDataSource: PageDetailsNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): PageDetailsRepository {
        return PageDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }
}
