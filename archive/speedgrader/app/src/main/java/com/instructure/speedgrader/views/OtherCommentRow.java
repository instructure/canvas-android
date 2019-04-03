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
import android.view.LayoutInflater;
import android.view.View;
import com.instructure.canvasapi.model.SubmissionComment;
import com.instructure.speedgrader.factories.SubmissionCommentRowFactory;
import com.instructure.speedgrader.fragments.SubmissionCommentsFragment;

public class OtherCommentRow implements SubmissionCommentsFragment.Row {

    final SubmissionComment submissionComment;
    final LayoutInflater inflater;
    final Activity context;

    public OtherCommentRow(LayoutInflater inflater, SubmissionComment comment, Activity context){
        this.submissionComment = comment;
        this.inflater = inflater;
        this.context = context;
    }

    @Override
    public View getView(View convertView) {
        return SubmissionCommentRowFactory.buildRowView(convertView, inflater, submissionComment, context, false);
    }

    @Override
    public int getViewType() {
        return  SubmissionCommentsFragment.RowType.OTHER_COMMENT.ordinal();
    }
}
