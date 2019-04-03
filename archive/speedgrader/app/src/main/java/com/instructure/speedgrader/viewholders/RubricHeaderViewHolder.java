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

package com.instructure.speedgrader.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.Submission;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.adapters.FilesAdapter;
import com.instructure.speedgrader.adapters.PassFailAdapter;
import com.instructure.speedgrader.adapters.RubricRecyclerAdapter;
import com.instructure.speedgrader.adapters.SubmissionsAdapter;
import com.instructure.speedgrader.interfaces.SubmissionListener;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.HelveticaTextView;
import java.util.ArrayList;

public class RubricHeaderViewHolder extends RecyclerView.ViewHolder {

    public Button saveButton;
    public Spinner submissionSpinner;
    public Spinner fileSpinner;
    public Spinner passFailSpinner;

    public HelveticaTextView pointsPossible;
    public View scoreEditTextHolder;
    public EditText scoreEditText;
    public RelativeLayout gradingRow;

    public Assignment mAssignment;
    public Submission mSubmission;
    public Context    mContext;
    public SubmissionListener mSubmissionListener;
    private boolean firstSelectAttachmentEventFired = false;
    private boolean firstPassFailSelectEventFired = false;
    private long currentAttempt; // keep track of which submissions we're currently showing on the rubric
    private Attachment currentAttachment;

    private RubricRecyclerAdapter mAdapter;
    private TextWatcher mTextWatcher;
    public RubricHeaderViewHolder(View itemView, Context context, Assignment assignment, Submission submission, RubricRecyclerAdapter adapter, TextWatcher textWatcher, SubmissionListener listener) {
        super(itemView);

        submissionSpinner       = (Spinner) itemView.findViewById(R.id.versionSpinner);
        fileSpinner             = (Spinner) itemView.findViewById(R.id.fileSpinner);
        gradingRow              = (RelativeLayout) itemView.findViewById(R.id.gradingRow);
        pointsPossible          = (HelveticaTextView) itemView.findViewById(R.id.pointsPossible);
        scoreEditText           = (EditText) itemView.findViewById(R.id.scoreEditText);
        scoreEditTextHolder     = itemView.findViewById(R.id.scoreEditTextHolder);
        passFailSpinner         = (Spinner) itemView.findViewById(R.id.passFailSpinner);
        saveButton              = (Button) itemView.findViewById(R.id.saveButton);
        mSubmission             = submission;
        mAssignment             = assignment;
        mContext                = context;
        mSubmissionListener     = listener;
        currentAttempt          = mSubmission.getAttempt();
        mAdapter                = adapter;
        mTextWatcher            = textWatcher;

        if(mSubmission.getAttachments() != null && mSubmission.getAttachments().size() > 0){
            currentAttachment = mSubmission.getAttachments().get(0);
        }
    }

    public static int holderResId() {
        return R.layout.rubric_listview_header;
    }

    public void setSubmission(Submission submission){
        mSubmission = submission;
    }

    public void handleScoreText(String scoreEditTextCache, boolean isSaveEnabled){
        scoreEditText.removeTextChangedListener(mTextWatcher);
        scoreEditText.setText("");
        scoreEditText.setHintTextColor(mContext.getResources().getColor(R.color.dividerColor));
        if(mAssignment.getGradingType().equals(Assignment.GRADING_TYPE.LETTER_GRADE)){
            initLetterGradeView();
        }else if(mAssignment.getGradingType().equals(Assignment.GRADING_TYPE.PASS_FAIL)){
            initPassFailView();
        }else if(mAssignment.getGradingType().equals(Assignment.GRADING_TYPE.PERCENT)){
            initPercentView();
        }else if(mAssignment.getGradingType().equals(Assignment.GRADING_TYPE.NOT_GRADED)){
            initNoGradeView();
        }else if(mAssignment.getGradingType().equals(Assignment.GRADING_TYPE.GPA_SCALE)){
            initGpaScaleView();
        }else{
            showScoreEditText();
            scoreEditText.setRawInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            pointsPossible.setText(" / " + mAssignment.getPointsPossible());
        }

        setScoreIfNoCache(scoreEditTextCache);

        if(mAssignment.isUseRubricForGrading() || mAssignment.getGradingType().equals(Assignment.GRADING_TYPE.NOT_GRADED)){
            scoreEditText.setFocusable(false);
            scoreEditTextHolder.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.edittext_white_disabled));
        }

        scoreEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mAssignment != null && mAssignment.isUseRubricForGrading()){
                    Toast.makeText(mContext, mContext.getString(R.string.useRubricError), Toast.LENGTH_SHORT).show();
                    return true;
                } else if(mAssignment != null && mAssignment.getGradingType().equals(Assignment.GRADING_TYPE.NOT_GRADED)){
                    Toast.makeText(mContext, mContext.getString(R.string.noGradeAssignment), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
        saveButton.setEnabled(isSaveEnabled);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.saveSubmission(scoreEditText.getText().toString());
            }
        });
        scoreEditText.addTextChangedListener(mTextWatcher);
    }

    /**
     *  If a cached score exists, set the scoreEditText to that value. Otherwise, set the edittext to
     *  reflect the submissions current grade.
     */
    public void setScoreIfNoCache(String scoreEditTextCache){
        if(mAssignment.getGradingType() == Assignment.GRADING_TYPE.NOT_GRADED){
            //Do nothing for No Grade assignments. Instead a spinner will be created to handle grading.
            return;
        }
        if(scoreEditTextCache == null){
            scoreEditText.setText(getGradeString());
        }else{
            scoreEditText.append(scoreEditTextCache);
        }
        scoreEditText.requestFocus();
    }

    /**
     * Depending on the Assignment Grading Type, the scoreEditText can contain a number value or a letter
     * grade, with the exception of Pass/Fail grades which show a dropdown.
     * This is a convenience method which will return the appropriate value given mAssignment.
     * @return
     */
    public String getGradeString(){
        if(mSubmission == null || !mSubmission.isGraded()){
            return "";
        }

        switch (mAssignment.getGradingType()){
            case LETTER_GRADE:
                return mSubmission.getGrade();
            case PERCENT:
                return mSubmission.getGrade();
            case POINTS:
                return String.valueOf(mSubmission.getScore());
            case GPA_SCALE:
                return mSubmission.getGrade();
            default:
                return String.valueOf(mSubmission.getScore());
        }
    }

    public void initGpaScaleView(){
        showScoreEditText();

        if(!mAssignment.isUseRubricForGrading()) {
            scoreEditText.setHint(mContext.getString(R.string.gpaScaleHint));
        }

        scoreEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        String scoreGrade;
        if(mSubmission.getGrade() == null){
            scoreGrade = "- / " +String.valueOf(mAssignment.getPointsPossible());
        }else{
            scoreGrade =  String.valueOf(mSubmission.getScore())+" / " +String.valueOf(mAssignment.getPointsPossible());
        }

        pointsPossible.setText(scoreGrade);
    }

    public void initNoGradeView(){
        if(this.mAssignment.getRubric().size() == 0){
            gradingRow.setVisibility(View.GONE);
        }
        pointsPossible.setText(" / - ");
    }

    public void initLetterGradeView(){
        showScoreEditText();
        if(!mAssignment.isUseRubricForGrading()){
            scoreEditText.setHint(mContext.getString(R.string.letterHint));
        }

        scoreEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        String pointsPossibleText;
        if(mSubmission.getGrade() == null){
            pointsPossibleText = " / " +String.valueOf(mAssignment.getPointsPossible());
        }else{
            pointsPossibleText = String.valueOf(mSubmission.getScore()) +" / " +String.valueOf(mAssignment.getPointsPossible());
        }
        pointsPossible.setText(pointsPossibleText);
    }

    public void initPercentView(){
        showScoreEditText();
        scoreEditText.setRawInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        String pointsPossibleText;
        if(mSubmission.getGrade() == null){
            pointsPossibleText = " / " +String.valueOf(mAssignment.getPointsPossible());
        }else{
            pointsPossibleText = String.valueOf(mSubmission.getScore()) +" / " +String.valueOf(mAssignment.getPointsPossible());
        }
        pointsPossible.setText(pointsPossibleText);
    }

    public void initPassFailView(){
        showPassFailSpinner();

        passFailSpinner.setAdapter(new PassFailAdapter(mContext, mAssignment.isUseRubricForGrading()));

        // Set our score if the mSubmission has been graded
        if(mSubmission.getGrade() != null){
            if(mSubmission.getGrade().toLowerCase().equals(mContext.getString(R.string.complete).toLowerCase())){
                passFailSpinner.setSelection(1);
            }else if(mSubmission.getGrade().toLowerCase().equals(mContext.getString(R.string.incomplete).toLowerCase())){
                passFailSpinner.setSelection(2);
            }
        }

        passFailSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(firstPassFailSelectEventFired){
                    if(passFailSpinner.getSelectedItem().equals(PassFailAdapter.getPassFailGradeType(mContext, mSubmission.getGrade()))){
                        mSubmissionListener.setPagingEnabled(true);
                    }
                    else{
                        mSubmissionListener.setPagingEnabled(false);
                        scoreEditText.setText(PassFailAdapter.getPassFailGradeString(mContext, (PassFailAdapter.PassFail) passFailSpinner.getSelectedItem()));
                    }
                }else{
                    firstPassFailSelectEventFired = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSubmissionListener.setPagingEnabled(true);
            }
        });

        passFailSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mAssignment != null && mAssignment.isUseRubricForGrading()){
                    Toast.makeText(mContext, mContext.getString(R.string.useRubricError), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        if(passFailSpinner.getSelectedItem().equals(PassFailAdapter.getPassFailGradeType(mContext, mSubmission.getGrade()))){
            mSubmissionListener.setPagingEnabled(true);
        }
    }

    public void showPassFailSpinner(){
        passFailSpinner.setVisibility(View.VISIBLE);
        scoreEditTextHolder.setVisibility(View.GONE);
        pointsPossible.setVisibility(View.GONE);
    }
    public void showScoreEditText(){
        scoreEditTextHolder.setVisibility(View.VISIBLE);
        pointsPossible.setVisibility(View.VISIBLE);
        passFailSpinner.setVisibility(View.GONE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Spinners
    ///////////////////////////////////////////////////////////////////////////
    public void initSpinners(Submission submission){
        mSubmission = submission;
        resetSpinnerSelectEvents();
        populateSubmissions(mSubmission.getSubmissionHistory());
        populateAttachments(mSubmission.getAttachments());
        fileSpinner.setOnItemSelectedListener(getFileSelectListener());
        submissionSpinner.setOnItemSelectedListener(getVersionSelectListener());
    }

    private void resetSpinnerSelectEvents(){
        firstPassFailSelectEventFired = false;
        firstSelectAttachmentEventFired = false;
        submissionSpinner.setOnItemSelectedListener(null);
        fileSpinner.setOnItemSelectedListener(null);
    }

    private void populateSubmissions(ArrayList<Submission> submissionList){
        // Canvas API sometimes returns fake submissions. Filter these out.
        SubmissionsAdapter submissionAdapter = new SubmissionsAdapter(mContext, R.layout.rubric_spinner_item, submissionList);
        submissionSpinner.setAdapter(submissionAdapter);
        setSpinnerSelectionByAttempt(currentAttempt);
    }

    private void populateAttachments(ArrayList<Attachment> attachments){
        FilesAdapter fileAdapter = new FilesAdapter(mContext, R.layout.rubric_spinner_item, attachments);
        fileSpinner.setAdapter(fileAdapter);
        if(currentAttachment != null){
            setAttachmentSelectionById(currentAttachment.getId());
        }
    }

    /**
     *  Sets the spinner to the correct mSubmission history item currently being displayed in our viewpager.
     *  We can't just set the spinner to the current attempt, since sometimes the API will return a mSubmission
     *  with 0 history items, but an attempt > 1. If this happens, we set the spinner to the first item.
     */
    private void setSpinnerSelectionByAttempt(long attempt){
        ArrayList<Submission> submissionAttempts = this.mSubmission.getSubmissionHistory();
        boolean foundAtempt = false;
        for(int i = 0; i < submissionAttempts.size(); i++){
            if(submissionAttempts.get(i).getAttempt() == attempt){
                submissionSpinner.setSelection(i);
                foundAtempt = true;
                break;
            }
        }

        // Sometimes the API will return an attempt > 0, but not the corresponding submissinoHistories, in this case, just use the latest mSubmission
        if(foundAtempt == false && mSubmission.getSubmissionHistory().size() > 0){
            submissionSpinner.setSelection(mSubmission.getSubmissionHistory().size() - 1);
        }
    }

    private void setAttachmentSelectionById(long attachmentId){
        ArrayList<Attachment> attachment = mSubmission.getAttachments();
        for(int i = 0; i < attachment.size(); i++){
            if(attachment.get(i).getId() == attachmentId){
                fileSpinner.setSelection(i);
            }
        }
    }

    private AdapterView.OnItemSelectedListener getFileSelectListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // this listener gets called when .setAdapter gets called for both the submissions adapter and the files adapter.
                // firstSelectEventFired is a workaround to stop these premature calls to our interface.
                if(firstSelectAttachmentEventFired){
                    Attachment attachment = ((FilesAdapter)fileSpinner.getAdapter()).getItem(position);
                    mSubmissionListener.onAttachmentSelected(attachment, mSubmission);
                }else{
                    firstSelectAttachmentEventFired = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }

    private AdapterView.OnItemSelectedListener getVersionSelectListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Submission submission = ((SubmissionsAdapter) submissionSpinner.getAdapter()).getItem(position);
                if(mSubmission.getAttempt() != submission.getAttempt()){
                    mSubmission = submission;
                    currentAttempt = submission.getAttempt();
                    mSubmissionListener.onSubmissionSelected(submission);
                    populateAttachments(submission.getAttachments());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }
}
