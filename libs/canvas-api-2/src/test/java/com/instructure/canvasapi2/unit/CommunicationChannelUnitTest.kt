/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class CommunicationChannelUnitTest : Assert() {

    @Test
    fun testCommunicationChannel() {
        val communicationChannels: Array<CommunicationChannel> = communicationChannelJSON.parse()

        Assert.assertNotNull(communicationChannels)
        Assert.assertEquals(3, communicationChannels.size)

        for (communicationChannel in communicationChannels) {
            Assert.assertNotNull(communicationChannel.id)
            Assert.assertNotNull(communicationChannel.position)
            Assert.assertNotNull(communicationChannel.userId)
            Assert.assertNotNull(communicationChannel.workflowState)
            Assert.assertNotNull(communicationChannel.address)
            Assert.assertNotNull(communicationChannel.type)
        }
    }

    @Language("JSON")
    private val communicationChannelJSON = """
      [
        {
          "id": 123245,
          "position": 1,
          "user_id": 123245,
          "workflow_state": "active",
          "address": "test@test.com",
          "type": "email"
        },
        {
          "id": 123245,
          "position": 2,
          "user_id": 123245,
          "workflow_state": "active",
          "address": "test@test.com",
          "type": "email"
        },
        {
          "id": 123245,
          "position": 3,
          "user_id": 123245,
          "workflow_state": "active",
          "address": "For All Devices",
          "type": "push"
        }
      ]"""
}
