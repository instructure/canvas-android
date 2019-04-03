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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.DiscussionEntry;
import com.instructure.canvasapi.model.DiscussionTopicHeader;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.activities.InternalWebviewActivity;
import com.instructure.speedgrader.factories.DiscussionEntryRowFactory;
import com.instructure.speedgrader.util.App;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class DiscussionSubmissionFragment extends BaseSubmissionView {

    private CanvasCallback<Submission> submissionCallback;
    private CanvasCallback<Assignment> assignmentCallback;

    private ArrayList<DiscussionEntry> discussionEntries = new ArrayList<DiscussionEntry>();
    private DiscussionEntryAdapter adapter;
    private DiscussionTopicHeader discussionTopicHeader;
    private String avatarURL = "";
    private boolean showStudentNames = true;
    // views
    private Button viewDiscussion;
    private ListView listView;

    @Override
    public int getRootLayout() {
        return R.layout.fragment_discussion_submission;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateLayout(inflater, container);

        setupViews(rootView);
        setupClickListeners();
        App applicationManager = (App) getActivity().getApplication();
        showStudentNames = applicationManager.showStudentNames();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(currentSubmission != null) {
            discussionEntries = submission.getDiscussion_entries();
            adapter = new DiscussionEntryAdapter(getActivity(), 0, discussionEntries);
            listView.setAdapter(adapter);

            setupCallbacks();
            AssignmentAPI.getAssignment(getCanvasContext().getId(), currentSubmission.getAssignment_id(), assignmentCallback);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private void setupViews(View rootView) {
        viewDiscussion = (Button)rootView.findViewById(R.id.viewDiscussion);
        listView = (ListView)rootView.findViewById(R.id.listView);
    }

    private void setupClickListeners() {
        viewDiscussion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If the user has the candroid app installed, show the discussion in that app so we don't have to totally recreate the discussions page. If the
                // user doesn't have the app installed show the webpage inside of our InternalWebView
                if(discussionTopicHeader != null) {
                    if (isCandroidAppInstalled()) {
                        String url = "canvas-courses://" + APIHelpers.getDomain(getActivity()) + getCanvasContext().toAPIString() + "/discussion_topics/" + discussionTopicHeader.getId();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } else {
                        String url = APIHelpers.getFullDomain(getActivity()) + getCanvasContext().toAPIString() + "/discussion_topics/" + discussionTopicHeader.getId();
                        startActivity(InternalWebviewActivity.createIntent(getActivity(), url, true));
                    }
                }
            }
        });

        // we don't do anything if they click it
        listView.setOnItemClickListener(null);
    }

    @Override
    public void setupCallbacks() {

        assignmentCallback = new CanvasCallback<Assignment>(this) {
            @Override
            public void cache(Assignment assignment) {
                if(assignment != null) {
                    //set the discussion topic header. We'll use this if the teacher/ta wants to see the full discussion
                    discussionTopicHeader = assignment.getDiscussionTopicHeader();
                }
            }

            @Override
            public void firstPage(Assignment assignment, LinkHeaders linkHeaders, Response response) {
                cache(assignment);
            }
        };
    }

    // Check if the user has the Candroid app installed on their device
    private boolean isCandroidAppInstalled() {
        PackageManager pm = getActivity().getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo("com.instructure.candroid", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed ;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////

    private class DiscussionEntryAdapter extends ArrayAdapter<DiscussionEntry> {


        private DiscussionEntryAdapter(Context context, int resource, List<DiscussionEntry> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getCount() {
            return discussionEntries.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return DiscussionEntryRowFactory.buildRowView(getActivity(), discussionEntries.get(position), submission.getUser().getName(), submission.getUser().getAvatarURL(), showStudentNames, convertView);
        }
    }
}
