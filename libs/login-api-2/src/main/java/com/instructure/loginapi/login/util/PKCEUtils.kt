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
package com.instructure.loginapi.login.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

data class PkcePair(
    val verifier: String,
    val challenge: String,
    val method: String = PKCEUtils.METHOD_S256
)

object PKCEUtils {

    const val METHOD_S256 = "S256"

    private const val VERIFIER_BYTE_LENGTH = 64
    private val encoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()

    fun generate(random: SecureRandom = SecureRandom()): PkcePair {
        val verifier = generateVerifier(random)
        return PkcePair(verifier = verifier, challenge = challengeFor(verifier))
    }

    fun challengeFor(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return encoder.encodeToString(digest)
    }

    private fun generateVerifier(random: SecureRandom): String {
        val bytes = ByteArray(VERIFIER_BYTE_LENGTH)
        random.nextBytes(bytes)
        return encoder.encodeToString(bytes)
    }
}
