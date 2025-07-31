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
 */package com.instructure.student.di

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.speedgrader.grade.comments.SpeedGraderCommentAttachment
import com.instructure.pandautils.features.speedgrader.grade.comments.SpeedGraderCommentsAttachmentRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SpeedGraderCommentsModule {

    @Provides
    fun provideSpeedGraderCommentsAttachmentRouter(): SpeedGraderCommentsAttachmentRouter {
        return object : SpeedGraderCommentsAttachmentRouter {
            override fun openAttachment(
                activity: FragmentActivity,
                attachment: SpeedGraderCommentAttachment
            ) {
                // Not implemented for StudentApp
            }
        }
    }
}
