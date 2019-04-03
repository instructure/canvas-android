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

import java.text.SimpleDateFormat
import java.util.*


val Int.week: Pair<Int, Int>
    get() =
        Pair(Calendar.WEEK_OF_YEAR, this)

val Int.days: Pair<Int, Int>
    get() =
        Pair(Calendar.DAY_OF_YEAR, this)

val Pair<Int, Int>.fromNow: Calendar
    get() {
        val cal = Calendar.getInstance()
        cal.add(this.first, this.second)
        return cal
    }

val Pair<Int, Int>.ago: Calendar
    get() {
        val cal = Calendar.getInstance()
        cal.add(this.first, -this.second)
        return cal
    }


val Calendar.iso8601: String
    get() {
        val formatted = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date(this.timeInMillis))
        return formatted.substring(0, 22) + ":" + formatted.substring(22)
    }