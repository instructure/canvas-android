/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.teacher.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.parcelCopy
import com.instructure.interactions.Identity
import com.instructure.pandautils.analytics.SCREEN_VIEW_CREATE_OR_EDIT_PAGE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.factory.CreateOrEditPagePresenterFactory
import com.instructure.teacher.presenters.CreateOrEditPagePresenter
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.withRequireNetwork
import com.instructure.teacher.viewinterface.CreateOrEditPageView
import kotlinx.android.synthetic.main.fragment_create_or_edit_page.*

@ScreenView(SCREEN_VIEW_CREATE_OR_EDIT_PAGE_DETAILS)
class CreateOrEditPageDetailsFragment :
        BasePresenterFragment<CreateOrEditPagePresenter, CreateOrEditPageView>(),
        CreateOrEditPageView,
        Identity {

    /* The course this page belongs to */
    private var mCanvasContext by ParcelableArg<CanvasContext>(Course())

    /* The page to be edited. This will be null if we're creating a new page */
    private var mPage by NullableParcelableArg<Page>()

    /* Menu buttons. We don't cache these because the toolbar is reconstructed on configuration change. */
    private val mSaveMenuButton get() = toolbar.menu.findItem(R.id.menuSavePage)
    private val mSaveButtonTextView: TextView? get() = view?.findViewById(R.id.menuSavePage)

    private var placeHolderList: ArrayList<Placeholder> = ArrayList()
    private var forceQuit = false

    override val identity = 0L
    override val skipCheck = false
    override fun onRefreshFinished() {}
    override fun onRefreshStarted() {}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { }
    override fun onPresenterPrepared(presenter: CreateOrEditPagePresenter) {}
    override fun layoutResId(): Int = R.layout.fragment_create_or_edit_page

    override fun getPresenterFactory() = CreateOrEditPagePresenterFactory(mCanvasContext, mPage?.parcelCopy())

    override fun onReadySetGo(presenter: CreateOrEditPagePresenter) {
        setupViews()
        setupToolbar()
    }

    fun setupToolbar() {
        toolbar.setupCloseButton {
            activity?.onBackPressed()
        }
        toolbar.title = getString(if (presenter.isEditing) R.string.editPageTitle else R.string.createPageTitle)
        toolbar.setupMenu(R.menu.menu_create_or_edit_page) { menuItem ->
            when (menuItem.itemId) {
                R.id.menuSavePage -> withRequireNetwork { savePage() }
            }
        }
        ViewStyler.themeToolbarBottomSheet(requireActivity(), isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        with(mSaveMenuButton) {
            setIcon(0)
            setTitle(R.string.save)
        }
        mSaveButtonTextView?.setTextColor(ThemePrefs.buttonColor)
    }

    private fun shouldAllowExit() : Boolean {
        // Check if this is a new page and has changes
        if(presenter.page.id == 0L &&
                !pageRCEView?.html.isValid() &&
                !pageNameEditText.text.toString().isValid()) {
            return true
        }
        // Check if edited page has changes
        if(presenter.page.id != 0L &&
                presenter.page.body ?: "" == pageRCEView?.html &&
                mPage?.title ?: "" == pageNameEditText.text.toString() &&
                mPage?.frontPage == frontPageSwitch.isChecked &&
                mPage?.published == publishSwitch.isChecked) {
            return true
        }
        return false
    }

    private fun setupViews() {
        setupTitle()
        setupDescription()
        setupFrontPageSwitch()
        setupCanEditSpinner()
        setupPublishSwitch()
        setupDelete()


        pageRCEView.hideEditorToolbar()
        pageRCEView.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // Get the image Uri
            when (requestCode) {
                RequestCodes.PICK_IMAGE_GALLERY -> data?.data
                RequestCodes.CAMERA_PIC_REQUEST -> MediaUploadUtils.handleCameraPicResult(requireActivity(), null)
                else -> null
            }?.let { imageUri ->
                presenter.uploadRceImage(imageUri, requireActivity())
            }
        }
    }

    override fun insertImageIntoRCE(text: String, alt: String) {
        pageRCEView.insertImage(text, alt)
    }

    private fun setupTitle() {
        ViewStyler.themeEditText(requireContext(), pageNameEditText, ThemePrefs.brandColor)
        pageNameTextInput.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        pageNameEditText.setText(this.presenter.page.title)
        pageNameEditText.onTextChanged { presenter.page.title = it }
        pageNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) pageRCEView.hideEditorToolbar()
        }
    }

    private fun setupDescription() {
        if (CanvasWebView.containsLTI(presenter.page.body.orEmpty(), "UTF-8")) {
            pageRCEView.setHtml(DiscussionUtils.createLTIPlaceHolders(requireContext(), presenter.page.body.orEmpty()) { _, placeholder ->
                placeHolderList.add(placeholder)
            },
                    getString(R.string.pageDetails),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.buttonColor
            )
        } else {
            pageRCEView.setHtml(
                    presenter.page.body,
                    getString(R.string.pageDetails),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.buttonColor
            )
        }
        // When the RCE editor has focus we want the label to be darker so it matches the title's functionality
        pageRCEView.setLabel(pageDescLabel, R.color.defaultTextDark, R.color.defaultTextGray)
    }

    private fun setupFrontPageSwitch() {
        frontPageSwitch.applyTheme()
        frontPageSwitch.isChecked = presenter.page.frontPage
        frontPageSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.page.frontPage = isChecked
        }
    }

    private fun setupCanEditSpinner() = with(Page){
        val spinnerAdapter =  if(mCanvasContext.type == CanvasContext.Type.GROUP) {
            ArrayAdapter.createFromResource(requireContext(), R.array.canEditRolesWithGroups, R.layout.simple_spinner_item)
        } else {
            ArrayAdapter.createFromResource(requireContext(), R.array.canEditRolesNoGroups, R.layout.simple_spinner_item)
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pageCanEditSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), pageCanEditSpinner, ThemePrefs.brandColor)
        pageCanEditSpinner.onItemSelectedListener = null


        val roleArray = presenter.page.editingRoles?.split(",")
        roleArray?.let {
            if(it.contains(TEACHERS) && it.size == 1) {
                pageCanEditSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.onlyTeachers)))
            } else if(it.contains(TEACHERS) && it.contains(STUDENTS)) {
                pageCanEditSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.teachersAndStudents)))
            } else if(it.contains(ANYONE)) {
                pageCanEditSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.anyone)))
            } else if(it.contains(GROUP_MEMBERS)) {
                pageCanEditSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.groupMembers)))
            }
        }

        pageCanEditSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(view == null) return
                when((view as TextView).text.toString()) {
                    getString(R.string.onlyTeachers) -> presenter.page.editingRoles = TEACHERS
                    getString(R.string.teachersAndStudents) -> presenter.page.editingRoles = listOf(TEACHERS, STUDENTS).joinToString(separator = ",")
                    getString(R.string.anyone) -> presenter.page.editingRoles = ANYONE
                    getString(R.string.groupMembers) -> presenter.page.editingRoles = GROUP_MEMBERS
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
    }

    private fun setupPublishSwitch()  {
        // If it's the front page we can't unpublish it
        publishWrapper.setVisible(!(mPage != null && (mPage as Page).frontPage))

        // Publish status
        publishSwitch.applyTheme()
        publishSwitch.isChecked = presenter.page.published

        publishSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.page.published = isChecked }
    }

    private fun setupDelete() {
        deleteWrapper.setVisible((mPage != null && !(mPage as Page).frontPage))
        deleteWrapper.onClickWithRequireNetwork {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pageDeleteTitle)
                    .setMessage(R.string.pageDeleteMessage)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        if(mPage != null) {
                            presenter.deletePage(mPage!!.url!!)
                        }
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .showThemed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.page.body = pageRCEView.html
    }

    private fun savePage() {
        if (pageNameEditText.text.isNullOrBlank()) {
            toast(R.string.pageTitleMustBeSet)
            return
        }

        // Can't have an unpublished front page
        if(presenter.page.frontPage && !presenter.page.published) {
            toast(R.string.frontPageUnpublishedError)
            return
        }

        val description = pageRCEView.html

        presenter.page.body = handleLTIPlaceHolders(placeHolderList, description)
        presenter.savePage()
    }

    override fun onSaveStarted() {
        mSaveMenuButton.isVisible = false
        savingProgressBar.announceForAccessibility(getString(R.string.saving))
        savingProgressBar.setVisible()
    }

    override fun onSaveError() {
        mSaveMenuButton.isVisible = true
        savingProgressBar.setGone()
        toast(R.string.errorSavingPage)
    }

    override fun onSaveSuccess() {
        if (presenter.isEditing) {
            toast(R.string.pageSuccessfullyUpdated)
        } else {
            toast(R.string.pageSuccessfullyCreated)
        }
        forceQuit = true
        pageNameEditText.hideKeyboard() // Close the keyboard
        requireActivity().onBackPressed() // Close this fragment
    }

    override fun pageDeletedSuccessfully() {
        requireActivity().onBackPressed() // Close this fragment
    }

    override fun onHandleBackPressed(): Boolean {
        return if(shouldAllowExit() || forceQuit) {
            false
        } else {
            UnsavedChangesExitDialog.show(requireFragmentManager()) {
                forceQuit = true
                activity?.onBackPressed()
            }
            true
        }
    }

    companion object {
        fun newInstance(bundle: Bundle) = CreateOrEditPageDetailsFragment().apply {
            arguments = bundle
        }

        fun newInstanceCreate(canvasContext: CanvasContext) = CreateOrEditPageDetailsFragment().apply {
            mCanvasContext = canvasContext
        }

        fun newInstanceEdit(canvasContext: CanvasContext, page: Page)
                = CreateOrEditPageDetailsFragment().apply {
            mCanvasContext = canvasContext
            mPage = page
        }
    }
}
