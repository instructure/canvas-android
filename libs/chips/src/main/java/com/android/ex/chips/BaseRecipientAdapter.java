/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ex.chips;

import android.content.Context;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import com.android.ex.chips.DropdownChipLayouter.AdapterType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseRecipientAdapter extends BaseAdapter implements Filterable, RecipientManager.RecipientPhotoCallback, RecipientManager.RecipientDataCallback {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Member Variables
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Recipients Representing our suggestions list
    private List<RecipientEntry> mEntries;

    // The last search term entered.
    protected CharSequence mCurrentConstraint;

    // Manages fetching and caching recipient info and avatars.
    private RecipientManager mRecipientManager;

    private DropdownChipLayouter mDropdownChipLayouter;

    private EntriesUpdatedObserver mEntriesUpdatedObserver;

    private final Context mContext;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public BaseRecipientAdapter(Context context) {
        mContext = context;
        mRecipientManager = getRecipientManager();
    }

    public interface EntriesUpdatedObserver {
        public void onChanged(List<RecipientEntry> entries);
    }

    public abstract RecipientManager getRecipientManager();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Getters & Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public Context getContext() {
        return mContext;
    }

    public void registerUpdateObserver(EntriesUpdatedObserver observer) {
        mEntriesUpdatedObserver = observer;
    }

    public void setDropdownChipLayouter(DropdownChipLayouter dropdownChipLayouter) {
        mDropdownChipLayouter = dropdownChipLayouter;
    }

    public DropdownChipLayouter getDropdownChipLayouter() {
        return mDropdownChipLayouter;
    }

    protected List<RecipientEntry> getEntries() {
        return  mEntries;
    }

    /** Resets {@link #mEntries} and notify the event to its parent ListView. */
    protected void updateEntries(List<RecipientEntry> newEntries) {
        mEntries = newEntries;
        mEntriesUpdatedObserver.onChanged(newEntries);
        notifyDataSetChanged();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Recipient Filtering
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onNewRecipientsLoaded() {
        getFilter().filter(mCurrentConstraint);
        notifyDataSetChanged();
    }

    /** Will be called from {@link android.widget.AutoCompleteTextView} to prepare auto-complete list. */
    @Override
    public Filter getFilter() {
        return new DefaultFilter();
    }

    private static class DefaultFilterResult {
        public final List<RecipientEntry> entries;

        public DefaultFilterResult(List<RecipientEntry> entries) {
            this.entries = entries;
        }
    }

    /**
     * An asynchronous filter used for loading two data sets: email rows from the local
     * contact provider and the list of {@link android.provider.ContactsContract.Directory}'s.
     */
    private final class DefaultFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<RecipientEntry> recipients = new ArrayList<>();
            if(constraint != null && !TextUtils.isEmpty(constraint)){
                recipients = mRecipientManager.getFilteredRecipients(constraint.toString());
            }

            final FilterResults results = new FilterResults();
            results.values              = new DefaultFilterResult(recipients);
            results.count = 1;

            return results;
        }

        @Override
        protected void publishResults(final CharSequence constraint, FilterResults results) {
            mCurrentConstraint = constraint;

            if (results.values != null) {
                DefaultFilterResult defaultFilterResult = (DefaultFilterResult) results.values;
                for(RecipientEntry entry : defaultFilterResult.entries){
                    fetchPhoto(entry, BaseRecipientAdapter.this);
                }
                updateEntries(defaultFilterResult.entries);
            } else {
                updateEntries(Collections.<RecipientEntry>emptyList());
            }
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            final RecipientEntry entry  = (RecipientEntry)resultValue;
            return entry.getName();
        }
    }

    /**
     *  When a contact is typed in without selecting from our list, we want to be able to
     *  check if that user exists in our cache or through the api. If it does, we can create a chip
     *  for the user along with an avatar URL. If the user is not found, we need to indicate
     *  to the user that the recipient wasn't found.
     */
    public interface RecipientMatchCallback {
        public void matchesFound(Map<String, RecipientEntry> results);
        public void matchesNotFound(Set<String> unfoundAddresses);
    }

    /**
     * Used to replace email addresses with chips. Default behavior
     * queries the ContactsProvider for contact information about the contact.
     * Derived classes should override this method if they wish to use a
     * new data source.
     * @param inAddresses addresses to query
     * @param callback callback to return results in case of success or failure
     */
    public void getMatchingRecipients(ArrayList<RecipientEntry> inAddresses,
                                      RecipientMatchCallback callback) {
        mRecipientManager.getMatchingRecipients(inAddresses, callback);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Adapter Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int getCount() {
        final List<RecipientEntry> entries = getEntries();
        return entries != null ? entries.size() : 0;
    }

    @Override
    public RecipientEntry getItem(int position) {
        return getEntries().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RecipientEntry entry = getEntries().get(position);

        final String constraint = mCurrentConstraint == null ? null :
                mCurrentConstraint.toString();

        return mDropdownChipLayouter.bindView(convertView, parent, entry, position,
                AdapterType.BASE_RECIPIENT, constraint);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Photos
    ////////////////////////////////////////////////////////////////////////////////////////////////
    protected void fetchPhoto(final RecipientEntry entry, RecipientManager.RecipientPhotoCallback cb) {
        mRecipientManager.populatePhotoBytesAsync(entry, cb);
    }

    @Override
    public void onPhotoBytesPopulated() {
        // Default implementation does nothing
    }

    @Override
    public void onPhotoBytesAsynchronouslyPopulated() {
        notifyDataSetChanged();
    }

    @Override
    public void onPhotoBytesAsyncLoadFailed() {
        // Default implementation does nothing
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
