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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.api.SectionAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentGroup;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.binders.AssignmentBinder;
import com.instructure.speedgrader.binders.EmptyBinder;
import com.instructure.speedgrader.binders.ExpandableHeaderBinder;
import com.instructure.speedgrader.interfaces.AssignmentAdapterToFragmentCallback;
import com.instructure.speedgrader.viewholders.AssignmentViewHolder;import com.instructure.speedgrader.viewholders.EmptyViewHolder;
import com.instructure.speedgrader.viewholders.ExpandableViewHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit.client.Response;

public class AssignmentGroupListRecyclerAdapter extends ExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder> {

    private CanvasContext mCanvasContext;
    private AssignmentAdapterToFragmentCallback mAdapterToFragmentCallback;

    // callbacks
    private CanvasCallback<AssignmentGroup[]> mCanvasCallbackGroups;
    private CanvasCallback<Assignment[]> mCanvasCallbackAssignment;
    private CanvasCallback<Section[]> mCanvasCallbackSections;
    private boolean isAssignmentsLoaded;
    private boolean isGroupsLoaded;

    // model
    private HashMap<Long, AssignmentGroup> mAssignmentGroupsHash = new HashMap<>();
    private ArrayList<AssignmentGroup> mAssignmentGroups = new ArrayList<>();
    private ArrayList<Assignment> mAssignments = new ArrayList<>();
    private ArrayList<Section> mSections;

    /* For testing purposes only */
    protected AssignmentGroupListRecyclerAdapter(Context context){
        super(context, AssignmentGroup.class, Assignment.class);
    }

    public AssignmentGroupListRecyclerAdapter(Context context, CanvasContext canvasContext, AssignmentAdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, AssignmentGroup.class, Assignment.class);
        mCanvasContext = canvasContext;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        setExpandedByDefault(true);
        setDisplayEmptyCell(true);
        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else if (viewType == Types.TYPE_EMPTY_CELL) {
            return new EmptyViewHolder(v);
        } else {
            return new AssignmentViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        }  else if (viewType == Types.TYPE_EMPTY_CELL) {
            return EmptyViewHolder.holderResId();
        } else {
            return AssignmentViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {}

    private void populateAdapter(List<Assignment> assignments, List<AssignmentGroup> assignmentGroups) {
        // wait for all api calls to return
        if(assignmentGroups.size() == 0 || assignments.size() == 0) {
            return;
        }

        for (Assignment assignment : assignments) {
            //put the mAssignments into assignment groups
            AssignmentGroup assignmentGroup = mAssignmentGroupsHash.get(assignment.getAssignmentGroupId());
            if(assignmentGroup != null) {
                addOrUpdateItem(assignmentGroup, assignment);
            }
        }

        if(isAssignmentsLoaded && isGroupsLoaded){
            mAdapterToFragmentCallback.onRefreshFinished();
            setAllPagesLoaded(true);
        }
    }

    @Override
    public void setupCallbacks() {
        mCanvasCallbackGroups = new CanvasCallback<AssignmentGroup[]>(this) {
            @Override
            public void cache(AssignmentGroup[] assignmentGroups, LinkHeaders linkHeaders, Response response) {
                mAssignmentGroups = new ArrayList<>(Arrays.asList(assignmentGroups));
                for(AssignmentGroup assignmentGroup: assignmentGroups) {
                    mAssignmentGroupsHash.put(assignmentGroup.getId(), assignmentGroup);
                }
                populateAdapter(mAssignments, mAssignmentGroups);
            }

            @Override
            public void firstPage(AssignmentGroup[] assignmentGroups, LinkHeaders linkHeaders, Response response) {
                mAssignmentGroups = new ArrayList<>(Arrays.asList(assignmentGroups));
                for(AssignmentGroup assignmentGroup: assignmentGroups) {
                    mAssignmentGroupsHash.put(assignmentGroup.getId(), assignmentGroup);
                }
                isGroupsLoaded = true;
                populateAdapter(mAssignments, mAssignmentGroups);
            }
        };

        mCanvasCallbackAssignment = new CanvasCallback<Assignment[]>(this) {
            @Override
            public void cache(Assignment[] assignmentsArray, LinkHeaders linkHeaders, Response response) {
                for(Assignment assignment : assignmentsArray){
                    if(assignment.isPublished()){
                        mAssignments.add(assignment);
                    }
                }
                populateAdapter(mAssignments, mAssignmentGroups);
            }

            @Override
            public void firstPage(Assignment[] assignments, LinkHeaders linkHeaders, Response response) {
                for(Assignment assignment : assignments){
                    if(assignment.isPublished()){
                        mAssignments.add(assignment);
                    }
                }
                isAssignmentsLoaded = true;
                if(mAssignments.size() == 0){
                    setAllPagesLoaded(true);
                }else{
                    populateAdapter(mAssignments, mAssignmentGroups);
                }
            }
        };

        mCanvasCallbackSections = new CanvasCallback<Section[]>(this) {
            @Override
            public void cache(Section[] courseSections, LinkHeaders linkHeaders, Response response) {
                mSections = new ArrayList<>(Arrays.asList(courseSections));
                mAdapterToFragmentCallback.onSectionsLoaded(mSections);
            }

            @Override
            public void firstPage(Section[] sections, LinkHeaders linkHeaders, Response response) {
                mSections = new ArrayList<>(Arrays.asList(sections));
                mAdapterToFragmentCallback.onSectionsLoaded(mSections);
            }
        };
    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, AssignmentGroup assignmentGroup, Assignment assignment) {
        AssignmentBinder.bind(getContext(), (AssignmentViewHolder) holder, assignment, mAdapterToFragmentCallback);
    }

    @Override
    public void onBindEmptyHolder(RecyclerView.ViewHolder holder, AssignmentGroup assignmentGroup) {
        EmptyBinder.bind((EmptyViewHolder) holder, getContext().getResources().getString(R.string.noAssignmentsInGroup));
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, AssignmentGroup assignmentGroup, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, assignmentGroup, assignmentGroup.getName(), isExpanded, getViewHolderHeaderClicked());
    }

    @Override
    protected void resetBooleans() {
        super.resetBooleans();
        isAssignmentsLoaded = false;
        isGroupsLoaded = false;
    }

    @Override
    public void loadData() {
        AssignmentAPI.getAssignmentGroupsList(mCanvasContext.getId(), mCanvasCallbackGroups);
        AssignmentAPI.getAllAssignmentsExhaustive(mCanvasContext.getId(), mCanvasCallbackAssignment);
        SectionAPI.getCourseSectionsWithStudents((Course) mCanvasContext, mCanvasCallbackSections);
    }

    // region Expandable callbacks
    @Override
    public GroupSortedList.GroupComparatorCallback<AssignmentGroup> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<AssignmentGroup>() {
            @Override
            public int compare(AssignmentGroup o1, AssignmentGroup o2) {
                return o1.getPosition() - o2.getPosition();
            }

            @Override
            public boolean areContentsTheSame(AssignmentGroup oldGroup, AssignmentGroup newGroup) {
                return oldGroup.getName().equals(newGroup.getName());
            }

            @Override
            public boolean areItemsTheSame(AssignmentGroup group1, AssignmentGroup group2) {
                return group1.getId() == group2.getId();
            }

            @Override
            public int getGroupType(AssignmentGroup group) {
                return Types.TYPE_HEADER;
            }

            @Override
            public long getUniqueGroupId(AssignmentGroup group) {
                return group.getId();
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment>() {
            @Override
            public int compare(AssignmentGroup group, Assignment o1, Assignment o2) {
                return o1.getPosition() - o2.getPosition();
            }

            @Override
            public boolean areContentsTheSame(Assignment oldItem, Assignment newItem) {
                boolean isSameName = oldItem.getName().equals(newItem.getName());
                if (oldItem.getDueDate() != null && newItem.getDueDate() != null) {
                    return isSameName && oldItem.getDueDate().equals(newItem.getDueDate());
                } else if (oldItem.getDueDate() == null && newItem.getDueDate() != null) {
                    return false;
                } else if (oldItem.getDueDate() != null && newItem.getDueDate() == null) {
                    return false;
                }
                return isSameName;
            }

            @Override
            public boolean areItemsTheSame(Assignment item1, Assignment item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public int getChildType(AssignmentGroup group, Assignment item) {
                return Types.TYPE_ITEM;
            }

            @Override
            public long getUniqueItemId(Assignment item) {
                return item.getId();
            }
        };
    }
    // endregion
}
