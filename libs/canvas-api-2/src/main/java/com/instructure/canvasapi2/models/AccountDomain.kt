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

package com.instructure.canvasapi2.models

import android.os.Parcelable

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

import java.util.Date

@Parcelize
data class AccountDomain(
        var domain: String? = null,
        var name: String? = null,
        var distance: Double = 0.0,
        @SerializedName("authentication_provider")
        val authenticationProvider: String? = null

) : CanvasModel<AccountDomain>(), Parcelable{
    override val id: Long get() = 0
    override val comparisonString: String? get() = domain
    override fun compareTo(other: AccountDomain): Int = this.distance.compareTo(other.distance)

    override fun toString(): String = "$name --- $domain"
}
