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

package com.instructure.loginapi.login.api.zendesk.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi.api.ErrorReportAPI;
import com.instructure.canvasapi.model.ErrorReportResult;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.materialdialogs.CustomDialog;
import com.instructure.loginapi.login.util.Const;
import com.instructure.loginapi.login.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * NOTE: this no longer uses Zendesk. There is an api from canvas that we can post to and the endpoint
 * will take care of the help desk client that customer support is currently using.
 */
public class ZendeskDialogStyled extends DialogFragment {

    public interface ZendeskDialogResultListener {
        public void onTicketPost();
        public void onTicketError();
    }
    private static String DEFAULT_DOMAIN = "canvas.instructure.com";
    public static final String TAG = "zendeskDialog";
    private static final int customFieldTag = 20470321;
    private EditText descriptionEditText;
    private EditText subjectEditText;
    private EditText emailAddressEditText;
    private TextView emailAddress;
    private Spinner severitySpinner;

    private int titleColor;
    private int positiveColor;
    private int negativeColor;
    private boolean fromLogin;
    private boolean mUseDefaultDomain;

    private ZendeskDialogResultListener resultListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            resultListener = (ZendeskDialogResultListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ZendeskDialogResultListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity(), getActivity().getString(R.string.zendesk_reportAProblem), getString(R.string.zendesk_send));
        builder.darkTheme(false);

        //set the default colors for the title and positive and negative buttons
        titleColor = getResources().getColor(R.color.courseBlueDark);
        positiveColor = getResources().getColor(R.color.courseGreen);
        negativeColor = getResources().getColor(R.color.gray);

        //check to see if there are custom colors we need to set
        handleBundle();

        builder.titleColor(titleColor);
        builder.positiveColor(positiveColor);
        builder.negativeText(getString(R.string.cancel));
        builder.negativeColor(negativeColor);

        final CustomDialog dialog = builder.build();

        // Create View
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_zendesk_ticket, null);
        subjectEditText = (EditText) view.findViewById(R.id.subjectEditText);
        descriptionEditText = (EditText) view.findViewById(R.id.descriptionEditText);
        emailAddressEditText = (EditText) view.findViewById(R.id.emailAddressEditText);
        emailAddress = (TextView) view.findViewById(R.id.emailAddress);

        if(fromLogin) {
            emailAddressEditText.setVisibility(View.VISIBLE);
            emailAddress.setVisibility(View.VISIBLE);
        }

        initSpinner(view);

        dialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                saveZendeskTicket();
            }

            @Override
            public void onCancelClick() {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCustomView(view);

        return dialog;
    }

    /////////////////////////////////////////////////////////////////
    //              Helpers
    /////////////////////////////////////////////////////////////////

    public void initSpinner(View view){
        List<String> severityList = Arrays.asList(
                getString(R.string.zendesk_casualQuestion),
                getString(R.string.zendesk_needHelp),
                getString(R.string.zendesk_somethingsBroken),
                getString(R.string.zendesk_cantGetThingsDone),
                getString(R.string.zendesk_extremelyCritical));

        severitySpinner = (Spinner) view.findViewById(R.id.severitySpinner);
        ZenDeskAdapter adapter = new ZenDeskAdapter(getActivity(), R.layout.zendesk_spinner_item, severityList);
        severitySpinner.setAdapter(adapter);
    }

    private class ZenDeskAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        public ZenDeskAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getViewForText(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getViewForText(position, convertView, parent);
        }

        private View getViewForText(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.zendesk_spinner_item, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(getItem(position));
            holder.text.post(new Runnable() {
                @Override
                public void run() {
                    holder.text.setSingleLine(false);
                }
            });


            return convertView;
        }
    }

    private static class ViewHolder {
        TextView text;
    }

    public void saveZendeskTicket(){
        String comment = descriptionEditText.getText().toString();
        String subject = subjectEditText.getText().toString();

        if (comment.isEmpty() || subject.isEmpty()){
            Toast.makeText(getContext(), R.string.empty_feedback, Toast.LENGTH_LONG).show();
            return;
        }

        // if we're on the login page we need to set the cache user's email address so that support can
        // contact the user
        if(fromLogin) {
            if(emailAddressEditText.getText() != null && emailAddressEditText.getText().toString() != null) {
                User user = new User();
                user.setEmail(emailAddressEditText.getText().toString());
                APIHelpers.setCacheUser(getActivity(), user);
            }

        }


        final String email = APIHelpers.getCacheUser(getActivity()).getEmail();
        String domain = APIHelpers.getDomain(getActivity());
        if(domain == null) {
            domain = DEFAULT_DOMAIN;
        }

        //add device info to comment
        //try to get the version number and version code
        PackageInfo pInfo = null;
        String versionName = "";
        int versionCode = 0;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Utils.d(e.getMessage());
        }
        String deviceInfo = "";
        deviceInfo += getString(R.string.device) + " " + Build.MANUFACTURER + " " + Build.MODEL + "\n" +
                getString(R.string.osVersion) + " " + Build.VERSION.RELEASE + "\n" +
                getString(R.string.versionNum) + ": " + versionName + " " + versionCode + "\n" +
                getString(R.string.zendesk_severityText) + " " + getUserSeveritySelectionTag() + "\n" +
                getString(R.string.utils_installDate) + " " + getInstallDateString() + "\n\n";

        comment = deviceInfo + comment;
        if(mUseDefaultDomain) {
            ErrorReportAPI.postGenericErrorReport(subject, domain, email, comment, getUserSeveritySelectionTag(), new CanvasCallback<ErrorReportResult>((APIStatusDelegate) resultListener) {
                @Override
                public void firstPage(ErrorReportResult errorReportResult, LinkHeaders linkHeaders, Response response) {
                    resetCachedUser();
                    resultListener.onTicketPost();
                }

                @Override
                public boolean onFailure(RetrofitError retrofitError) {
                    resetCachedUser();
                    resultListener.onTicketError();
                    return super.onFailure(retrofitError);
                }
            });
        } else {
            ErrorReportAPI.postErrorReport(subject, domain, email, comment, getUserSeveritySelectionTag(), new CanvasCallback<ErrorReportResult>((APIStatusDelegate) resultListener) {
                @Override
                public void cache(ErrorReportResult errorReportResult) {
                    resetCachedUser();
                }

                @Override
                public void firstPage(ErrorReportResult errorReportResult, LinkHeaders linkHeaders, Response response) {
                    cache(errorReportResult);
                    resultListener.onTicketPost();
                }

                @Override
                public boolean onFailure(RetrofitError retrofitError) {
                    resetCachedUser();
                    resultListener.onTicketError();
                    return super.onFailure(retrofitError);
                }
            });
        }
    }

    private void resetCachedUser() {
        if(fromLogin) {
            //reset the cached user so we don't have any weird data hanging around
            APIHelpers.setCacheUser(getActivity(), null);
        }
    }

    private String getInstallDateString() {
        try {
            long installed = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0)
                    .firstInstallTime;
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
            return format.format(new Date(installed));
        } catch (Exception e) {
            return "";
        }
    }


    private String getUserSeveritySelectionTag(){
        if(severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_extremelyCritical))){
            return getString(R.string.zendesk_extremelyCritical_tag);
        }else if(severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_casualQuestion))){
            return getString(R.string.zendesk_casualQuestion_tag);
        }else if(severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_somethingsBroken))){
            return getString(R.string.zendesk_somethingsBroken_tag);
        }else if(severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_cantGetThingsDone))){
            return getString(R.string.zendesk_cantGetThingsDone_tag);
        }else if(severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_needHelp))){
            return getString(R.string.zendesk_needHelp_tag);
        }else{
            return "";
        }
    }

    /**
     * A way to customize the colors that display in the dialog. Use 0 if you don't want to change that color
     * @param titleColor
     * @param positiveColor
     * @param negativeColor
     * @return
     */
    public static Bundle createBundle(int titleColor, int positiveColor, int negativeColor) {
        return createBundle(titleColor, positiveColor, negativeColor, false);
    }

    /**
     * if we're coming from the login screen there won't be any user information (because the user hasn't
     * logged in)
     *
     * @param titleColor
     * @param positiveColor
     * @param negativeColor
     * @param fromLogin
     * @return
     */
    public static Bundle createBundle(int titleColor, int positiveColor, int negativeColor, boolean fromLogin) {
        Bundle bundle = new Bundle();
        if(titleColor != 0) {
            bundle.putInt(Const.TITLE_COLOR, titleColor);
        }
        if(positiveColor != 0) {
            bundle.putInt(Const.POSITIVE_COLOR, positiveColor);
        }
        if(negativeColor != 0) {
            bundle.putInt(Const.NEGATIVE_COLOR, negativeColor);
        }

        bundle.putBoolean(Const.FROM_LOGIN, fromLogin);
        return bundle;
    }

    /**
     * if we're coming from the parent app we want to use the default domain
     *
     * @param titleColor
     * @param positiveColor
     * @param negativeColor
     * @param fromLogin
     * @param useDefaultDomain
     * @return
     */
    public static Bundle createBundle(int titleColor, int positiveColor, int negativeColor, boolean fromLogin, boolean useDefaultDomain) {
        Bundle bundle = new Bundle();
        if(titleColor != 0) {
            bundle.putInt(Const.TITLE_COLOR, titleColor);
        }
        if(positiveColor != 0) {
            bundle.putInt(Const.POSITIVE_COLOR, positiveColor);
        }
        if(negativeColor != 0) {
            bundle.putInt(Const.NEGATIVE_COLOR, negativeColor);
        }

        bundle.putBoolean(Const.FROM_LOGIN, fromLogin);
        bundle.putBoolean(Const.USE_DEFAULT_DOMAIN, useDefaultDomain);
        return bundle;
    }

    /**
     * Set the colors of the dialog based on the arguments passed in the bundle. If there isn't a color
     * we just use what is already set
     */
    public void handleBundle() {
        if(getArguments() == null) {
            return;
        }

        titleColor = getArguments().getInt(Const.TITLE_COLOR, titleColor);
        positiveColor = getArguments().getInt(Const.POSITIVE_COLOR, positiveColor);
        negativeColor = getArguments().getInt(Const.NEGATIVE_COLOR, negativeColor);
        fromLogin = getArguments().getBoolean(Const.FROM_LOGIN, false);
        mUseDefaultDomain = getArguments().getBoolean(Const.USE_DEFAULT_DOMAIN, false);
    }

}
