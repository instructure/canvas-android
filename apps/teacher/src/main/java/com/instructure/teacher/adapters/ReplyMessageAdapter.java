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

package com.instructure.teacher.adapters;

import android.content.Context;
import androidx.annotation.NonNull;

import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Message;
import com.instructure.teacher.binders.ReplyMessageBinder;
import com.instructure.teacher.holders.MessageHolder;
import com.instructure.teacher.interfaces.MessageAdapterCallback;

import instructure.androidblueprint.SyncPresenter;


public class ReplyMessageAdapter extends MessageAdapter {

    public ReplyMessageAdapter(Context context, SyncPresenter presenter, Conversation conversation, @NonNull MessageAdapterCallback callback) {
        super(context, presenter, conversation, callback);
    }

    @Override
    public void bindHolder(Message message, MessageHolder holder, int position) {
        ReplyMessageBinder.Companion.bind(message, getMConversation(), holder, position, getMCallback());
    }
}
