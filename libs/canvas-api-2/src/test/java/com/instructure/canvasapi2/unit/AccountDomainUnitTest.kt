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

import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class AccountDomainUnitTest : Assert() {

    @Test
    fun testAccountDomain() {
        val accountDomains: Array<AccountDomain> = accountDomainsJSON.parse()
        Assert.assertNotNull(accountDomains)
        Assert.assertEquals(8, accountDomains.size)
        for (accountDomain in accountDomains) {
            Assert.assertNotNull(accountDomain.name)
            Assert.assertNotNull(accountDomain.domain)
        }
    }

    @Language("JSON")
    private val accountDomainsJSON = """
      [
        {
          "name": "Northeastern Panda Educational",
          "domain": "npe.instructure.com",
          "distance": null
        },
        {
          "name": "Southern Panda University",
          "domain": "spu.instructure.com",
          "distance": null
        },
        {
          "name": "University of Panda",
          "domain": "panda.instructure.com",
          "distance": null
        },
        {
          "name": "Panda Education Network",
          "domain": "pen.instructure.com",
          "distance": null
        },
        {
          "name": "Panda Electronic High School",
          "domain": "learn.panda.org",
          "distance": null
        },
        {
          "name": "Panda State University",
          "domain": "psu.instructure.com",
          "distance": null
        },
        {
          "name": "Panda Students Connect",
          "domain": "pandastudentsconnect.instructure.com",
          "distance": null
        },
        {
          "name": "Panda Valley University",
          "domain": "pvu.instructure.com",
          "distance": null
        }
      ]"""
}
