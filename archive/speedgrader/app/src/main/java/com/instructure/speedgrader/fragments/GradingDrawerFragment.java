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
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Submission;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.interfaces.OnSubmissionChangedListener;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.CheckedLinearLayout;
import com.instructure.speedgrader.views.HelveticaTextView;
import com.instructure.speedgrader.views.StaticViewPager;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;

public class GradingDrawerFragment extends ParentFragment implements OnSubmissionChangedListener{

    StaticViewPager viewPager;
    private Assignment assignment;
    private Submission submission;
    private Course course;
    private CheckedLinearLayout gradeSwitch;
    GradingFragmentPagerAdapter pagerAdapter;
    ArrayList<ParentFragment> mGradingFragments;

    RubricFragment rubricFragment;
    SubmissionCommentsFragment commentsFragment;

    private static String rubricFragmentTag = "";
    private static String commentsFragmentTag = "";

    @Override
    public boolean retainInstanceState() {
        return false;
    }

    @Override
    public int getRootLayout() {
        return R.layout.fragment_grading_drawer;
    }

    @Override
    public void setupCallbacks() {}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Const.canvasContext, getCanvasContext());
        outState.putParcelable(Const.submission, submission);
        outState.putParcelable(Const.assignment, assignment);
        outState.putString(Const.RUBRIC_FRAGMENT_TAG, rubricFragment.getTag());
        outState.putString(Const.COMMENTS_FRAGMENT_TAG, commentsFragment.getTag());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflateLayout(inflater, container);

        if(savedInstanceState != null){
            getBundleData(savedInstanceState);
        }

        setupViews();
        setupViewPager(savedInstanceState);

        return mRootView;
    }

    public void setupViews(){
        viewPager = (StaticViewPager) mRootView.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(1);

        HelveticaTextView assignmentNameTextView;
        assignmentNameTextView = (HelveticaTextView) mRootView.findViewById(R.id.assignmentName);
        assignmentNameTextView.setText(assignment.getName());

        setupViewSwitcher();
    }

    public void setupViewSwitcher(){
        gradeSwitch = (CheckedLinearLayout) mRootView.findViewById(R.id.gradeCommentSwitch);
        gradeSwitch.setLeftAsChecked(true);
        gradeSwitch.setLeftIndicatorText(getResources().getString(R.string.grade));
        gradeSwitch.setRightIndicatorText(getResources().getString(R.string.comment));
        gradeSwitch.setCallbacks(new CheckedLinearLayout.OnSwitchListener() {
            @Override
            public void onSwitch(boolean isLeftChecked) {
                // isLeftChecked == grade
                // !isLeftChecked  == comments
                if(isLeftChecked){
//                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                    viewPager.setCurrentItem(0, true);
                }else{
//                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    viewPager.setCurrentItem(1, true);
                }
            }
        });
    }

    public void setupViewPager(Bundle savedInstanceState){
        mGradingFragments = new ArrayList<>();

        if(savedInstanceState == null){
            rubricFragment = ParentFragment.newInstance(RubricFragment.class, RubricFragment.createBundle(getCanvasContext(), submission, assignment));
            boolean isGroup = assignment.getAssignmentGroupId() != 0 && !assignment.isGradeGroupsIndividually() && submission.getGroup() != null && submission.getGroup().getId() != 0;
            commentsFragment = ParentFragment.newInstance(SubmissionCommentsFragment.class, SubmissionCommentsFragment.createBundle(getCanvasContext(), submission, assignment.getId(), isGroup));
        }else{
            rubricFragment = (RubricFragment) getChildFragmentManager().findFragmentByTag(rubricFragmentTag);
            commentsFragment = (SubmissionCommentsFragment) getChildFragmentManager().findFragmentByTag(commentsFragmentTag);
        }
        mGradingFragments.add(rubricFragment);
        mGradingFragments.add(commentsFragment);
        pagerAdapter = new GradingFragmentPagerAdapter(getChildFragmentManager(), mGradingFragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
    }

    public class GradingFragmentPagerAdapter extends FragmentPagerAdapter {

        ArrayList<ParentFragment > gradingFragments;

        public GradingFragmentPagerAdapter(android.support.v4.app.FragmentManager fm, ArrayList<ParentFragment> gradingFragments){
            super(fm);
            this.gradingFragments = gradingFragments;
        }

        public void set(int index, ParentFragment newDocumentFragment){
            gradingFragments.set(index, newDocumentFragment);
            notifyDataSetChanged();
        }

        public int indexOf(ParentFragment documentFragment){
            return gradingFragments.indexOf(documentFragment);
        }

        public void add(ParentFragment newDocumentFragment){
            gradingFragments.add(newDocumentFragment);
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public ParentFragment getItem(int position) {
            return gradingFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onPageChanged(Assignment newAssignment, Submission submission, Attachment attachment, long attempt) {
        // Update our fragments
        if(mGradingFragments == null){ return; }
        for(ParentFragment fragment : mGradingFragments){
            if(fragment instanceof OnSubmissionChangedListener){
                ((OnSubmissionChangedListener)fragment).onPageChanged(newAssignment, submission, attachment, attempt);
            }
        }
    }

    @Override
    public void updateAssignmentDetails(Assignment assignment){
        HelveticaTextView assignmentNameTextView;
        assignmentNameTextView = (HelveticaTextView) mRootView.findViewById(R.id.assignmentName);
        assignmentNameTextView.setText(assignment.getName());

        rubricFragment.updateAssignmentDetails(assignment);
    }

    public void updateRubricSubmissionInfo(Submission submission){
        if(rubricFragment == null){return;}
        rubricFragment.updateRubric(submission);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    public boolean isGradeView(){
        if(viewPager != null && viewPager.getCurrentItem() == 0){
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent Stuff
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void getBundleData(Bundle bundle) {
        super.getBundleData(bundle);
        course = bundle.getParcelable(Const.canvasContext);
        // Copy the assignment
        Assignment newAssignment = bundle.getParcelable(Const.assignment);
        this.assignment = SerializationUtils.clone(newAssignment);
        this.submission = bundle.getParcelable(Const.submission);
        this.assignment.setLastSubmission(this.submission);
        this.rubricFragmentTag = bundle.getString(Const.RUBRIC_FRAGMENT_TAG);
        this.commentsFragmentTag = bundle.getString(Const.COMMENTS_FRAGMENT_TAG);
    }

    public static Bundle createBundle(CanvasContext canvasContext, Submission submission, Assignment assignment){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.canvasContext, canvasContext);
        bundle.putParcelable(Const.submission, submission);
        bundle.putParcelable(Const.assignment, assignment);
        return bundle;
    }
}
