package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.pages.list.PageListLocalDataSource
import com.instructure.student.features.pages.list.PageListNetworkDataSource
import com.instructure.student.features.pages.list.PageListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class PageListModule {

    @Provides
    fun provideLocalDataSource(pageDao: PageDao): PageListLocalDataSource {
        return PageListLocalDataSource(pageDao)
    }

    @Provides
    fun provideNetworkDataSource(pageApi: PageAPI.PagesInterface): PageListNetworkDataSource {
        return PageListNetworkDataSource(pageApi)
    }

    @Provides
    fun providePageListRepository(
        localDataSource: PageListLocalDataSource,
        networkDataSource: PageListNetworkDataSource,
        networkStateProvider: NetworkStateProvider
    ): PageListRepository {
        return PageListRepository(localDataSource, networkDataSource, networkStateProvider)
    }
}