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

import android.content.DialogInterface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.adapter.MyPagerAdapter;
import com.instructure.teacheraid.dialogs.StudentChooserSettingsDialog;
import com.instructure.teacheraid.util.Const;
import com.instructure.teacheraid.util.Prefs;
import com.instructure.teacheraid.views.CircleButton;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import retrofit.client.Response;

public class StudentChooserFragment extends ParentFragment implements DialogInterface.OnDismissListener {

    public static final String TAG = "StudentChooserFragment";

    private CanvasCallback<User[]> peopleCallback;

    public MyPagerAdapter adapter;
    public ViewPager pager;
    private CircleButton randomize;

    private TextView emptyView;
    private TextView courseName;
    private CardView tutorial;

    private boolean shouldRemoveStudent = true;
    private boolean shouldReadName = true;
    private boolean hasSelected = false;
    private boolean hasTutorialBeenViewed = false;

    private ArrayList<User> sectionPeople;


    public ViewPager getPager() {
        return pager;
    }

    private TextToSpeech tts;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.student_chooser_fragment, container, false);

        setupViews(rootView);
        setupClickListeners();
        setupCallbacks();

        if(savedInstanceState != null && savedInstanceState.getBoolean(Const.HOT_SEAT_TUTORIAL_VIEWED)) {
            tutorialViewed();
        }
        return rootView;
    }

    private void setupViews(View rootView) {
        pager = (ViewPager) rootView.findViewById(R.id.myviewpager);

        adapter = new MyPagerAdapter(getActivity(), getActivity().getSupportFragmentManager());
        pager.setAdapter(adapter);

        // Necessary or the pager will only have one extra page to show
        // make this at least however many pages you can see
        pager.setOffscreenPageLimit(2);

        pager.setPageMargin(100);

        randomize = (CircleButton) rootView.findViewById(R.id.randomize);

        emptyView = (TextView) rootView.findViewById(R.id.emptyView);

        courseName = (TextView) rootView.findViewById(R.id.courseName);

        tutorial = (CardView) rootView.findViewById(R.id.tutorial);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments() != null) {
            Course course = getArguments().getParcelable(Const.COURSE);
            courseName.setText(course.getName());

            sectionPeople = getArguments().getParcelableArrayList(Const.SECTION_PEOPLE);
            for(User user : sectionPeople) {
                adapter.addItem(user);
            }

            if(adapter.getCount() == 0) {
                showEmptyView();
                emptyView.setText(getString(R.string.noStudents));
            }
            getParentActivity().setActionBarTitle(getString(R.string.studentChooser));

        }

        getCheckedState();

        //initialize the text to speech engine
        tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        tts.setLanguage(Locale.getDefault());
                    }
                } else {
                    Log.e("error", "Initilization Failed!");
                }
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //we don't want to show that first tutorial screen again when the user rotates
        outState.putBoolean(Const.HOT_SEAT_TUTORIAL_VIEWED, hasTutorialBeenViewed);
        super.onSaveInstanceState(outState);
    }

    private void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        pager.setVisibility(View.GONE);
        tutorial.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.student_chooser, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                StudentChooserSettingsDialog dialog = new StudentChooserSettingsDialog();
                dialog.setOnDismissListener(this);
                dialog.show(getFragmentManager(), StudentChooserSettingsDialog.TAG);

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {


        //Close the Text to Speech Library
        if(tts != null) {

            tts.stop();
            tts.shutdown();
            Log.d(TAG, "TTS Destroyed");
        }
        super.onDestroy();
    }

    private void getCheckedState() {
        shouldReadName = Prefs.load(getActivity(), Const.SHOULD_READ_NAME, true);
        shouldRemoveStudent = Prefs.load(getActivity(), Const.SHOULD_REMOVE_STUDENT, true);

    }
    private void tutorialViewed() {
        hasTutorialBeenViewed = true;
        tutorial.setVisibility(View.GONE);
        pager.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        randomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(tutorial.getVisibility() == View.VISIBLE) {
                    tutorialViewed();
                }
                //if the user has selected to remove the current student remove the one that has been previously selected
                if (adapter.getCount() > 0 && shouldRemoveStudent && hasSelected) {
                    int currentSelected = pager.getCurrentItem();
                    adapter.removeItemAtIndex(currentSelected);
                }
                hasSelected = true;
                int size = adapter.getCount();

                if(size > 0) {
                    Random r = new Random();
                    final int newItem = r.nextInt(size);

                    pager.setCurrentItem(newItem, true);

                    if(tts != null && shouldReadName) {
                        tts.speak(adapter.getUser(newItem).getName(), TextToSpeech.QUEUE_ADD, null);
                    }
                } else {
                    showEmptyView();
                }


            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////
    //  Callbacks
    ////////////////////////////////////////////////////////////////////////////////

    private void setupCallbacks() {
        peopleCallback = new CanvasCallback<User[]>(this) {
            @Override
            public void cache(User[] users, LinkHeaders linkHeaders, Response response) {

            }

            @Override
            public void firstPage(User[] users, LinkHeaders linkHeaders, Response response) {
                for(User user : users) {
                    adapter.addItem(user);
                }

                if(linkHeaders.nextURL != null) {
                    UserAPI.getNextPagePeople(linkHeaders.nextURL, peopleCallback);
                }
            }
        };
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        getCheckedState();
    }
}
