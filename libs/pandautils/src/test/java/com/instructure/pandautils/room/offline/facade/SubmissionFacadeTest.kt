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
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.offline.daos.GroupDao
import com.instructure.pandautils.room.offline.daos.SubmissionDao
import com.instructure.pandautils.room.offline.daos.UserDao
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class SubmissionFacadeTest {

    private val submissionDao: SubmissionDao = mockk(relaxed = true)
    private val groupDao: GroupDao = mockk(relaxed = true)
    private val mediaCommentDao: MediaCommentDao = mockk(relaxed = true)
    private val userDao: UserDao = mockk(relaxed = true)
    private val userApi: UserAPI.UsersInterface = mockk(relaxed = true)

    private val facade = SubmissionFacade(submissionDao, groupDao, mediaCommentDao, userDao, userApi)

    @Test
    fun `Calling insertSubmission should insert submission and related entities`() = runTest {

    }

    @Test
    fun `Calling getSubmissionById should return the submission with the specified ID`() = runTest {

    }
}