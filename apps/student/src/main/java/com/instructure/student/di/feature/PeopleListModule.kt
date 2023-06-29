package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.people.list.PeopleListLocalDataSource
import com.instructure.student.features.people.list.PeopleListNetworkDataSource
import com.instructure.student.features.people.list.PeopleListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class PeopleListModule {
    @Provides
    fun provideLocalDataSource(/*pageDao: PageDao*/): PeopleListLocalDataSource {
        return PeopleListLocalDataSource()
    }

    @Provides
    fun provideNetworkDataSource(userAPI: UserAPI.UsersInterface): PeopleListNetworkDataSource {
        return PeopleListNetworkDataSource(userAPI)
    }

    @Provides
    fun providePeopleListRepository(
        localDataSource: PeopleListLocalDataSource,
        networkDataSource: PeopleListNetworkDataSource,
        networkStateProvider: NetworkStateProvider
    ): PeopleListRepository {
        return PeopleListRepository(localDataSource, networkDataSource, networkStateProvider)
    }
}