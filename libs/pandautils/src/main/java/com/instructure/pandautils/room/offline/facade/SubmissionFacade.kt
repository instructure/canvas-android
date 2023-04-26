/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.entities.MediaCommentEntity
import com.instructure.pandautils.room.offline.daos.GroupDao
import com.instructure.pandautils.room.offline.daos.SubmissionDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.GroupEntity
import com.instructure.pandautils.room.offline.entities.SubmissionEntity
import com.instructure.pandautils.room.offline.entities.UserEntity

class SubmissionFacade(
    private val submissionDao: SubmissionDao,
    private val groupDao: GroupDao,
    private val mediaCommentDao: MediaCommentDao,
    private val userDao: UserDao,
    private val userApi: UserAPI.UsersInterface,
) {

    suspend fun insertSubmission(submission: Submission): Long {
        val groupId = submission.group?.let { group -> groupDao.insert(GroupEntity(group)) }
        submission.mediaComment?.let { mediaComment ->
            mediaCommentDao.insert(
                MediaCommentEntity(
                    mediaComment
                )
            )
        }
        if (submission.userId != 0L) {
            val user = submission.user ?: userApi.getUser(
                submission.userId,
                RestParams(isForceReadFromNetwork = true)
            ).dataOrNull

            user?.let { userDao.insert(UserEntity(it)) }
        }

        if (submission.graderId != 0L) {
            val grader = userApi.getUser(submission.graderId, RestParams(isForceReadFromNetwork = true)).dataOrNull
            grader?.let { userDao.insert(UserEntity(it)) }
        }

        submission.submissionHistory.forEach { submissionHistoryItem ->
            submissionHistoryItem?.let { insertSubmission(it) }
        }

        return submissionDao.insert(SubmissionEntity(submission, groupId, submission.mediaComment?.mediaId))
    }
}