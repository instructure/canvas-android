package com.instructure.teacher.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.replaceText
import com.instructure.teacher.R

/**
 * Represents the Login Find School Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the login
 * find school page. It contains various view elements such as toolbar, "What's your school name"
 * text view, dividers, domain input edit text, find school recycler view, and toolbar next menu button.
 */
@Suppress("unused")
class LoginFindSchoolPage: BasePage() {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val whatsYourSchoolNameTextView by OnViewWithId(R.id.whatsYourSchoolName)
    private val topDivider by OnViewWithId(R.id.topDivider)
    private val bottomDivider by OnViewWithId(R.id.bottomDivider)
    private val domainInputEditText by OnViewWithId(R.id.domainInput)
    private val findSchoolRecyclerView by OnViewWithId(R.id.findSchoolRecyclerView)
    private val toolbarNextMenuButton by OnViewWithId(R.id.next)

    /**
     * Clicks the toolbar next menu item.
     */
    fun clickToolbarNextMenuItem() {
        toolbarNextMenuButton.click()
    }

    /**
     * Enters the domain into the domain input edit text.
     *
     * @param domain The domain to enter.
     */
    fun enterDomain(domain: String) {
        domainInputEditText.replaceText(domain)
    }
}

