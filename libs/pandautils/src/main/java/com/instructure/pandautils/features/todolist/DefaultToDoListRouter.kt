/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.features.todolist

import org.threeten.bp.LocalDate

/**
 * Default implementation of ToDoListRouter with no-op implementations.
 * Used when the app doesn't need custom routing behavior.
 */
class DefaultToDoListRouter : ToDoListRouter {

    override fun openNavigationDrawer() {
        // No-op implementation
    }

    override fun attachNavigationDrawer() {
        // No-op implementation
    }

    override fun openToDoItem(htmlUrl: String) {
        // No-op implementation
    }

    override fun openCalendar(date: LocalDate) {
        // No-op implementation
    }
}