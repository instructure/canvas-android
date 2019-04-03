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


package com.instructure.dataseeding.util

import java.util.concurrent.TimeUnit.*
import kotlin.math.pow

object RetryBackoff {

    private val Int.minutes
        get() = MINUTES.toSeconds(this.toLong())

    private val Long.millis
        get() = SECONDS.toMillis(this)

    private fun sleep(seconds: Long) {
        Thread.sleep(seconds.millis)
    }

    fun wait(attempt: Int) {
        // use AWS retry_backoff:
        // https://github.com/aws/aws-sdk-ruby/blob/a2e85a4db05f52804a1ec9bc1552732c7bb8a7b8/aws-sdk-core/lib/aws-sdk-core/plugins/retry_errors.rb#L16
        val backoff = 2f.pow(attempt) * 0.3
        sleep(seconds = minOf(5.minutes, backoff.toLong()))
    }
}
