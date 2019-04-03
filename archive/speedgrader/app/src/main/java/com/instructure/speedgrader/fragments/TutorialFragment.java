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

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.instructure.speedgrader.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class TutorialFragment extends Fragment {

    private TutorialException callback;
    private boolean isTablet;
    private boolean isSmallTablet;

    public interface TutorialException {
        public void forceExit_Exception(OutOfMemoryError e);
    }

    public static final String PAGE_NUMBER = "pageNumber";
    private int pageNumber = 0;
    private int[] backgroundColorResIds = new int[]{
            R.color.tutorialPage1, R.color.tutorialPage2, R.color.tutorialPage3, R.color.tutorialPage4, R.color.tutorialPage5, R.color.tutorialPage6
    };

    private int[] backgroundImageResIds_phone = new int[]{
            R.drawable.speedgrader_googleplay_7tablet_01, R.drawable.speedgrader_googleplay_7tablet_02,
            R.drawable.speedgrader_googleplay_7tablet_03, R.drawable.speedgrader_googleplay_7tablet_04,
            R.drawable.speedgrader_googleplay_7tablet_05, R.drawable.speedgrader_googleplay_7tablet_06
    };

    private int[] backgroundImageResIds_smallTablet = new int[]{
            R.drawable.speedgrader_googleplay_7tablet_01, R.drawable.speedgrader_googleplay_7tablet_02,
            R.drawable.speedgrader_googleplay_7tablet_03, R.drawable.speedgrader_googleplay_7tablet_04,
            R.drawable.speedgrader_googleplay_7tablet_05, R.drawable.speedgrader_googleplay_7tablet_06
    };

    private int[] backgroundImageResIds_tablet = new int[]{
            R.drawable.speedgrader_googleplay_10tablet_01, R.drawable.speedgrader_googleplay_10tablet_02,
            R.drawable.speedgrader_googleplay_10tablet_03, R.drawable.speedgrader_googleplay_10tablet_01,
            R.drawable.speedgrader_googleplay_10tablet_05, R.drawable.speedgrader_googleplay_10tablet_06
    };

    private int[] imageText = new int[]{
            R.string.tutorialPage1, R.string.tutorialPage2, R.string.tutorialPage3, R.string.tutorialPage4, R.string.tutorialPage5, R.string.tutorialPage6
    };

    private ImageView deviceImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(PAGE_NUMBER);
        isTablet = getResources().getBoolean(R.bool.isTablet);
        isSmallTablet = getResources().getBoolean(R.bool.isSmallTablet);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof TutorialException) {
            callback = ((TutorialException)activity);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tutorial_fragment, container, false);

        View containerView = rootView.findViewById(R.id.container);
        containerView.setBackgroundColor(getResources().getColor(backgroundColorResIds[pageNumber]));

        TextView text = (TextView) rootView.findViewById(R.id.text);
        text.setText(getString(imageText[pageNumber]));

        deviceImage = (ImageView) rootView.findViewById(R.id.deviceImage);

        try {
            if(!isTablet) {
                Picasso.with(getActivity()).load(backgroundImageResIds_phone[pageNumber]).into(deviceImage, new Callback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError() {
                        if(deviceImage.getDrawable() != null) {
                            ((BitmapDrawable)deviceImage.getDrawable()).getBitmap().recycle();
                        }
                        // try again
                        Picasso.with(getActivity()).load(backgroundImageResIds_phone[pageNumber]).into(deviceImage);
                    }
                });
            } else if(isSmallTablet) {
                Picasso.with(getActivity()).load(backgroundImageResIds_smallTablet[pageNumber]).into(deviceImage, new Callback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError() {
                        if(deviceImage.getDrawable() != null) {
                            ((BitmapDrawable)deviceImage.getDrawable()).getBitmap().recycle();
                        }
                        // try again
                        Picasso.with(getActivity()).load(backgroundImageResIds_smallTablet[pageNumber]).into(deviceImage);
                    }
                });
            } else {
                Picasso.with(getActivity()).load(backgroundImageResIds_tablet[pageNumber]).into(deviceImage, new Callback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError() {
                        if(deviceImage.getDrawable() != null) {
                            ((BitmapDrawable)deviceImage.getDrawable()).getBitmap().recycle();
                        }
                        // try again
                        Picasso.with(getActivity()).load(backgroundImageResIds_tablet[pageNumber]).into(deviceImage);
                    }
                });
            }

        } catch (OutOfMemoryError e) {
            if(callback != null) {
                callback.forceExit_Exception(e);
            }
        }
        return rootView;
    }
}
