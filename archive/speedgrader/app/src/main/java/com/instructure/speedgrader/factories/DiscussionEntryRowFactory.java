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

package com.instructure.speedgrader.factories;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;

import com.instructure.canvasapi.model.DiscussionEntry;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.views.HelveticaTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class DiscussionEntryRowFactory {

    /////////////////////////////////////////////////////////////////
    //   ViewHolders
    /////////////////////////////////////////////////////////////////
    private static class ViewHolder {
        CircleImageView avatar;
        HelveticaTextView discussionEntry;
        HelveticaTextView date;
        HelveticaTextView name;
    }


    /////////////////////////////////////////////////////////////////
    //   Row Factories
    /////////////////////////////////////////////////////////////////

    public static View buildRowView(Context context, DiscussionEntry discussionEntry, String author, String avatarURL, boolean showStudentNames, View convertView) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listview_item_row_discussion_entry, null);
            holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.discussionEntry = (HelveticaTextView) convertView.findViewById(R.id.discussionEntry);
            holder.date = (HelveticaTextView) convertView.findViewById(R.id.date);
            holder.name = (HelveticaTextView) convertView.findViewById(R.id.name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (avatarURL != null) {
            // I tried doing this using a Target, but it wasn't loading the first time. I think it has to deal with
            // the target being garbage collected. See more from Jake Wharton's answer from the link below.
            //
            // http://stackoverflow.com/questions/23732556/sometimes-picasso-doesnt-load-the-image-from-memory-cache
            Picasso.with(context).load(avatarURL).into(holder.avatar, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    holder.avatar.setImageResource(R.drawable.ic_cv_user);
                }
            });
        }

        holder.discussionEntry.setText(Html.fromHtml(discussionEntry.getMessage(context.getString(R.string.deletedEntry))));
        //make any links in the textview clickable. This will allow the user to view the link in whatever app they want
        holder.discussionEntry.setMovementMethod(LinkMovementMethod.getInstance());

        holder.date.setText(DateHelpers.getFormattedDate(context, discussionEntry.getCreatedAt()) + " " + DateHelpers.getFormattedTime(context, discussionEntry.getCreatedAt()));
        if(showStudentNames){
            holder.name.setText(author);
        }

        return convertView;
    }

}
