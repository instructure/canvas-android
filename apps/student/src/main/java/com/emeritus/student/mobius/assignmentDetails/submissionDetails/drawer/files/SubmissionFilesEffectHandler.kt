/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files

import com.emeritus.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui.SubmissionFilesView
import com.emeritus.student.mobius.common.ChannelSource
import com.emeritus.student.mobius.common.ui.EffectHandler
import kotlinx.coroutines.ObsoleteCoroutinesApi

class SubmissionFilesEffectHandler : EffectHandler<SubmissionFilesView, SubmissionFilesEvent, SubmissionFilesEffect>() {
    @ObsoleteCoroutinesApi
    override fun accept(value: SubmissionFilesEffect) {
        when (value) {
            is SubmissionFilesEffect.BroadcastFileSelected -> {
                ChannelSource.getChannel<SubmissionDetailsSharedEvent>().trySend(
                    SubmissionDetailsSharedEvent.FileSelected(value.file)
                )
            }
        }
    }
}
