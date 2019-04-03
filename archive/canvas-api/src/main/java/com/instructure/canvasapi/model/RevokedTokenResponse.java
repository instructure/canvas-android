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

package com.instructure.canvasapi.model;

import com.google.gson.annotations.SerializedName;

/**
 * This is a response body for use in the ParentErrorDelegate class in the parentApp
 */
public class RevokedTokenResponse {

    @SerializedName("student_id")
    public String studentId;

    @SerializedName("parent_id")
    public String parentId;

    @SerializedName("student_name")
    public String studentName;

    @SerializedName("student_domain")
    public String studentDomain;

    @SerializedName("domain_name")
    public String domainName;

    @SerializedName("sortable_name")
    public String sortableName;

    @SerializedName("short_name")
    public String shortName;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("locale")
    public String locale;

}
