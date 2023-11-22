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

import android.os.Build
import androidx.annotation.RequiresApi
import org.apache.commons.lang3.StringUtils
import java.time.LocalDateTime
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

fun capitalizeFirstLetter(inputText: String): String {
    return if (inputText.isNotEmpty()) {
        val firstLetter = inputText.substring(0, 1).uppercase()
        val restOfWord = inputText.substring(1).lowercase()
        firstLetter + restOfWord
    } else StringUtils.EMPTY
}


@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDateInCanvasFormat(): String {
    val expectedDate = LocalDateTime.now()
    val monthString = capitalizeFirstLetter(expectedDate.month.name.take(3))
    val dayString = expectedDate.dayOfMonth
    val yearString = expectedDate.year
    return "$monthString $dayString, $yearString"
}

fun retry(
    times: Int = 3,
    delay: Long = 1000,
    block: () -> Unit
) {
    repeat(times - 1) {
        try {
            block()
            return
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        Thread.sleep(3000)
    }
    block()
}