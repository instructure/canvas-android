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

package com.instructure.speedgrader.fragments;

import android.os.Bundle;

import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Submission;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.R;

public class BaseSubmissionView extends ParentFragment {

    public Submission submission;
    public Submission currentSubmission;

    private Attachment attachment;
    private long currentSubmissionAttempt;

    public long getCurrentSubmissionAttempt(){
        return currentSubmissionAttempt;
    }

    public void setCurrentSubmissionAttempt(long submissionAttempt){
        this.currentSubmissionAttempt = submissionAttempt;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            this.submission = savedInstanceState.getParcelable(Const.submission);
            this.currentSubmission = savedInstanceState.getParcelable(Const.currentSubmission);
            this.currentSubmissionAttempt = savedInstanceState.getLong(Const.attempt);
            this.attachment = savedInstanceState.getParcelable(Const.attachment);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Const.submission, submission);
        outState.putParcelable(Const.currentSubmission, currentSubmission);
        outState.putLong(Const.attempt, currentSubmissionAttempt);
        outState.putParcelable(Const.attachment, attachment);
    }

    @Override
    public void getBundleData(Bundle bundle) {
        this.submission = getArguments().getParcelable(Const.submission);
        this.currentSubmissionAttempt = getArguments().getLong(Const.attempt);
        this.currentSubmission = getCurrentAttemptSubmission();
    }

    @Override
    public boolean retainInstanceState() {
        return true;
    }

    @Override
    public int getRootLayout() {
        return R.layout.fragment_generic_submission;
    }

    @Override
    public void setupCallbacks() {}

    public static Bundle createBundle(CanvasContext canvasContext, Submission submission, long currentAttempt){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.canvasContext, canvasContext);
        bundle.putParcelable(Const.submission, submission);
        bundle.putLong(Const.attempt, currentAttempt);
        return bundle;
    }

    public Submission getCurrentAttemptSubmission(){
        Submission returnSubmission = null;
        for(Submission submission : this.submission.getSubmissionHistory()){
            if(submission.getAttempt() == getCurrentSubmissionAttempt()){
                returnSubmission = submission;
            }
        }
        if(returnSubmission == null){
            returnSubmission = this.submission;
        }
        return returnSubmission;
    }
}
