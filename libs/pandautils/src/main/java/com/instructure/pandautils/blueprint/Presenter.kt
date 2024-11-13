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
package com.instructure.pandautils.blueprint

/**
 * Presenter Responsibilities
 *
 * Decide when the data is loaded based on the presentation lifecycle.
 * Decide what is the part of the model used to retrieve data.
 * React to view events to stimulate the software if needed.
 * Validate input data.
 * Transform information if needed to avoid coupling the view implementation to the domain layer implementation.
 * Decide what to do for every incoming event
 * Retrieving or updating data from the Model and preparing the data so the View can display it
 * Is not aware of Context, Activity or any thing of that sort - getApplicationContext() might be okay
 */
interface Presenter<VIEW> {
    fun onViewAttached(view: VIEW): Presenter<VIEW>
    fun onViewDetached()
    fun onDestroyed()
}
