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

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.model.Submission;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.TutorialUtils;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.adapters.UserSubmissionsListAdapter;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.util.ExpandCollapseAnimation;
import com.instructure.speedgrader.util.ViewUtils;

import java.util.List;

public class StudentListView  extends PopupWindow{

    private Assignment mAssignment;
    private List<Submission> mSubmissionsList;
    private List<Section> mSections;
    private long mCurrentSectionId;

    private UserSubmissionsListAdapter mSubmissionsAdapter;
    private FragmentActivity mActivity;
    private Context mApplicationContext;
    private ListView mListView;
    private StudentListViewListener mListener;
    private boolean isSortByName = true;

    public interface StudentListViewListener{
        void onCurrentSubmission(int position);
        void onSubmissionListUpdated();
    }

    public StudentListView(FragmentActivity activity, Assignment assignment, List<Section> sections, List<Submission> submissionsList, long currentSectionId, int gradedCount) {
        super((int) ViewUtils.convertDipsToPixels(320, activity.getApplicationContext()), RelativeLayout.LayoutParams.WRAP_CONTENT);

        try {
            mListener = (StudentListViewListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement StudentListViewListener");
        }

        mCurrentSectionId = currentSectionId;
        mSections = sections;
        mAssignment = assignment;
        mSubmissionsList = submissionsList;
        mActivity = activity;
        mApplicationContext = activity.getApplicationContext();

        View popupView = LayoutInflater.from(mActivity).inflate(R.layout.student_list, null);
        HelveticaTextView gradeCount = (HelveticaTextView)popupView.findViewById(R.id.gradeCount);
        gradeCount.setText(String.format("%s/%s %s", String.valueOf(gradedCount), String.valueOf(submissionsList.size()), mActivity.getString(R.string.graded)));

        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        initListView(popupView);
        initSearchView(popupView);
        initSortSelector(popupView);

        setContentView(popupView);
        setFocusable(true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private void initListView(View popupView){
        // get the listview inside our popupwindow and set our adapter to it
        mListView = (ListView) popupView.findViewById(R.id.studentList);

        mSubmissionsAdapter = new UserSubmissionsListAdapter(mActivity.getApplicationContext(), mSubmissionsList, mAssignment, mListView, ((App) mActivity.getApplication()).showStudentNames(), mListener);
        if(isSortByName){
            mSubmissionsAdapter.sortSubmissionsByName(((App) mActivity.getApplication()).showUngradedStudentsFirst());
        }else{
            mSubmissionsAdapter.sortSubmissionsByGrade();
        }

        final ViewGroup header = (ViewGroup)LayoutInflater.from(mActivity).inflate(R.layout.student_list_headerview, mListView, false);
        final RelativeLayout row    = (RelativeLayout) header.findViewById(R.id.row);
        final LinearLayout dropDown = (LinearLayout) header.findViewById(R.id.rowDropdown);
        final RadioGroup radioGroup = (RadioGroup) header.findViewById(R.id.radioGroup);
        final ImageView expandArrow = (ImageView) header.findViewById(R.id.expandArrow);
        final TextView sectionTitle = (TextView) header.findViewById(R.id.sectionTitle);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpandCollapseAnimation.setHeightForWrapContent(mActivity, dropDown);
                ExpandCollapseAnimation expandCollapseAnimation = new ExpandCollapseAnimation(dropDown, 200);
                expandCollapseAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        RotateAnimation rotationAnim;
                        if(dropDown.getHeight() < 50){ // For some reason, onAnimatinoEnd is getting called slightly before the animation actually ends, causing the height to be a value something greater than 0.
                            //currently closed
                            rotationAnim =  new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        }else{
                            // currently open
                            rotationAnim =  new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        }
                        rotationAnim.setDuration(100);
                        rotationAnim.setFillAfter(true);
                        expandArrow.startAnimation(rotationAnim);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                dropDown.startAnimation(expandCollapseAnimation);
            }
        });

        // Set our arrow to gray
        final Drawable d = CanvasContextColor.getColoredDrawable(mApplicationContext, R.drawable.ic_cv_arrow_down_fill, ContextCompat.getColor(mActivity.getApplicationContext(), R.color.lightGray));
        expandArrow.setImageDrawable(d);

        mListView.addHeaderView(header);
        mListView.setTextFilterEnabled(true);
        mListView.setAdapter(mSubmissionsAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // show the submission clicked
                dismiss();
                int positionInList = mSubmissionsAdapter.getPositionForSubmission((Submission) mListView.getItemAtPosition(position));
                mListener.onCurrentSubmission(positionInList);
            }
        });

        initSectionsRadioGroup(radioGroup, sectionTitle, header);
    }

    public void notifyDataSetChanged(){
        if(mSubmissionsAdapter != null){
            mSubmissionsAdapter.notifyDataSetChanged();
        }
    }

    private void initSectionsRadioGroup(RadioGroup radioGroup, final TextView sectionTitle, ViewGroup header){
        sectionTitle.setText(getCurrentSection().getName());
        // Create our radio buttons
        for(int i = 0 ; i < mSections.size() ; i++){
            //create our new radiobutton
            RadioButton newRadioButton = new RadioButton(mApplicationContext);
            newRadioButton.setId(i);
            newRadioButton.setText(mSections.get(i).getName());
            newRadioButton.setTextColor(mActivity.getResources().getColor(R.color.canvasTextDark));
            newRadioButton.setTypeface(Typeface.createFromAsset(mApplicationContext.getAssets(), "HelveticaNeueLTCom-MdCn.ttf"));
            RadioGroup.LayoutParams  params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 4, 0, 4);

            // add the new radio button to the buttongroup
            radioGroup.addView(newRadioButton, params);

            // set the default selection for this radiobutton group
            if(mSections.get(i) != null && getCurrentSection() != null && mSections.get(i).getId() == mCurrentSectionId){
                radioGroup.check(newRadioButton.getId());
            }
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mSubmissionsAdapter.filterBySection(mSections.get(checkedId));
                // get the nearest student submission by section
                goToClosestSubmissionBySection(mSections.get(checkedId));
                sectionTitle.setText(mSections.get(checkedId).getName());
            }
        });

        // tutorial
        ImageView pulse = (ImageView) header.findViewById(R.id.pulse);
        new TutorialUtils(mActivity, App.getPrefs(), pulse, TutorialUtils.TYPE.FILTER_SECTIONS)
                .setContent(mActivity.getApplicationContext().getString(R.string.tutorial_tipFilterSections), mApplicationContext.getString(R.string.tutorial_tipFilterSectionsMessage))
                .build();

        mSubmissionsAdapter.filterBySection(getCurrentSection());
        goToClosestSubmissionBySection(getCurrentSection());
        mListView.invalidateViews();
    }


    private void initSearchView(View popupView){
        final SearchView searchView = (SearchView)popupView.findViewById(R.id.searchView);
        // Unfortunately Searchviews aren't very customizable and this is a hacky way of turning searchview text white
        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        theTextArea.setTextColor(ContextCompat.getColor(mApplicationContext, R.color.white));
        theTextArea.setHintTextColor(ContextCompat.getColor(mApplicationContext, R.color.white));

        // When the user types something into our searchview, we call our adapters' filter method
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSubmissionsAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void initSortSelector(View popupView){

        final CheckedLinearLayout sortSwitch = (CheckedLinearLayout) popupView.findViewById(R.id.sortSelector);

        sortSwitch.setLeftAsChecked(isSortByName);
        sortSwitch.setLeftIndicatorText(mApplicationContext.getResources().getString(R.string.name));
        sortSwitch.setRightIndicatorText(mApplicationContext.getResources().getString(R.string.grade));
        sortSwitch.setCallbacks(new CheckedLinearLayout.OnSwitchListener() {
            @Override
            public void onSwitch(boolean isLeftChecked) {
                if (!isLeftChecked) {
                    // !isLeftChecked == sort by Grade
                    isSortByName = false;
                    mSubmissionsAdapter.sortSubmissionsByGrade();
                    mSubmissionsAdapter.notifyDataSetChanged();
                    mListener.onSubmissionListUpdated();
                } else {
                    // isLeftChecked == sort by Name
                    isSortByName = true;
                    mSubmissionsAdapter.sortSubmissionsByName(((App) mActivity.getApplication()).showUngradedStudentsFirst());
                    mSubmissionsAdapter.notifyDataSetChanged();
                    mListener.onSubmissionListUpdated();
                }
            }
        });

        //set tutorial
        ImageView pulse = (ImageView) popupView.findViewById(R.id.pulse);
        new TutorialUtils(mActivity, App.getPrefs(), pulse, TutorialUtils.TYPE.FILTER_BY_GRADE)
                .setContent(mApplicationContext.getString(R.string.tutorial_tipSortStudentList), mApplicationContext.getString(R.string.tutorial_tipSortStudentListMessage))
                .build();
    }


    public Section getCurrentSection(){
        if(mSections != null){
            for(Section section : mSections){
                if(section.getId() == mCurrentSectionId){
                    return section;
                }
            }
        }
        return null;
    }

    private void goToClosestSubmissionBySection(Section section){
        int closestSubmission = mSubmissionsAdapter.getFirstSubmissionBySection(section);
        if(closestSubmission != -1){
            mListener.onCurrentSubmission(closestSubmission);
        }
    }
}
