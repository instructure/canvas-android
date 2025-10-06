/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.ui.pages.classic

import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.Atoms
import androidx.test.espresso.web.model.Atoms.transform
import androidx.test.espresso.web.model.ElementReference
import androidx.test.espresso.web.model.Evaluation
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.DriverAtoms.clearElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView
import androidx.test.espresso.web.webdriver.Locator.CLASS_NAME
import androidx.test.espresso.web.webdriver.Locator.ID
import com.instructure.student.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsString

/** Singleton object for performing operations on the conferences page.
 * As it is a webview, we get to use a lot of javascript to write our tests!
 * That means that these tests are potentially brittle; they will be broken
 * if any of the html element ids / class names that we key on get changed
 * by the web folks.
 */
object ConferencesPage {

    // Because two elements had the class name of "btn-primary", we had to create our own
    // script/atom to specify "use the second one".
    private val FIND_UPDATE_BUTTON_ATOM_SCRIPT = """
        function() {
            candidates = document.getElementsByClassName("btn-primary")
            return candidates[1]
        }
    """.trimIndent()

    private val findUpdateButtonAtom = Atoms.script(FIND_UPDATE_BUTTON_ATOM_SCRIPT, { evaluation ->
        evaluation.value as ElementReference
    })


    // Script and functions to find an element with the given class name containing the given text.
    // arguments[0]: Class name of element for which we are searching
    // arguments[1]: Text that we would like to find in that element
    private val CHECK_ELEMENT_TEXT_ATOM_SCRIPT = """
        function() {
          candidates = document.getElementsByClassName(arguments[0])
          candidatesArray = [].slice.call(candidates)
          result = candidatesArray.find(element => element.innerText.toLowerCase().includes(arguments[1].toLowerCase()));
          return result;
        }
        """.trimIndent()

    private fun findConferenceTitleAtom(title: String) : Atom<Evaluation> {
        return Atoms.scriptWithArgs(CHECK_ELEMENT_TEXT_ATOM_SCRIPT, listOf("ig-title", title))
    }

    private fun findConferenceDescriptionAtom(description: String) : Atom<Evaluation> {
        return Atoms.scriptWithArgs(CHECK_ELEMENT_TEXT_ATOM_SCRIPT, listOf("ig-details", description))
    }

    /**
     * Create a conference with a given name/title and description
     */
    fun createConference(name: String, description: String) {
        // Bring up new conference screen
        Web.onWebView(Matchers.allOf(ViewMatchers.withId(R.id.contentWebView), ViewMatchers.isDisplayed()))
                .withElement(DriverAtoms.findElement(CLASS_NAME, "new-conference-btn"))
                .perform(webClick())

        // Populate the title
        Web.onWebView(Matchers.allOf(ViewMatchers.withId(R.id.contentWebView), ViewMatchers.isDisplayed()))
                .withElement(DriverAtoms.findElement(ID, "web_conference_title"))
                .perform(webScrollIntoView())
                .perform(clearElement())
                .perform(webKeys(name))

        // Populate the description
        Web.onWebView(Matchers.allOf(ViewMatchers.withId(R.id.contentWebView), ViewMatchers.isDisplayed()))
                .withElement(DriverAtoms.findElement(ID, "web_conference_description"))
                .perform(webScrollIntoView())
                .perform(clearElement())
                .perform(webKeys(description))

        // Press the button that creates the conference
        Web.onWebView(Matchers.allOf(ViewMatchers.withId(R.id.contentWebView), ViewMatchers.isDisplayed()))
                .withElement(findUpdateButtonAtom)
                .perform(webScrollIntoView())
                .perform(webClick())
    }

    /** Assert that a conference with the specified name/title is displayed on the screen. */
    fun assertConferenceTitlePresent(title: String) {
        Web.onWebView(Matchers.allOf(ViewMatchers.withId(R.id.contentWebView), ViewMatchers.isDisplayed()))
                .withElement(transform(findConferenceTitleAtom(title), { evaluation ->
                    evaluation.value as ElementReference}))
                .perform(webScrollIntoView())
                .check(webMatches(getText(), containsString(title)))
    }

    /** Assert that a conference with the specified description is displayed on the screen. */
    fun assertConferenceDescriptionPresent(description: String) {
        Web.onWebView(Matchers.allOf(ViewMatchers.withId(R.id.contentWebView), ViewMatchers.isDisplayed()))
                .withElement(transform(findConferenceDescriptionAtom(description), { evaluation ->
                    evaluation.value as ElementReference}))
                .perform(webScrollIntoView())
                .check(webMatches(getText(), containsString(description)))
    }


}