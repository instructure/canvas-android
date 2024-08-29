package com.instructure.pandautils.di

import android.content.Context
import com.instructure.pandautils.utils.FileDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class FileDownloaderModule {
    @Provides
    fun provideFileDownloader(@ApplicationContext context: Context): FileDownloader {
        return FileDownloader(context)
    }
}