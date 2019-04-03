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

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.model.User;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.activities.DocumentActivity;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.HelveticaTextView;
import com.instructure.speedgrader.views.StudentListView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Description:
 * Adapter used inside DocumentActivity dropdown menu to display the filterable list of students
 */
public class UserSubmissionsListAdapter extends BaseAdapter implements Filterable {

    private List<Submission> submissions;
    private List<Submission> originalData;
    private LayoutInflater li;

    private UserFilter userFilter;
    private SectionFilter sectionFilter;
    private Context context;
    private Assignment assignment;
    private ListView listView;

    private StudentListView.StudentListViewListener listener;
    private boolean showStudentNames;
    private boolean isGroupAssignment;
    private boolean isGradeIndividually;

    public UserSubmissionsListAdapter(Context context, List<Submission> objects, Assignment assignment, ListView listView, boolean showStudentNames, StudentListView.StudentListViewListener listener) {
        this.submissions =  objects;
        this.originalData = new ArrayList<>(objects);
        this.context = context;
        this.assignment = assignment;
        this.listView = listView;
        this.showStudentNames = showStudentNames;
        this.listener = listener;
        this.li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isGradeIndividually = this.assignment.isGradeGroupsIndividually();
        this.isGroupAssignment = this.assignment.getGroupCategoryId() != 0; }
    public void add(Submission submission) {
        submissions.add(submission);
        notifyDataSetChanged();
    }

    public void clear(){
        submissions.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Submission> newSubmissions){
        submissions.clear();
        submissions = newSubmissions;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent,true);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent, boolean isDropDown) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder              = new ViewHolder();
            convertView             = li.inflate(R.layout.list_item_student_grades, parent, false);
            viewHolder.rootView     = convertView.findViewById(R.id.rootView);
            viewHolder.sectionTitle = (HelveticaTextView) convertView.findViewById(R.id.gradeTitle);
            viewHolder.checkmark    = (ImageView) convertView.findViewById(R.id.checkmark);
            viewHolder.score        = (HelveticaTextView) convertView.findViewById(R.id.score);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // set user name
        if(showStudentNames){
            viewHolder.sectionTitle.setText(DocumentActivity.getDisplayNameBySubmission(submissions.get(position), isGroupAssignment, isGradeIndividually));
        }else{
            viewHolder.sectionTitle.setText(context.getString(R.string.student) + " " + String.valueOf(position+1));
        }

        if(DocumentPagerAdapter.isEmptySubmission(submissions.get(position))){
            viewHolder.sectionTitle.setTextColor(context.getResources().getColor(R.color.lightGray));
        }else{
            viewHolder.sectionTitle.setTextColor(context.getResources().getColor(R.color.sg_darkText));
        }

        // get the submission
        Submission userSubmission = submissions.get(position);

        // display grade and isGraded checkmark
        DecimalFormat format = new DecimalFormat("0.#"); // format the score to remove trailing zeros
        if(userSubmission.getGraderID() != 0 || userSubmission.getGrade() != null){
            if(isGraded(userSubmission)){
                // set the checkmark if the submission has been graded. It's possible that the user has a grade for the submission,
                // has resbumitted the assignment after receiving a grade. In this case, we want to regrade the submission, but still show the last grade.
                Drawable checkmark = CanvasContextColor.getColoredDrawable(context,
                        R.drawable.ic_cv_checkmark_fill, context.getResources().getColor(R.color.sg_checkmark_green));
                viewHolder.checkmark.setImageDrawable(checkmark);
                viewHolder.checkmark.setVisibility(View.VISIBLE);
            }else{
                viewHolder.checkmark.setVisibility(View.INVISIBLE);
                viewHolder.score.setText(context.getString(R.string.slash) + format.format(assignment.getPointsPossible()));
            }

            String grade = format.format(userSubmission.getScore()) +"/" +format.format(assignment.getPointsPossible());
            viewHolder.score.setText(grade);
        }else{
            viewHolder.checkmark.setVisibility(View.INVISIBLE);
            viewHolder.score.setText(context.getString(R.string.slash) + format.format(assignment.getPointsPossible()));
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return submissions.size();
    }

    @Override
    public Submission getItem(int position) {
        return submissions.get(position);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {}

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {}

    @Override
    public long getItemId(int position) { return 0; }

    @Override
    public boolean hasStableIds() { return false; }

    @Override
    public int getItemViewType(int position) { return 1; }

    @Override
    public int getViewTypeCount() { return 1; }

    @Override
    public boolean isEmpty() { return false; }

    class ViewHolder{
        ImageView checkmark;
        HelveticaTextView sectionTitle;
        View rootView;
        HelveticaTextView score;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Sorting
    ///////////////////////////////////////////////////////////////////////////
    public void sortSubmissionsByName(boolean showUngradedFirst){
        //sort submissions data
        sortByName(submissions, showUngradedFirst);

        // sort original data used for filtering
        sortByName(originalData, showUngradedFirst);

        notifyDataSetChanged();
        if(listView != null){
            listView.invalidateViews();
        }
    }

    public void sortSubmissionsByGrade(){
        //sort submissions data
        sortByGrade(submissions);

        // sort original data used for filtering
        sortByGrade(originalData);

        notifyDataSetChanged();
        if(listView != null){
            listView.invalidateViews();
        }
    }

    private void sortByName(List<Submission> submissions, final boolean showUngradedFirst){
        Collections.sort(submissions, new Comparator<Submission>() {
            @Override
            public int compare(Submission lhs, Submission rhs) {
                if (lhs.getUser() == null || rhs.getUser() == null) {
                    return 0;
                }
                if(showUngradedFirst){
                    if((isGraded(lhs) && isGraded(rhs)) || (!isGraded(lhs) && !isGraded(rhs))) {
                        // both ungraded/graded, compare them normally
                        return compareDisplayNames(lhs, rhs);
                    }
                    else if(isGraded(lhs) && !isGraded(rhs)){
                        // lhs is ungraded, rhs is graded
                        return 1;
                    }else if(!isGraded(lhs) && isGraded(rhs)){
                        // lhs is graded, rhs is ungraded
                        return -1;
                    }else{
                        return compareDisplayNames(lhs, rhs);
                    }
                }
                return compareDisplayNames(lhs, rhs);
            }
        });
    }

    // We want to always show ungraded first by default. and sort ungraded students by name. This will keep our list consistent for rotation changes.
    private void sortByGrade(List<Submission> submissions){
        Collections.sort(submissions, new Comparator<Submission>() {
            @Override
            public int compare(Submission lhs, Submission rhs) {
                Double left = Double.valueOf(lhs.getScore());
                Double right = Double.valueOf(rhs.getScore());

                if((isGraded(lhs) && isGraded(rhs))) {
                    // both ungraded/graded, compare them normally
                    return left.compareTo(right);
                }
                else if(!isGraded(lhs) && !isGraded(rhs)){
                    return compareDisplayNames(lhs, rhs);
                }
                else if(isGraded(lhs) && !isGraded(rhs)){
                    // lhs is ungraded, rhs is graded
                    return 1;
                }else if(!isGraded(lhs) && isGraded(rhs)){
                    // lhs is graded, rhs is ungraded
                    return -1;
                }else{
                    return compareDisplayNames(lhs, rhs);
                }
            }
        });
    }

    private int compareDisplayNames(Submission lhs, Submission rhs) {
        return DocumentActivity.getDisplayNameBySubmission(lhs, isGroupAssignment, isGradeIndividually).compareTo(DocumentActivity.getDisplayNameBySubmission(rhs, isGroupAssignment, isGradeIndividually));
    }

    public static boolean isGraded(Submission submission){
        if(submission.getSubmissionType() != null && submission.getSubmissionType().equals(Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ)) && submission.getBody() == null) {
            return true;
        }
        else if (((submission.getWorkflowState().equals(Const.GRADED)) || (submission.getGraderID() != 0 && submission.getGrade() != null)) && submission.isGradeMatchesCurrentSubmission()){
            return true;
        }

        return false;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Filters
    ///////////////////////////////////////////////////////////////////////////
    public int getFirstSubmissionBySection(Section section){
        for(Submission submission : originalData){
            for(User user : section.getStudents()){
                if(submission.getUser().equals(user)){
                    return originalData.indexOf(submission);
                }
            }
        }
        return -1;
    }

    @Override
    public Filter getFilter(){
        if(userFilter == null){
            userFilter = new UserFilter();
        }
        return userFilter;
    }

    public Filter getSectionsFilter(){
        if(sectionFilter == null){
            sectionFilter = new SectionFilter();
        }
        return sectionFilter;
    }

    List<User> userFilterList;

    public void filterBySection(Section section){
        if(section.getId() < 0){
            // All sections
            userFilterList = null;
        }else{
            userFilterList = section.getStudents();
        }

        getSectionsFilter().filter(String.valueOf(section.getId()));
    }

    private class UserFilter extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase(Locale.getDefault());
            FilterResults results = new FilterResults();
            List<Submission> searchResults = new ArrayList<>();

            if(constraint.toString().length() > 0){
                // filter our results
                for(Submission submission : originalData){
                    if(submission.getUser().getSortableName().toLowerCase(Locale.getDefault()).contains(constraint)){
                        searchResults.add(submission);
                    }
                }
                results.values = searchResults;
                results.count = searchResults.size();
            }else{
                // return original data
                results.values = originalData;
                results.count = originalData.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            submissions.clear();
            submissions.addAll((ArrayList<Submission>) results.values);
            listener.onSubmissionListUpdated();

            listView.invalidateViews();
        }
    }

    private class SectionFilter extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Submission> searchResults = new ArrayList<>();

            if(userFilterList != null){
                // filter our results
                for(Submission submission : originalData){
                    if(userFilterList.contains(submission.getUser())){
                        searchResults.add(submission);
                    }
                }
                results.values = searchResults;
                results.count = searchResults.size();
            }else{
                // return original data
                results.values = originalData;
                results.count = originalData.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            submissions.clear();
            submissions.addAll((ArrayList<Submission>) results.values);
            listener.onSubmissionListUpdated();
            listView.invalidateViews();
        }
    }

    public int getPositionForSubmission(Submission submission) {
        return submissions.indexOf(submission);
    }
}