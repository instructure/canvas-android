/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.student.mobius.syllabus.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.syllabus.SyllabusRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SyllabusRepositoryFragment : SyllabusFragment() {

    @Inject
    lateinit var syllabusRepository: SyllabusRepository

    @Inject
    lateinit var webViewRouter: WebViewRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SyllabusView(canvasContext, webViewRouter, inflater, parent)

    companion object {

        fun makeRoute(course: Course): Route {
            return Route(null, SyllabusRepositoryFragment::class.java, course, course.makeBundle())
        }

        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course
        }

        fun newInstance(route: Route): SyllabusRepositoryFragment? {
            if (!validRoute(route)) return null

            return SyllabusRepositoryFragment().withArgs(route.arguments)
        }
    }

    override fun getRepository(): SyllabusRepository = syllabusRepository
}