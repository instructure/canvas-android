/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEntry;
import com.android.ex.chips.RecipientManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Recipient;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.ProfileUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

/**
 * Default implementation of {@link RecipientManager} that fetches recipients and
 * their photos using {@link com.android.ex.chips.RecipientEntry}'s
 * photoThumbnailUri.
 */
public class CanvasRecipientManager implements RecipientManager {
    private static CanvasRecipientManager instance;


    private static final String TAG = "CanvasRecipientManager";
    private static final int RECIPIENTS_API_CALL_DELAY = 500;
    public static final String RECIPIENTS_CACHE = "Recipients_Cache";

    private Context applicationContext;
    private StatusCallback<List<Recipient>> recipientSuggestionsCallback;
    private CanvasContext canvasContext;

    private ArrayList<RecipientEntry> allRecipients;

    private RecipientPhotoCallback photoCallback;
    private RecipientDataCallback recipientCallback;

    private String mLastConstraint;

    Handler handler = new Handler();
    RecipientRunnable run;

    // private constructor for singleton
    private CanvasRecipientManager(Context context) {
        this.applicationContext = context.getApplicationContext();
        this.allRecipients = new ArrayList<>();
        loadCache();
        setUpCallback();
    }

    public static CanvasRecipientManager getInstance(Context context){
        if(instance == null){
            instance = new CanvasRecipientManager(context.getApplicationContext());
        }
        return instance;
    }

    public CanvasRecipientManager setPhotoCallback(RecipientPhotoCallback photoCallback) {
        this.photoCallback = photoCallback;
        return this;
    }

    public CanvasRecipientManager setRecipientCallback(RecipientDataCallback recipientCallback) {
        this.recipientCallback = recipientCallback;
        return this;
    }

    public Context getContext() {
        return this.applicationContext;
    }

    @Override
    public List<RecipientEntry> getRecipients() {
        return allRecipients;
    }

    // Should be called off the UI thread (performFiltering)
    @Override
    public synchronized List<RecipientEntry> getFilteredRecipients(String constraint){
        if(constraint == null || TextUtils.isEmpty(constraint)){
            return Collections.emptyList();
        }

         // When our API call returns, this method gets called again. If constraint != mConstraint, then we know the user
         // has typed in additional info, and another API call needs to be performed.
        if(!constraint.equals(mLastConstraint)){
            fetchAdditionalRecipients(constraint);
        }
        mLastConstraint = constraint;

        List<RecipientEntry> results = new ArrayList<>();
        for(RecipientEntry entry : allRecipients){
            if(entry.getName().toLowerCase().contains(constraint.toLowerCase()) && entry.isInCourseOrGroup(canvasContext.getId())){
                results.add(entry);
            }
        }

        return results;
    }

    /**
     * Get a HashMap of address to RecipientEntry that contains all contact
     * information for a contact with the provided address, if one exists. This
     * may block the UI, so run it in an async task.
     */
    public void getMatchingRecipients(ArrayList<RecipientEntry> recipients, BaseRecipientAdapter.RecipientMatchCallback callback){
        Map<String, RecipientEntry> resultMap = new HashMap<>();
        for(RecipientEntry entry : recipients){
            if(allRecipients.contains(entry)){
                resultMap.put(entry.getDestination(), entry);
            }
        }
        callback.matchesFound(resultMap);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Recipient Fetching
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void notifyNewRecipientsAdded(){
        if(recipientCallback != null){
            recipientCallback.onNewRecipientsLoaded();
        }
    }

    /**
     *  Calls our API to query for possible recipients, with the mCurrentConstraint as the search parameter.
     *  This process will "kill" any pending runnables. With a delay of 500ms.
     */
    private void fetchAdditionalRecipients(String constraint){
        if(run != null){
            run.kill();
            handler.removeCallbacks(run);
        }
        run = new RecipientRunnable(constraint);
        handler.postDelayed(run, RECIPIENTS_API_CALL_DELAY);
    }

    private void setUpCallback(){
        recipientSuggestionsCallback = new StatusCallback<List<Recipient>>() {

            @Override
            public void onResponse(@NonNull Response<List<Recipient>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                for(Recipient recipient : response.body()){
                    // TODO : modify the recipient entry to display canvas course info. Currently displaying recipient course id instead of an "address"
                    RecipientEntry entry = new RecipientEntry(recipient.getIdAsLong(), recipient.getName(), recipient.getStringId(), "", recipient.getAvatarURL(), recipient.getUserCount(), recipient.getItemCount(), true,
                            recipient.getCommonCourses() != null ? recipient.getCommonCourses().keySet() : null,
                            recipient.getCommonGroups() != null ?  recipient.getCommonGroups().keySet() : null);
                    if(!allRecipients.contains(entry)){
                        allRecipients.add(entry);
                    }
                    else{
                        //replace the entry in case it was updated recently
                        allRecipients.set(allRecipients.indexOf(entry), entry);
                    }
                }
                notifyNewRecipientsAdded();
            }
        };
    }

    public class RecipientRunnable implements Runnable{
        private boolean isKilled = false;
        private String constraint = "";
        RecipientRunnable(String constraint){
            this.constraint = constraint;
        }

        @Override
        public void run() {
            if(!isKilled && null != constraint && !TextUtils.isEmpty(constraint) && canvasContext != null){
                com.instructure.canvasapi2.managers.RecipientManager.searchRecipients(constraint, canvasContext.getContextId(), recipientSuggestionsCallback);
            }
        }

        final public void kill(){
            isKilled = true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Photos
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void populatePhotoBytesAsync(final RecipientEntry entry) {
        populatePhotoBytesAsync(entry, photoCallback);
    }

    /***
     * Allow Glide to handle our image caching.
     * @param entry
     * @param callback
     */
    @Override
    public void populatePhotoBytesAsync(final RecipientEntry entry, final RecipientPhotoCallback callback) {
        if(ProfileUtils.shouldLoadAltAvatarImage(entry.getAvatarUrl())) {
            Bitmap avatar = ProfileUtils.getInitialsAvatarBitMap(getContext(), entry.getName());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            avatar.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            entry.setPhotoBytes(stream.toByteArray());
            if(callback != null){
                callback.onPhotoBytesAsynchronouslyPopulated();
            }
        } else {
            final Uri photoThumbnailUri = Uri.parse(entry.getAvatarUrl());

            if (photoThumbnailUri != null) {
                if (isDefaultImage(photoThumbnailUri.toString())) {
                    generateDefaultImage(entry, callback);
                } else {
                    downloadImageWithGlide(entry, callback);
                }
            } else if (callback != null) {
                callback.onPhotoBytesAsyncLoadFailed();
            }
        }
    }

    private void downloadImageWithGlide(final RecipientEntry entry, final RecipientPhotoCallback callback) {

        Picasso.with(getContext()).load(entry.getAvatarUrl()).resize(150, 150).centerCrop().into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                entry.setPhotoBytes(stream.toByteArray());
                if(callback != null){
                    callback.onPhotoBytesAsynchronouslyPopulated();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if(callback != null) {
                    callback.onPhotoBytesAsyncLoadFailed();
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    /**
     * Helper to create generate a default avatar bitmap given a recipientEntry.
     * @param callback
     * @return
     */
    public void generateDefaultImage(RecipientEntry recipient, final RecipientPhotoCallback callback) {
        try{
            Bitmap image = ProfileUtils.getInitialsAvatarBitMap(getContext(), recipient.getName());
            // Convert the generated bitmap to a bytearray
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 75, stream);
            byte[] byteArray = stream.toByteArray();
            recipient.setPhotoBytes(byteArray);
            if(callback != null){
                callback.onPhotoBytesAsynchronouslyPopulated();
            }
        }catch (Exception e){
            callback.onPhotoBytesAsyncLoadFailed();
        }
    }

    //TODO : Better way to determine if a user has an avatar set. (needs api work)
    private boolean isDefaultImage(String avatarUrl){
        return avatarUrl != null && (avatarUrl.contains(Const.PROFILE_URL) || avatarUrl.contains(Const.NO_PICTURE_URL));
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Caching
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void clearCache() {
        FileUtils.DeleteFile(getContext(),RECIPIENTS_CACHE);
        allRecipients = new ArrayList<>();
    }

    private void loadCache(){
        new ReadCacheData().execute(RECIPIENTS_CACHE);
    }

    private class ReadCacheData extends AsyncTask<String, Void, Serializable> {
        private String path = null;

        @Override
        protected Serializable doInBackground(String... params) {
            path = params[0];
            try {
                return FileUtils.FileToSerializable(getContext(), path);
            } catch (Exception E) {
                Logger.e("NO CACHE: " + path);
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Serializable serializable) {
            super.onPostExecute(serializable);
            if (serializable != null && getContext() != null && serializable instanceof ArrayList) {
                try{
                    allRecipients = (ArrayList) serializable;
                }catch (ClassCastException exception){
                    Log.d(TAG, "Unable to read cache file");
                    FileUtils.DeleteFile(getContext(),RECIPIENTS_CACHE);
                    allRecipients = new ArrayList<>();
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Getter & Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    public void setCanvasContext(CanvasContext canvasContext) {
        fetchAdditionalRecipients(mLastConstraint);
        this.canvasContext = canvasContext;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
