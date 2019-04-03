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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi.api.CustomGradebookColumnAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.ColumnDatum;
import com.instructure.canvasapi.model.CustomColumn;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.view.PandaLoading;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.util.Const;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class StudentNoteDetailsFragment extends ParentFragment {

    private User user;
    private CanvasContext course;
    private TextView userName;
    private CircleImageView avatar;
    private EditText notes;
    private Button submit;
    private PandaLoading pandaLoading;

    private long teachersNotesColumnId;
    private String existingNotes;

    private CanvasCallback<ColumnDatum[]> columnDatumCanvasCallback;
    private CanvasCallback<ColumnDatum> updateColumnCanvasCallback;
    private CanvasCallback<CustomColumn[]> getColumnCanvasCallback;

    public static final String TAG = "StudentNoteDetailsFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.student_note_details, container, false);

        setupViews(rootView);
        setOnClickListeners();
        setupCallbacks();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments() != null) {
            user = getArguments().getParcelable(Const.USER);
            course = getArguments().getParcelable(Const.COURSE);
            userName.setText(user.getName());
            Picasso.with(getActivity()).load(user.getAvatarURL()).into(avatar);
            pandaLoading.setVisibility(View.VISIBLE);
            CustomGradebookColumnAPI.getGradebookColumns(course.getId(), getColumnCanvasCallback);
        }

        getParentActivity().setActionBarTitle(getString(R.string.studentDetails));
    }

    private void setupViews(View rootView) {
        userName = (TextView) rootView.findViewById(R.id.username);
        avatar = (CircleImageView) rootView.findViewById(R.id.avatar);
        notes = (EditText) rootView.findViewById(R.id.notes);
        submit = (Button) rootView.findViewById(R.id.submit);
        pandaLoading = (PandaLoading) rootView.findViewById(R.id.pandaLoading);
        notes.addTextChangedListener(notesWatcher);
    }

    private void setOnClickListeners() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noteText = notes.getText().toString();
                CustomGradebookColumnAPI.updateColumnData(course.getId(), teachersNotesColumnId, user.getId(), noteText, updateColumnCanvasCallback);
            }
        });
    }

    private void setupCallbacks() {
        columnDatumCanvasCallback = new CanvasCallback<ColumnDatum[]>(this) {
            @Override
            public void cache(ColumnDatum[] columnData) {

            }

            @Override
            public void firstPage(ColumnDatum[] columnData, LinkHeaders linkHeaders, Response response) {
                //loop through all the data to find the correct student
                String studentNotes = null;
                for (ColumnDatum datum : columnData) {
                    if (datum.getUser_id() == user.getId()) {
                        studentNotes = datum.getContent();
                    }
                }

                pandaLoading.setVisibility(View.GONE);

                if(TextUtils.isEmpty(studentNotes)) {
                    notes.setHint(getString(R.string.studentDetailsHint));
                } else if(existingNotes == null){
                    notes.setText(studentNotes);
                }
            }
        };

        updateColumnCanvasCallback = new CanvasCallback<ColumnDatum>(this) {
            @Override
            public void cache(ColumnDatum columnDatum, LinkHeaders linkHeaders, Response response) {

            }

            @Override
            public void firstPage(ColumnDatum columnDatum, LinkHeaders linkHeaders, Response response) {
                Toast.makeText(getActivity(), getString(R.string.successfullyUpdatedNotes) + " " + user.getName(), Toast.LENGTH_SHORT).show();
                getParentActivity().onBackPressed();
            }
        };

        getColumnCanvasCallback = new CanvasCallback<CustomColumn[]>(this) {
            @Override
            public void cache(CustomColumn[] customColumns, LinkHeaders linkHeaders, Response response) {

            }

            @Override
            public void firstPage(CustomColumn[] customColumns, LinkHeaders linkHeaders, Response response) {

                //find the teacher's note column
                for(CustomColumn column : customColumns) {
                    if(column.isTeacher_notes()) {
                        //we'll use this id later to update the column
                        teachersNotesColumnId = column.getId();
                        break;
                    }
                }

                if(teachersNotesColumnId != 0) {
                    CustomGradebookColumnAPI.getColumnData(course.getId(), teachersNotesColumnId, columnDatumCanvasCallback);
                } else {
                    //we need to create the notes column for the teacher
                    CustomGradebookColumnAPI.createGradebookColumns(course.getId(), getString(R.string.notesColumnName), 1, false, true, new CanvasCallback<CustomColumn>(StudentNoteDetailsFragment.this) {
                        @Override
                        public void cache(CustomColumn customColumn) {

                        }

                        @Override
                        public void firstPage(CustomColumn customColumn, LinkHeaders linkHeaders, Response response) {
                            teachersNotesColumnId = customColumn.getId();
                            CustomGradebookColumnAPI.getColumnData(course.getId(), teachersNotesColumnId, columnDatumCanvasCallback);
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                super.failure(retrofitError);
                pandaLoading.setVisibility(View.GONE);
            }
        };
    }

    private TextWatcher notesWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            existingNotes = s.toString();
        }
    };
}
