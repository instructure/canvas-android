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

package com.instructure.loginapi.login.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.instructure.loginapi.login.OAuthWebLogin;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.dialog.GenericDialogStyled;
import com.instructure.loginapi.login.model.SignedInUser;
import com.instructure.loginapi.login.util.Const;
import com.instructure.loginapi.login.util.ProfileUtils;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class PreviouslySignedInUserAdapter extends BaseAdapter implements GenericDialogStyled.GenericDialogListener {

    public interface SignedInUserCallback {
        public void onUserDelete();
    }

    private Picasso picasso;
    private LayoutInflater layoutInflater;

    private GenericDialogStyled genericDialogStyled;

    private FragmentActivity activity;
    private String selectedUserGlobalId;
    private SignedInUser userToRemove;
    private SignedInUserCallback callback;

    private ArrayList<SignedInUser> previouslySignedInUsers = new ArrayList<SignedInUser>();

    public PreviouslySignedInUserAdapter(FragmentActivity activity, SignedInUserCallback callback, ArrayList<SignedInUser> previouslySignedInUsers) {
        this.previouslySignedInUsers = previouslySignedInUsers;
        this.activity = activity;
        this.callback = callback;
        this.layoutInflater = activity.getLayoutInflater();
        this.picasso = new Picasso.Builder(activity).build();
    }

    public void setSelectedUserGlobalId (String selectedUserGlobalId, SignedInUser user, Context context){
        this.selectedUserGlobalId = selectedUserGlobalId;
        OAuthWebLogin.setCalendarFilterPrefs(user.calendarFilterPrefs, context);
        notifyDataSetChanged();
    }

    public void clearSelectedGlobalId (){
        this.selectedUserGlobalId = null;
        notifyDataSetChanged();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public int getCount() {
        return previouslySignedInUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return previouslySignedInUsers.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final SignedInUser signedInUser = previouslySignedInUsers.get(position);
        String globalID = OAuthWebLogin.getGlobalUserId(signedInUser.domain, signedInUser.user);

        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.previously_signed_in_user, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.domain = (TextView) convertView.findViewById(R.id.domain);
            viewHolder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.loading);
            viewHolder.delete = (ImageView) convertView.findViewById(R.id.delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.delete.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_cv_login_x));


        ProfileUtils.configureAvatarView(activity, signedInUser.user, viewHolder.avatar);

        viewHolder.name.setText(signedInUser.user.getShortName());
        viewHolder.domain.setText(signedInUser.domain);


        if (selectedUserGlobalId == null) {
            viewHolder.delete.setVisibility(View.VISIBLE);
        } else {
            viewHolder.delete.setVisibility(View.GONE);
        }

        //Handle deleting items.
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Make sure they actually want to delete that user.
                userToRemove = signedInUser;
                genericDialogStyled = GenericDialogStyled.newInstance(
                        R.string.removeUser,
                        R.string.removedForever,
                        R.string.confirm,
                        R.string.cancel,
                        R.drawable.ic_cv_information_light,
                        PreviouslySignedInUserAdapter.this);
                genericDialogStyled.show(activity.getSupportFragmentManager(), "Delete confirmation");
            }
        });

        if (globalID.equals(selectedUserGlobalId)) {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        } else {
            viewHolder.progressBar.setVisibility(View.GONE);
        }

        return convertView;
    }


    public static class ViewHolder {
        CircleImageView avatar;
        TextView name;
        TextView domain;
        ProgressBar progressBar;
        ImageView delete;
    }


    @Override
    public void onPositivePressed() {
        OAuthWebLogin.removeFromPreviouslySignedInUsers(userToRemove, activity);
        previouslySignedInUsers = OAuthWebLogin.getPreviouslySignedInUsers(activity);

        if(genericDialogStyled != null){
            genericDialogStyled.dismiss();
        }
        notifyDataSetChanged();
        if(callback != null) {
            callback.onUserDelete();
        }
    }

    @Override public void onNegativePressed() {
        if(genericDialogStyled != null){
            genericDialogStyled.dismiss();
        }
    }
}
