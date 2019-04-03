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

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class CanvasTheme(
        @SerializedName("ic-brand-primary")
        var brand: String, // TODO: null?
        @SerializedName("ic-brand-font-color-dark")
        var fontColorDark: String, // TODO: null?
        @SerializedName("ic-brand-button--primary-bgd")
        var button: String, // TODO: null?
        @SerializedName("ic-brand-button--primary-text")
        var buttonText: String, // TODO: null?
        @SerializedName("ic-brand-global-nav-bgd")
        var primary: String, // TODO: null?
        @SerializedName("ic-brand-global-nav-menu-item__text-color")
        var primaryText: String, // TODO: null?
        @SerializedName("ic-brand-global-nav-menu-item__text-color--active")
        var accent: String, // TODO: null?
        @SerializedName("ic-brand-header-image")
        var logoUrl: String // TODO: null?
) : CanvasComparable<CanvasTheme>()