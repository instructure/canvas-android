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

package com.instructure.loginapi.login.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;

import com.instructure.canvasapi.model.User;
import com.instructure.loginapi.login.R;
import com.instructure.pandautils.utils.ColorUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileUtils {

    public static final String noPictureURL = "images/dotted_pic.png";
    public static final String noPictureURLAlternate = "images%2Fmessages%2Favatar-50.png";
    public static final String noPictureURLGroup = "images/messages/avatar-group-50.png";

    public static String getUserInitials(User user){
        return getUserInitials(user.getShortName());
    }

    public static String getUserInitials(String name){
        if(name == null){
            return "";
        }
        
        name = name.trim();
        String[] nameArr = name.split("\\s+");

        if(nameArr.length == 2 && (nameArr[0].length() > 0 && nameArr[1].length() > 0)){ //if First Last = FL
            return nameArr[0].substring(0, 1).toUpperCase(Locale.getDefault()) + nameArr[1].substring(0, 1).toUpperCase(Locale.getDefault());
        }else if (nameArr.length > 0 && nameArr[0].length() > 0){//else First = F
                return nameArr[0].substring(0, 1).toUpperCase(Locale.getDefault());
        }else{
            return ""; // use default picture
        }
    }

    public static int getUserColor(User user, Context context){
        return getUserColor(user.getShortName(), context);
    }

    public static int getUserColor(String name, Context context){
        char ch;
        int i = 0;
        if(!TextUtils.isEmpty(name)){
            ch = name.toLowerCase(Locale.getDefault()).charAt(0);
            i = ch % 13;
        }

        switch(i){
            case 0:
                return context.getResources().getColor(R.color.courseRedLight);
            case 1:
                return context.getResources().getColor(R.color.courseOrangeLight);
            case 2:
                return context.getResources().getColor(R.color.courseGoldLight);
            case 3:
                return context.getResources().getColor(R.color.courseGreenLight);
            case 4:
                return context.getResources().getColor(R.color.courseChartreuseLight);
            case 5:
                return context.getResources().getColor(R.color.courseCyanLight);
            case 6:
                return context.getResources().getColor(R.color.courseSlateLight);
            case 7:
                return context.getResources().getColor(R.color.courseBlueLight);
            case 8:
                return context.getResources().getColor(R.color.coursePurpleLight);
            case 9:
                return context.getResources().getColor(R.color.courseVioletLight);
            case 10:
                return context.getResources().getColor(R.color.coursePinkLight);
            case 11:
                return context.getResources().getColor(R.color.courseHotPinkLight);
            case 12:
                return context.getResources().getColor(R.color.courseYellowLight);
            case 13:
                return context.getResources().getColor(R.color.courseLavenderLight);
            default:
                return context.getResources().getColor(R.color.courseSlateLight);
        }
    }

    public static String getUserHexColorString(String user){
        char ch = user.toLowerCase(Locale.getDefault()).charAt(0);

        int i = ch % 15;

        switch(i){
            case 0:
                return "#EF4437";
            case 1:
                return "#F0592B";
            case 2:
                return "#F8971C";
            case 3:
                return "#009688";
            case 4:
                return "#4CAE4E";
            case 5:
                return "#09BCD3";
            case 6:
                return "#35A4DC";
            case 7:
                return "#2083C5";
            case 8:
                return "#4554A4";
            case 9:
                return "#65499D";
            case 10:
                return "#F06291";
            case 11:
                return "#E71F63";
            case 12:
                return "#9D9E9E";
            case 13:
                return "#FDC010";
            case 14:
                return "#8F3E97";
            default:
                return "#35A4DC";
        }
    }

    public static void configureAvatarView(final Context context, final User user, final CircleImageView avatar){
        if(user == null) {
            configureAvatarView(context, "", "", avatar);
            return;
        }
        configureAvatarView(context, user.getName(), user.getAvatarURL(), avatar);
    }

    public static void configureAvatarView(final Context context, final User user, final CircleImageView avatar, int color){
        if(user == null) {
            configureAvatarView(context, "", "", avatar);
            return;
        }
        configureAvatarView(context, user.getName(), user.getAvatarURL(), avatar, false, color, null);
    }

    public static void configureAvatarView(final Context context, final String username, final String avatarURL, final CircleImageView avatar) {
        final int color = ProfileUtils.getUserColor(username, context);
        configureAvatarView(context, username, avatarURL, avatar, false, color, null);
    }

    public static void configureAvatarView(final Context context, final String username, final String avatarURL, final CircleImageView avatar, final boolean isGroup) {
        final int color = ProfileUtils.getUserColor(username, context);
        configureAvatarView(context, username, avatarURL, avatar, isGroup, color, null);
    }

    public static void configureAvatarView(final Context context, final String username, final String avatarURL, final CircleImageView avatar, final boolean isGroup, final int color, Long conversationId){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && conversationId != null) {
            avatar.setTransitionName(com.instructure.pandautils.utils.Const.MESSAGE + String.valueOf(conversationId));
        }

        if (!isGroup && avatarURL != null && !avatarURL.contains(noPictureURL) && !avatarURL.contains(noPictureURLAlternate)) {
            avatar.setBorderWidth(0);
            Picasso.with(context)
                    .load(avatarURL)
                    .fit()
                    .centerCrop()
                    .into(avatar, new Callback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError() {
                            avatar.setImageDrawable(createInitialsAvatar(context, color, username));
                        }
                    });
        } else{
            if(!isGroup) {
                avatar.setImageDrawable(createInitialsAvatar(context, color, username));
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_group_32, options);
                avatar.setImageBitmap(ColorUtils.colorIt(color, bm));
                avatar.setBorderWidth((int) Utils.convertDipsToPixels(1, context));
                avatar.setBorderColor(color);
            }
        }
    }

    public static TextDrawable createInitialsAvatar(Context context, final int color, final String username) {
        final String initials = ProfileUtils.getUserInitials(username);
        return TextDrawable.builder()
                .beginConfig()
                .height(context.getResources().getDimensionPixelSize(R.dimen.avatar_size))
                .width(context.getResources().getDimensionPixelSize(R.dimen.avatar_size))
                .toUpperCase()
                .textColor(Color.WHITE)
                .endConfig()
                .buildRound(initials, color);
    }

    public static Bitmap getInitialsAvatarBitMap(Context context, final String username) {
        int color = getUserColor(username, context);
        final String initials = ProfileUtils.getUserInitials(username);
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .height(context.getResources().getDimensionPixelSize(R.dimen.avatar_size))
                .width(context.getResources().getDimensionPixelSize(R.dimen.avatar_size))
                .toUpperCase()
                .textColor(Color.WHITE)
                .endConfig()
                .buildRound(initials, color);

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
