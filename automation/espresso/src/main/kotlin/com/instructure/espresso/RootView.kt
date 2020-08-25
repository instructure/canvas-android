//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package com.instructure.espresso

import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import android.view.View

import org.hamcrest.Matcher
import org.hamcrest.Matchers

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot

class RootView : ViewAction {
    var rootView: View? = null
        private set

    override fun getConstraints(): Matcher<View> {
        return Matchers.allOf(ViewMatchers.isRoot())
    }

    override fun getDescription(): String {
        return "get root view"
    }

    override fun perform(uiController: UiController, view: View) {
        rootView = view
    }

    companion object {

        fun get(): View? {
            val getRoot = RootView()
            onView(isRoot()).perform(getRoot)
            return getRoot.rootView
        }
    }
}
