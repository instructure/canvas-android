package com.instructure.student.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

class LegalPage : BasePage(R.id.legalPage) {
    private val privacyPolicyLabel by OnViewWithId(R.id.privacyPolicyLabel)
    private val termsOfUseLabel by OnViewWithId(R.id.termsOfUseLabel)
    private val openSourceLabel by OnViewWithId(R.id.openSourceLabel)

}