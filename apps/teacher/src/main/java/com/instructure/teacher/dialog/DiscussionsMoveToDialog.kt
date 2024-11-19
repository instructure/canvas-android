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

package com.instructure.teacher.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.widget.CompoundButtonCompat
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSION_MOVE_TO
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.teacher.R
import com.instructure.teacher.presenters.DiscussionListPresenter
import com.instructure.teacher.utils.getColorCompat
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_DISCUSSION_MOVE_TO)
class DiscussionsMoveToDialog : BaseCanvasDialogFragment() {

    init {
        retainInstance = true
    }

    private var mMoveToCallback: (String) -> Unit by Delegates.notNull()

    companion object {
        const val GROUP = "group"
        const val DISCUSSION_TOPIC_HEADER = "discussionTopicHeader"

        fun show(manager: FragmentManager, group: String, discussionTopicHeader: DiscussionTopicHeader, callback: (String) -> Unit) {
            manager.dismissExisting<DiscussionsMoveToDialog>()
            val dialog = DiscussionsMoveToDialog()
            val args = Bundle()
            args.putString(GROUP, group)
            args.putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
            dialog.arguments = args
            dialog.mMoveToCallback = callback
            dialog.show(manager, DiscussionsMoveToDialog::class.java.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(ContextThemeWrapper(requireActivity(), 0), R.layout.dialog_move_discussion_to, null)
        val radioGroup = view.findViewById<RadioGroup>(R.id.coursePages)
        val group = nonNullArgs.getString(GROUP)
        val discussion = nonNullArgs.getParcelable<DiscussionTopicHeader>(DISCUSSION_TOPIC_HEADER)!!

        when(group) {
            DiscussionListPresenter.PINNED -> {
                setupRadioButton(view.findViewById(R.id.rb_closedOpenForComments),
                        if (discussion.locked) getString(R.string.discussions_open)
                        else getString(R.string.discussions_close),
                        true, DiscussionListPresenter.CLOSED_FOR_COMMENTS)
                setupRadioButton(view.findViewById(R.id.rb_pinnedUnpinned),
                        getString(R.string.discussions_unpin), false, DiscussionListPresenter.UNPINNED)
            }
            DiscussionListPresenter.UNPINNED -> {
                setupRadioButton(view.findViewById(R.id.rb_closedOpenForComments),
                        getString(R.string.discussions_close), true, DiscussionListPresenter.CLOSED_FOR_COMMENTS)
                setupRadioButton(view.findViewById(R.id.rb_pinnedUnpinned),
                        getString(R.string.discussions_pin), false, DiscussionListPresenter.PINNED)
            }
            DiscussionListPresenter.CLOSED_FOR_COMMENTS -> {
                setupRadioButton(view.findViewById(R.id.rb_closedOpenForComments),
                        getString(R.string.discussions_open), true, DiscussionListPresenter.CLOSED_FOR_COMMENTS)
                setupRadioButton(view.findViewById(R.id.rb_pinnedUnpinned),
                        getString(R.string.discussions_pin), false, DiscussionListPresenter.PINNED)
            }
        }

        setupRadioButton(view.findViewById(R.id.rb_delete),
                getString(R.string.delete), false, DiscussionListPresenter.DELETE)

        val dialog = AlertDialog.Builder(requireContext())
                .setCancelable(true)
                .setTitle(getString(R.string.discussions_options))
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok), null)
                .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener { _ ->
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val selected = radioGroup.findViewById<AppCompatRadioButton>(radioGroup.checkedRadioButtonId)
                if(selected.tag != null) {
                    mMoveToCallback(selected.tag as String)
                    dialog.dismiss()
                }
            }
        }

        return dialog
    }

    private fun setupRadioButton(radioButton: AppCompatRadioButton, text: String, isChecked: Boolean, group: String) {

        radioButton.text = text
        radioButton.tag = group
        radioButton.isChecked = isChecked

        if(group == DiscussionListPresenter.DELETE) {
            val destructiveColor =  requireContext().getColorCompat(R.color.textDanger)
            CompoundButtonCompat.setButtonTintList(radioButton, ViewStyler.makeColorStateListForRadioGroup(destructiveColor, destructiveColor))
            radioButton.setTextColor(destructiveColor)
        } else {
            val unselectedColor = requireContext().getColorCompat(R.color.textDarkest)
            CompoundButtonCompat.setButtonTintList(radioButton, ViewStyler.makeColorStateListForRadioGroup(unselectedColor, ThemePrefs.brandColor))
        }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
