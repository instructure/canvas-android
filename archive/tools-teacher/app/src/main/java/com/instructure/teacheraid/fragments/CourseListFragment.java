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
 */

package com.instructure.teacheraid.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.SectionAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.rowfactories.CourseRowFactory;
import com.instructure.teacheraid.util.Const;
import com.instructure.teacheraid.util.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import retrofit.client.Response;

public class CourseListFragment extends PaginatedExpandableListFragment<String, CanvasContext> {

    //callback
    private CanvasCallback<Course[]> favoriteCoursesCallback;
    private CanvasCallback<Section[]> sectionCallback;

    private int nextFragment;

    private String nextURL;

    private Map<Long, CanvasContext> courseGroupMap;
    private Map<Long, ArrayList<Section>> courseSectionMap = new HashMap<Long, ArrayList<Section>>();


    public static final String TAG = "CourseListFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        if(getArguments() != null && getArguments().containsKey(Const.NEXT_FRAGMENT)) {
            nextFragment = getArguments().getInt(Const.NEXT_FRAGMENT);
        }

        getParentActivity().setActionBarTitle(getString(R.string.pickCourse));

        courseGroupMap = new LinkedHashMap<>();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    ///////////////////////////////////////////////////////////////////////////
    // View Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int getRootLayoutCode() {
        return R.layout.courses;
    }

    @Override
    protected boolean areGroupsCollapsible() {
        return false;
    }

    @Override
    public View getRowViewForItem(CanvasContext item, View convertView, int groupPosition, int childPosition, boolean isLastRowInGroup, boolean isLastRow) {
        return CourseRowFactory.buildRowView(getActivity(), getLayoutInflater(), item, convertView);
    }

    @Override
    public View getGroupViewForItem(String groupItem, View convertView, int groupPosition, boolean isExpanded) {
        String groupName = groupItem;

        return CourseRowFactory.buildGroupView(getLayoutInflater(), groupName, convertView);
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public boolean onRowClick(CanvasContext item) {
        Bundle bundle = new Bundle();
        Section section = (Section)item;
        section.getCourse_id();
        CanvasContext course = courseGroupMap.get(section.getCourse_id());
        bundle.putParcelable(Const.SECTION, item);
        bundle.putParcelable(Const.COURSE, course);
        bundle.putParcelableArrayList(Const.SECTION_PEOPLE, (ArrayList<User>) ((Section) item).getStudents());

        if(nextFragment == Const.HOT_SEAT) {
            StudentChooserFragment studentChooserFragment = new StudentChooserFragment();
            studentChooserFragment.setArguments(bundle);
            getParentActivity().swapFragment(studentChooserFragment, StudentChooserFragment.TAG);
        } else if(nextFragment == Const.GUESS_WHO) {
            GuessWhoFragment guessWhoFragment = new GuessWhoFragment();
            guessWhoFragment.setArguments(bundle);
            getParentActivity().swapFragment(guessWhoFragment, GuessWhoFragment.TAG);
        } else if(nextFragment == Const.STUDENT_NOTES) {
            StudentNotesFragment studentNotesFragment = new StudentNotesFragment();
            studentNotesFragment.setArguments(bundle);
            getParentActivity().swapFragment(studentNotesFragment, StudentNotesFragment.TAG);
        }
        return true;
    }

    @Override
    public int getDividerHeight() {
        return (int) ViewUtils.convertDipsToPixels(1, getContext());
    }

    @Override
    public int getDividerColor() {
        return super.getDividerColor();
    }

    @Override
    public void setupCallbacks() {
        favoriteCoursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void cache(Course[] courses, LinkHeaders linkHeaders, Response response) {

                //keep track of the courses that the user is a teacher in
                for (Course course : courses) {
                    if(course.isTeacher() || course.isTA()) {
                        courseGroupMap.put(course.getId(), course);
                        //don't want to make an api call if we already have
                        if (!courseSectionMap.containsKey(course.getId())) {
                            SectionAPI.getCourseSectionsWithStudents(course, sectionCallback);
                        } else {
                            ArrayList<Section> sections =  courseSectionMap.get(course.getId());
                            for(Section section : sections) {
                                addItem(course.getName(), section);
                            }
                            expandAllGroups();
                        }
                    }
                }
                amendCourseGroupList();
            }

            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                //We use get resources, so check for null.
                if (getActivity() == null) return;

                cache(courses, linkHeaders, response);

                nextURL = linkHeaders.nextURL;
            }
        };

        sectionCallback = new CanvasCallback<Section[]>(this) {
            @Override
            public void cache(Section[] sections, LinkHeaders linkHeaders, Response response) {

                //add the sections to the course/section map
                if (sections != null && sections.length > 0) {

                    long courseId = sections[0].getCourse_id();
                    Course c = (Course) courseGroupMap.get(courseId);

                    Section allSections = new Section();
                    allSections.setId(Long.MIN_VALUE);
                    allSections.setName(getString(R.string.allSections));
                    allSections.setCourseId(courseId);

                    //use a hash set so we don't allow duplicate students
                    HashSet<User> allStudents = new HashSet<>();
                    for(Section section : sections) {
                        allStudents.addAll(section.getStudents());
                    }
                    allSections.setStudents(new ArrayList<>(allStudents));
                    //if the course section map already has the course in it, we'll want to add to the section list
                    if (courseSectionMap.containsKey(courseId)) {
                        ArrayList<Section> currentSections = courseSectionMap.get(courseId);
                        //we want "all sections" to appear at the top, so we'll sort by id (all sections has a small id)
                        TreeSet<Section> currentSectionStudents = new TreeSet<>(new Comparator<Section>() {
                            @Override
                            public int compare(Section section1, Section section2) {
                                if(section1.getId() == Long.MIN_VALUE) {
                                    return -1;
                                } else {
                                    return section1.getName().compareToIgnoreCase(section2.getName());
                                }
                            }
                        });
                        currentSectionStudents.addAll(currentSections);
                        currentSectionStudents.addAll(Arrays.asList(sections));

                        //add the all sections first if there is more than one section.
                        if(currentSections.size() > 1) {
                            currentSectionStudents.add(allSections);
                        }
                        courseSectionMap.put(courseId, new ArrayList<>(currentSectionStudents));
                    } else {
                        courseSectionMap.put(courseId,  new ArrayList<>(Arrays.asList(sections)));
                    }

                    //add the items to the expandable list
                    if (c.isTeacher() || c.isTA()) {
                        for (Section section : courseSectionMap.get(courseId)) {
                            addItem(c.getName(), section);
                        }
                    }
                }
                expandAllGroups();
            }

            @Override
            public void firstPage(Section[] sections, LinkHeaders linkHeaders, Response response) {

                cache(sections, linkHeaders, response);
                if (linkHeaders.nextURL != null) {
                    SectionAPI.getNextPageSectionsList(linkHeaders.nextURL, sectionCallback);
                }

            }
        };

    }

    @Override
    public boolean areItemsSorted() {
        //"all sections" at the top and then sort by name
        return false;
    }


    @Override
    public void loadFirstPage() {
        CourseAPI.getFirstPageFavoriteCourses(favoriteCoursesCallback);
    }

    @Override
    public void loadNextPage(String nextURL) {
        CourseAPI.getNextPageCourses(favoriteCoursesCallback, nextURL);
    }

    @Override
    public String getNextURL() {
        return nextURL;
    }

    @Override
    public void setNextURLNull() {
        nextURL = null;
    }


    @Override
    public void resetData() {
        courseGroupMap.clear();
    }

    @Override
    public void reloadData() {
        super.reloadData();
        clearItems();
        clear();
        loadFirstPage();
    }

    @Override
    public boolean areGroupsSorted() {
        return true;
    }

    @Override
    public boolean areGroupsReverseSorted() {
        return false;
    }

    private void amendCourseGroupList() {

        expandAllGroups();
        finishLoading();
        notifyDataSetChanged();
    }

    public static Bundle createBundle(int nextFragment) {
        Bundle bundle = new Bundle();
        bundle.putInt(Const.NEXT_FRAGMENT, nextFragment);
        return bundle;
    }
}
