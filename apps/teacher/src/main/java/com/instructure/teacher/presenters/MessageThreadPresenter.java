/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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

package com.instructure.teacher.presenters;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.InboxApi;
import com.instructure.canvasapi2.managers.InboxManager;
import com.instructure.canvasapi2.models.BasicUser;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Message;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.teacher.R;
import com.instructure.teacher.viewinterface.MessageThreadView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import instructure.androidblueprint.SyncPresenter;
import retrofit2.Call;
import retrofit2.Response;

public class MessageThreadPresenter extends SyncPresenter<Message, MessageThreadView> {

    private Conversation mConversation;
    private int mPosition;

    private long conversationId;

    private HashMap<Long, BasicUser> mParticipants = new HashMap<>();

    private boolean mSkipCacheForRefresh = false;

    private boolean forceNetwork;

    public MessageThreadPresenter(@NonNull Conversation conversation, int position) {
        super(Message.class);
        mConversation = conversation;
        mPosition = position;
        conversationId = mConversation.getId();
    }

    // Used for when coing from a push notification and all we have is the conversation id
    public MessageThreadPresenter(long conversationId) {
        super(Message.class);
        this.conversationId = conversationId;
    }

    @Override
    public void loadData(boolean forceNetwork) {
        this.forceNetwork = forceNetwork;
        if (getData().size() == 0) {
            InboxManager.getConversation(conversationId, forceNetwork, mConversationCallback);
        } else if (getViewCallback() != null) {
            getViewCallback().onRefreshFinished();
            getViewCallback().checkIfEmpty();
        }
    }

    @Override
    public void refresh(boolean forceNetwork) {
        mSkipCacheForRefresh = true;
        onRefreshStarted();
        mConversationCallback.reset();
        clearData();
        loadData(forceNetwork);
    }

    private StatusCallback<Conversation> mConversationCallback = new StatusCallback<Conversation>() {

        @Override
        public void onResponse(@NonNull Response<Conversation> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {

            if (mConversation == null) {
                // Happens when coming from a push notification; No initial conversation object passed in, so we get it here
                mConversation = response.body();
            }

            if (mConversation.getWorkflowState() == Conversation.WorkflowState.UNREAD && forceNetwork) {
                //we need to update this for our event
                mConversation.setWorkflowState(Conversation.WorkflowState.READ);
                //we need to inform the inbox fragment to update this to read
                if (getViewCallback() != null) {
                    getViewCallback().onConversationRead(mPosition);
                }
            }

            if (getViewCallback() != null) {
                getViewCallback().setupConversationDetails();
            }

            // Skip cache if we're refreshing
            if (type == ApiType.CACHE && mSkipCacheForRefresh) {
                mSkipCacheForRefresh = false;
                return;
            }

            // Assemble list of messages
            List<Message> messages = new ArrayList<>();
            for (Message message : response.body().getMessages()) {
                appendMessages(messages, message);
            }

            // Map out conversation participants
            for (BasicUser participant : response.body().getParticipants()) {
                mParticipants.put(participant.getId(), participant);
            }

            getData().addOrUpdate(messages);
        }

        @Override
        public void onFinished(ApiType type) {
            if (getViewCallback() != null) {
                getViewCallback().onRefreshFinished();
                getViewCallback().checkIfEmpty();
            }
        }

        @Override
        public void onFail(@Nullable Call<Conversation> call, @NotNull Throwable error, @Nullable Response<?> response) {
            if (getViewCallback() != null) {
                getViewCallback().onConversationLoadFailed();
            }
        }
    };

    private void appendMessages(List<Message> list, Message message) {
        for (Message innerMessage : message.getForwardedMessages()) {
            appendMessages(list, innerMessage);
        }
        list.add(message);
    }

    @Nullable
    public BasicUser getParticipantById(long id) {
        return mParticipants.get(id);
    }

    @Nullable
    public Conversation getConversation() {
        return mConversation;
    }

    public void toggleArchived() {
        final boolean archive = mConversation.getWorkflowState() != Conversation.WorkflowState.ARCHIVED;
        InboxManager.archiveConversation(mConversation.getId(), archive, new StatusCallback<Conversation>() {
            @Override
            public void onResponse(@NonNull Response<Conversation> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if(type.isAPI()) {
                    showUserMessage(archive ? R.string.message_archived : R.string.message_unarchived);
                    if (getViewCallback() != null) {
                        if(mConversation.getWorkflowState() == Conversation.WorkflowState.ARCHIVED) {
                           mConversation.setWorkflowState(Conversation.WorkflowState.UNKNOWN);
                        } else {
                            mConversation.setWorkflowState(Conversation.WorkflowState.ARCHIVED);
                        }
                        getViewCallback().onConversationArchived(mPosition);
                        getViewCallback().refreshConversationData();
                    }
                }
            }

            @Override
            public void onFail(@Nullable Call<Conversation> call, @NonNull Throwable error, @Nullable Response<?> response) {
                showUserMessage(R.string.error_conversation_generic);
            }
        });
    }

    public void toggleStarred() {
        InboxManager.starConversation(mConversation.getId(), !mConversation.isStarred(), mConversation.getWorkflowState(), new StatusCallback<Conversation>() {
            @Override
            public void onResponse(@NonNull Response<Conversation> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if(type.isAPI()) {
                    if (getViewCallback() != null) {
                        if(mConversation.isStarred()) {
                            mConversation.setStarred(false);
                        } else {
                            mConversation.setStarred(true);
                        }
                        getViewCallback().onConversationStarred(mPosition);
                        getViewCallback().refreshConversationData();
                    }
                }
            }

            @Override
            public void onFail(@Nullable Call<Conversation> call, @NonNull Throwable error, @Nullable Response<?> response) {
                showUserMessage(R.string.error_conversation_generic);
            }
        });
    }

    public void deleteConversation() {
        InboxManager.deleteConversation(mConversation.getId(), new StatusCallback<Conversation>() {
            @Override
            public void onResponse(@NonNull Response<Conversation> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if (getViewCallback() != null && type.isAPI()) {
                    getViewCallback().onConversationDeleted(mPosition);
                }
            }

            @Override
            public void onFail(@Nullable Call<Conversation> call, @NonNull Throwable error, @Nullable Response<?> response) {
                showUserMessage(R.string.error_conversation_generic);
            }
        });
    }

    public void deleteMessage(final Message message) {
        List<Long> messageIds = new ArrayList<>(1);
        messageIds.add(message.getId());
        InboxManager.deleteMessages(mConversation.getId(), messageIds, new StatusCallback<Conversation>() {
            @Override
            public void onResponse(@NonNull Response<Conversation> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if(type.isAPI()) {
                    boolean needsUpdate = false;
                    if(getData().indexOf(message) == 0) {
                        //the top one was removed, we need to refresh the list so the reply button is on the top message
                        needsUpdate = true;
                    }
                    getData().remove(message);
                    if (getData().size() > 0) {
                        showUserMessage(R.string.message_deleted);
                        if(needsUpdate && getViewCallback() != null) {
                            getViewCallback().onMessageDeleted();
                        }
                    } else if (getViewCallback() != null) {
                        getViewCallback().onConversationDeleted(mPosition);

                    }
                }
            }

            @Override
            public void onFail(@Nullable Call<Conversation> call, @NonNull Throwable error, @Nullable Response<?> response) {
                showUserMessage(R.string.error_conversation_generic);
            }
        });
    }

    public void markConversationUnread() {
        InboxManager.markConversationAsUnread(mConversation.getId(), InboxApi.CONVERSATION_MARK_UNREAD, new StatusCallback<Void>() {
            @Override
            public void onResponse(@NonNull Response<Void> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if(response.isSuccessful()) {
                    if(getViewCallback() != null) {
                        //we need to update this item since the api returns nothing
                        mConversation.setWorkflowState(Conversation.WorkflowState.UNREAD);
                        getViewCallback().onConversationMarkedAsUnread(mPosition);
                    }
                }
            }
        });
    }
    private void showUserMessage(int userMessageResId) {
        if (getViewCallback() != null) {
            getViewCallback().showUserMessage(userMessageResId);
        }
    }

    public ArrayList<Message> getMessageChainForMessage(Message message) {
        int idx = getData().indexOf(message);
        ArrayList<Message> messageChain = new ArrayList<>();
        for (int i = idx; i >= 0; i--) {
            messageChain.add(getData().get(i));
        }
        return messageChain;
    }

    public ArrayList<BasicUser> getParticipants() {
        return new ArrayList<>(mParticipants.values());
    }

    @Override
    public int compare(Message message1, Message message2) {
        if(message1 != null && message1.getComparisonDate() != null && message2 != null && message2.getComparisonDate() != null) {
            return message2.getComparisonDate().compareTo(message1.getComparisonDate());
        }
        return super.compare(message1, message2);
    }

    @Override
    public boolean areContentsTheSame(Message oldItem, Message newItem) {
        return areItemsTheSame(oldItem, newItem);
    }

    @Override
    public boolean areItemsTheSame(Message item1, Message item2) {
        return item1.getId() == item2.getId();
    }

}
