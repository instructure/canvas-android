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

package com.instructure.speedgrader.adapters;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.fragments.BaseSubmissionView;
import com.instructure.speedgrader.fragments.DiscussionSubmissionFragment;
import com.instructure.speedgrader.fragments.EmptyViewFragment;
import com.instructure.speedgrader.fragments.MediaUploadFragment;
import com.instructure.speedgrader.fragments.OnlineURLFragment;
import com.instructure.speedgrader.fragments.ParentFragment;
import com.instructure.speedgrader.fragments.PdfSubmissionFragment;
import com.instructure.speedgrader.fragments.SubmissionWebViewFragment;
import com.instructure.speedgrader.util.Const;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

public class DocumentPagerAdapter extends SortableFragmentPagerAdapter {

    List<Submission> submissions;
    Assignment assignment;
    CanvasContext canvasContext;
    private WeakReference<Activity> activity;

    private HashMap<String, Long> currentAttempts; // We use a hashmap to maintain current attempts per submission since submissions can now be sorted.

    // get strings of the different submission types we are supporting to make it easier to read
    private static final String onlineURL = Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_URL);
    private static final String discussionTopic = Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.DISCUSSION_TOPIC);
    private static final String mediaUpload = Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.MEDIA_RECORDING);

    public DocumentPagerAdapter(Activity activity, FragmentManager fm, List<Submission> submissions, Assignment assignment, HashMap<String, Long> currentAttempts, CanvasContext canvasContext) {
        super(fm);
        destroyAllFragments();
        this.submissions = submissions;
        this.assignment = assignment;
        this.canvasContext = canvasContext;
        this.currentAttempts = currentAttempts;
        this.activity = new WeakReference<>(activity);
        createIdCache();
    }

    @Override
    public Fragment getItem(int position) {
        return getSubmissionFragment(submissions.get(position), assignment, canvasContext, currentAttempts.get(String.valueOf(submissions.get(position).getId())));
    }

    public Submission getSubmission(int position){
        return submissions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return submissions.get(position).getId() + currentAttempts.get(String.valueOf(submissions.get(position).getId()));
    }

    @Override
    public int getCount() {
        return submissions.size();
    }

    public int indexOf(Submission submission){
        return submissions.indexOf(submission);
    }

    public void setSubmission(int index, long submissionAttempt){
        currentAttempts.put(String.valueOf(submissions.get(index).getId()), submissionAttempt);
        setNeedsRefresh(index);
        replaceFragment(index, getSubmissionFragment(submissions.get(index), assignment, canvasContext, submissionAttempt));
    }

    /**
     * Function    : getSubmissionFragment
     * Description : This is a static method that takes a submission and an assignment and generates the corresponding document fragment.
     *               Used to generate our Viewpager, and also to swap out a fragment if necessary.
     */
    public Fragment getSubmissionFragment(Submission tempSubmission, Assignment assignment, CanvasContext canvasContext, long currentAttempt){
        // Check the submission type

        final Submission submissionAttempt = getCurrentAttemptSubmission(tempSubmission, currentAttempt);
        final String submissionType = submissionAttempt.getSubmissionType();
        Attachment attachment = getCurrentAttemptAttachment(submissionAttempt);

        tempSubmission.setAssignment(assignment);
        if(isEmptySubmission(submissionAttempt)) {
            // Check for empty submissions
            boolean isGroupAssignment = assignment.getGroupCategoryId() != 0 && !assignment.isGradeGroupsIndividually();
            boolean useGroupMessage = isGroupAssignment && (tempSubmission.getGroup() != null && tempSubmission.getGroup().getId() != 0);
            int messageResId = useGroupMessage ? R.string.noGroupSubmission : R.string.noSubmission;
            return ParentFragment.newInstance(EmptyViewFragment.class, EmptyViewFragment.createBundle(canvasContext, tempSubmission, messageResId));
        }
        // online url
        else if(submissionAttempt.getSubmissionType() != null && submissionType.equals(onlineURL)) {
            return ParentFragment.newInstance(OnlineURLFragment.class, BaseSubmissionView.createBundle(canvasContext, tempSubmission, currentAttempt));
        }
        // discussion topic
        else if(submissionAttempt.getSubmissionType() != null && submissionType.equals(discussionTopic)) {
            return ParentFragment.newInstance(DiscussionSubmissionFragment.class, BaseSubmissionView.createBundle(canvasContext, tempSubmission, currentAttempt));
        }
        // media upload
        else if(submissionAttempt.getSubmissionType() != null && submissionType.equals(mediaUpload)) {
            return ParentFragment.newInstance(MediaUploadFragment.class, BaseSubmissionView.createBundle(canvasContext, submissionAttempt, currentAttempt));
        }
        else if(submissionType.equals(Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_UPLOAD)) && attachment != null && attachment.getMimeType().contains("pdf") ){
            return ParentFragment.newInstance(PdfSubmissionFragment.class, BaseSubmissionView.createBundle(canvasContext, submissionAttempt, currentAttempt));
        }
        else {
            // online upload & online text
            return ParentFragment.newInstance(SubmissionWebViewFragment.class, BaseSubmissionView.createBundle(canvasContext, tempSubmission, currentAttempt));
        }
    }

    public static @Nullable  Attachment getCurrentAttemptAttachment(Submission submissionAttempt){
        if(submissionAttempt.getAttachments().size() > 0){
            return submissionAttempt.getAttachments().get(0);
        }
        return null;
    }

    public String getAttachmentPreviewUrl(@Nullable Attachment attachment){
        if(attachment == null){
            return "";
        }
        return APIHelpers.getFullDomain(activity.get()) + attachment.getPreviewURL();
    }

    public static Submission getCurrentAttemptSubmission(Submission submission, long attempt){
        Submission returnSubmission = null;
        for(Submission tempSubmission : submission.getSubmissionHistory()){
            if(tempSubmission.getAttempt() == attempt){
                returnSubmission = tempSubmission;
            }
        }
        if(returnSubmission == null){
            // Sometimes the API returns an attempt >0, but a submission history missing that corresponding attempt
            return submission;
        }
        return returnSubmission;
    }

    public static boolean isEmptySubmission(Submission submission) {
        //if it's a quiz without a body, it is still an empty submission but it may have been graded
        if(submission.getSubmissionType() == null || submission.getAttempt() == 0) {
            return true;
        }
        return false;
    }
}
