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

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentGroup;
import com.instructure.canvasapi.model.Section;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.activities.DocumentActivity;
import com.instructure.speedgrader.activities.HomeActivity;
import com.instructure.speedgrader.adapters.AssignmentGroupListRecyclerAdapter;
import com.instructure.speedgrader.adapters.ExpandableRecyclerAdapter;
import com.instructure.speedgrader.interfaces.AssignmentAdapterToFragmentCallback;
import com.instructure.speedgrader.util.Const;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class AssignmentListFragment extends ParentFragment {

    private AssignmentAdapterToFragmentCallback mAdapterToFragmentCallback;
    private ExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder> mRecyclerAdapter;

    // model
    private HashMap<Long, AssignmentGroup> mAssignmentGroupsHash = new HashMap<>();
    private AssignmentGroup[] mAssignmentGroups;
    private List<Assignment> mAssignments = new ArrayList<>();
    private List<Section> mSections;

    // mSections filter spinner
    private Spinner mSectionsSpinner;
    private int mCurrentSectionSelection = 0;

    //routing
    private Boolean isRouting = false;
    private Assignment mRoutedAssignment;

    ///////////////////////////////////////////////////////////////////////////
    // View Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean retainInstanceState() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(savedInstanceState != null){
            loadSavedInstanceState(savedInstanceState);
        }

        mAdapterToFragmentCallback = new AssignmentAdapterToFragmentCallback() {
            @Override
            public void onRowClicked(Assignment assignment) {
                if(mSectionsSpinner.getSelectedItem() != null){
                    startActivityForResult(DocumentActivity.createIntent(getContext(), getCanvasContext(), assignment, ((Section) mSectionsSpinner.getSelectedItem()).getId()), Const.UPDATED_ASSIGNMENT_FLAGS);
                }else{
                    startActivityForResult(DocumentActivity.createIntent(getContext(), getCanvasContext(), assignment, getAllSectionsItem().getId()), Const.UPDATED_ASSIGNMENT_FLAGS);
                }
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }

            @Override
            public Section getCurrentSection() {
                if(mSectionsSpinner == null || mSectionsSpinner.getSelectedItem() == null){
                    return getAllSectionsItem();
                }
                return (Section) mSectionsSpinner.getSelectedItem();
            }

            @Override
            public void onSectionsLoaded(List<Section> sections) {
                if(!isAdded()){return;}
                mSections = sections;
                if(mSectionsSpinner != null){
                    createSectionsSpinner();
                }
            }
        };

        if(mRecyclerAdapter == null){
            mRecyclerAdapter = new AssignmentGroupListRecyclerAdapter(getContext(), getCanvasContext(), mAdapterToFragmentCallback);
        }

        if(getActivity() instanceof HomeActivity){
            ((HomeActivity)getActivity()).hideDrawer();
        }

        handleActionBarColor();
        setTitle(getCanvasContext().getName());
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);

        if(isRouting){
            isRouting = false;
            startActivityForResult(DocumentActivity.createIntent(getContext(), getCanvasContext(), mRoutedAssignment, getAllSectionsItem().getId()), Const.UPDATED_ASSIGNMENT_FLAGS);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        return mRootView;
    }

    public void setRefreshing(boolean isRefreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(isRefreshing);
        }
    }

    public void handleActionBarColor(){
        setActionBarColor(CanvasContextColor.getCachedColor(getContext(), getCanvasContext()));
    }

    @Override
    public int getRootLayout() {
        return R.layout.swipe_refresh_expandable_layout;
    }

    /**
     * If changes are made to the assignment by the user inside the EditAssignmentFragment, we update that assignment to reflect new changes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Const.UPDATED_ASSIGNMENT_FLAGS && data != null && mAssignments != null){
            HashMap<Long, Assignment> mUpdatedAssignments = (HashMap) data.getSerializableExtra(Const.UPDATED_ASSIGNMENTS);
            final Set<Long> keys = mUpdatedAssignments.keySet();
            for(Long assignmentId : keys){
                Assignment assignment = mUpdatedAssignments.get(assignmentId);
                mRecyclerAdapter.addOrUpdateItem(mRecyclerAdapter.getGroup(assignment.getAssignmentGroupId()), assignment);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void loadSavedInstanceState(Bundle savedInstanceState){
        this.mAssignments = (ArrayList) savedInstanceState.getSerializable(Const.ASSIGNMENTS);

        // get all AssignmentGroups from parcelable array
        Parcelable[] assignmentGroupParcels = savedInstanceState.getParcelableArray(Const.assignmentGroups);
        if(assignmentGroupParcels != null){
            this.mAssignmentGroups = new AssignmentGroup[assignmentGroupParcels.length];
            // Casting the parcelable array into an array of AssignmentGroup can cause crashes sometimes, so instead we iterate over the parcelable
            // array and cast each item into a AssignmentGroup to rebuild our arrray
            for(int i = 0; i < assignmentGroupParcels.length; i++){
                this.mAssignmentGroups[i] = (AssignmentGroup) assignmentGroupParcels[i];
            }
            for(AssignmentGroup assignmentGroup: this.mAssignmentGroups) {
                mAssignmentGroupsHash.put(assignmentGroup.getId(), assignmentGroup);
            }
        }

        this.mSections = (ArrayList) savedInstanceState.getSerializable(Const.sections);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Const.ASSIGNMENTS, (ArrayList)mAssignments);
        outState.putParcelableArray(Const.assignmentGroups, mAssignmentGroups);
        outState.putSerializable(Const.sections, (ArrayList)mSections);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setupCallbacks() {}

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    private void addAllSectionsItem(){
        Section tempSection = getAllSectionsItem();
        if(mSections != null && !mSections.contains(tempSection)){
            mSections.add(0, tempSection);
        }
    }

    private Section getAllSectionsItem(){
        Section tempSection = new Section();
        tempSection.setId(Integer.MIN_VALUE);
        tempSection.setName(getResources().getString(R.string.allSections));
        return tempSection;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Sections Selector Spinner
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.assignments, menu);
        mSectionsSpinner = (Spinner) menu.findItem(R.id.coursesSpinner).getActionView().findViewById(R.id.sectionSpinner);
        mSectionsSpinner.setVisibility(View.VISIBLE);
        if(mSections != null){
           createSectionsSpinner();
        }
    }

    public void createSectionsSpinner(){
        addAllSectionsItem();
        SpinnerAdapter mSpinnerAdapter = new SpinnerAdapter(getContext(), R.layout.generic_spinner_item, mSections);
        mSectionsSpinner.setVisibility(View.VISIBLE);
        mSectionsSpinner.setAdapter(mSpinnerAdapter);
        mSectionsSpinner.setSelection(mCurrentSectionSelection);
        mSectionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentSectionSelection = position;
                mRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public class SpinnerAdapter extends ArrayAdapter<Section> {

        private List<Section> sections = new ArrayList<>();
        private LayoutInflater li;

        public SpinnerAdapter(Context context, int textViewResourceId,  List<Section> objects) {
            super(context, textViewResourceId, objects);
            this.sections = objects;
            li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void add(Section section) {
            sections.add(section);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent, true);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent,false);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent, boolean isDropDown) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = li.inflate(R.layout.generic_spinner_item, parent, false);
                viewHolder.rootView = convertView.findViewById(R.id.rootView);
                viewHolder.sectionTitle = (TextView) convertView.findViewById(R.id.itemTitle);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if(isDropDown){
                viewHolder.rootView.setBackgroundColor(CanvasContextColor.getCachedColor(getContext(), getCanvasContext().getContextId()));
            }

            viewHolder.sectionTitle.setText(sections.get(position).getName());
            return convertView;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {}

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {}

        @Override
        public int getCount() {
            return sections.size();
        }

        @Override
        public Section getItem(int position) {
            return sections.get(position);
        }

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
            TextView sectionTitle;
            View rootView;
        }
    }

    @Override
    public void getBundleData(Bundle bundle) {
        super.getBundleData(bundle);
        mRoutedAssignment = bundle.getParcelable(Const.assignmentId);
        if(mRoutedAssignment != null){
            isRouting = true;
        }
    }
}
