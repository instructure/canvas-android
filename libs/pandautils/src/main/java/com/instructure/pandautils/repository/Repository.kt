package com.instructure.pandautils.repository

import com.instructure.pandautils.utils.FEATURE_FLAG_OFFLINE
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider

abstract class Repository<T>(
    private val localDataSource: T,
    private val networkDataSource: T,
    private val networkStateProvider: NetworkStateProvider,
    private val featureFlagProvider: FeatureFlagProvider
) {

    fun isOnline() = networkStateProvider.isOnline()

    suspend fun isOfflineEnabled() = featureFlagProvider.offlineEnabled()

    suspend fun dataSource(): T {
        return if (isOnline() || !isOfflineEnabled()) {
                networkDataSource
            } else {
                localDataSource
            }
    }
}