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
package com.instructure.teacher.features.syllabus.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_SYLLABUS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.*
import com.instructure.teacher.databinding.FragmentEditSyllabusBinding
import com.instructure.teacher.mobius.common.ui.EffectHandler
import com.instructure.teacher.mobius.common.ui.MobiusFragment
import com.instructure.teacher.mobius.common.ui.Presenter
import com.instructure.teacher.mobius.common.ui.UpdateInit

private const val SUMMARY_ALLOWED = "summaryAllowed"

@ScreenView(SCREEN_VIEW_EDIT_SYLLABUS)
class EditSyllabusFragment : MobiusFragment<EditSyllabusModel, EditSyllabusEvent, EditSyllabusEffect,
        EditSyllabusView, EditSyllabusViewState, FragmentEditSyllabusBinding>(), NavigationCallbacks {

    private val course by ParcelableArg<Course>()
    private val summaryAllowed: Boolean by BooleanArg(key = SUMMARY_ALLOWED)

    override fun makeEffectHandler(): EffectHandler<EditSyllabusView, EditSyllabusEvent, EditSyllabusEffect> = EditSyllabusEffectHandler()

    override fun makeUpdate(): UpdateInit<EditSyllabusModel, EditSyllabusEvent, EditSyllabusEffect> = EditSyllabusUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup): EditSyllabusView =
        EditSyllabusView(requireFragmentManager(), inflater, parent) { MediaUploadUtils.showPickImageDialog(this) }

    override fun makePresenter(): Presenter<EditSyllabusModel, EditSyllabusViewState> = EditSyllabusPresenter()

    override fun makeInitModel(): EditSyllabusModel = EditSyllabusModel(course, summaryAllowed)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // Get the image Uri
            when (requestCode) {
                RequestCodes.PICK_IMAGE_GALLERY -> data?.data
                RequestCodes.CAMERA_PIC_REQUEST -> MediaUploadUtils.handleCameraPicResult(requireActivity(), null)
                else -> null
            }?.let { imageUri ->
                view.uploadRceImage(imageUri, requireActivity(), course)
            }
        }
    }

    override fun onHandleBackPressed() = view.onHandleBackPressed()

    override fun onPause() {
        view.saveState()
        super.onPause()
    }

    companion object {

        fun newInstance(bundle: Bundle): EditSyllabusFragment {
            return EditSyllabusFragment().apply {
                arguments = bundle
            }
        }

        fun createArgs(course: Course, summaryAllowed: Boolean): Bundle {
            val extras = Bundle()
            extras.putParcelable(Const.COURSE, course)
            extras.putBoolean(SUMMARY_ALLOWED, summaryAllowed)
            return extras
        }
    }
}