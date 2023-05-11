/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.db.sqlColAdapters

import com.squareup.sqldelight.ColumnAdapter
import com.instructure.canvasapi2.models.CanvasContext

class CanvasContextColAdapter : ColumnAdapter<CanvasContext, String> {
    override fun decode(databaseValue: String): CanvasContext {
       return CanvasContext.fromContextCode(databaseValue) ?: CanvasContext.defaultCanvasContext()
    }

    override fun encode(value: CanvasContext): String {
        return value.contextId
    }
}