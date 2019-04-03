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
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.instructure.canvasapi.api.SubmissionAPI;
import com.instructure.canvasapi.model.RubricAssessment;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.speedgrader.R;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.RubricCriterion;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.canvasapi.model.Submission;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.speedgrader.activities.DocumentActivity;
import com.instructure.speedgrader.binders.ExpandableHeaderBinder;
import com.instructure.speedgrader.binders.RubricBinder;
import com.instructure.speedgrader.binders.RubricTopHeaderBinder;
import com.instructure.speedgrader.interfaces.RubricAdapterToFragmentCallback;
import com.instructure.speedgrader.interfaces.SubmissionListener;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.viewholders.BaseRubricViewHolder;
import com.instructure.speedgrader.viewholders.ExpandableViewHolder;
import com.instructure.speedgrader.viewholders.RubricHeaderViewHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RubricRecyclerAdapter extends ExpandableRecyclerAdapter<RubricCriterion, RubricCriterionRating, RecyclerView.ViewHolder> {

    private CanvasContext mCanvasContext;
    private Assignment mAssignment;
    private Submission mSubmission;

    private RubricAdapterToFragmentCallback mAdapterToFragment;
    private SubmissionListener mSubmissionListener;

    private RubricCriterion mTopViewHeader; // The top header is just a group with a different view layout

    // We generate an additional rowitem for each RubricCriterion to display a comment. We keep a reference
    // to this generated item in order to update it.
    private HashMap<String, RubricCriterionRating>  mCommentRowItems = new HashMap<>(); // <criterionID, commentText>

    private boolean saveButtonEnabled = false;
    private boolean scoreTextChanged = false;
    private TextWatcher mTextWatcher;
    private String scoreEditTextCache;

    // Since BaseListRecyclerAdapter uses a sorted list to store the list items, there has to be something to order them by.
    private HashMap<String, Integer> mInsertedOrderHash = new HashMap<>();
    private List<RubricCriterion> mAssignmentRubric = new ArrayList<>();
    private HashMap<String, RubricCriterionRating> mRubricAssessment = new HashMap<>(); // <criterionId, rating>

    private HashMap<String, Double> mOriginalPointsHash = new HashMap<>(); // <criterionID, points>

    /* For testing purposes only */
    protected RubricRecyclerAdapter(Context context){
        super(context, RubricCriterion.class, RubricCriterionRating.class);
    }

    public RubricRecyclerAdapter(Context context, CanvasContext canvasContext, Assignment assignment,
            Submission submission, SubmissionListener submissionListener, RubricAdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, RubricCriterion.class, RubricCriterionRating.class);
        mTopViewHeader = new RubricCriterion(null);
        mTopViewHeader.setId("TopViewHeader"); // needs an id for expandableRecyclerAdapter to work
        mCanvasContext = canvasContext;
        mAdapterToFragment = adapterToFragmentCallback;
        mSubmissionListener = submissionListener;
        mAssignment = assignment;
        mSubmission = submission;
        mAssignmentRubric = mAssignment.getRubric();
        mRubricAssessment = submission.getRubricAssessmentHash();
        setExpandedByDefault(true);
        saveOriginalAssessment(mSubmission.getRubricAssessment());
        initCache(mAssignment.getRubric());
        initFragmentWithSubmissionData();
    }

    @Override
    public void contextReady() {}

    /**
     * Unsaved comment data
     * @param rubric
     */
    private void initCache(List<RubricCriterion> rubric){
        scoreTextChanged = false;
        saveButtonEnabled = false;
        scoreEditTextCache = null;
        mCommentRowItems.clear();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else if (viewType == Types.TYPE_TOP_HEADER) {
            return new RubricHeaderViewHolder(v, getContext(), mAssignment, mSubmission, this, mTextWatcher, mSubmissionListener);
        } else {
            BaseRubricViewHolder holder =  BaseRubricViewHolder.getRubricViewHolder(v, viewType);
            holder.rubricType = viewType;
            return holder;
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else if (viewType == Types.TYPE_TOP_HEADER){
            return RubricHeaderViewHolder.holderResId();
        } else {
            return BaseRubricViewHolder.getChildLayoutResIds(viewType);
        }
    }



    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, RubricCriterion rubricCriterion, RubricCriterionRating rubricRating) {
        // It's important to differentiate between the RubricCriterionRatings from an Assignment Rubric and the RubricCriterionRatings
        // returned from a submission's RubricAssessment. RubricAssessment ratings will contain meta data such as isComment isRating,
        // where as an Assignment Rubric does not. Displaying comments needs a comment cache as well as the submission assessment
        // rating in order to differentiate between no comment, comment, and unsaved comments.
        RubricCriterionRating assessmentRating = mRubricAssessment.get(rubricRating.getCriterionId());
        final boolean isGrade = assessmentRating != null && (assessmentRating.getPoints() == rubricRating.getPoints());

        RubricBinder.bind(getContext(), (BaseRubricViewHolder) holder, rubricRating, assessmentRating, mCommentRowItems.get(rubricCriterion.getId()), isGrade, mAdapterToFragment);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, RubricCriterion rubricCriterion, boolean isExpanded) {
        if (holder instanceof RubricHeaderViewHolder) {
            onBindTopHeaderHolder(holder);
        } else {
            ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, rubricCriterion,
                    rubricCriterion.getCriterionDescription(), isExpanded, getViewHolderHeaderClicked());
        }
    }

    private void onBindTopHeaderHolder(RecyclerView.ViewHolder holder) {
        RubricTopHeaderBinder.bind((RubricHeaderViewHolder) holder, mSubmission, scoreEditTextCache, saveButtonEnabled);
    }

    // region Data

    /**
     * Update our rubric with new submission data. Used by our RubricFragment's viewpager when
     * a new submission is selected.
     * @param submission
     */
    public void setSubmission(Submission submission){
        resetData();
        mSubmission = submission;
        mAssignmentRubric = mAssignment.getRubric();
        mRubricAssessment = submission.getRubricAssessmentHash();
        initCache(mAssignment.getRubric());
        saveOriginalAssessment(mSubmission.getRubricAssessment());
        handleSaveButton();
        initFragmentWithSubmissionData();
    }

    public void initFragmentWithSubmissionData(){
        mAdapterToFragment.onRefreshFinished();
        addOrUpdateGroup(mTopViewHeader); // acts as a place holder for the top header
        initTextWatcher();
        populateAssignmentDetails();
        setAllPagesLoaded(true);
    }

    @Override
    public void loadData() {
        SubmissionAPI.getUserSubmissionWithCommentsHistoryAndRubric(mCanvasContext, mAssignment.getId(), mSubmission.getUser_id(), new CanvasCallback<Submission>(this) {
            @Override
            public void cache(Submission submission, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(Submission submission, LinkHeaders linkHeaders, Response response) {
                setSubmission(submission);
                mSubmissionListener.onSubmissionRubricAssessmentUpdated(submission.getRubricAssessmentHash(), submission.getScore(), submission.getGrade());
                mAdapterToFragment.onRefreshFinished();
            }
        });
    }

    private void initTextWatcher() {
        // grey out the edit text if it's use rubric for grading type
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                scoreEditTextCache = s.toString();
                if(s.toString().equals(String.valueOf(mSubmission.getScore())) || s.toString().equals(String.valueOf(mSubmission.getGrade())) || (mSubmission.getScore() == 0 && s.toString().toLowerCase().equals(""))){
                    setScoreEditTextChanged(false);
                }else{
                    setScoreEditTextChanged(true);
                }
            }
        };
    }

    private void populateAssignmentDetails() {
        final List<RubricCriterion> rubric = mAssignment.getRubric();

        if (mAssignment.hasRubric() && !mAssignment.isFreeFormCriterionComments()) {
            populateRatingItems(rubric);
        }
        else if(mAssignment.isFreeFormCriterionComments()){
            populateFreeFormRatingItems(rubric);
        }
        else if(getAdapterToRecyclerViewCallback() != null) {
            getAdapterToRecyclerViewCallback().setIsEmpty(true);
        }
    }
    // endregion

    //region populate adapters

    /**
     * Populates the adapter with RubricCriterionRatings from the ASSIGNMENT rubric. RubricCriterionRatings from
     * the Submission RubricAssessment should not be used here.
     * @param rubric
     */
    private void populateRatingItems(List<RubricCriterion> rubric){
        int insertCount = 0;
        for (RubricCriterion rubricCriterion : rubric) {
            final List<RubricCriterionRating> rubricCriterionRatings = rubricCriterion.getRatingsWithCriterionIds();
            for(RubricCriterionRating rating : rubricCriterionRatings) {
                mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
                addOrUpdateItem(rubricCriterion, rating);
            }

            // Add an additional row to each rubric criterion for comments
            RubricCriterionRating comment = createCommentItem(rubricCriterion);
            mCommentRowItems.put(rubricCriterion.getId(), comment);
            mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
            addOrUpdateItem(rubricCriterion, comment);
        }
    }

    private void populateFreeFormRatingItems(List<RubricCriterion> rubric) {
        int insertCount = 0;
        for(RubricCriterion rubricCriterion : rubric){
            RubricCriterionRating freeformItem = createFreeFormCommentItem(rubricCriterion);
            mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
            mCommentRowItems.put(rubricCriterion.getId(), freeformItem);
            addOrUpdateItem(rubricCriterion, freeformItem);
        }
    }

    private static RubricCriterionRating createCommentItem(RubricCriterion rubricCriterion){
        RubricCriterionRating commentItem = new RubricCriterionRating();
        commentItem.setComments(Const.NO_COMMENT);
        commentItem.setCriterionId(rubricCriterion.getId());
        commentItem.setId(String.valueOf(App.getUniqueId()));
        return commentItem;
    }

    private static RubricCriterionRating createFreeFormCommentItem(RubricCriterion rubricCriterion){
        RubricCriterionRating commentItem = new RubricCriterionRating();
        commentItem.setIsFreeFormComment(true);
        commentItem.setMaxPoints(rubricCriterion.getPoints());

        commentItem.setComments(Const.NO_COMMENT);
        commentItem.setPoints(Integer.MIN_VALUE);
        commentItem.setCriterionId(rubricCriterion.getId());
        commentItem.setId(String.valueOf(App.getUniqueId()));
        return commentItem;
    }
    //endregion

    // region Grade Helpers
    public void saveSubmission(String scoreText){

        saveCommentsCacheToRubricAssessment();

        SubmissionAPI.postSubmissionRubricAssessmentMap(mCanvasContext, mRubricAssessment, scoreText, mAssignment.getId(), mSubmission.getUser_id(), new CanvasCallback<Submission>((DocumentActivity)mContext) {
            @Override
            public void cache(Submission submission, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(Submission submission, LinkHeaders linkHeaders, Response response) {
                if(APIHelpers.isCachedResponse(response)){
                    return;
                }

                Toast.makeText(mContext, mContext.getString(R.string.gradeSaved), Toast.LENGTH_SHORT).show();
                submission.setRubricAssessment(mRubricAssessment);
                setSubmission(submission);
                mSubmissionListener.onSubmissionRubricAssessmentUpdated(mRubricAssessment, submission.getScore(), submission.getGrade());

                // Hide the keyboard if it has focus
                if(((Activity)mContext).getCurrentFocus() != null){
                    InputMethodManager inputMethodManager = (InputMethodManager)  mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(((Activity)mContext).getCurrentFocus().getWindowToken(), 0);
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                Toast.makeText(mContext, mContext.getString(R.string.errorSavingGrade), Toast.LENGTH_SHORT).show();
                mSubmissionListener.setPagingEnabled(false);
                return super.onFailure(retrofitError);
            }
        });
    }

    private void saveCommentsCacheToRubricAssessment(){
        for(Map.Entry<String, RubricCriterionRating> entry : mCommentRowItems.entrySet()) {
            // It's possible to save a submission comment, without marking a grade, in which case the submission
            // rubric assessment will not contain a key for that criterion id. In this case,
            // we need to create a new rubric rating and save that.
            RubricCriterionRating rating = entry.getValue();
            if(rating == null) {
                return;
            }

            String newComment =  rating.getComments();
            if(mRubricAssessment.containsKey(entry.getKey())){
                RubricCriterionRating oldRating = mRubricAssessment.get(entry.getKey());
                if(!Const.NO_COMMENT.equals(rating.getComments())){
                    // If there is a new comment entered. Use that, otherwise save the original comment
                    oldRating.setComments(newComment);
                }
                if(mAssignment.isFreeFormCriterionComments()){
                    oldRating.setPoints(rating.getPoints());
                }
            }else{
                if(Const.NO_COMMENT.equals(newComment)){
                    // If no prior comment and no new comment, save an empty string
                    rating.setComments("");
                }
                mRubricAssessment.put(entry.getKey(), rating);
            }
        }
    }

    /**
     * Called by RubricFragment's onActivityResult method when a user saves a new comment in the
     * RubricCommentDialog
     * @param position
     */
    public void updateCommentCache(RubricCriterionRating rating, int position){
        RubricCriterionRating commentRating = mCommentRowItems.get(rating.getCriterionId());
        commentRating.setComments(rating.getComments());
        commentRating.setPoints(rating.getPoints());
        handleSaveButton();
        notifyItemChanged(position);
    }

    public boolean hasUnsavedComments(){
        // Check our comments cache to see if new comments have been entered
        for(Map.Entry<String, RubricCriterionRating> entry : mCommentRowItems.entrySet()){
            RubricCriterionRating rating = entry.getValue();
            if(!Const.NO_COMMENT.equals(rating.getComments())){
                return true;
            }
        }
        return false;
    }

    /**
     * When a new submission is displayed, keep track of points given for each criterionID. We use
     * this later to see if a new grade was selected on RowClick.
     * @param rubricAssessment
     */
    private void saveOriginalAssessment(RubricAssessment rubricAssessment) {
        mOriginalPointsHash.clear();
        for(RubricCriterionRating rating : rubricAssessment.getRatings()){
            mOriginalPointsHash.put(rating.getCriterionId(), rating.getPoints());
        }
    }

    /**
     * Check to see if the current rubric assessments points match our original rubric assessment.
     * Note : we save the original scores since the user could click a new rubric item, change their mind,
     * and then click the original score. In that case, we want to grey out the save button.
     * @return
     */
    public boolean hasRubricAssessmentChanged() {
        for (Map.Entry<String, RubricCriterionRating> entry : mRubricAssessment.entrySet()) {
            RubricCriterionRating currentRating = entry.getValue();
            if(mOriginalPointsHash.get(currentRating.getCriterionId()) == null ||
                    currentRating.getPoints() != mOriginalPointsHash.get(currentRating.getCriterionId())){
                return true;
            }
        }
        return false;
    }

    public void setScoreEditTextChanged(boolean hasChanged){
        scoreTextChanged = hasChanged;
        handleSaveButton();
    }

    public void handleSaveButton(){
        boolean hasUnsavedData = hasRubricAssessmentChanged() || hasUnsavedComments() || scoreTextChanged;
        // The save button is ENABLED when unsaved data exists
        // Paging is DISABLED when unsaved data exists

        mSubmissionListener.setPagingEnabled(!hasUnsavedData);
        saveButtonEnabled = hasUnsavedData;
        if(mAssignment.isUseRubricForGrading() && hasUnsavedData){
            scoreEditTextCache = String.valueOf(getRubricAssessmentScore());
        }
        notifyItemChanged(0);
    }

    public RubricCriterionRating getSubmissionRatingForCriterionId(String criterionId){
        RubricCriterionRating rating = mRubricAssessment.get(criterionId);
        if(rating != null){
            rating.setCriterionId(criterionId);
        }
        return rating;
    }

    /**
     * Returns the total value of items inside of mRubricAssessment. This method is  called when
     * useRubricForGrading is true in order to automatically update the assessment score.
     * @return
     */
    private long getRubricAssessmentScore(){
        if(mRubricAssessment == null){
            return 0;
        }

        long score = 0;
        for(Map.Entry<String, RubricCriterionRating> entry : mRubricAssessment.entrySet()) {
            RubricCriterionRating assessmentRating = entry.getValue();
            score += assessmentRating.getPoints();
        }
        return score;
    }

    private boolean hasRatingChanged(RubricCriterionRating oldRating, RubricCriterionRating newRating) {
        return oldRating == null || newRating.getPoints() != oldRating.getPoints();
    }

    public RubricCriterion getCriterionById(String id){
        return getGroup((long)id.hashCode());
    }

    /**
     * Called by our RubricFragment's RubricAdapterToFragmentCallback method when a new rubric rating is selected
     * @param newRating
     */
    public void updateRubricAssessment(RubricCriterionRating newRating){
        RubricCriterionRating oldRating = getSubmissionRatingForCriterionId(newRating.getCriterionId());
        RubricCriterion group = getCriterionById(newRating.getCriterionId());

        if(hasRatingChanged(oldRating, newRating)){
            mRubricAssessment.put(newRating.getCriterionId(), newRating);
        }

        addOrUpdateItem(group, newRating);
        updateOriginalRating(newRating.getCriterionId());
        handleSaveButton();
    }

    private void updateOriginalRating(String criterionId){
        for(RubricCriterion criteria : mAssignmentRubric){
            if(criteria.getId() == criterionId){
                for(RubricCriterionRating currentRating : criteria.getRatings()){
                    addOrUpdateItem(criteria, currentRating);
                }
            }
        }
    }
    // endregion

    // region Expandable Callbacks
    @Override
    public GroupSortedList.GroupComparatorCallback<RubricCriterion> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<RubricCriterion>() {
            @Override
            public int compare(RubricCriterion o1, RubricCriterion o2) {
                if (o1 == o2 && o1 == mTopViewHeader) {
                    return 0;
                } else if (o1 == mTopViewHeader) {
                    return -1;
                } else if (o2 == mTopViewHeader) {
                    return 1;
                }
                return mInsertedOrderHash.get(o1.getId()) - mInsertedOrderHash.get(o2.getId());
            }

            @Override
            public boolean areContentsTheSame(RubricCriterion oldGroup, RubricCriterion newGroup) {
                if (oldGroup == mTopViewHeader && newGroup == mTopViewHeader) {
                    return true;
                }
                return oldGroup.getCriterionDescription().equals(newGroup.getCriterionDescription());
            }

            @Override
            public boolean areItemsTheSame(RubricCriterion group1, RubricCriterion group2) {
                return group1.getId().equals(group2.getId());
            }

            @Override
            public long getUniqueGroupId(RubricCriterion group) {
                return group.getId().hashCode();
            }

            @Override
            public int getGroupType(RubricCriterion group) {
                if (group == mTopViewHeader) {
                    return Types.TYPE_TOP_HEADER;
                } else {
                    return Types.TYPE_HEADER;
                }
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<RubricCriterion, RubricCriterionRating> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<RubricCriterion, RubricCriterionRating>() {
            @Override
            public int compare(RubricCriterion group, RubricCriterionRating o1, RubricCriterionRating o2) {
                // put comments at the bottom
                if (o1.isComment() && o2.isComment()) {
                    return 0;
                } else if (o1.isComment()) {
                    return 1;
                } else if (o2.isComment()) {
                    return -1;
                }
                return Double.compare(o2.getPoints(), o1.getPoints());
            }

            @Override
            public boolean areContentsTheSame(RubricCriterionRating oldItem, RubricCriterionRating newItem) {
                if(oldItem.getRatingDescription() == null || newItem.getRatingDescription() == null){return false;}

                return oldItem.getRatingDescription().equals(newItem.getRatingDescription())
                        && !(oldItem.isComment() || newItem.isComment()) // if its a comment always refresh the layout
                        && oldItem.getPoints() == newItem.getPoints();
            }

            @Override
            public boolean areItemsTheSame(RubricCriterionRating item1, RubricCriterionRating item2) {
                return item1.getId().equals(item2.getId());
            }

            @Override
            public long getUniqueItemId(RubricCriterionRating item) {
                return item.getId().hashCode();
            }

            @Override
            public int getChildType(RubricCriterion group, RubricCriterionRating item) {
                return BaseRubricViewHolder.getChildType(item, mRubricAssessment.get(group.getId()), mCommentRowItems.get(group.getId()));
            }
        };
    }
    // endregion
}
