package com.instructure.teacher.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.replaceText
import com.instructure.teacher.R

@Suppress("unused")
class LoginFindSchoolPage: BasePage() {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val whatsYourSchoolNameTextView by OnViewWithId(R.id.whatsYourSchoolName)
    private val topDivider by OnViewWithId(R.id.topDivider)
    private val bottomDivider by OnViewWithId(R.id.bottomDivider)
    private val domainInputEditText by OnViewWithId(R.id.domainInput)
    private val findSchoolRecyclerView by OnViewWithId(R.id.findSchoolRecyclerView)
    private val toolbarNextMenuButton by OnViewWithId(R.id.next)

    fun clickToolbarNextMenuItem() {
        toolbarNextMenuButton.click()
    }

    fun enterDomain(domain: String) {
        domainInputEditText.replaceText(domain)
    }
}
