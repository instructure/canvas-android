/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.postmodels.PagePostBody
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_PAGE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.dialog.UnsavedChangesExitDialog
import com.instructure.student.events.PageUpdatedEvent
import kotlinx.android.synthetic.main.fragment_edit_page.*
import org.greenrobot.eventbus.EventBus

@ScreenView(SCREEN_VIEW_EDIT_PAGE_DETAILS)
class EditPageDetailsFragment : ParentFragment() {

    private var apiJob: WeaveJob? = null
    private var rceImageJob: WeaveJob? = null

    /* The page to be edited */
    private var page: Page by ParcelableArg(key = Const.PAGE)
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private val saveMenuButton get() = toolbar.menu.findItem(R.id.menuSavePage)
    private val saveButtonTextView: TextView? get() = view?.findViewById(R.id.menuSavePage)

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_edit_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        setupDescription()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pageRCEView.actionUploadImageCallback = {
            MediaUploadUtils.showPickImageDialog(this)
        }
        pageRCEView.requestEditorFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rceImageJob?.cancel()
        apiJob?.cancel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSavePage -> savePage()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // Get the image Uri
            when (requestCode) {
                RequestCodes.PICK_IMAGE_GALLERY -> data?.data
                RequestCodes.CAMERA_PIC_REQUEST -> MediaUploadUtils.handleCameraPicResult(requireActivity(), null)
                else -> null
            }?.let { imageUri ->
                // If the image Uri is not null, upload it
                rceImageJob = MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext, requireActivity()) { text, alt -> pageRCEView.insertImage(text, alt) }
            }
        }
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun applyTheme() = Unit

    override fun title(): String = getString(R.string.editPage)
    //endregion

    //region Functionality

    private fun shouldAllowExit(): Boolean {
        // Check if edited page has changes
        return page.id != 0L && page.body ?: "" == pageRCEView?.html
    }

    private fun setupDescription() {
        pageRCEView.setHtml(
                page.body,
                getString(R.string.pageDetails),
                getString(R.string.rce_empty_description),
                ThemePrefs.brandColor, ThemePrefs.buttonColor
        )
        // when the RCE editor has focus we want the label to be darker so it matches the title's functionality
        pageRCEView.setLabel(pageDescLabel, R.color.defaultTextDark, R.color.defaultTextGray)
    }

    private fun savePage() {
        onSaveStarted()
        apiJob = tryWeave {
            val postBody = PagePostBody(
                    pageRCEView.html,
                    page.title,
                    page.frontPage == true,
                    page.editingRoles,
                    page.published == true
            )

            val updatedPage = awaitApi<Page> { PageManager.editPage(canvasContext, page.url ?: "", postBody, it) }
            EventBus.getDefault().post(PageUpdatedEvent(updatedPage))

            onSaveSuccess()
        } catch {
            onSaveError()
        }
    }

    private fun onSaveStarted() {
        saveMenuButton.isVisible = false
        savingProgressBar.announceForAccessibility(getString(R.string.saving))
        savingProgressBar.setVisible()
    }

    private fun onSaveError() {
        saveMenuButton.isVisible = true
        savingProgressBar.setGone()
        toast(R.string.errorSavingPage)
    }

    private fun onSaveSuccess() {
        toast(R.string.pageSuccessfullyUpdated)
        requireActivity().onBackPressed() // close this fragment
    }
    //endregion

    //region Setup
    private fun setupToolbar() {
        toolbar.setupAsCloseButton {
            if (shouldAllowExit()) {
                activity?.onBackPressed()
            } else {
                UnsavedChangesExitDialog.show(requireFragmentManager()) { activity?.onBackPressed() }
            }
        }
        toolbar.title = page.title
        setupToolbarMenu(toolbar, R.menu.menu_edit_page)
        ViewStyler.themeToolbarBottomSheet(requireActivity(), isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        with(saveMenuButton) {
            setIcon(0)
            setTitle(R.string.save)
        }
        saveButtonTextView?.setTextColor(ThemePrefs.buttonColor)
    }
    //endregion



    companion object {

        fun makeRoute(canvasContext: CanvasContext, page: Page): Route {
            val bundle = Bundle().apply { putParcelable(Const.PAGE, page) }
            return Route(EditPageDetailsFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null && route.arguments.getParcelable<Page>(Const.PAGE) != null
        }

        fun newInstance(route: Route): EditPageDetailsFragment? {
            if (!validateRoute(route)) return null
            return EditPageDetailsFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }

    }
}
