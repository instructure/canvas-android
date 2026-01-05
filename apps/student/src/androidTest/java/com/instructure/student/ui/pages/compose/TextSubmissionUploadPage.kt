package com.instructure.student.ui.pages.compose

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.instructure.canvas.espresso.TypeInRCETextEditor
import com.instructure.canvas.espresso.explicitClick
import com.instructure.composetest.clickToolbarIconButton
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.RCETextEditorContentAssertion
import com.instructure.espresso.RCETextEditorHtmlAssertion
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withText
import com.instructure.student.R

class TextSubmissionUploadPage(private val composeTestRule: ComposeTestRule) : BasePage(R.id.textSubmissionUpload) {

    val submitButton by OnViewWithId(R.id.menuSubmit)
    val contentRceView by OnViewWithId(R.id.rce_webView)
    val textEntryLabel by OnViewWithText(R.string.textEntry)

    fun typeText(textToType: String) {
        contentRceView.perform(TypeInRCETextEditor(textToType))
    }

    fun clickToolbarBackButton() {
        composeTestRule.clickToolbarIconButton("Back")
    }

    fun clickOnSubmitButton() {
        submitButton.perform(explicitClick())
    }

    fun clickCancel() {
        onView(withText(com.instructure.pandautils.R.string.cancel)).click()
    }

    fun clickSaveDraft() {
        waitForViewWithText(com.instructure.pandautils.R.string.save).click()
    }

    fun clickDontSaveDraft() {
        waitForViewWithText(com.instructure.pandautils.R.string.dontSave).click()
    }

    fun assertTextSubmissionContentDescriptionDisplayed(expectedText: String) {
        waitForViewWithId(com.instructure.pandautils.R.id.rce_webView).check(
            RCETextEditorContentAssertion(expectedText)
        )
    }

    fun assertTextSubmissionDisplayed(expectedHtml: String) {
        waitForViewWithId(com.instructure.pandautils.R.id.rce_webView).check(
            RCETextEditorHtmlAssertion(expectedHtml)
        )
    }
}