/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

object Const {
    const val NORMAL_FLOW = 0
    const val CANVAS_LOGIN_FLOW = 1
    const val MASQUERADE_FLOW = 2
    const val MOBILE_VERIFY_FLOW = 3
    const val SNICKER_DOODLES = "snickerDoodles"

    const val CANVAS_LOGIN = "canvas_login"
    const val FROM_LOGIN = "fromLogin"
    const val APP_NAME = "appName"
    const val PRE_FILL_DATA: String = "preFillData"

    const val URL_CANVAS_NETWORK = "learn.canvas.net"

    const val NO_LOCATION_INDICATOR_INT = -999

    const val noPictureURL = "images/dotted_pic.png"
    const val USE_DEFAULT_DOMAIN = "useDefaultDomain"

    const val FIND_SCHOOL_HELP_URL = "https://community.instructure.com/en/kb/articles/662717-where-do-i-find-my-institutions-url-to-access-canvas"
}
