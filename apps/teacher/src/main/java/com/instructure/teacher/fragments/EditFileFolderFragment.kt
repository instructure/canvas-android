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

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.textfield.TextInputLayout
import com.instructure.canvasapi2.models.FileAccessStatus
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.FileUsageRightsJustification
import com.instructure.canvasapi2.models.License
import com.instructure.canvasapi2.models.PublishStatus
import com.instructure.canvasapi2.models.RestrictedScheduleStatus
import com.instructure.canvasapi2.models.RestrictedStatus
import com.instructure.canvasapi2.models.UnpublishStatus
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.parcelCopy
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_FILE_FOLDER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.FileFolderDeletedEvent
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ParcelableArrayListArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.descendants
import com.instructure.pandautils.utils.postSticky
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.adapters.LongNameArrayAdapter
import com.instructure.teacher.databinding.FragmentEditFilefolderBinding
import com.instructure.teacher.dialog.ConfirmDeleteFileFolderDialog
import com.instructure.teacher.factory.EditFilePresenterFactory
import com.instructure.teacher.presenters.EditFileFolderPresenter
import com.instructure.teacher.utils.formatOrDoubleDash
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.view.EditFileView
import java.util.Calendar
import java.util.Date
import java.util.Locale

@ScreenView(SCREEN_VIEW_EDIT_FILE_FOLDER)
class EditFileFolderFragment : BasePresenterFragment<
        EditFileFolderPresenter,
        EditFileView,
        FragmentEditFilefolderBinding>(), EditFileView {

    private var currentFileOrFolder: FileFolder by ParcelableArg()
    private var usageRightsEnabled: Boolean by BooleanArg()
    private var licenseList: ArrayList<License> by ParcelableArrayListArg()
    private var courseId: Long by LongArg()

    private var mAccessStatus: FileAccessStatus = UnpublishStatus()
    private var mUsageType: FileUsageRightsJustification? = null
    private var mLicenseType: License? = null

    private val mDateFormat = DateHelper.fullMonthNoLeadingZeroDateFormat
    private val mTimeFormat by lazy { DateHelper.getPreferredTimeFormat(requireContext()) }
    private var lockDate: Date? = null
    private var unlockDate: Date? = null
    private val saveButton: TextView? get() = view?.findViewById(R.id.menuSave)

    private lateinit var updateFileFolder: FileFolder

    val deleteCallback = { presenter.deleteFileFolder() }

    private val dateClickListener: (View, Boolean) -> Unit = { view, isLockDate ->
        DatePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, if (isLockDate) lockDate else unlockDate) { year, month, dayOfMonth ->
            val updatedDate = setupDateCalendar(year, month, dayOfMonth, if (isLockDate) lockDate else unlockDate)
            val dateString: String = mDateFormat.formatOrDoubleDash(updatedDate)
            (view as EditText).setText(dateString)
            if (isLockDate) lockDate = updatedDate else unlockDate = updatedDate

            // Clear any date/time errors
            binding.unlockDateTextInput.error = null
            binding.unlockDateTextInput.isErrorEnabled = false
        }.show(requireActivity().supportFragmentManager, DatePickerDialogFragment::class.java.simpleName)
    }

    private val timeClickListener: (View, Boolean) -> Unit = { view, isLockDate ->
        TimePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, if (isLockDate) lockDate else unlockDate) { hour, min ->
            val updatedDate = setupTimeCalendar(hour, min, if (isLockDate) lockDate else unlockDate)
            val timeString = mTimeFormat.formatOrDoubleDash(updatedDate)
            (view as EditText).setText(timeString)
            if (isLockDate) lockDate = updatedDate else unlockDate = updatedDate

            // Clear any date/time errors
            binding.unlockDateTextInput.error = null
            binding.unlockDateTextInput.isErrorEnabled = false
        }.show(requireActivity().supportFragmentManager, TimePickerDialogFragment::class.java.simpleName)
    }

    private val titleTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s?.isBlank() == false) {
                binding.titleLabel.error = null
            }
        }
    }

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentEditFilefolderBinding = FragmentEditFilefolderBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onReadySetGo(presenter: EditFileFolderPresenter) {
        showUsageRights(presenter.usageRightsEnabled)
        setupToolbar()
        setupViews()
    }

    override fun getPresenterFactory() = EditFilePresenterFactory(currentFileOrFolder, usageRightsEnabled, licenseList, courseId)

    override fun onPresenterPrepared(presenter: EditFileFolderPresenter) = Unit
    override fun onRefreshFinished() = Unit
    override fun onRefreshStarted() = Unit

    override fun folderDeleted(deletedFileFolder: FileFolder) {
        FileFolderDeletedEvent(deletedFileFolder).postSticky()
        requireActivity().onBackPressed()
    }

    override fun fileFolderUpdated(updatedFileFolder: FileFolder) {
        FileFolderUpdatedEvent(updatedFileFolder).postSticky()
        requireActivity().onBackPressed()
    }

    override fun showError(stringResId: Int) {
        super.showToast(stringResId)
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setupCloseButton(this@EditFileFolderFragment)

        if (presenter.isFile) toolbar.title = getString(R.string.editFile)
        else toolbar.title = getString(R.string.editFolder)

        toolbar.setupMenu(R.menu.menu_save_generic) { saveFileFolder() }
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)

        saveButton?.setTextColor(ThemePrefs.textButtonColor)
    }

    private fun setupViews() = with(binding) {
        setupAccess()
        setupRestrictedAccess()
        setupUsageRights()

        deleteWrapper.setOnClickListener {
            ConfirmDeleteFileFolderDialog.show(childFragmentManager, presenter.isFile)
        }

        if (!presenter.isFile) {
            setupFolderViews()
        } else if (presenter.usageRightsEnabled) {
            setupLicenses(presenter.licenseList)
        }

        titleEditText.setText(presenter.currentFileOrFolder.name ?: presenter.currentFileOrFolder.displayName)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        // Setup click listeners for lock/unlock dates/times
        lockDateEditText.setOnClickListener { dateClickListener(it, true) }
        lockTimeEditText.setOnClickListener { timeClickListener(it, true) }
        unlockDateEditText.setOnClickListener { dateClickListener(it, false) }
        unlockTimeEditText.setOnClickListener { timeClickListener(it, false) }

        titleEditText.addTextChangedListener(titleTextWatcher)

        // Apply theming
        ViewStyler.themeEditText(requireActivity(), titleEditText, ThemePrefs.brandColor)
        ViewStyler.themeEditText(requireActivity(), copyrightEditText, ThemePrefs.brandColor)

        ViewStyler.themeSpinner(requireActivity(), accessSpinner, ThemePrefs.brandColor)
        ViewStyler.themeSpinner(requireActivity(), restrictedAccessSpinner, ThemePrefs.brandColor)
        ViewStyler.themeSpinner(requireActivity(), usageRightsSpinner, ThemePrefs.brandColor)
        ViewStyler.themeSpinner(requireActivity(), licenseSpinner, ThemePrefs.brandColor)

        val textList = arrayOf(lockDateEditText, lockTimeEditText, unlockDateEditText, unlockTimeEditText)
        textList.forEach {
            ViewStyler.themeEditText(requireContext(), it, ThemePrefs.brandColor)
            // Prevent user from long clicking
            it.setOnLongClickListener { true }
            // Don't show cursor
            it.isCursorVisible = false
        }

        (view as? ViewGroup)?.descendants<TextInputLayout>()?.forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        saveButton?.setTextColor(ThemePrefs.textButtonColor)
    }

    private fun showUsageRights(show: Boolean) = with(binding) {
        usageRightsLabel.setVisible(show)
        usageRightsSpinner.setVisible(show)
        copyrightHolderLabel.setVisible(show)
        copyrightEditText.setVisible(show)
    }

    private fun setupFolderViews() = with(binding) {
        copyrightHolderLabel.setGone()
        usageRightsLabel.setGone()
        usageRightsSpinner.setGone()
        deleteText.text = getText(R.string.deleteFolder)
    }

    private fun setupAccess() = with(binding) {
        val spinnerAdapter = ArrayAdapter.createFromResource(requireActivity(), R.array.fileAccessTypes, R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accessSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), accessSpinner, ThemePrefs.brandColor)
        accessSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view ?: return
                mAccessStatus = when ((view as TextView).text.toString()) {
                    getString(R.string.publish) -> PublishStatus()
                    getString(R.string.unpublish) -> UnpublishStatus()
                    getString(R.string.restrictedAccess) -> {
                        if (lockDate != null || unlockDate != null)
                            RestrictedScheduleStatus()
                        RestrictedStatus()
                    }
                    else -> UnpublishStatus()
                }
                when (mAccessStatus) {
                    is RestrictedStatus -> {
                        restrictedAccessLabel.setVisible()
                        restrictedAccessSpinner.setVisible()
                        restrictedAccessUnlock.setGone()
                        restrictedAccessLock.setGone()
                    }
                    is RestrictedScheduleStatus -> {
                        restrictedAccessLabel.setVisible()
                        restrictedAccessSpinner.setVisible()
                        restrictedAccessUnlock.setVisible()
                        restrictedAccessLock.setVisible()
                    }
                    else -> {
                        restrictedAccessLabel.setGone()
                        restrictedAccessSpinner.setGone()
                        restrictedAccessUnlock.setGone()
                        restrictedAccessLock.setGone()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        val initialPosition =
                when {
                    presenter.currentFileOrFolder.isLocked -> spinnerAdapter.getPosition(getString(R.string.unpublish))
                    presenter.currentFileOrFolder.isHidden ||
                            presenter.currentFileOrFolder.unlockDate != null ||
                            presenter.currentFileOrFolder.lockDate != null -> {
                        lockDate = presenter.currentFileOrFolder.lockDate
                        unlockDate = presenter.currentFileOrFolder.unlockDate
                        spinnerAdapter.getPosition(getString(R.string.restrictedAccess))
                    }
                    else -> spinnerAdapter.getPosition(getString(R.string.publish))
                }
        accessSpinner.setSelection(initialPosition)
    }

    private fun setupRestrictedAccess() = with(binding) {
        val spinnerAdapter = LongNameArrayAdapter.createFromResource(requireActivity(), if (presenter.isFile) R.array.fileRestrictedTypes else R.array.folderRestrictedTypes, R.layout.simple_spinner_item)
        restrictedAccessSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), accessSpinner, ThemePrefs.brandColor)
        restrictedAccessSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view ?: return
                mAccessStatus = when ((view as TextView).text.toString()) {
                    getString(R.string.studentsWithLink) -> {
                        restrictedAccessLock.setGone()
                        restrictedAccessUnlock.setGone()
                        RestrictedStatus()
                    }
                    getString(R.string.scheduleAvailability) -> {
                        restrictedAccessLock.setVisible()
                        restrictedAccessUnlock.setVisible()
                        RestrictedScheduleStatus()
                    }
                    else -> {
                        restrictedAccessLock.setGone()
                        restrictedAccessUnlock.setGone()
                        RestrictedStatus()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        val initialPosition = when {
            presenter.currentFileOrFolder.isHidden -> {
                if (presenter.isFile)
                    spinnerAdapter.getPosition(getString(R.string.studentsWithLink))
                else
                    spinnerAdapter.getPosition(getString(R.string.hidden))
            }
            presenter.currentFileOrFolder.lockDate != null || presenter.currentFileOrFolder.unlockDate != null -> {
                if (presenter.currentFileOrFolder.lockDate != null) {
                    lockDateEditText.setText(mDateFormat.format(presenter.currentFileOrFolder.lockDate))
                    lockTimeEditText.setText(mTimeFormat.format(presenter.currentFileOrFolder.lockDate))
                }
                if (presenter.currentFileOrFolder.unlockDate != null) {
                    unlockDateEditText.setText(mDateFormat.format(presenter.currentFileOrFolder.unlockDate))
                    unlockTimeEditText.setText(mTimeFormat.format(presenter.currentFileOrFolder.unlockDate))
                }
                spinnerAdapter.getPosition(getString(R.string.scheduleAvailability))
            }
            else -> spinnerAdapter.getPosition(getString(R.string.studentsWithLink))
        }
        restrictedAccessSpinner.setSelection(initialPosition)
    }

    /**
     * Method is called assuming the usage rights feature is enabled and we are editing a file
     */
    private fun setupUsageRights() = with(binding) {
        val spinnerAdapter = ArrayAdapter.createFromResource(requireActivity(), R.array.fileUsageRightsTypes, R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        usageRightsSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), usageRightsSpinner, ThemePrefs.brandColor)
        usageRightsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view ?: return
                mUsageType = when ((view as TextView).text.toString()) {
                    getString(R.string.holdCopyright) -> FileUsageRightsJustification.OWN_COPYRIGHT
                    getString(R.string.havePermission) -> FileUsageRightsJustification.USED_BY_PERMISSION
                    getString(R.string.publicDomain) -> FileUsageRightsJustification.PUBLIC_DOMAIN
                    getString(R.string.fairUse) -> FileUsageRightsJustification.FAIR_USE
                    getString(R.string.creativeCommons) -> FileUsageRightsJustification.CREATIVE_COMMONS
                    else -> FileUsageRightsJustification.OWN_COPYRIGHT
                }

                licenseLabel.setVisible(mUsageType == FileUsageRightsJustification.CREATIVE_COMMONS)
                licenseSpinner.setVisible(mUsageType == FileUsageRightsJustification.CREATIVE_COMMONS)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        // Setup initial value
        val initialPosition =
                when (presenter.currentFileOrFolder.usageRights?.useJustification?.name?.lowercase(
                    Locale.getDefault()
                )) {
                    "own_copyright" -> spinnerAdapter.getPosition(getString(R.string.holdCopyright))
                    "used_by_permission" -> spinnerAdapter.getPosition(getString(R.string.havePermission))
                    "public_domain" -> spinnerAdapter.getPosition(getString(R.string.publicDomain))
                    "fair_use" -> spinnerAdapter.getPosition(getString(R.string.fairUse))
                    "creative_commons" -> spinnerAdapter.getPosition(getString(R.string.creativeCommons))
                    else -> spinnerAdapter.getPosition(getString(R.string.holdCopyright))
                }
        usageRightsSpinner.setSelection(initialPosition)
    }

    private fun setupLicenses(licenses: List<License>) = with(binding) {
        // Create adapter, filtering for creative common licenses
        val spinnerAdapter = ArrayAdapter(requireActivity(), R.layout.simple_spinner_item, presenter.licenseList.filter { it.name.contains("CC") }.map { it.name })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        licenseSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), licenseSpinner, ThemePrefs.brandColor)
        licenseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view ?: return
                mLicenseType = licenses.find { it.name == ((view as TextView).text.toString()) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        // Setup initial values
        copyrightEditText.setText(presenter.currentFileOrFolder.usageRights?.legalCopyright ?: "")

        // Use license field in the file to match the license ID in licenses, filter by
        // license name, get position in adapter, set that as the initial position. If that isn't found,
        // set the default to the first license in the list
        val initialPosition = spinnerAdapter.getPosition(presenter.licenseList
                .find { it.id == presenter.currentFileOrFolder.usageRights?.license }?.name ?: presenter.licenseList.filter { it.name.contains("CC") }[0].name)
        licenseSpinner.setSelection(initialPosition)
    }

    private fun saveFileFolder() = with(binding) {
        if (binding.titleEditText.text?.isBlank() == true) {
            binding.titleLabel.error = getString(R.string.errorEmptyTitle)
            return
        }

        // Check unlock/lock dates
        if (unlockDate != null && lockDate != null)
            if (unlockDate!!.after(lockDate)) {
                unlockDateTextInput.isErrorEnabled = true
                unlockDateTextInput.error = getString(R.string.availableFromAfterTo)
                return
            }

        updateFileFolder = updateFileFolder.copy(name = titleEditText.text.toString())

        mAccessStatus.lockAt = lockDate.toApiString()
        mAccessStatus.unlockAt = unlockDate.toApiString()

        presenter.updateFileFolder(updateFileFolder, mAccessStatus, mUsageType, mLicenseType, copyrightEditText.text.toString())
    }

    /**
     * Handles time changes to the Date passed in. Sets a default time if there is none set already.
     *
     * @return The resulting date after updating it with the new year, month and day
     */
    private fun setupDateCalendar(year: Int, month: Int, dayOfMonth: Int, date: Date? = null): Date =
            Calendar.getInstance().apply { time = date ?: Date(); set(year, month, dayOfMonth) }.time

    /**
     * Handles time changes to the Date passed in. Sets a default date if there is none set already.
     *
     * @return The resulting date after updating it with the hour and minute
     */
    private fun setupTimeCalendar(hour: Int, min: Int, date: Date? = null): Date =
            Calendar.getInstance().apply { time = date ?: Date(); set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, min) }.time

    companion object {
        private const val CURRENT_FILE_OR_FOLDER = "currentFileOrFolder"
        private const val USAGE_RIGHTS_ENABLED = "usageRightsEnabled"
        private const val LIST_OF_LICENSES = "licenseList"
        private const val COURSE_ID = "courseId"

        fun makeBundle(fileFolder: FileFolder, usageRightsEnabled: Boolean, licenseList: List<License>, courseId: Long) = Bundle().apply {
            putParcelable(CURRENT_FILE_OR_FOLDER, fileFolder)
            putBoolean(USAGE_RIGHTS_ENABLED, usageRightsEnabled)
            putParcelableArrayList(LIST_OF_LICENSES, ArrayList(licenseList))
            putLong(COURSE_ID, courseId)
        }

        fun newInstance(bundle: Bundle) = EditFileFolderFragment().apply {
            arguments = bundle
            updateFileFolder = currentFileOrFolder.parcelCopy()
        }
    }
}

