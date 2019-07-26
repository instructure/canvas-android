package com.instructure.student.ui.pages

import com.instructure.espresso.OnViewWithStringTextIgnoreCase
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

// This is a little hokey, as the options that appear are somewhat governed by the results of
// the /api/v1/accounts/self/help_links call.  If that changes a lot over time (thus breaking
// this test), we can back off to some easier test like "some options are visible".
class HelpPage : BasePage(R.id.helpDialog) {
    private val askInstructorLabel by OnViewWithText(R.string.askInstructor)
    private val searchGuidesLabel by OnViewWithText(R.string.searchGuides)
    private val reportProblemLabel by OnViewWithText(R.string.reportProblem)
    private val submitFeatureLabel by OnViewWithStringTextIgnoreCase("Submit a Feature Idea")
    private val shareLoveLabel by OnViewWithText(R.string.shareYourLove)
}