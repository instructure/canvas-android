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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.instructure.canvasapi.api.SubmissionAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.model.SubmissionComment;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.activities.KalturaMediaUploadPicker;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.RequestCodes;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.interfaces.OnSubmissionChangedListener;
import com.instructure.speedgrader.interfaces.SubmissionListener;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.FloatingActionMessageView;
import com.instructure.speedgrader.views.OtherCommentRow;
import com.instructure.speedgrader.views.UserCommentRow;

import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;


public class SubmissionCommentsFragment extends ParentFragment
                                        implements OnSubmissionChangedListener, FloatingActionMessageView.OnMessageActionsClickedListener{

    private Submission submission;
    private ListView listView;
    private long assignmentId;
    private boolean isGroup;
    private FloatingActionMessageView fabMessageView;
    private CanvasCallback<Submission> canvasCallbackMessage;
    private CanvasCallback<Submission> canvasCallbackSubmission;


    private BroadcastReceiver submissionCommentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(), getString(R.string.filesUploadedSuccessfully), Toast.LENGTH_SHORT).show();
            SubmissionAPI.getSubmissionWithCommentsAndHistory(getCanvasContext(), assignmentId, submission.getUser_id(), canvasCallbackSubmission);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(submissionCommentReceiver, new IntentFilter(com.instructure.pandautils.utils.Const.SUBMISSION_COMMENT_SUBMITTED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(submissionCommentReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Const.canvasContext, getCanvasContext());
        outState.putParcelable(Const.submission, submission);
        outState.putLong(Const.assignmentId, assignmentId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean retainInstanceState() {
        return false;
    }

    @Override
    public int getRootLayout() {
        return R.layout.fragment_submission_comments;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(savedInstanceState != null){
            getBundleData(savedInstanceState);
        }

        initViews();
        populateComments();

        return mRootView;
    }

    private void initViews(){
        fabMessageView = (FloatingActionMessageView) mRootView.findViewById(R.id.fabMessageView);
        fabMessageView.setListener(this)
                      .setColor(CanvasContextColor.getCachedColor(getContext(), getCanvasContext().getContextId()))
                      .build();
    }

    private void populateComments() {
        listView = (ListView) mRootView.findViewById(R.id.listView);

        listView.setAdapter(new SubmissionCommentsAdapter(getContext(), R.layout.list_item_other_comment, this.submission.getComments()));
        listView.setEmptyView(mRootView.findViewById(R.id.noItems));
        listView.setSelection(listView.getAdapter().getCount()-1);
    }

    @Override
    public void setupCallbacks() {
        canvasCallbackMessage = new CanvasCallback<Submission>(this) {
            @Override
            public void cache(Submission submission) {}

            @Override
            public void firstPage(Submission submission, LinkHeaders linkHeaders, Response response) {
                //See if it was successful.
                if(submission != null) {
                    listView.setAdapter(new SubmissionCommentsAdapter(getContext(), R.layout.list_item_other_comment, submission.getComments()));
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.errorPostingComment), Toast.LENGTH_SHORT);
                }

                // Update our activity with latest data
                if(getActivity() instanceof SubmissionListener){
                    ((SubmissionListener)getActivity()).onSubmissionCommentsUpdated(submission);
                }

                listView.setSelection(listView.getAdapter().getCount()-1);
                //enable the send message button again
                fabMessageView.resetSendActionDrawable(true);
                fabMessageView.enableSendButton(true);
                fabMessageView.clearComposeEditText();
            }

            public boolean onFailure(RetrofitError retrofitError) {
                //enable the send message button again if there was an Error
                Toast.makeText(getContext(), getString(R.string.errorPostingComment), Toast.LENGTH_SHORT);
                fabMessageView.enableSendButton(true);
                return false;
            }
        };

        // We use a NoNetworkErrorDelegate because sometimes old submissions are deleted.
        // We don't want to display unnecessary croutons.
        canvasCallbackSubmission = new CanvasCallback<Submission>(this) {
            @Override
            public void cache(Submission submission) {}

            @Override
            public void firstPage(Submission submission, LinkHeaders linkHeaders, Response response) {
                if(!isAdded()){
                    return;
                }

                // Update our activity with latest data
                if(getActivity() instanceof SubmissionListener){
                    ((SubmissionListener)getActivity()).onSubmissionCommentsUpdated(submission);
                }

                if (submission != null) {
                    listView.setAdapter(new SubmissionCommentsAdapter(getContext(), R.layout.list_item_other_comment, submission.getComments()));
                    listView.setSelection(listView.getAdapter().getCount()-1);
                }
            }
        };

    }

    private void sendMessage(String message){
        if(message.trim().length() == 0) {
            Toast.makeText(getContext(), getString(R.string.emptyMessage), Toast.LENGTH_SHORT);
        } else {
            //disable the comment button so the user can't submit the same comment multiple times.
            //It gets enabled after the api call is made
            fabMessageView.enableSendButton(false);
            SubmissionAPI.postSubmissionComment(getCanvasContext(), assignmentId, submission.getUser_id(), message, isGroup, canvasCallbackMessage);
            hideKeyboard();
        }
    }

    public void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(fabMessageView.getEditTextWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent Stuff
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void getBundleData(Bundle bundle) {
        super.getBundleData(bundle);
        this.submission = bundle.getParcelable(Const.submission);
        this.assignmentId = bundle.getLong(Const.assignmentId);
        this.isGroup = bundle.getBoolean(Const.isGroup);
    }

    public static Bundle createBundle(CanvasContext canvasContext, Submission submission, long assignmentId, boolean isGroup){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.canvasContext, canvasContext);
        bundle.putParcelable(Const.submission, submission);
        bundle.putLong(Const.assignmentId, assignmentId);
        bundle.putBoolean(Const.isGroup, isGroup);
        return bundle;
    }

    @Override
    public void onPageChanged(Assignment newAssignment, Submission submission, Attachment attachment, long attempt) {
        this.submission = submission;
        this.assignmentId = newAssignment.getId();

        // In cases where we are filtering by sections, it's possible that onPageChanged may be called before the submission comments fragment has been added. In this case, we need to only
        // set the new submission data and these views will be configured when the fragment is added.
        if(isAdded()){
            listView.setAdapter(new SubmissionCommentsAdapter(getContext(), R.layout.list_item_other_comment, this.submission.getComments()));
        }
        if(fabMessageView != null){
            fabMessageView.resetSendActionDrawable(true);
        }
    }


    @Override
    public void updateAssignmentDetails(Assignment newAssignment) {}

    ///////////////////////////////////////////////////////////////////////////
    //  OnMessageActionsClickedListener Interface overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onSendButtonClicked(String message) {
        sendMessage(message);
    }

    @Override
    public void onAttachButtonClicked() {
        Intent intent = KalturaMediaUploadPicker.createIntentForTeacherSubmissionComment(getContext(), submission.getAssignment(), submission.getUser().getId(), isGroup);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, RequestCodes.KALTURA_REQUEST);
        fabMessageView.resetSendActionDrawable(true);
    }

    @Override
    public void onMenuExpanded() {}

    @Override
    public void onMenuCollapsed() {
        hideKeyboard();
    }
    ///////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////
    public enum RowType {
        USER_COMMENT,
        OTHER_COMMENT
    }

    public interface Row {
        public View getView(View convertView);
        public int getViewType();
    }

    private class SubmissionCommentsAdapter extends ArrayAdapter<SubmissionComment> {

        private ArrayList<Row> commentRows = new ArrayList<Row>();

        private SubmissionCommentsAdapter(Context context, int resource, ArrayList<SubmissionComment> objects) {
            super(context, resource, objects);
            for(SubmissionComment comment : objects){
                long userId = APIHelpers.getCacheUser(context).getId();
                if(comment.getAuthorID() == userId){
                    commentRows.add(new UserCommentRow(LayoutInflater.from(getContext()), comment, getActivity()));
                }else{
                    commentRows.add(new OtherCommentRow(LayoutInflater.from(getContext()), comment, getActivity()));
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return RowType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            return commentRows.get(position).getViewType();
        }

        @Override
        public int getCount() {
            return commentRows.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return commentRows.get(position).getView(convertView);
        }
    }
}
