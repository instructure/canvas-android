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

package com.instructure.speedgrader.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentGroup;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.activities.DocumentActivity;
import com.instructure.speedgrader.fragments.GradingDrawerFragment;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.DateTextView;
import com.instructure.speedgrader.views.HelveticaTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditAssignmentDialog extends DialogFragment {

    public static final String TAG = "editAssignmentDialog";

    //VIEWS
    private EditText pointsPossibleET;
    private Spinner assignmentGroupSpinner;
    private Spinner gradingTypeSpinner;
    private Spinner turnInTypeSpinner;
    private LinearLayout turnInTypeWrapper;
    private LinearLayout onlineSubmissionTypeLayout;
    private LinearLayout allOnlineSubmissionTypes;
    private EditText assignmentTitleET;
    private DateTextView dueDateDT;
    private CheckBox notifyUsers;
    private CheckBox useRubricForGrading;
    private CheckBox mutedCheckBox;
    private ArrayList<CheckBox> submissionTypesArrayList;
    private Assignment.TURN_IN_TYPE assignmentTurnInType;
    private HelveticaTextView onlineEntriesText;
    private Assignment assignment;
    private Course course;

    private AssignmentGroup[] assignmentGroups;

    private static final Assignment.TURN_IN_TYPE[] TURN_IN_TYPES_ALLOWED = { Assignment.TURN_IN_TYPE.NONE,Assignment.TURN_IN_TYPE.ONLINE, Assignment.TURN_IN_TYPE.ON_PAPER};

    private CanvasCallback<AssignmentGroup[]> assignmentGroupCanvasCallback;
    private CanvasCallback<Assignment> assignmentCanvasCallback;

    //Dialog that holds this fragment. Null on phone.
    private MaterialDialog dialog;
    private ArrayList<Assignment.SUBMISSION_TYPE> checkedSubmissionTypes;

    // Cached items
    private HashMap<Assignment.SUBMISSION_TYPE, Boolean> checkedSubmissionTypesCache;
    private String assignmentTitleCache;
    private String pointsPossibleCache;
    private boolean isNotifyUsers;
    private boolean isMuted;
    private Date dueDateCache;

    private boolean isDisabledSave = false;

    // Saving progress
    View negativeButton;
    View positiveButton;
    View progress;
    View viewWrapper;
    //////////////////////////////////////////////////////////////
    // Lifecycle
    //////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState == null){
            AssignmentAPI.getAssignmentGroupsList(course.getId(), assignmentGroupCanvasCallback);
        }
        assignmentTurnInType = assignment.getTurnInType();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setupCallbacks();

        MaterialDialog.Builder builder =
                  new MaterialDialog.Builder(getActivity())
                                    .title(getString(R.string.editAssignment))
                                    .titleColor(getResources().getColor(R.color.sg_defaultPrimary))
                                    .positiveText(getString(R.string.save))
                                    .positiveColor(getResources().getColor(R.color.courseGreen))
                                    .negativeText(getString(R.string.cancel))
                                    .negativeColor(getResources().getColor(R.color.gray))
                                    .autoDismiss(false);

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                if(!isDisabledSave){
                    saveData();
                }
                super.onPositive(dialog);
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                dismiss();
                super.onNegative(dialog);
            }
        });

        //check to see if there are custom colors we need to set
        if(savedInstanceState == null){
            getBundleData(getArguments());
        }else{
            getBundleData(savedInstanceState);
        }

        // Create View
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_assignment_details_fragment, null);
        initViews(view);

        if(savedInstanceState != null){
            getBundleData(savedInstanceState);
            populateViewData();
        }

        builder.customView(view, true);
        dialog = builder.build();
        dialog.setCanceledOnTouchOutside(true);

        negativeButton = dialog.getActionButton(DialogAction.NEGATIVE);
        positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
        return dialog;
    }

    //////////////////////////////////////////////////////////////
    // Save Data
    //////////////////////////////////////////////////////////////
    private void setupCallbacks() {
        assignmentGroupCanvasCallback = new CanvasCallback<AssignmentGroup[]>(APIHelpers.statusDelegateWithContext(getActivity())) {
            @Override
            public void cache(AssignmentGroup[] assignmentGroups) {
                firstPage(assignmentGroups, null,null);
            }

            @Override
            public void firstPage(AssignmentGroup[] assignmentGroups, LinkHeaders linkHeaders, Response response) {
                if(!isAdded()){return;}
                EditAssignmentDialog.this.assignmentGroups = assignmentGroups;
                populateViewData();
            }
        };

        assignmentCanvasCallback = new CanvasCallback<Assignment>(APIHelpers.statusDelegateWithContext(getActivity())) {
            @Override
            public void cache(Assignment assignment) {}

            @Override
            public void firstPage(Assignment assignment, LinkHeaders linkHeaders, Response response) {
                if(!isAdded()){return;}
                enableSave();
                assignmentEdited(assignment);
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                enableSave();
                return super.onFailure(retrofitError);
            }
        };
    }

    private void disableSave(){
        isDisabledSave = true;
        viewWrapper.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        positiveButton.setEnabled(false);
        negativeButton.setEnabled(false);
        dialog.setActionButton(DialogAction.POSITIVE, getString(R.string.savingIndeterminate));
    }

    private void enableSave(){
        isDisabledSave = false;
        viewWrapper.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        positiveButton.setEnabled(true);
        negativeButton.setEnabled(true);
        dialog.setActionButton(DialogAction.POSITIVE, getString(R.string.save));
    }

    public void saveData(){
        // disable save, show our progress
        disableSave();

        String editedAssignmentTitle = assignmentTitleET.getText().toString();
        Date editedDueAt = dueDateDT.getDate();
        boolean shouldNotifyUsers = notifyUsers.isChecked();

        Double editedPointsPossible = Double.parseDouble(pointsPossibleET.getText().toString());
        String editedAssignmentGroupString = (String) assignmentGroupSpinner.getSelectedItem();
        Long editedAssignmentGroup = null;

        for (AssignmentGroup group : assignmentGroups) {
            if (editedAssignmentGroupString.equals(group.getName())) {
                editedAssignmentGroup = group.getId();
                break;
            }
        }

        Assignment.GRADING_TYPE editedGradingType = Assignment.getGradingTypeFromString((String) gradingTypeSpinner.getSelectedItem(), getActivity());
        Assignment.SUBMISSION_TYPE[] editedSubmissionTypes;
        Assignment.TURN_IN_TYPE editedTurnInType = Assignment.stringToTurnInType((String) turnInTypeSpinner.getSelectedItem(),getActivity());

        if (editedTurnInType == Assignment.TURN_IN_TYPE.ONLINE) {
            ArrayList<Assignment.SUBMISSION_TYPE> newSubmissionTypes = new ArrayList<Assignment.SUBMISSION_TYPE>();
            for (CheckBox checkbox : submissionTypesArrayList) {
                if (checkbox.isChecked()) {
                    newSubmissionTypes.add((Assignment.SUBMISSION_TYPE) checkbox.getTag());
                }
            }

            editedSubmissionTypes = newSubmissionTypes.toArray(new Assignment.SUBMISSION_TYPE[newSubmissionTypes.size()]);
        } else if (editedTurnInType == Assignment.TURN_IN_TYPE.ON_PAPER) {
            editedSubmissionTypes = new Assignment.SUBMISSION_TYPE[1];
            editedSubmissionTypes[0] = Assignment.SUBMISSION_TYPE.ON_PAPER;
        } else if (editedTurnInType == Assignment.TURN_IN_TYPE.NONE) {
            editedSubmissionTypes = new Assignment.SUBMISSION_TYPE[1];
            editedSubmissionTypes[0] = Assignment.SUBMISSION_TYPE.NONE;
        } else {
            editedSubmissionTypes = null;
        }

        assignment.setName(editedAssignmentTitle);
        assignment.setAssignmentGroupId(editedAssignmentGroup);
        if(editedSubmissionTypes != null){
            assignment.setSubmissionTypes(editedSubmissionTypes);
        }

        assignment.setPointsPossible(editedPointsPossible);
        assignment.setGradingType(editedGradingType);
        assignment.setDueDate(editedDueAt);
        assignment.setMuted(mutedCheckBox.isChecked());

        AssignmentAPI.editAssignment(assignment,shouldNotifyUsers, assignmentCanvasCallback);
    }

    private void assignmentEdited(Assignment assignment){
        if(getActivity().getSupportFragmentManager().findFragmentById(R.id.drawerFragment) instanceof GradingDrawerFragment){
            ((GradingDrawerFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.drawerFragment)).updateAssignmentDetails(assignment);
        }
        if(getActivity() instanceof DocumentActivity){
            ((DocumentActivity) getActivity()).onAssignmentUpdated(assignment);
        }

        dialog.dismiss();
    }

    //////////////////////////////////////////////////////////////
    // View Helpers
    //////////////////////////////////////////////////////////////
    private void initViews(View rootView) {
        progress = rootView.findViewById(R.id.editAssignmentProgress);
        viewWrapper = rootView.findViewById(R.id.editAssignmentWrapper);
        pointsPossibleET = (EditText) rootView.findViewById(R.id.pointsPossibleET);
        assignmentGroupSpinner = (Spinner) rootView.findViewById(R.id.assignmentGroupSpinner);
        gradingTypeSpinner = (Spinner) rootView.findViewById(R.id.gradingTypeSpinner);
        turnInTypeSpinner = (Spinner) rootView.findViewById(R.id.submissionTypeSelectedSpinner);
        turnInTypeWrapper = (LinearLayout) rootView.findViewById(R.id.submissionTypeWrapper);
        onlineSubmissionTypeLayout = (LinearLayout) rootView.findViewById(R.id.allOnlineSubmissionTypes);
        allOnlineSubmissionTypes = (LinearLayout) rootView.findViewById(R.id.allOnlineSubmissionTypes);
        submissionTypesArrayList = new ArrayList<CheckBox>();
        assignmentTitleET = (EditText)rootView.findViewById(R.id.assignmenttitleET);
        onlineEntriesText = (HelveticaTextView) rootView.findViewById(R.id.onlineEntries);
        dueDateDT = (DateTextView)rootView.findViewById(R.id.dueDateDT);
        dueDateDT.setSimpleDateFormat(DateHelpers.getPreferredDateFormat(getActivity()));
        dueDateDT.shouldModifyTime(true);
        dueDateDT.setColor(getResources().getColor(R.color.speedgrader_blue));
        if(assignment.getDueDates() != null && assignment.getDueDates().size() > 1){
            dueDateDT.setEnabled(false);
        }

        notifyUsers = (CheckBox)rootView.findViewById(R.id.notifyUsers);
        useRubricForGrading = (CheckBox) rootView.findViewById(R.id.useRubricForGrading);
        mutedCheckBox = (CheckBox) rootView.findViewById(R.id.mutedCheckBox);
        assignmentTitleET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(!isAdded()){return;}
                if(!s.toString().toLowerCase().equals(assignment.getName().toString().toLowerCase())){
                    assignmentTitleCache = s.toString();
                }else{
                    assignmentTitleCache = null;
                }
            }
        });

        notifyUsers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isNotifyUsers = isChecked;
            }
        });


        mutedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMuted = isChecked;
            }
        });

        dueDateDT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(!isAdded()){return;}
                if(!s.toString().equals(getString(R.string.noDueDateSet)) && !dueDateDT.getDate().equals(assignment.getDueDate())){
                    dueDateCache = dueDateDT.getDate();
                }else{
                    dueDateCache = null;
                }
            }
        });
    }

    private void populateViewData(){

        if(assignment.isUseRubricForGrading()){
            useRubricForGrading.setChecked(true);
        }else{
            useRubricForGrading.setChecked(false);
        }

        if(assignmentTitleCache != null){
            assignmentTitleET.setText(assignmentTitleCache);
        }else{
            assignmentTitleET.setText(assignment.getName());
        }

        if(isNotifyUsers){
            notifyUsers.setChecked(true);
        }

        if(isMuted){
            mutedCheckBox.setChecked(true);
        }else{
            mutedCheckBox.setChecked(false);
        }

        if(dueDateCache != null){
            dueDateDT.setDate(dueDateCache);
        }else {
            dueDateDT.setDate(assignment.getDueDate());
        }

        if(pointsPossibleCache != null){
            pointsPossibleET.setText(pointsPossibleCache);
        }else{
            pointsPossibleET.setText(String.valueOf(assignment.getPointsPossible()));
        }

        //We are unable to set points possible on a quiz
        if(assignment.getTurnInType() == Assignment.TURN_IN_TYPE.QUIZ){
            pointsPossibleET.setEnabled(false);
        }

        int currentIndex = 0;

        //Set up assignmentGroupSpinner adapter
        ArrayAdapter<String> assignmentGroupSpinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.generic_spinner_textview);
        for (int i = 0; i < assignmentGroups.length; i++) {
            AssignmentGroup currentAssignmentGroup = assignmentGroups[i];
            if (currentAssignmentGroup.getId() == assignment.getAssignmentGroupId()) {
                currentIndex = i;
            }
            assignmentGroupSpinnerArrayAdapter.add(currentAssignmentGroup.getName());
        }
        assignmentGroupSpinner.setAdapter(assignmentGroupSpinnerArrayAdapter);
        assignmentGroupSpinner.setSelection(currentIndex, true);

        //Set up gradingTypeSpinner adapter
        currentIndex = 0;
        ArrayAdapter<String> gradingTypeSpinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.generic_spinner_textview);
        for (int i = 0; i < Assignment.GRADING_TYPE.values().length; i++) {
            Assignment.GRADING_TYPE currentGradingType = Assignment.GRADING_TYPE.values()[i];
            if (currentGradingType == assignment.getGradingType()) {
                currentIndex = i;
            }

            gradingTypeSpinnerArrayAdapter.add(Assignment.gradingTypeToPrettyPrintString(currentGradingType, getActivity()));
        }
        gradingTypeSpinner.setAdapter(gradingTypeSpinnerArrayAdapter);
        gradingTypeSpinner.setSelection(currentIndex, true);

        //Set the Type spinner adapter
        if (assignmentTurnInType != Assignment.TURN_IN_TYPE.DISCUSSION && assignmentTurnInType != Assignment.TURN_IN_TYPE.QUIZ && assignmentTurnInType != Assignment.TURN_IN_TYPE.EXTERNAL_TOOL) {
            currentIndex = 0;
            ArrayAdapter<String> submissionTypeSelectedAdapter = new ArrayAdapter<String>(getActivity(), R.layout.generic_spinner_textview);
            for (int i = 0; i < TURN_IN_TYPES_ALLOWED.length; i++) {

                Assignment.TURN_IN_TYPE turnInType = TURN_IN_TYPES_ALLOWED[i];
                if (turnInType == assignmentTurnInType) {
                    currentIndex = i;
                }

                submissionTypeSelectedAdapter.add(Assignment.turnInTypeToPrettyPrintString(turnInType, getActivity()));

            }

            turnInTypeSpinner.setAdapter(submissionTypeSelectedAdapter);
            turnInTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Assignment.TURN_IN_TYPE turnInType = Assignment.stringToTurnInType((String) adapterView.getItemAtPosition(i), getActivity());

                    if (turnInType == Assignment.TURN_IN_TYPE.ONLINE) {
                        allOnlineSubmissionTypes.setVisibility(View.VISIBLE);
                        onlineEntriesText.setVisibility(View.VISIBLE);

                    } else {
                        allOnlineSubmissionTypes.setVisibility(View.GONE);
                        onlineEntriesText.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView){}
            });
            turnInTypeSpinner.setSelection(currentIndex, true);
        } else {
            turnInTypeWrapper.setVisibility(View.GONE);
            onlineEntriesText.setVisibility(View.GONE);
        }

        checkedSubmissionTypes = (ArrayList) assignment.getSubmissionTypes();

        //Populate Online Submission options
        submissionTypesArrayList.clear();
        allOnlineSubmissionTypes.removeAllViews();
        for (int i = 0; i < Assignment.ONLINE_SUBMISSIONS.length; i++) {
            final Assignment.SUBMISSION_TYPE currentSubmissionType = Assignment.ONLINE_SUBMISSIONS[i];
            final CheckBox submissionTypeCheckBox = new CheckBox(getActivity());
            submissionTypeCheckBox.setTextColor(getResources().getColor(R.color.sg_darkText));
            submissionTypeCheckBox.setTag(currentSubmissionType);
            submissionTypeCheckBox.setText(Assignment.submissionTypeToPrettyPrintString(currentSubmissionType, getActivity()));
            if ((checkedSubmissionTypes.contains(currentSubmissionType) &&
                    ( !checkedSubmissionTypesCache.containsKey(currentSubmissionType) || (checkedSubmissionTypesCache.containsKey(currentSubmissionType)) && checkedSubmissionTypesCache.get(currentSubmissionType).equals(true)))
                    || (checkedSubmissionTypesCache.containsKey(currentSubmissionType) && checkedSubmissionTypesCache.get(currentSubmissionType).equals(true)) ) {
                submissionTypeCheckBox.setChecked(true);
            }
            submissionTypeCheckBox.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
            submissionTypeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        checkedSubmissionTypesCache.put(currentSubmissionType, true);
                    } else {
                        checkedSubmissionTypesCache.put(currentSubmissionType, false);
                    }
                    if (checkedSubmissionTypes.contains(currentSubmissionType) && checkedSubmissionTypesCache.containsKey(currentSubmissionType) && checkedSubmissionTypesCache.get(currentSubmissionType).equals(true)) {
                        checkedSubmissionTypesCache.remove(currentSubmissionType);
                    }
                }
            });

            submissionTypesArrayList.add(submissionTypeCheckBox);
            allOnlineSubmissionTypes.addView(submissionTypeCheckBox);
        }
    }

    //////////////////////////////////////////////////////////////
    // Bundle Data
    //////////////////////////////////////////////////////////////
    public static Bundle createBundle(Assignment assignment, Course course) {
        Bundle bundle = new  Bundle();
        bundle.putParcelable(com.instructure.speedgrader.util.Const.ASSIGNMENT, assignment);
        bundle.putParcelable(com.instructure.pandautils.utils.Const.COURSE, course);

        return bundle;
    }

    public void getBundleData(Bundle extras) {
        course = (Course) extras.getParcelable(com.instructure.pandautils.utils.Const.COURSE);
        assignment = extras.getParcelable(com.instructure.speedgrader.util.Const.ASSIGNMENT);
        isMuted = (Boolean) extras.getBoolean(com.instructure.speedgrader.util.Const.isMuted, assignment.isMuted());
        // get all AssignmentGroups from parcelable array
        Parcelable[] assignmentGroupParcels = extras.getParcelableArray(Const.assignmentGroups);
        if(assignmentGroupParcels != null){
            this.assignmentGroups = new AssignmentGroup[assignmentGroupParcels.length];
            // Casting the parcelable array into an array of AssignmentGroup can cause crashes sometimes, so instead we iterate over the parcelable
            // array and cast each item into a AssignmentGroup to rebuild our arrray
            for(int i = 0; i < assignmentGroupParcels.length; i++){
                this.assignmentGroups[i] = (AssignmentGroup) assignmentGroupParcels[i];
            }
        }
        checkedSubmissionTypesCache = (HashMap) extras.getSerializable(com.instructure.speedgrader.util.Const.checkedSubmissionTypes);
        if(checkedSubmissionTypesCache == null){
            checkedSubmissionTypesCache = new HashMap<Assignment.SUBMISSION_TYPE, Boolean>();
        }
        assignmentTitleCache = (String) extras.getSerializable(com.instructure.speedgrader.util.Const.assignmentTitleCache);
        pointsPossibleCache  = (String) extras.getSerializable(com.instructure.speedgrader.util.Const.pointsPossibleCache);
        isNotifyUsers = (Boolean) extras.getBoolean(com.instructure.speedgrader.util.Const.isNotifyUsers, false);
        dueDateCache = (Date) extras.getSerializable(com.instructure.speedgrader.util.Const.dueDateCache);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(com.instructure.speedgrader.util.Const.canvasContext, course);
        outState.putParcelable(com.instructure.speedgrader.util.Const.ASSIGNMENT, assignment);
        outState.putBoolean(com.instructure.speedgrader.util.Const.isMuted, isMuted);
        outState.putSerializable(com.instructure.speedgrader.util.Const.assignmentGroups, assignmentGroups);
        outState.putSerializable(com.instructure.speedgrader.util.Const.checkedSubmissionTypes, checkedSubmissionTypesCache);
        outState.putSerializable(com.instructure.speedgrader.util.Const.assignmentTitleCache, assignmentTitleCache);
        outState.putSerializable(com.instructure.speedgrader.util.Const.pointsPossibleCache, pointsPossibleCache);
        outState.putBoolean(com.instructure.speedgrader.util.Const.isNotifyUsers, isNotifyUsers);
        outState.putSerializable(com.instructure.speedgrader.util.Const.dueDateCache, dueDateCache);
        super.onSaveInstanceState(outState);
    }
}
