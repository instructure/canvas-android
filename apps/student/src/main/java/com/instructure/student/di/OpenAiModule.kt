package com.instructure.student.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
class OpenAiModule {

    @Provides
    @Singleton
    fun provideOpenAi(): OpenAI {
        return OpenAI(
            token = "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",
            timeout = Timeout(socket = 60.seconds)
        )
    }
}