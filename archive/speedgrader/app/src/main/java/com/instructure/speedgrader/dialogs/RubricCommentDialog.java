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
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.canvasapi.model.RubricCriterion;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.HelveticaTextView;

public class RubricCommentDialog extends DialogFragment {

    public static final String TAG = "editRubricCommentDialog";

    private String currentComment = "";
    private RubricCriterion criterion;
    private RubricCriterionRating rating;
    private int position;
    private MaterialDialog dialog;

    //VIEWS
    private HelveticaTextView pointsPossibleEditText;
    private EditText freeFormPointsEditText;
    private View freeFormPointsHolder;

    // Saving progress
    View negativeButton;
    View positiveButton;

    EditText commentEditText;

    boolean isFreeForm;

    //////////////////////////////////////////////////////////////
    // Lifecycle
    //////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //check to see if there are custom colors we need to set
        if(savedInstanceState == null){
            getBundleData(getArguments());
        }else{
            getBundleData(savedInstanceState);
        }

        MaterialDialog.Builder builder =
                  new MaterialDialog.Builder(getActivity())
                                    .title(criterion.getCriterionDescription())
                                    .titleColor(getResources().getColor(R.color.sg_defaultPrimary))
                                    .positiveText(getString(R.string.saveContinue))
                                    .positiveColor(getResources().getColor(R.color.courseGreen))
                                    .negativeText(getString(R.string.cancel))
                                    .negativeColor(getResources().getColor(R.color.gray))
                                    .autoDismiss(false);

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                Intent returnIntent = getReturnIntentIfValid();

                if(returnIntent != null){
                    getTargetFragment().onActivityResult(getTargetRequestCode(), 123, returnIntent);
                    positiveButton.requestFocus();
                    dismiss();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                dismiss();
                super.onNegative(dialog);
            }
        });

        // Create View
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_rubric_comment, null);
        initViews(view);

        builder.customView(view, true);
        dialog = builder.build();
        dialog.setCanceledOnTouchOutside(true);

        negativeButton = dialog.getActionButton(DialogAction.NEGATIVE);
        positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
        return dialog;
    }

    private Intent getReturnIntentIfValid() {

        if(isFreeForm){
            // Get the score entered and make sure it's an int
            String scoreInput = freeFormPointsEditText.getText().toString();
            try {
                int num = Integer.parseInt(scoreInput);
                rating.setPoints(num);
                rating.setCriterionId(criterion.getId());
                rating.setComments(commentEditText.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), getString(R.string.invalidScore), Toast.LENGTH_SHORT).show();
                return null;
            }
        }else{
            rating.setCriterionId(criterion.getId());
            rating.setComments(commentEditText.getText().toString());
        }

        Intent intent = new Intent();
        intent.putExtra(Const.currentPosition, position);
        intent.putExtra(Const.rubricCriterionRating, (Parcelable)rating);
        return intent;
    }

    //////////////////////////////////////////////////////////////
    // View Helpers
    //////////////////////////////////////////////////////////////
    private void initViews(View rootView) {
        commentEditText = (EditText)rootView.findViewById(R.id.commentEditText);
        freeFormPointsHolder = rootView.findViewById(R.id.freeFormPointsHolder);
        freeFormPointsEditText = (EditText)rootView.findViewById(R.id.pointsEditText);
        pointsPossibleEditText = (HelveticaTextView) rootView.findViewById(R.id.pointsPossible);
        if(!isFreeForm){
            freeFormPointsHolder.setVisibility(View.GONE);
        }else{
            pointsPossibleEditText.setText(String.valueOf(rating.getMaxPoints()));
        }
    }

    //////////////////////////////////////////////////////////////
    // Bundle Data
    //////////////////////////////////////////////////////////////
    public static Bundle createBundle(RubricCriterion criterion, RubricCriterionRating rating, int position, boolean isFreeForm) {
        Bundle bundle = new  Bundle();
        bundle.putString(Const.commentsCache, rating.getComments());
        bundle.putInt(Const.currentPosition, position);
        bundle.putParcelable(Const.rubricCriterion, criterion);
        bundle.putParcelable(Const.rubricCriterionRating, rating);
        bundle.putBoolean(Const.isFreeForm, isFreeForm);
        return bundle;
    }

    public void getBundleData(Bundle extras) {
        currentComment = extras.getString(Const.commentsCache, "");
        criterion      = extras.getParcelable(Const.rubricCriterion);
        rating         = extras.getParcelable(Const.rubricCriterionRating);
        position       = extras.getInt(Const.currentPosition);
        isFreeForm     = extras.getBoolean(Const.isFreeForm);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Const.commentsCache, currentComment);
        outState.putInt(Const.currentPosition, position);
        outState.putString(Const.freeFormPointsCache, freeFormPointsEditText.getText().toString());
        outState.putParcelable(Const.rubricCriterion, criterion);
        outState.putParcelable(Const.rubricCriterionRating, rating);
        super.onSaveInstanceState(outState);
    }
}