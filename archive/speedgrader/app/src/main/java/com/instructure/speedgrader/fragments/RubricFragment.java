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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.RubricAssessment;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.canvasapi.model.Submission;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.adapters.RubricRecyclerAdapter;
import com.instructure.speedgrader.decorations.RubricDecorator;
import com.instructure.speedgrader.dialogs.EditAssignmentDialog;
import com.instructure.speedgrader.dialogs.RubricCommentDialog;
import com.instructure.speedgrader.interfaces.OnSubmissionChangedListener;
import com.instructure.speedgrader.interfaces.RubricAdapterToFragmentCallback;
import com.instructure.speedgrader.interfaces.SubmissionListener;
import com.instructure.speedgrader.util.Const;
import org.apache.commons.lang3.SerializationUtils;
import java.util.HashMap;

public class RubricFragment extends ParentFragment implements OnSubmissionChangedListener{

    // Interface
    private SubmissionListener mSubmissionListener;
    private RubricAssessment originalRubricAssessment;
    private RubricRecyclerAdapter mRecyclerAdapter;

    // Data
    private Assignment mOriginalAssignment;
    private Assignment mAssignment;
    private Submission mSubmission;

    private long currentAttempt; // keep track of which submissions we're currently showing on the rubric
    private Attachment currentAttachment;
    private RubricCommentDialog commentDialog;

    @Override
    public boolean retainInstanceState() {
        return false;
    }

    @Override
    public int getRootLayout() {
        return R.layout.fragment_rubric;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(savedInstanceState != null){
            getBundleDataWithCache(savedInstanceState);
        }

        this.currentAttempt     = this.mSubmission.getAttempt();

        mRecyclerAdapter = new RubricRecyclerAdapter(getContext(), getCanvasContext(), mAssignment, mSubmission, mSubmissionListener, new RubricAdapterToFragmentCallback() {

            @Override
            public void onFreeFormRowClicked(RubricCriterionRating rating, int position) {

            }

            @Override
            public void onRowClicked(RubricCriterionRating rubricCriterionRating) {}

            @Override
            public void onRowClicked(RubricCriterionRating rubricCriterionRating, int position) {
                if(mAssignment.isFreeFormCriterionComments()){return;}
                mRecyclerAdapter.updateRubricAssessment(rubricCriterionRating);
            }

            @Override
            public void onCommentRowClicked(RubricCriterionRating rating, int position) {
                commentDialog = new RubricCommentDialog();
                commentDialog.setArguments(RubricCommentDialog.createBundle(mRecyclerAdapter.getGroup((long)rating.getCriterionId().hashCode()), rating, position, mAssignment.isFreeFormCriterionComments()));
                commentDialog.setTargetFragment(RubricFragment.this, 123);
                commentDialog.show(getChildFragmentManager(), EditAssignmentDialog.TAG);
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }
        });

        configureRecyclerView(mRootView, getContext(), mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
        PandaRecyclerView pandaRecyclerView = (PandaRecyclerView) mRootView.findViewById(R.id.listView);
        pandaRecyclerView.addItemDecoration(new RubricDecorator(getContext()));

        return mRootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 123){
            RubricCriterionRating rating = data.getExtras().getParcelable(Const.rubricCriterionRating);
            if(rating != null){
                int position = data.getIntExtra(Const.currentPosition, 0);
                mRecyclerAdapter.updateCommentCache(rating, position);
            }
        }
    }

    /**
     * Called by our SubmissionWebView for updating quiz results
     * @param submission
     */
    public void updateRubric(Submission submission){
        Toast.makeText(getActivity(), getString(R.string.gradeSaved), Toast.LENGTH_SHORT).show();
        // Update our rubric assessment to reflect any new comment data
        HashMap<String,RubricCriterionRating> rubric_assessment = new HashMap<String, RubricCriterionRating>();
        for(RubricCriterionRating rating :  mSubmission.getRubricAssessment().getRatings()){
            rubric_assessment.put(rating.getCriterionId(), rating);
        }
        mSubmissionListener.onSubmissionRubricAssessmentUpdated(rubric_assessment, submission.getScore(), submission.getGrade());

        // Enable paging on viewpager
        mSubmissionListener.setPagingEnabled(true);

        mRecyclerAdapter.clear();

        mAssignment = mOriginalAssignment;
    }

    @Override
    public void setupCallbacks() {}

    ///////////////////////////////////////////////////////////////////////////
    // Submission Interface overrides
    ///////////////////////////////////////////////////////////////////////////
    /***
     * @desc  Notifies the RubricFragment when the user has swiped to a new mSubmission on the viewpager. Updates the rubric with new mSubmission data.
     * @param newAssignment
     * @param submission
     */
    @Override
    public void onPageChanged(Assignment newAssignment, Submission submission, Attachment attachment, long attempt) {
        mSubmission = submission;
        originalRubricAssessment =  SerializationUtils.clone(submission.getRubricAssessment());
        currentAttempt = attempt;
        currentAttachment = attachment;
        mSubmission = SerializationUtils.clone(submission);
        // Since the markGrades method will alter the assignments rubric, we need to make a copy of the current mAssignment.
        mOriginalAssignment = SerializationUtils.clone(newAssignment);
        mAssignment.setLastSubmission(mSubmission);

        // Mark grades
        mRecyclerAdapter.setSubmission(submission);
    }

    // Update Assignment details after editting
    @Override
    public void updateAssignmentDetails(Assignment newAssignment){
        mOriginalAssignment = SerializationUtils.clone(newAssignment);
        mAssignment = SerializationUtils.clone(newAssignment);
        mAssignment.setLastSubmission(mSubmission);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent Stuff
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void getBundleData(Bundle bundle) {
        // Since the markGrades method will alter the rubric on an mAssignment. We need to copy the mAssignment, so that grading a mSubmission will not affect others
        Assignment newAssignment = bundle.getParcelable(Const.assignment);
        mOriginalAssignment = SerializationUtils.clone(newAssignment);
        mAssignment = SerializationUtils.clone(newAssignment);
        mSubmission = bundle.getParcelable(Const.submission);
        mAssignment.setLastSubmission(mSubmission);
        originalRubricAssessment = SerializationUtils.clone(mSubmission.getRubricAssessment());

        if(mSubmission.getAttachments() != null){
            currentAttachment = mSubmission.getAttachments().get(0);
        }
    }

    public void getBundleDataWithCache(Bundle bundle) {
        super.getBundleData(bundle);

        // Since the markGrades method will alter the rubric on an mAssignment. We need to copy the mAssignment, so that grading a mSubmission will not affect others
        Assignment newAssignment = bundle.getParcelable(Const.assignment);
        mOriginalAssignment = SerializationUtils.clone(newAssignment);;
        mAssignment = SerializationUtils.clone(newAssignment);
        mSubmission = bundle.getParcelable(Const.submission);
        mAssignment.setLastSubmission(mSubmission);
        originalRubricAssessment = SerializationUtils.clone(mSubmission.getRubricAssessment());
    }

    public static Bundle createBundle(CanvasContext canvasContext, Submission submission, Assignment assignment){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.canvasContext, canvasContext);
        bundle.putParcelable(Const.submission, submission);
        bundle.putParcelable(Const.assignment, assignment);
        return bundle;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            mSubmissionListener = (SubmissionListener) activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString()+ "must implement OnAttachmentSelectedListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Const.canvasContext, getCanvasContext());
        outState.putParcelable(Const.submission, mSubmission);
        outState.putParcelable(Const.assignment, mOriginalAssignment);
        super.onSaveInstanceState(outState);
    }
}