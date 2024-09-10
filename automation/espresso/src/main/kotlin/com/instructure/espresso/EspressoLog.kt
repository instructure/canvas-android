//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package com.instructure.espresso

import android.util.Log

import androidx.test.espresso.core.internal.deps.dagger.internal.Preconditions.checkNotNull

/**
 * Wrapper for android.util.log
 */
class EspressoLog
/**
 * Create a logger using class simple name for the tag.
 * @param klass The class to derive the tag from
 */
(klass: Class<*>) {

    private val tag: String

    init {
        checkNotNull(klass)
        this.tag = klass.simpleName
    }

    /**
     * Send a [android.util.Log.VERBOSE] log message.
     *
     * @param msg The message you would like logged.
     */
    fun v(msg: String): Int {
        checkNotNull(msg)
        return Log.v(tag, msg)
    }

    /**
     * Send a [android.util.Log.VERBOSE] log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun v(msg: String, tr: Throwable): Int {
        checkNotNull(msg)
        return Log.v(tag, msg, tr)
    }

    /**
     * Send a [android.util.Log.DEBUG] log message.
     *
     * @param msg The message you would like logged.
     */
    fun d(msg: String): Int {
        checkNotNull(msg)
        return Log.d(tag, msg)
    }

    /**
     * Send a [android.util.Log.DEBUG] log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun d(msg: String, tr: Throwable): Int {
        checkNotNull(msg)
        return Log.d(tag, msg, tr)
    }

    /**
     * Send an [android.util.Log.INFO] log message.
     *
     * @param msg The message you would like logged.
     */
    fun i(msg: String): Int {
        checkNotNull(msg)
        return Log.i(tag, msg)
    }

    /**
     * Send a [android.util.Log.INFO] log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun i(msg: String, tr: Throwable): Int {
        checkNotNull(msg)
        return Log.i(tag, msg, tr)
    }

    /**
     * Send a [android.util.Log.WARN] log message.
     *
     * @param msg The message you would like logged.
     */
    fun w(msg: String): Int {
        checkNotNull(msg)
        return Log.w(tag, msg)
    }

    /**
     * Send a [android.util.Log.WARN] log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun w(msg: String, tr: Throwable): Int {
        checkNotNull(msg)
        return Log.w(tag, msg, tr)
    }

    /**
     * Send a [android.util.Log.WARN] log message and log the exception.
     *
     * @param tr An exception to log
     */
    fun w(tr: Throwable): Int {
        return Log.w(tag, tr)
    }

    /**
     * Send an [android.util.Log.ERROR] log message.
     *
     * @param msg The message you would like logged.
     */
    fun e(msg: String): Int {
        checkNotNull(msg)
        return Log.e(tag, msg)
    }

    /**
     * Send a [android.util.Log.ERROR] log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun e(msg: String, tr: Throwable): Int {
        checkNotNull(msg)
        return Log.e(tag, msg, tr)
    }
}
