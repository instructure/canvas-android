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
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.interfaces.GenericDialogListener;


public class GenericDialogStyled extends DialogFragment {

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String POSITIVE = "positive";
    private static final String NEGATIVE = "negative";
    private static final String ICON = "icon";
    private static final String HANDLER = "handler";
    private static final String SHOULD_DISMISS = "shouldDimiss";

    private int mTitleColor;
    private int mIconRes;
    private String mTitle;
    private String mMessage;
    private String mPositiveText;
    private String mNegativeText;
    private boolean mShouldDismissWhenPressed;
    private GenericDialogListener mListener;

    public static GenericDialogStyled newInstance(
            boolean dismissOnButtonPress,
            int dialogTitle, int dialogMessage, int positiveButtonText,
            int negativeButtonText, int iconDrawableResource, GenericDialogListener listener) {

        GenericDialogStyled frag = new GenericDialogStyled();
        Bundle args = new Bundle();
        args.putInt(TITLE, dialogTitle);
        args.putInt(MESSAGE, dialogMessage);
        args.putInt(POSITIVE, positiveButtonText);
        args.putInt(NEGATIVE, negativeButtonText);
        args.putInt(ICON, iconDrawableResource);
        args.putBoolean(SHOULD_DISMISS, dismissOnButtonPress);
        args.putSerializable(HANDLER, listener);
        frag.setArguments(args);
        return frag;
    }

    public void loadBundleData(Bundle args){
        mTitle = getString( args.getInt(TITLE) );
        mMessage = getString(args.getInt(MESSAGE));
        mTitleColor = getResources().getColor(R.color.sg_defaultPrimary);
        mPositiveText = getString(args.getInt(POSITIVE));
        mNegativeText = getString(args.getInt(NEGATIVE));
        mIconRes = args.getInt(ICON);
        mShouldDismissWhenPressed = args.getBoolean(SHOULD_DISMISS);
        mListener = (GenericDialogListener) args.getSerializable(HANDLER);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if(getArguments() != null){
            loadBundleData(getArguments());
        }

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getActivity())
                        .title(mTitle)
                        .titleColor(mTitleColor)
                        .positiveText(mPositiveText)
                        .positiveColor(getResources().getColor(R.color.courseGreen))
                        .negativeText(mNegativeText)
                        .content(mMessage)
                        .negativeColor(getResources().getColor(R.color.gray))
                        .autoDismiss(mShouldDismissWhenPressed);

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                if (mListener != null) {
                    mListener.onPositivePressed();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                if(mListener != null){
                    mListener.onNegativePressed();
                }
            }
        });

        Dialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}
