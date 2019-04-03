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
package com.instructure.canvasapi2.models.postmodels

import com.google.gson.annotations.SerializedName


/**
 * Editing a quiz requires us to use a GSON serializer that serializes null. Because of this, we need to
 * make sure that we're not replacing existing values with null. So don't put a value here unless you
 * are setting it in the EditQuizDetailsFragment
 */
class QuizPostBody {

    var title: String? = null

    var description: String? = null

    @SerializedName("notify_of_update")
    var notifyOfUpdate: Boolean? = null

    var published: Boolean? = null

    @SerializedName("access_code")
    var accessCode: String? = null

    @SerializedName("quiz_type")
    var quizType: String? = null
}

class QuizPostBodyWrapper {
    var quiz: QuizPostBody? = null
}
