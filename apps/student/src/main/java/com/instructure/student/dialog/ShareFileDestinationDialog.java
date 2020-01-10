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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.AssignmentManager;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Pronouns;
import com.instructure.pandautils.dialogs.UploadFilesDialog;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.student.R;
import com.instructure.student.adapter.FileUploadAssignmentsAdapter;
import com.instructure.student.adapter.FileUploadCoursesAdapter;
import com.instructure.student.util.AnimationHelpers;
import com.instructure.student.util.UploadCheckboxManager;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import retrofit2.Response;

public class ShareFileDestinationDialog extends DialogFragment implements UploadCheckboxManager.OnOptionCheckedListener{

    public static final String TAG = "uploadFileSourceFragment";

    // Dismiss interface
    public interface DialogCloseListener{
        void onDismiss(DialogInterface dialog);
        void onCancel(DialogInterface dialog);
        void onNext(Bundle bundle);
    }

    private View rootView;

    // Custom dialog header
    private ImageView avatar;
    private TextView userName;
    private TextView title;

    private UploadCheckboxManager checkboxManager;
    private View contentView;

    private Spinner studentCoursesSpinner;
    private Spinner assignmentSpinner;

    private Uri uri;
    private ArrayList<Course> courses = new ArrayList<>();

    private User user;
    private StatusCallback<List<Assignment>> canvasCallbackAssignments;
    private Assignment selectedAssignment;

    private FileUploadCoursesAdapter studentEnrollmentsAdapter;

    public static ShareFileDestinationDialog newInstance(Bundle bundle) {
        ShareFileDestinationDialog uploadFileSourceFragment = new ShareFileDestinationDialog();
        uploadFileSourceFragment.setArguments(bundle);
        return uploadFileSourceFragment;
    }

    @Override public void onStart() {
        super.onStart();
        // Don't dim the background when the dialog is created.
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupCallbacks();

        if(getDialog() != null){
            getDialog().getWindow().getAttributes().windowAnimations = R.style.FileDestinationDialogAnimation;
            getDialog().getWindow().setWindowAnimations(R.style.FileDestinationDialogAnimation);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loadBundleData();

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(initViews())
                .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        validateAndShowNext();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismissAllowingStateLoss();
                    }
                })
                .setCancelable(true)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if(alertDialog != null) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.getButtonColor());
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.getButtonColor());
                }
            }
        });

        return alertDialog;
    }

    public void onDismiss(DialogInterface dialog){
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener){
            ((DialogCloseListener)activity).onCancel(dialog);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener){
            ((DialogCloseListener)activity).onCancel(dialog);
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().dismiss();
        super.onDestroyView();
    }

    private void validateAndShowNext(){
        // validate selections
        String errorString = validateForm();
        if(errorString.length() > 0){
            Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
        }
        else if(getActivity() instanceof DialogCloseListener){
            ((DialogCloseListener) getActivity()).onNext(getUploadBundle());
            dismiss();
        }
    }

    /**
     * Checks if user has filled out form completely.
     * @return Returns an error string if the form is not valid.
     */
    private String validateForm(){
        //make sure the user has selected a course and an assignment
        UploadFilesDialog.FileUploadType uploadType = checkboxManager.getSelectedType();

        // Make sure an assignment & course was selected if FileUploadType.Assignment
        if (uploadType == UploadFilesDialog.FileUploadType.ASSIGNMENT){
            if(studentCoursesSpinner.getSelectedItem() == null){
                return  getString(R.string.noCourseSelected);
            }
            else if(assignmentSpinner.getSelectedItem() == null || ((Assignment)assignmentSpinner.getSelectedItem()).getId() == Long.MIN_VALUE){
                return getString(R.string.noAssignmentSelected);
            }
        }

        return "";
    }

    @NonNull
    private Bundle getUploadBundle(){
        Bundle bundle;
        switch (checkboxManager.getSelectedCheckBox().getId()){
            case R.id.myFilesCheckBox:
                bundle = UploadFilesDialog.createFilesBundle(uri, null);
                break;
            case R.id.assignmentCheckBox:
                bundle = UploadFilesDialog.createAssignmentBundle(uri, (Course)studentCoursesSpinner.getSelectedItem(), (Assignment)assignmentSpinner.getSelectedItem());
                break;
            default:
                bundle = UploadFilesDialog.createFilesBundle(uri, null);
                break;
        }
        return bundle;
    }

    private View initViews(){
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.upload_file_destination, null);

        contentView = rootView.findViewById(R.id.dialogContents);

        studentCoursesSpinner = rootView.findViewById(R.id.studentCourseSpinner);
        assignmentSpinner = rootView.findViewById(R.id.assignmentSpinner);

        // animated header views
        title = rootView.findViewById(R.id.dialogTitle);
        avatar = rootView.findViewById(R.id.avatar);
        userName = rootView.findViewById(R.id.userName);
        userName.setText(Pronouns.span(user.getName(), user.getPronouns()));

        initCheckBoxes(rootView);
        setRevealContentsListener();

        rootView.findViewById(R.id.assignmentContainer).setVisibility(View.VISIBLE);

        return rootView;
    }

    private void initCheckBoxes(View view){
        CheckedTextView assignmentCheckBox = view.findViewById(R.id.assignmentCheckBox);
        CheckedTextView myFilesCheckBox = view.findViewById(R.id.myFilesCheckBox);
        CardView selectionIndicator = view.findViewById(R.id.selectionIndicator);

        checkboxManager = new UploadCheckboxManager(this, selectionIndicator);
        checkboxManager.add(myFilesCheckBox);
        checkboxManager.add(assignmentCheckBox);
    }

    private void setAssignmentsSpinnerToLoading(){
        Assignment loading = new Assignment();
        ArrayList<Assignment> courseAssignments = new ArrayList<>();
        loading.setName(getString(R.string.loadingAssignments));
        loading.setId(Long.MIN_VALUE);
        courseAssignments.add(loading);
        assignmentSpinner.setAdapter(new FileUploadAssignmentsAdapter(getActivity(), courseAssignments));
    }

    private void setupCallbacks() {
        canvasCallbackAssignments = new StatusCallback<List<Assignment>>() {
            @Override
            public void onResponse(@NonNull Response<List<Assignment>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if (!isAdded()) {
                    return;
                }

                List<Assignment> assignments = response.body();

                if(assignments == null) return;

                if (assignments.size() > 0 && courseSelectionChanged(assignments.get(0).getCourseId())) {
                    return;
                }

                ArrayList<Assignment> courseAssignments = FileUploadAssignmentsAdapter.Companion.getOnlineUploadAssignmentsList(getContext(), assignments);
                // init student spinner
                final FileUploadAssignmentsAdapter adapter = new FileUploadAssignmentsAdapter(getActivity(), courseAssignments);
                assignmentSpinner.setAdapter(adapter);
                if (selectedAssignment != null) {
                    assignmentSpinner.setOnItemSelectedListener(null); // prevent listener from firing the when selection is placed
                    int position = adapter.getPosition(selectedAssignment);
                    if (position >= 0) {
                        assignmentSpinner.setSelection(position, false); // prevents the network callback from replacing the what the user selected while cache was being displayed
                    }
                }
                assignmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position < 0) {
                            return;
                        }

                        if (position < adapter.getCount()) {
                            selectedAssignment = adapter.getItem(position);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        };
    }

    private void setupCourseSpinners() {
        if(getActivity() == null || getActivity().isFinishing()) return;
        if(studentEnrollmentsAdapter == null){
            studentEnrollmentsAdapter = new FileUploadCoursesAdapter(getActivity(), getActivity().getLayoutInflater(), FileUploadCoursesAdapter.getFilteredCourseList(courses, FileUploadCoursesAdapter.Type.STUDENT));
            studentCoursesSpinner.setAdapter(studentEnrollmentsAdapter);
        }else{
            studentEnrollmentsAdapter.setCourses(FileUploadCoursesAdapter.getFilteredCourseList(courses, FileUploadCoursesAdapter.Type.STUDENT));
        }

        studentCoursesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //make the allowed extensions disappear
                Course course = (Course) parent.getAdapter().getItem(position);
                //if the user is a teacher, let them know and don't let them select an assignment
                if(course.getId() > 0) {
                    setAssignmentsSpinnerToLoading();
                    AssignmentManager.getAllAssignments(course.getId(), false, canvasCallbackAssignments);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    private boolean courseSelectionChanged(long newCourseId){
        // Api call for assignments returning after user changes course; ignore results
        return checkboxManager.getSelectedCheckBox().getId() == R.id.assignmentCheckBox && studentCoursesSpinner != null && newCourseId != ((Course) studentCoursesSpinner.getSelectedItem()).getId();
    }

    private void setRevealContentsListener(){
        final Animation avatarAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.ease_in_shrink);
        final Animation titleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.ease_in_bottom);

        avatar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(avatar, this);
                avatar.startAnimation(avatarAnimation);
                userName.startAnimation(titleAnimation);
                title.startAnimation(titleAnimation);
            }
        });

        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(contentView, this);

                final Animator revealAnimator = AnimationHelpers.createRevealAnimator(contentView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        contentView.setVisibility(View.VISIBLE);
                        revealAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                setupCourseSpinners();
                            }
                        });
                        revealAnimator.start();
                    }
                }, 600);
            }
        });
    }

    private void enableStudentSpinners(boolean isEnabled){
        assignmentSpinner.setEnabled(isEnabled);
        studentCoursesSpinner.setEnabled(isEnabled);
    }

    @Override
    public void onUserFilesSelected() {
        enableStudentSpinners(false);
    }

    @Override
    public void onCourseFilesSelected() {
        enableStudentSpinners(false);
    }

    @Override
    public void onAssignmentFilesSelected() {
        enableStudentSpinners(true);
    }

    private void loadBundleData(){
        Bundle bundle = getArguments();
        courses = bundle.getParcelableArrayList(Const.COURSES);
        user = ApiPrefs.getUser();
        uri = bundle.getParcelable(Const.URI);
    }

    public static Bundle createBundle(Uri uri, ArrayList<Course> courses){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.URI, uri);
        bundle.putParcelableArrayList(Const.COURSES, courses);
        return bundle;
    }
}
