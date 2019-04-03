/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.interfaces;

import androidx.annotation.Nullable;

import com.instructure.canvasapi2.models.Attachment;
import com.instructure.canvasapi2.models.BasicUser;
import com.instructure.canvasapi2.models.Message;
import com.instructure.pandautils.views.AttachmentView;
import com.instructure.teacher.R;

public interface MessageAdapterCallback {

    enum MessageClickAction {
        REPLY(R.string.reply),
        FORWARD(R.string.forward),
        DELETE(R.string.delete);

        public int labelResId;

        MessageClickAction(int labelResId) {
            this.labelResId = labelResId;
        }
    }

    void onAvatarClicked(BasicUser user);
    void onAttachmentClicked(AttachmentView.AttachmentAction action, Attachment attachment);
    void onMessageAction(MessageClickAction action, Message message);
    @Nullable BasicUser getParticipantById(long id);
}
