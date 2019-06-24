/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.SubmissionManager;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.RubricCriterion;
import com.instructure.canvasapi2.models.RubricCriterionAssessment;
import com.instructure.canvasapi2.models.RubricCriterionRating;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.NumberHelper;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.student.R;
import com.instructure.student.binders.BaseBinder;
import com.instructure.student.binders.ExpandableHeaderBinder;
import com.instructure.student.binders.RubricBinder;
import com.instructure.student.binders.RubricTopHeaderBinder;
import com.instructure.student.holders.ExpandableViewHolder;
import com.instructure.student.holders.RubricTopHeaderViewHolder;
import com.instructure.student.holders.RubricViewHolder;
import com.instructure.student.interfaces.AdapterToFragmentCallback;
import com.instructure.student.model.RubricCommentItem;
import com.instructure.student.model.RubricItem;
import com.instructure.student.model.RubricRatingItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Response;

public class RubricRecyclerAdapter extends ExpandableRecyclerAdapter<RubricCriterion, RubricItem, RecyclerView.ViewHolder> {

    private CanvasContext mCanvasContext;
    private Assignment mAssignment;
    private AdapterToFragmentCallback mAdapterToFragment;
    private StatusCallback<Submission> mSubmissionCallback;
    private RubricCriterion mTopViewHeader; // The top header is just a group with a different view layout
    private Map<String, RubricCriterionAssessment> mAssessmentMap = new HashMap<>();

    // region Order Work-around
    // Since BaseListRecyclerAdapter uses a sorted list to store the list items, there has to be something to order them by.
    // Recipients have no clear way to order (Can't do by name, because the Last name isn't always in a consistent spot)
    // Since a hash is pretty easy, it made more sense than to create another BaseListRA that had a different representation.
    private HashMap<String, Integer> mInsertedOrderHash = new HashMap<>();

    /* For testing purposes only */
    protected RubricRecyclerAdapter(Context context){
        super(context, RubricCriterion.class, RubricItem.class);
    }

    public RubricRecyclerAdapter(Context context, CanvasContext canvasContext, AdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, RubricCriterion.class, RubricItem.class);
        mTopViewHeader = new RubricCriterion(
                "TopViewHeader", // Needs an id for expandableRecyclerAdapter to work
                null, null, -1, new ArrayList<RubricCriterionRating>(), false, false);

        mCanvasContext = canvasContext;
        mAdapterToFragment = adapterToFragmentCallback;
        setExpandedByDefault(true);
        // loadData is called from RubricFragment
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else if (viewType == Types.TYPE_TOP_HEADER) {
            return new RubricTopHeaderViewHolder(v);
        } else {
            return new RubricViewHolder(v, viewType);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else if (viewType == Types.TYPE_TOP_HEADER){
            return RubricTopHeaderViewHolder.holderResId();
        } else {
            return RubricViewHolder.holderResId(viewType);
        }
    }

    @Override
    public void contextReady() {}

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, RubricCriterion rubricCriterion, RubricItem rubricItem) {
        if(!mAssignment.getMuted()){
            RubricCriterionAssessment assessment = mAssessmentMap.get(rubricCriterion.getId());
            RubricBinder.Companion.bind(
                    getContext(),
                    (RubricViewHolder) holder,
                    rubricItem,
                    rubricCriterion,
                    mAssignment.getFreeFormCriterionComments(),
                    mAssignment.getRubricSettings() != null && mAssignment.getRubricSettings().getHidePoints(),
                    assessment,
                    mCanvasContext);
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, RubricCriterion rubricCriterion, boolean isExpanded) {
        if (holder instanceof RubricTopHeaderViewHolder) {
            onBindTopHeaderHolder(holder);
        } else {
            if(!mAssignment.getMuted()) {
                ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, rubricCriterion, rubricCriterion.getDescription(), isExpanded, getViewHolderHeaderClicked());
            }
        }
    }

    private void onBindTopHeaderHolder(RecyclerView.ViewHolder holder) {
        RubricTopHeaderBinder.bind(getContext(), mCanvasContext, (RubricTopHeaderViewHolder) holder, getCurrentPoints(), getCurrentGrade(), getLatePenalty(), getFinalGrade(), mAssignment.getMuted());
    }

    // region Data

    @Override
    public void loadData() {
        // use loadDataChained instead, since its a nested fragment and has chained callbacks
        loadDataChained(); // Used when data is refreshed
    }

    /**
     * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.student.activity.CallbackActivity}
     */
    public void loadDataChained() {
        if (mAssignment == null) { return; }
        SubmissionManager.getSingleSubmission(mCanvasContext.getId(), mAssignment.getId(), ApiPrefs.getUser().getId(), mSubmissionCallback, isRefresh());
    }

    @Override
    public void setupCallbacks() {
        mSubmissionCallback = new StatusCallback<Submission>() {
            @Override
            public void onResponse(@NonNull Response<Submission> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                Submission submission = response.body();
                mAssessmentMap = submission.getRubricAssessment();
                mAssignment.setSubmission(submission);
                mAdapterToFragment.onRefreshFinished();
                populateAssignmentDetails();
                setAllPagesLoaded(true);
            }

            @Override
            public void onFail(@Nullable Call<Submission> call, @NonNull Throwable error, @Nullable Response<?> response) {
                populateAssignmentDetails();
            }
        };
    }

    private void populateAssignmentDetails() {
        addOrUpdateGroup(mTopViewHeader); // acts as a place holder for the top header

        final List<RubricCriterion> rubric = mAssignment.getRubric();

        if (mAssignment.hasRubric() && !mAssignment.getFreeFormCriterionComments()) {
            populateRatingItems(rubric);
        } else if(mAssignment.getFreeFormCriterionComments()){
            populateFreeFormRatingItems(rubric);
        } else {
            getAdapterToRecyclerViewCallback().setIsEmpty(true);
        }
    }
    // endregion

    // region Grade Helpers
    private void populateRatingItems(List<RubricCriterion> rubric){
        int insertCount = 0;
        for (RubricCriterion rubricCriterion : rubric) {
            final List<RubricCriterionRating> rubricCriterionRatings = rubricCriterion.getRatings();
            for(RubricCriterionRating rating : rubricCriterionRatings) {
                mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
                addOrUpdateItem(rubricCriterion, new RubricRatingItem(rating));
            }
            populateFreeFormRatingItems(rubric);
            insertCount = addTotalPointsFromRange(rubricCriterion, insertCount);
        }
    }

    private void populateFreeFormRatingItems(List<RubricCriterion> rubric) {
        int insertCount = 0;
        for(RubricCriterion rubricCriterion : rubric){
            RubricItem gradedRating = getFreeFormRatingForCriterion(rubricCriterion);
            if( gradedRating != null){
                mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
                addOrUpdateItem(rubricCriterion, gradedRating);
            }
            insertCount = addTotalPointsFromRange(rubricCriterion, insertCount);
        }
    }

    /**
     * We only want to add a score if the rubric uses a range, otherwise they already know their score
     * @param rubricCriterion
     */
    private int addTotalPointsFromRange(RubricCriterion rubricCriterion, int insertCount) {
        if(!rubricCriterion.getCriterionUseRange()) return insertCount;

        Submission lastSubmission = mAssignment.getSubmission();
        if(lastSubmission != null){
            RubricCriterionAssessment rating =  lastSubmission.getRubricAssessment().get(rubricCriterion.getId());
            if(rating != null){
                double points = 0.0;
                if(rating.getPoints() != null) {
                    points = rating.getPoints();
                }
                RubricCriterionRating rubricCriterionRating = new RubricCriterionRating(
                        "null" + (rubricCriterion.getId()),
                        getContext().getString(R.string.score),
                        null,
                        points
                );
                mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
                addOrUpdateItem(rubricCriterion, new RubricRatingItem(rubricCriterionRating));
            }
        }
        return insertCount;
    }

    @Nullable
    private RubricItem getFreeFormRatingForCriterion(RubricCriterion criterion){
        Submission lastSubmission = mAssignment.getSubmission();
        if (lastSubmission != null){
          RubricCriterionAssessment rating = lastSubmission.getRubricAssessment().get(criterion.getId());
            // we only care about the comment if it isn't null
            if (rating != null && !(rating.getComments() == null && rating.getPoints() == null)){
                return new RubricCommentItem(rating.getComments(), rating.getPoints());
            }
            return null;
        }
        return null;
    }

    private String getPointsPossible() {
        if (mAssignment != null) {
            return NumberHelper.formatDecimal(mAssignment.getPointsPossible(), 2, true);
        }
        return "";
    }

    private boolean containsGrade() {
        return mAssignment != null && mAssignment.getSubmission() != null && mAssignment.getSubmission().getGrade() != null && !mAssignment.getSubmission().getGrade().equals("null");
    }

    private boolean isExcused() {
        return mAssignment != null && mAssignment.getSubmission() != null && mAssignment.getSubmission().getExcused();
    }

    private boolean isGradeLetterOrPercentage(String grade) {
        return grade.contains("%") || grade.matches("[a-zA-Z]+");
    }

    @Nullable
    private String getCurrentGrade() {
        String pointsPossible = getPointsPossible();
        if (isExcused()) {
            return getContext().getString(R.string.grade) + "\n" + getContext().getString(R.string.excused) + " / " + pointsPossible;
        }
        if (containsGrade()) {
            String grade;
            if(mAssignment.getSubmission().getPointsDeducted() == null) {
                grade = mAssignment.getSubmission().getGrade();
            } else {
                grade = mAssignment.getSubmission().getEnteredGrade();
            }
            if (isGradeLetterOrPercentage(grade)) {
                return getContext().getString(R.string.grade) + "\n" + grade;
            } else {
                grade = BaseBinder.Companion.getGrade(mAssignment.getSubmission(), mAssignment.getPointsPossible(), getContext());
                return getContext().getString(R.string.grade) + "\n" + grade;
            }
        }
        return null;
    }

    /**
     * Takes into account late policy
     * @return
     */
    @Nullable
    private String getLatePenalty() {

        if (isExcused()) {
            return null;
        }
        if (mAssignment.getSubmission() != null && mAssignment.getSubmission().getPointsDeducted() != null) {
            return String.format(Locale.getDefault(), getContext().getString(R.string.latePenalty), NumberHelper.formatDecimal(mAssignment.getSubmission().getPointsDeducted(),2, true));
        }
        return null;
    }

    /**
     * Takes into account late policy
     * @return
     */
    @Nullable
    private String getFinalGrade() {
        if (isExcused()) {
            return null;
        }
        if (containsGrade()) {
            String grade = mAssignment.getSubmission().getGrade();
            if (isGradeLetterOrPercentage(grade)) {
                return getContext().getString(R.string.finalGrade) + "\n" + grade;
            } else {
                grade = BaseBinder.Companion.getGrade(mAssignment.getSubmission(), mAssignment.getPointsPossible(), getContext());
                return getContext().getString(R.string.finalGrade) + "\n" + grade;
            }
        }
        return null;
    }

    private String getCurrentPoints() {
        String pointsPossible = getPointsPossible();
        if (isExcused()) {
            return null;
        }
        if (containsGrade()) {
            String grade = mAssignment.getSubmission().getGrade();

            if (isGradeLetterOrPercentage(grade)) {
                double score;
                if(mAssignment.getSubmission().getPointsDeducted() == null) {
                    score = mAssignment.getSubmission().getScore();
                } else {
                    score = mAssignment.getSubmission().getEnteredScore();
                }
                String formattedScore = NumberHelper.formatDecimal(score, 2, true);
                return getContext().getString(R.string.points) + "\n" + formattedScore + " / " + pointsPossible;
            } else {
                return null;
            }
        } else {
            //the user doesn't have a grade, but we should display points possible if the mAssignment isn't null
            if(mAssignment != null) {
                //if the user is a teacher show them how many points are possible for the mAssignment
                if(((Course)mCanvasContext).isTeacher()) {
                    return getContext().getString(R.string.pointsPossibleNoPeriod) + "\n" + pointsPossible;
                } else {
                    return getContext().getString(R.string.points) + "\n" + "- / " + pointsPossible;
                }
            }
        }
        return getContext().getString(R.string.points) + "\n" + "- / -";
    }

    // endregion

    // region Expandable Callbacks
    @Override
    public GroupSortedList.GroupComparatorCallback<RubricCriterion> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<RubricCriterion>() {
            @Override
            public int compare(RubricCriterion o1, RubricCriterion o2) {
                // Always put the TopViewHeader at the top
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
                return oldGroup.getDescription().equals(newGroup.getDescription());
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
    public GroupSortedList.ItemComparatorCallback<RubricCriterion, RubricItem> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<RubricCriterion, RubricItem>() {
            @Override
            public int compare(RubricCriterion group, RubricItem o1, RubricItem o2) {
                // put comments at the bottom
                if (o1 instanceof RubricCommentItem && o2 instanceof RubricCommentItem) {
                    return 0;
                } else if (o1 instanceof RubricCommentItem) {
                    return 1;
                } else if (o2 instanceof RubricCommentItem) {
                    return -1;
                }
                RubricCriterionRating r1 = ((RubricRatingItem)o1).getRating();
                RubricCriterionRating r2 = ((RubricRatingItem)o2).getRating();
                if (r2.getId() != null && r2.getId().contains("null")) {
                    return -1;
                } else if (r1.getId() != null && r1.getId().contains("null")) {
                    return -1;
                }
                return Double.compare(r2.getPoints(), r1.getPoints());
            }

            @Override
            public boolean areContentsTheSame(RubricItem oldItem, RubricItem newItem) {
                if (newItem instanceof RubricCommentItem || oldItem instanceof RubricCommentItem) {
                    // if its a comment always refresh the layout
                    return false;
                } else {
                    RubricCriterionRating oldRating = ((RubricRatingItem) oldItem).getRating();
                    RubricCriterionRating newRating = ((RubricRatingItem) newItem).getRating();
                    return !(oldRating.getDescription() == null || newRating.getDescription() == null)
                            && oldRating.getDescription().equals(newRating.getDescription())
                            && oldRating.getPoints() == newRating.getPoints();
                }
            }

            @Override
            public boolean areItemsTheSame(RubricItem item1, RubricItem item2) {
                if (item1 instanceof RubricCommentItem ^ item2 instanceof RubricCommentItem) {
                    return false;
                } else if ((item1 instanceof RubricCommentItem && ((RubricCommentItem) item1).getComment() == null) &&  (((RubricCommentItem) item2).getComment() == null)) {
                    return true;
                } else if (item1 instanceof RubricCommentItem) {
                    return (((RubricCommentItem) item1).getComment() != null &&
                            ((RubricCommentItem) item2).getComment() != null) &&
                        ((RubricCommentItem) item1).getComment().equals(((RubricCommentItem) item2).getComment());

                } else {
                    return (((RubricRatingItem) item1).getRating().getId() != null &&
                            ((RubricRatingItem) item2).getRating().getId() != null) &&
                         ((RubricRatingItem) item1).getRating().getId().equals(((RubricRatingItem) item2).getRating().getId());
                }
            }

            @Override
            public long getUniqueItemId(RubricItem item) {
                if (item instanceof RubricCommentItem) {
                    if (!TextUtils.isEmpty(((RubricCommentItem) item).getComment())) {
                        return (((RubricCommentItem) item).getComment() + UUID.randomUUID().toString().hashCode()).hashCode();
                    } else {
                        return UUID.randomUUID().toString().hashCode();
                    }
                } else {
                    if (!TextUtils.isEmpty(((RubricRatingItem)item).getRating().getId())) {
                        return (((RubricRatingItem) item).getRating().getId() + ((RubricRatingItem)item).getRating().getDescription()).hashCode();
                    } else {
                        return UUID.randomUUID().toString().hashCode();
                    }
                }
            }

            @Override
            public int getChildType(RubricCriterion group, RubricItem item) {
                return (item instanceof RubricCommentItem) ? RubricViewHolder.TYPE_ITEM_COMMENT : RubricViewHolder.TYPE_ITEM_POINTS;
            }
        };
    }
    // endregion

    // region Getter & Setters

    public Assignment getAssignment() {
        return mAssignment;
    }

    public void setAssignment(Assignment assignment) {
        this.mAssignment = assignment;
    }


    // endregion

}
