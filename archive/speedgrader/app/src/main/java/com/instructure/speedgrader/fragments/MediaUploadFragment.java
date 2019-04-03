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

package com.instructure.speedgrader.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.VideoViewWithBackground;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MediaUploadFragment extends BaseSubmissionView {

    // views
    private VideoViewWithBackground videoView;

    private MediaController mediaController;

    @Override
    public int getRootLayout() {
        return R.layout.fragment_media_upload;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateLayout(inflater, container);

        setupViews(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(submission != null) {
            loadMedia(submission);
        }
    }

    public void loadMedia(Submission mediaSubmission){
        if(mediaSubmission.getMediaComment() == null){return;}

        mediaController.setAnchorView(videoView);
        Uri video = Uri.parse(mediaSubmission.getMediaComment().getUrl());
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(video);
        mediaController.setMediaPlayer(videoView);
        //this is the url for the preview of the media. We use picasso to load the image and set it as the background of the video view
        String url = APIHelpers.getFullDomain(getActivity()) + "/media_objects/" + mediaSubmission.getMediaComment().getMediaId() + "/thumbnail?height=480&width=480";

        Picasso.with(getActivity()).load(url).into(target);
    }

    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            videoView.initBackground(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    private void setupViews(final View rootView) {
        mediaController = new MediaController(getActivity());
        videoView = (VideoViewWithBackground) rootView.findViewById(R.id.videoView);
    }

    //this will notify us when the fragment is visible in the view pager and will show us the controls for the media
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if(mediaController != null) {
                mediaController.show(3000);
            }
        }else{
            if(mediaController != null){
                mediaController.hide();
            }
        }
    }

}
