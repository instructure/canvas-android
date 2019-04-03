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
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.User;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.adapter.StudentGridAdapter;
import com.instructure.teacheraid.util.Const;

import java.util.ArrayList;

public class StudentNotesFragment extends ParentFragment {

    private GridView gridView;
    private StudentGridAdapter adapter;
    private TextView emptyView;

    private ArrayList<User> sectionPeople;
    private CanvasContext course;

    public static final String TAG = "StudentNotesFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.student_notes_grid, container, false);

        gridView = (GridView) rootView.findViewById(R.id.gridView);
        emptyView = (TextView) rootView.findViewById(R.id.emptyView);

        setClickListeners();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments() != null) {
            course = getArguments().getParcelable(Const.COURSE);
            sectionPeople = getArguments().getParcelableArrayList(Const.SECTION_PEOPLE);
            adapter = new StudentGridAdapter(getActivity(),sectionPeople);
            gridView.setAdapter(adapter);
        }

        if(adapter.getCount() == 0) {
            gridView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }

        getParentActivity().setActionBarTitle(getString(R.string.studentNotes));
    }

    private void setClickListeners() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StudentNoteDetailsFragment studentNoteDetailsFragment = new StudentNoteDetailsFragment();
                studentNoteDetailsFragment.setArguments(createBundle(course, adapter.getItem(position)));
                getParentActivity().swapFragment(studentNoteDetailsFragment, StudentNoteDetailsFragment.TAG);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }
}
