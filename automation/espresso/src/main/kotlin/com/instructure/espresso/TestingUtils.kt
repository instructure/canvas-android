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
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.instructure.espresso.page.plus
import com.instructure.pandautils.binding.BindableViewHolder
import org.apache.commons.lang3.StringUtils
import org.hamcrest.Matcher
import org.junit.Assert
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.TimeZone

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
fun getDateInCanvasFormat(date: LocalDateTime? = null): String {
    val expectedDate = date ?: LocalDateTime.now()
    val monthString = capitalizeFirstLetter(expectedDate.month.name.take(3))
    val dayString = expectedDate.dayOfMonth
    val yearString = expectedDate.year
    return "$monthString $dayString, $yearString"
}

fun convertIso8601ToCanvasFormat(iso8601Date: String): String {
    val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = iso8601Format.parse(iso8601Date) ?: throw IllegalArgumentException("Invalid date format: $iso8601Date")

    val calendar = Calendar.getInstance()
    calendar.time = date

    val monthString = capitalizeFirstLetter(SimpleDateFormat("MMM", Locale.getDefault()).format(date))
    val dayString = calendar.get(Calendar.DAY_OF_MONTH)
    val yearString = calendar.get(Calendar.YEAR)

    return "$monthString $dayString, $yearString"
}


fun getDateInCanvasCalendarFormat(dateString: String? = getCurrentDateInIso8601()): String {
    val calendar = Calendar.getInstance()

    val day = calendar.get(Calendar.DAY_OF_MONTH)

    if(dateString != null) {
        return if (day in 1..9) formatIso8601ToCustom(dateString, SimpleDateFormat("MMM d", Locale.getDefault()))
        else formatIso8601ToCustom(dateString, SimpleDateFormat("MMM dd", Locale.getDefault()))
    }

    var dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    if (day in 1..9) dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    return dateFormat.format(Date())
}

fun getCurrentDateInIso8601(): String {
    val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    return iso8601Format.format(Date())
}

fun formatIso8601ToCustom(iso8601DateString: String?, customDateFormat: SimpleDateFormat): String {

    val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    val date: Date? = iso8601DateString?.let { iso8601Format.parse(it) }

    return customDateFormat.format(date!!)
}
    
fun getCustomDateCalendar(dayDiffFromToday: Int): Calendar {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.add(Calendar.DATE, dayDiffFromToday)
    cal.set(Calendar.HOUR_OF_DAY, 10)
    cal.set(Calendar.MINUTE, 1)
    cal.set(Calendar.SECOND, 1)
    return cal
}

fun retry(
    times: Int = 3,
    delay: Long = 1000,
    catchBlock: (() -> Unit)? = null,
    block: () -> Unit
) {
    repeat(times - 1) {
        try {
            block()
            return
        } catch (e: Throwable) {
            e.printStackTrace()
            Thread.sleep(delay)
            catchBlock?.invoke()
        }
    }
    block()
}

fun retryWithIncreasingDelay(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    catchBlock: (() -> Unit)? = null,
    block: () -> Unit
) {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            block()
            return
        } catch (e: Throwable) {
            e.printStackTrace()
            Thread.sleep(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            catchBlock?.invoke()
        }
    }
    block()
}

fun withIdlingResourceDisabled(resourceName: String = "okhttp", block: () -> Unit) {
    val registry = IdlingRegistry.getInstance()
    val idlingResource = registry.resources.find { it.name == resourceName }

    if (idlingResource != null) {
        registry.unregister(idlingResource)
    }

    try {
        block()
    } finally {
        if (idlingResource != null) {
            registry.register(idlingResource)
        }
    }
}

fun extractInnerTextById(html: String, id: String): String? {
    val pattern = "<[^>]*?\\bid=\"$id\"[^>]*?>(.*?)</[^>]*?>".toRegex(RegexOption.DOT_MATCHES_ALL)
    val matchResult = pattern.find(html)
    return matchResult?.groupValues?.getOrNull(1)
}

fun waitForWebElement(
    webViewMatcher: Matcher<View>,
    locator: Locator,
    value: String,
    timeoutMillis: Long = 5000,
    intervalMillis: Long = 500
) {
    val endTime = System.currentTimeMillis() + timeoutMillis

    while (System.currentTimeMillis() < endTime) {
        try {
            Web.onWebView(webViewMatcher)
                .withElement(DriverAtoms.findElement(locator, value))
            return
        } catch (e: Exception) {
            Thread.sleep(intervalMillis)
        }
    }

    Assert.fail("Element not found: $locator=$value within $timeoutMillis ms")
}

fun scrollToItem(ancestorId: Int, itemText: String, recyclerView: Matcher<View>, target: Matcher<View>? = null) {
    var i: Int = getRecyclerViewLastVisibleItemPosition(getRecyclerViewFromMatcher(recyclerView))
    while (true) {
        scrollToPosition(recyclerView, i)
        Thread.sleep(300)
        try {
            if(target == null) onView(isDescendantOfA(withId(ancestorId)) + withText(itemText)).scrollTo()
            else onView(target + withText(itemText)).scrollTo()
            break
        } catch(e: NoMatchingViewException) {
            i+=1
        }
    }
}

fun scrollToPosition(recyclerViewMatcher: Matcher<View>, position: Int) {
    onView(recyclerViewMatcher).perform(RecyclerViewActions.scrollToPosition<BindableViewHolder>(position))
}

fun getRecyclerViewLastVisibleItemPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager
    return if (layoutManager is androidx.recyclerview.widget.LinearLayoutManager) {
        layoutManager.findLastVisibleItemPosition()
    } else {
        return -1
    }
}

fun getRecyclerViewFromMatcher(matcher: Matcher<View>): RecyclerView {
    var recyclerView: RecyclerView? = null

    onView(matcher).check { view, noViewFoundException ->
        if (noViewFoundException != null) {
            throw IllegalArgumentException("No view found matching the provided matcher", noViewFoundException)
        }
        if (view !is RecyclerView) {
            throw IllegalArgumentException("The view matched is not a RecyclerView")
        }
        recyclerView = view
    }

    return recyclerView ?: throw IllegalStateException("Failed to retrieve RecyclerView")
}

/**
 * Triggers all enqueued WorkManager jobs by setting their constraints as met.
 * This allows workers with network/battery/storage constraints to run in tests.
 *
 * @param tag Optional tag to filter workers. If null, triggers all enqueued workers.
 * @param timeoutMillis Maximum time to wait for workers to be enqueued (default: 10000ms)
 */
fun triggerWorkManagerJobs(tag: String? = null, timeoutMillis: Long = 10000) {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>() as? com.instructure.canvas.espresso.WorkManagerTestAppManager
    val testDriver = app?.testDriver

    if (testDriver == null) {
        return
    }

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val endTime = System.currentTimeMillis() + timeoutMillis
    var workInfos: List<WorkInfo>

    while (System.currentTimeMillis() < endTime) {
        workInfos = if (tag != null) {
            workManager.getWorkInfosByTag(tag).get().filter {
                it.state == WorkInfo.State.ENQUEUED
            }
        } else {
            workManager.getWorkInfos(WorkQuery.Builder.fromStates(listOf(WorkInfo.State.ENQUEUED)).build()).get()
        }

        if (workInfos.isNotEmpty()) {
            workInfos.forEach { workInfo ->
                testDriver.setAllConstraintsMet(workInfo.id)
            }
            Thread.sleep(200)
            waitForWorkManagerJobsToComplete(tag, timeoutMillis)
            return
        }

        Thread.sleep(100)
    }
}

/**
 * Waits for all WorkManager jobs with the specified tag to complete.
 *
 * @param tag Optional tag to filter workers. If null, waits for all workers.
 * @param timeoutMillis Maximum time to wait (default: 10000ms)
 */
private fun waitForWorkManagerJobsToComplete(tag: String? = null, timeoutMillis: Long = 10000) {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
    val startTime = System.currentTimeMillis()

    while (System.currentTimeMillis() - startTime < timeoutMillis) {
        val workInfos = if (tag != null) {
            workManager.getWorkInfosByTag(tag).get()
        } else {
            workManager.getWorkInfos(
                WorkQuery.Builder.fromStates(
                    listOf(
                        WorkInfo.State.RUNNING,
                        WorkInfo.State.ENQUEUED
                    )
                ).build()
            ).get()
        }

        val unfinished = workInfos.filter {
            !it.state.isFinished
        }

        if (unfinished.isEmpty()) {
            Espresso.onIdle()
            return
        }

        Thread.sleep(100)
    }
}
