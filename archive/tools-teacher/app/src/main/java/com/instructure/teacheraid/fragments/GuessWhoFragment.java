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
 */

package com.instructure.teacheraid.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.User;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.adapter.GuessWhoAdapter;
import com.instructure.teacheraid.util.CanvasContextColor;
import com.instructure.teacheraid.util.Const;
import com.instructure.teacheraid.util.Prefs;

import java.util.ArrayList;
import java.util.Collections;

public class GuessWhoFragment extends ParentFragment {

    public GuessWhoAdapter adapter;
    public ViewPager pager;

    private ArrayList<User> sectionPeople;
    private CanvasContext canvasContext;

    private TextView courseName;
    private TextView sectionName;
    private TextView emptyView;
    private LinearLayout tutorial;
    private ImageView swipeLeft;
    private TextView tutorialText;

    public static final String TAG = "GuessWhoFragment";


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.guess_who_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_randomize) {
            Collections.shuffle(adapter.getAllItems());
            adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.guess_who_fragment, container, false);

        pager = (ViewPager) rootView.findViewById(R.id.guessWhoViewPager);

        adapter = new GuessWhoAdapter(getActivity(), getActivity().getSupportFragmentManager(), pager);
        pager.setAdapter(adapter);

        // Necessary or the pager will only have one extra page to show
        // make this at least however many pages you can see
        pager.setOffscreenPageLimit(3);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tutorialAnimationDismiss();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setupViews(rootView);

        if(!Prefs.doesPrefExist(getActivity(), Const.GUESS_WHO_TUTORIAL)) {
            tutorialAnimations();
        }
        return rootView;
    }


    private void setupViews(View rootView) {
        courseName = (TextView) rootView.findViewById(R.id.courseName);
        sectionName = (TextView) rootView.findViewById(R.id.sectionName);
        emptyView = (TextView) rootView.findViewById(R.id.emptyView);
        tutorial = (LinearLayout) rootView.findViewById(R.id.tutorial);
        swipeLeft = (ImageView) rootView.findViewById(R.id.swipeLeft);
        Drawable d = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.back_button, getResources().getColor(R.color.white));
        swipeLeft.setImageDrawable(d);
        tutorialText = (TextView) rootView.findViewById(R.id.tutorialText);

    }
    private void tutorialAnimations() {
        tutorial.setVisibility(View.VISIBLE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = (int)getResources().getDimension(R.dimen.tutorialGuessWhoHeight);

        //make the image come in from the right and grow, make the text come up from the bottom
        ObjectAnimator inAnim = ObjectAnimator.ofFloat(swipeLeft, "x", width, width/2 - swipeLeft.getDrawable().getIntrinsicWidth()/2);
        ObjectAnimator textAnim = ObjectAnimator.ofFloat(tutorialText, "y", height, height - 100);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(swipeLeft, "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(swipeLeft, "scaleY", 0.2f, 1.0f);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(inAnim, scaleUpX, scaleUpY, textAnim);
        animSetXY.setDuration(600);
        animSetXY.start();

    }

    private void tutorialAnimationDismiss() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int height = size.y;

        //make the tutorial go down and go away
        ObjectAnimator inAnim = ObjectAnimator.ofFloat(tutorial, "y", height);
        inAnim.setDuration(400);
        inAnim.start();

        inAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tutorial.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        //save that the user has seen the tutorial
        Prefs.save(getActivity(), Const.GUESS_WHO_TUTORIAL, true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments() != null) {
            Course course = getArguments().getParcelable(Const.COURSE);
            courseName.setText(course.getName());
            canvasContext = getArguments().getParcelable(Const.SECTION);
            sectionName.setText(canvasContext.getName());
            sectionPeople = getArguments().getParcelableArrayList(Const.SECTION_PEOPLE);
            for(User user : sectionPeople) {
                adapter.addItem(user);
            }
            if(adapter.getCount() == 0) {
                //show the empty view
                pager.setVisibility(View.INVISIBLE);
                emptyView.setVisibility(View.VISIBLE);
            }
        }

        getParentActivity().setActionBarTitle(getString(R.string.guessWho));
    }
}
