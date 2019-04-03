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

import com.instructure.canvasapi2.models.NotoriousConfig
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class NotoriousUnitTest : Assert() {

    @Test
    fun test1() {
        val notoriousConfig: NotoriousConfig = notoriousConfigJSON.parse()

        Assert.assertNotNull(notoriousConfig)
        Assert.assertTrue(notoriousConfig.isEnabled)
        Assert.assertNotNull(notoriousConfig.domain == "www.instructuremedia.com")
        Assert.assertTrue(notoriousConfig.partnerId == 101L)
    }

    /** Notorious Config */
    @Language("JSON")
    private val notoriousConfigJSON = """
      {
        "enabled": true,
        "domain": "www.instructuremedia.com",
        "resource_domain": "www.instructuremedia.com",
        "rtmp_domain": "rtmp.instructuremedia.com",
        "partner_id": "101"
      }"""
}
