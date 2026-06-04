/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.loginapi.login.unit

import com.instructure.loginapi.login.util.PKCEUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PKCEUtilsTest {

    private val verifierCharset = Regex("^[A-Za-z0-9\\-_]+$")

    @Test
    fun `challengeFor matches RFC 7636 Appendix B test vector`() {
        // From RFC 7636, Appendix B
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val expectedChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"

        assertEquals(expectedChallenge, PKCEUtils.challengeFor(verifier))
    }

    @Test
    fun `generated pair uses S256 method`() {
        val pair = PKCEUtils.generate()
        assertEquals(PKCEUtils.METHOD_S256, pair.method)
    }

    @Test
    fun `generated challenge is sha256 of verifier`() {
        val pair = PKCEUtils.generate()
        assertEquals(PKCEUtils.challengeFor(pair.verifier), pair.challenge)
    }

    @Test
    fun `verifier length is within RFC 7636 bounds`() {
        val verifier = PKCEUtils.generate().verifier
        assertTrue("Verifier length ${verifier.length} out of [43, 128]", verifier.length in 43..128)
    }

    @Test
    fun `verifier uses unreserved url-safe charset`() {
        val verifier = PKCEUtils.generate().verifier
        assertTrue("Verifier '$verifier' contains disallowed characters", verifierCharset.matches(verifier))
    }

    @Test
    fun `challenge uses unreserved url-safe charset`() {
        val challenge = PKCEUtils.generate().challenge
        assertTrue("Challenge '$challenge' contains disallowed characters", verifierCharset.matches(challenge))
    }

    @Test
    fun `successive generate calls produce distinct verifiers`() {
        val verifiers = (1..20).map { PKCEUtils.generate().verifier }.toSet()
        assertEquals("Generated verifiers were not unique", 20, verifiers.size)
    }

    @Test
    fun `generated verifier and challenge differ`() {
        val pair = PKCEUtils.generate()
        assertNotEquals(pair.verifier, pair.challenge)
    }
}
