/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.model;

public class CourseColorDataSync {

    //The Canvas Context ID.
    public long contextId;

    //The HEX VALUE AARRGGBB of a color.
    public String color;

    public CourseColorDataSync(){}

    public CourseColorDataSync(long contextId, String color) {
        this.contextId = contextId;
        this.color = color;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append(" - CANVAS ID: ");
        builder.append(contextId);
        builder.append("\n");
        builder.append(" - CANVAS COLOR: ");
        builder.append(color);

        return builder.toString();
    }
}
