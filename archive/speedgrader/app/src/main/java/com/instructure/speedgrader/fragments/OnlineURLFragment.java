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
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.activities.InternalWebviewActivity;
import com.instructure.speedgrader.util.ScaleBitmapToWrapContent;
import com.instructure.speedgrader.views.CircularProgressBar;
import com.instructure.speedgrader.views.HelveticaTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class OnlineURLFragment extends BaseSubmissionView {

    //view variables
    private ImageView previewImageHolder;
    private Button urlButton;
    private View loadingView;
    private HelveticaTextView urlLabel;
    private String url;
    private int screenWidth;


    @Override
    public int getRootLayout() {
        return R.layout.fragment_online_url;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateLayout(inflater, container);
        setupViews(rootView);

        if(currentSubmission.getAttachments() != null && currentSubmission.getAttachments().size() > 0) {
            //get the image from the server and display it
            setUrlAttachment(currentSubmission.getAttachments().get(0), currentSubmission.getUrl());
        }else{
            // No Attachment Image (display error?)
            loadingView.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void getScreenWidth(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenWidth = size.x - 32;
    }

    private void setupViews(View rootView) {
        previewImageHolder = (ImageView)    rootView.findViewById(R.id.previewImage);
        urlButton    = (Button)           rootView.findViewById(R.id.urlButton);
        urlLabel     = (HelveticaTextView)rootView.findViewById(R.id.urlLabel);
        loadingView  = rootView.findViewById(R.id.loadingLayout);
        loadingView.setVisibility(View.VISIBLE);
        //allow long presses to show context menu
        previewImageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMedia(submission.getAttachments().get(0).getMimeType(), submission.getAttachments().get(0).getUrl(), submission.getAttachments().get(0).getDisplayName());
            }
        });

        CircularProgressBar progressBar = (CircularProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.setColor(CanvasContextColor.getCachedColor(getContext(), getCanvasContext().getContextId()));
    }

    public void setUrlAttachment(Attachment attachment, String url){
        if(getActivity() == null){return;}
        getScreenWidth();
        Picasso.with(getActivity())
                .load(attachment.getUrl())
                .transform(new ScaleBitmapToWrapContent(this.screenWidth))
                .into(target);
        urlLabel.setText(getString(R.string.url) + " " + url);
        setupListeners(url);
    }

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            previewImageHolder.setImageBitmap(bitmap);
            loadingView.setVisibility(View.GONE);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    private void setupListeners(final String url) {
        urlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(InternalWebviewActivity.createIntent(getActivity(), url, false));
                getActivity().overridePendingTransition(R.anim.slide_down, 0);
            }
        });
    }
}
