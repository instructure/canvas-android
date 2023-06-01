package com.instructure.pandautils.repository

import com.instructure.pandautils.utils.NetworkStateProvider

abstract class Repository<T>(
    private val localDataSource: T,
    private val networkDataSource: T,
    private val networkStateProvider: NetworkStateProvider
) {

    fun isOnline() = networkStateProvider.isOnline()

    val dataSource: T
        get() {
            return if (networkStateProvider.isOnline()) {
                networkDataSource
            } else {
                localDataSource
            }
        }
}