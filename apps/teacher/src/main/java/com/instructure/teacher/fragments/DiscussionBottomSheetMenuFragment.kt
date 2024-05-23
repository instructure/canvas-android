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
package com.instructure.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.teacher.R
import com.instructure.teacher.databinding.BottomSheetDiscussionMenuBinding
import com.instructure.teacher.events.DiscussionOverflowMenuClickedEvent
import org.greenrobot.eventbus.EventBus

class DiscussionBottomSheetMenuFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(BottomSheetDiscussionMenuBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_discussion_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.markAsUnread.setOnClickListener {
            EventBus.getDefault().post(DiscussionOverflowMenuClickedEvent(DiscussionBottomSheetChoice.MARK_AS_UNREAD, entryId))
            this.dismiss()
        }

        binding.edit.setOnClickListener {
            EventBus.getDefault().post(DiscussionOverflowMenuClickedEvent(DiscussionBottomSheetChoice.EDIT, entryId))
            this.dismiss()
        }

        binding.delete.setOnClickListener {
            EventBus.getDefault().post(DiscussionOverflowMenuClickedEvent(DiscussionBottomSheetChoice.DELETE, entryId))
            this.dismiss()
        }

        (dialog as? BottomSheetDialog)?.behavior?.let {
            it.state = BottomSheetBehavior.STATE_EXPANDED
            it.skipCollapsed = true
        }
    }

    companion object {
        var entryId: Long = -1

        fun newInstance(): DiscussionBottomSheetMenuFragment = DiscussionBottomSheetMenuFragment()

        fun show(manager: FragmentManager, id: Long) {
            entryId = id
            manager.dismissExisting<DiscussionBottomSheetMenuFragment>()
            val dialog = newInstance()

            dialog.show(manager, DiscussionBottomSheetMenuFragment::class.java.simpleName)
        }
    }
}

enum class DiscussionBottomSheetChoice {
    MARK_AS_UNREAD, EDIT, DELETE
}
