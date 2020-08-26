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
 */
package com.instructure.espresso

import java.util.*

private val RANDOM = Random()
private val DIGITS = "0123456789"
private val CHARS = "0123456789abcdefghijklmnopqrstuvwxyz"

fun randomString(length: Int = 20): String = StringBuilder().apply {
    repeat(length) { append(CHARS[RANDOM.nextInt(CHARS.length)]) }
}.toString()

fun randomDouble(length: Int = 8): Double = StringBuilder().apply {
    repeat(length) { append(DIGITS[RANDOM.nextInt(DIGITS.length)]) }
}.toString().toDouble()
