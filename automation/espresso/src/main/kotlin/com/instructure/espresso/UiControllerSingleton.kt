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
import android.view.View

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot

object UiControllerSingleton {
    private var uiController: UiController? = null

    fun exists(): Boolean {
        return uiController != null
    }

    fun get(): UiController? {
        if (uiController != null) {
            return uiController
        }

        onView(isRoot()).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return object : TypeSafeMatcher<View>() {
                    override fun describeTo(description: Description) {
                        description.appendText("is anything.")
                    }

                    public override fun matchesSafely(view: View): Boolean {
                        return true
                    }
                }
            }

            override fun getDescription(): String {
                return "UiControllerSingleton view action"
            }

            override fun perform(controllerObject: UiController, view: View) {
                uiController = controllerObject
            }
        })

        if (uiController == null) {
            throw RuntimeException("uiController is null!")
        }

        return uiController
    }
}
