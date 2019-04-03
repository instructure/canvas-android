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

package com.instructure.student.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.student.R;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.UserAPI;
import com.instructure.canvasapi2.managers.InboxManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.pandautils.utils.ViewStyler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class AskInstructorDialogStyled extends DialogFragment {

    public static final String TAG = "askInstructorDialog";

    //dialogs
    private ProgressDialog progressDialog;

    private ArrayList<Course> courseList;
    private HashSet<User> emailList;
    private Course course;

    //views
    private Spinner courseSpinner;
    private EditText message;

    //adapter
    private CourseSpinnerAdapter courseAdapter;

    //Inflater
    private LayoutInflater inflater;

    private boolean hasLoadedFirstPage = false;
    private String nextURL;
    private boolean foundTeachers = false;

    //Callbacks
    StatusCallback<List<User>> getPeopleCallback;
    StatusCallback<List<Course>> getFavoriteCoursesCallback;
    StatusCallback<List<Conversation>> sendMessageCanvasCallback;

    private boolean canClickSend = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity())
                                  .setTitle(getActivity().getString(R.string.instructor_question))
                                  .setPositiveButton(getActivity().getString(R.string.send), new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                          if(canClickSend) {
                                              if (message == null || message.getText().toString().trim().equals("")) {
                                                  Toast.makeText(getActivity(), getString(R.string.emptyMessage), Toast.LENGTH_SHORT).show();
                                              } else {
                                                  progressDialog = ProgressDialog.show(getActivity(), "", getActivity().getString(R.string.sending));
                                                  loadTeacherData();
                                              }
                                          }
                                      }
                                  });


        @SuppressLint("InflateParams") // Suppress lint warning about null parent when inflating layout
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.ask_instructor, null);
        courseSpinner = view.findViewById(R.id.courseSpinner);

        ArrayList<Course> loadingIndicator = new ArrayList<Course>();
        Course loading = new Course();
        loading.setName(getActivity().getString(R.string.loading));
        loadingIndicator.add(loading);
        courseAdapter = new CourseSpinnerAdapter (getActivity(), android.R.layout.simple_spinner_dropdown_item, loadingIndicator);
        //we haven't set an onItemSelectedListener, so selecting the item shouldn't do anything
        courseSpinner.setAdapter(courseAdapter);

        message = view.findViewById(R.id.message);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface worthless) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.getBrandColor());
                ViewStyler.themeEditText(getContext(), (AppCompatEditText) message, ThemePrefs.getBrandColor());

            }
        });

        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // anything that relies on intent data belongs here
        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        emailList = new HashSet<>();
        courseList = new ArrayList<>();

        //Set up the callbacks.
        setUpCallbacks();
        CourseManager.getAllFavoriteCourses(true, getFavoriteCoursesCallback);
    }

    private void loadTeacherData(){
        UserAPI.EnrollmentType enrollmentType;
        if(!foundTeachers){
            enrollmentType = UserAPI.EnrollmentType.TEACHER;
        }
        else{
            enrollmentType = UserAPI.EnrollmentType.TA;
        }

        if(!hasLoadedFirstPage){
            UserManager.getFirstPagePeopleList(course, enrollmentType, true, getPeopleCallback);
        }
        else{
            UserManager.getNextPagePeopleList(true, nextURL, getPeopleCallback);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////

    public class CourseSpinnerAdapter extends ArrayAdapter<Course> {

        private ArrayList<Course> courses;

        CourseSpinnerAdapter(Context context, int textViewResourceId,
                             ArrayList<Course> courses) {
            super(context, textViewResourceId, courses);
            this.courses = courses;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    @NonNull ViewGroup parent) {
            return getCustomView(position, convertView);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView);
        }

        View getCustomView(int position, View convertView) {
            CourseViewHolder holder;

            if ( convertView == null ) {
				/* There is no view at this position, we create a new one.
		           In this case by inflating an xml layout */
                convertView = inflater.inflate(R.layout.spinner_row_courses, null);

                holder = new CourseViewHolder();
                holder.courseName = convertView.findViewById(R.id.courseName);

                convertView.setTag (holder);
            } else {
				/* We recycle a View that already exists */
                holder = (CourseViewHolder) convertView.getTag ();
            }

            if(courses.get(position) != null) {
                holder.courseName.setText(courses.get(position).getName());
            }

            return convertView;
        }
    }
    private static class CourseViewHolder {
        TextView courseName;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////
    public void setUpCallbacks(){
        getFavoriteCoursesCallback = new StatusCallback<List<Course>>() {
            @Override
            public void onResponse(@NonNull Response<List<Course>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                //only add courses in which the user isn't a teacher
                if(!isAdded()){return;}
                for(Course course : response.body()) {
                    if(!course.isTeacher())  {
                        //for duplicate prevention
                        if(!courseList.contains(course)){
                            courseList.add(course);
                        }
                    }
                }

                courseAdapter = new CourseSpinnerAdapter (getActivity(), android.R.layout.simple_spinner_dropdown_item, courseList);
                courseSpinner.setAdapter(courseAdapter);
                courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        course = (Course)parent.getAdapter().getItem(position);
                        //we now have a valid course, let them send a message
                        canClickSend = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        };

        getPeopleCallback = new StatusCallback<List<User>>() {
            @Override
            public void onResponse(@NonNull Response<List<User>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                nextURL = linkHeaders.getNextUrl();
                hasLoadedFirstPage = true;
                List<User> users = response.body();
                if(users != null) {
                    emailList.addAll(users);
                }

                //only get the next group if we haven't found all the groups and we've found all the members of the current group
                if((nextURL == null) && (!foundTeachers)) {

                    //we got all of this type's enrollment because nextURL is null (so no next page)
                    //now get the next enrollment
                    hasLoadedFirstPage = false;
                    nextURL = null;

                    foundTeachers = true;

                    //get the next group.
                    loadTeacherData();
                    return;
                }


                //not done yet, still have a next page of teachers or tas to get
                if(nextURL != null) {
                    //get all the people in the paginated list, force it
                    loadTeacherData();
                }
                else {
                    //now we should be done, send the message
                    String messageText = message.getText().toString();

                    //Get the ids as an arraylist.
                    ArrayList<String> ids = new ArrayList<>();
                    for(User user : emailList)
                    {
                        ids.add(Long.toString(user.getId()));
                    }

                    InboxManager.createConversation(ids, messageText, "", course.getContextId(), new long[0], true, sendMessageCanvasCallback);
                }
            }

            @Override
            public void onFail(@Nullable Call<List<User>> call, @NonNull Throwable error, @Nullable Response<?> response) {
                hasLoadedFirstPage = false;
                progressDialog.dismiss();
            }
        };

        sendMessageCanvasCallback = new StatusCallback<List<Conversation>>() {

            @Override
            public void onResponse(@NonNull Response<List<Conversation>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                //close progress dialog
                progressDialog.dismiss();
                //close this dialog
                dismiss();
            }

            @Override
            public void onFail(@Nullable Call<List<Conversation>> call, @NonNull Throwable error, @Nullable Response<?> response) {
                // Croutons are shown in the background, which makes them hard to see. Use a dialog instead
                FatalErrorDialogStyled fatalErrorDialog = FatalErrorDialogStyled.Companion.newInstance(R.string.error, R.string.errorSendingMessage, true);
                if(getActivity() == null) {
                    return;
                }

                fatalErrorDialog.show(getActivity().getSupportFragmentManager(), FatalErrorDialogStyled.TAG);
            }
        };
    }
}
