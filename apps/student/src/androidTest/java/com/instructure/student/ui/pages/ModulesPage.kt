/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.pages

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.dataseeding.model.ModuleApiModel
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.withAncestor
import com.instructure.student.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

class ModulesPage : BasePage(R.id.modulesPage) {
    fun assertModuleDisplayed(module: ModuleApiModel) {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.listView), ViewMatchers.isDisplayed()))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(ViewMatchers.withText(module.name))))

    }

    fun assertEmptyView() {
        onView(allOf(withId(R.id.emptyView), withAncestor(R.id.modulesPage))).assertDisplayed()
    }
}