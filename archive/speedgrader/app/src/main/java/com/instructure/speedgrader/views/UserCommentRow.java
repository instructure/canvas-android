/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.speedgrader.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.instructure.canvasapi.model.SubmissionComment;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.factories.SubmissionCommentRowFactory;
import com.instructure.speedgrader.fragments.SubmissionCommentsFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class UserCommentRow implements SubmissionCommentsFragment.Row {

    private SubmissionComment submissionComment;
    private LayoutInflater inflater;
    private Activity context;

    public UserCommentRow(LayoutInflater inflater, SubmissionComment comment, Activity context){
        this.submissionComment = comment;
        this.inflater = inflater;
        this.context = context;
    }

    @Override
    public View getView(View convertView) {
        return SubmissionCommentRowFactory.buildRowView(convertView, inflater, submissionComment, context, true);
    }

    @Override
    public int getViewType() {
        return SubmissionCommentsFragment.RowType.USER_COMMENT.ordinal();
    }
}
