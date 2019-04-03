/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.loginapi.login.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import java.io.Serializable;

public class GenericDialogStyled extends DialogFragment {

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String POSITIVE = "positive";
    private static final String NEGATIVE = "negative";
    private static final String ICON = "icon";
    private static final String HANDLER = "handler";
    private static final String MESSAGE_STRING = "message_string";


    public interface GenericDialogListener extends Serializable {
        abstract public void onPositivePressed();

        abstract public void onNegativePressed();
    }

    public static GenericDialogStyled newInstance(
            boolean dismissOnButtonPress,
            int dialogTitle, int dialogMessage, int positiveButtonText,
            int negativeButtonText, int iconDrawableResource, GenericDialogListener listener) {

        GenericDialogStyled frag = new GenericDialogStyled();

        frag.setArguments(populateBundle(dismissOnButtonPress, dialogTitle, dialogMessage, positiveButtonText, negativeButtonText, iconDrawableResource, listener));
        return frag;
    }

    private static Bundle populateBundle(boolean dismissOnButtonPress, int dialogTitle, int dialogMessage, int positiveButtonText,
                                  int negativeButtonText, int iconDrawableResource, GenericDialogListener listener) {
        Bundle args = new Bundle();
        args.putInt("title", dialogTitle);
        args.putBoolean("dismiss", dismissOnButtonPress);
        args.putInt("message", dialogMessage);
        args.putInt("positive", positiveButtonText);
        args.putInt("negative", negativeButtonText);
        args.putInt("icon", iconDrawableResource);
        args.putSerializable("handler", listener);

        return args;
    }

    public static GenericDialogStyled newInstance(
            boolean dismissOnButtonPress, int dialogTitle, int dialogMessage, int positiveButtonText,
            int negativeButtonText, int iconDrawableResource, String message, GenericDialogListener listener) {

        GenericDialogStyled frag = new GenericDialogStyled();

        Bundle bundle = populateBundle(dismissOnButtonPress, dialogTitle, dialogMessage, positiveButtonText, negativeButtonText,
                iconDrawableResource, listener);
        bundle.putString(MESSAGE_STRING, message);

        frag.setArguments(bundle);
        return frag;
    }

    public static GenericDialogStyled newInstance(
            int dialogTitle, int dialogMessage, int positiveButtonText,
            int negativeButtonText, int iconDrawableResource, GenericDialogListener listener) {
        return newInstance(false, dialogTitle, dialogMessage, positiveButtonText, negativeButtonText, iconDrawableResource, listener);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FragmentActivity activity = (FragmentActivity) getActivity();

        Bundle args = getArguments();
        if (args == null)
            return null;

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                        .title(args.getInt(TITLE))
                        .positiveText(args.getInt(POSITIVE))
                        .content(getString(args.getInt(MESSAGE)))
                        .negativeText(args.getInt(NEGATIVE));

        final boolean dismissOnButtonPress = args.getBoolean("dismiss", false);
        final GenericDialogListener listener = (GenericDialogListener) args.getSerializable(HANDLER);

        if(args.getString(MESSAGE_STRING) != null) {
            builder.content(args.getString(MESSAGE_STRING));
        }

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                if (listener != null) {
                    listener.onPositivePressed();
                }

                if (dismissOnButtonPress) {
                    getDialog().cancel();
                    dismissAllowingStateLoss();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                if (listener != null) {
                    listener.onNegativePressed();
                }

                if (dismissOnButtonPress) {
                    getDialog().cancel();
                    dismissAllowingStateLoss();
                }
            }
        });

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
}
