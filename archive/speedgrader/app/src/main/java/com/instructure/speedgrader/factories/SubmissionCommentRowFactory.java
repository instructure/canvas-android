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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.MediaComment;
import com.instructure.canvasapi.model.SubmissionComment;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader;
import com.instructure.pandautils.utils.LoaderUtils;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.interfaces.SubmissionListener;
import com.instructure.speedgrader.util.ViewUtils;
import com.instructure.speedgrader.views.HelveticaTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubmissionCommentRowFactory {

    /////////////////////////////////////////////////////////////////
    //   ViewHolders
    /////////////////////////////////////////////////////////////////
    private static class ViewHolder {
        CircleImageView avatar;
        HelveticaTextView comment;
        HelveticaTextView date;
        LinearLayout attachmentsHolder;
    }

    /////////////////////////////////////////////////////////////////
    //   Row Factories
    /////////////////////////////////////////////////////////////////

    public static View buildRowView(View convertView, LayoutInflater inflater, SubmissionComment comment, final Activity context, boolean isUser) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            if(isUser){
                convertView = inflater.inflate(R.layout.list_item_user_comment, null);
            }else{
                convertView = inflater.inflate(R.layout.list_item_other_comment, null);
            }
            holder.avatar            = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.comment           = (HelveticaTextView) convertView.findViewById(R.id.commentText);
            holder.date              = (HelveticaTextView) convertView.findViewById(R.id.date);
            holder.attachmentsHolder = (LinearLayout) convertView.findViewById(R.id.attachmentsHolder);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String avatarURL = comment.getAuthor().getAvatarImageUrl();

        if (avatarURL != null) {
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

        holder.comment.setText(comment.getComment());

        holder.date.setText(DateHelpers.getShortDate(context, comment.getComparisonDate()) +context.getString(R.string.at) + DateHelpers.getFormattedTime(context, comment.getComparisonDate()) +", " + comment.getAuthor().getDisplayName()) ;

        holder.attachmentsHolder.removeAllViews();
        List<Attachment> attachments = comment.getAttachments();
        if( attachments != null){
            for(final Attachment attachment : attachments){
                if(attachment.getThumbnailUrl() != null && attachment.getUrl() != null){
                    ImageView imageView = getAttachmentImageView(context);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((SubmissionListener) context).onMediaOpened(attachment.getMimeType(), attachment.getUrl(), attachment.getFilename());
                        }
                    });
                    Picasso.with(context).load(attachment.getThumbnailUrl()).into(imageView);
                    holder.attachmentsHolder.addView(imageView);
                }
            }
        }

        final MediaComment mediaComment = comment.getMedia_comment();
        if(mediaComment != null){
            final String url = APIHelpers.getFullDomain(context) + "/media_objects/" + mediaComment.getMediaId() + "/thumbnail?height=140&width=140";
            ImageView imageView = getAttachmentImageView(context);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SubmissionListener) context).onMediaOpened(mediaComment.getMimeType(), mediaComment.getUrl(), mediaComment.getFileName());
                }
            });
            Picasso.with(context).load(url).into(imageView);
            holder.attachmentsHolder.addView(imageView);
        }

        return convertView;
    }

    public static ImageView getAttachmentImageView(Context context){
        ImageView imageView = new ImageView(context);
        imageView.setMinimumHeight((int) ViewUtils.convertDipsToPixels(150, context));
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        imageView.setPadding(0,8,0,8);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAdjustViewBounds(true);
        return imageView;
    }
}
