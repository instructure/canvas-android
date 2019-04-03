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

package com.instructure.teacheraid.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.util.Const;
import com.instructure.teacheraid.util.Prefs;

public class StudentChooserSettingsDialog extends DialogFragment {

    public static final String TAG = "chooserDialog";

    private CheckBox removeStudent;
    private CheckBox readName;

    private boolean shouldRemoveStudent = true;
    private boolean shouldReadName = true;

    private DialogInterface.OnDismissListener onDismissListener;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FragmentActivity activity = getActivity();

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                        .positiveText(R.string.okay)
                        .title(activity.getString(R.string.action_settings));

        View view = LayoutInflater.from(activity).inflate(R.layout.student_chooser_dialog, null);

        removeStudent = (CheckBox) view.findViewById(R.id.removeStudents);

        readName = (CheckBox) view.findViewById(R.id.readName);

        setCheckedState();

        removeStudent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //set whether to remove the student after he/she is selected
                shouldRemoveStudent = isChecked;
                Prefs.save(getActivity(), Const.SHOULD_REMOVE_STUDENT, shouldRemoveStudent);
            }
        });

        readName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                shouldReadName = isChecked;
                Prefs.save(getActivity(), Const.SHOULD_READ_NAME, shouldReadName);
            }
        });

        builder.customView(view, true);

        final MaterialDialog dialog = builder.build();

        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    private void setCheckedState() {
        shouldReadName = Prefs.load(getActivity(), Const.SHOULD_READ_NAME, true);
        shouldRemoveStudent = Prefs.load(getActivity(), Const.SHOULD_REMOVE_STUDENT, true);

        readName.setChecked(shouldReadName);
        removeStudent.setChecked(shouldRemoveStudent);
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if(onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
}
