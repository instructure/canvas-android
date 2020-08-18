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
@file:Suppress("DEPRECATION")

package com.instructure.student.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.android.ex.chips.BaseRecipientAdapter.RecipientMatchCallback
import com.android.ex.chips.RecipientEntry
import com.android.ex.chips.RecipientManager
import com.android.ex.chips.RecipientManager.RecipientDataCallback
import com.android.ex.chips.RecipientManager.RecipientPhotoCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.transition.Transition
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.RecipientManager.searchAllRecipients
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger.e
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ProfileUtils.getInitialsAvatarBitMap
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.*

/**
 * Default implementation of [RecipientManager] that fetches recipients and
 * their photos using [com.android.ex.chips.RecipientEntry]'s
 * photoThumbnailUri.
 */
class CanvasRecipientManager private constructor() : RecipientManager {
    private var recipientSuggestionsCallback: StatusCallback<List<Recipient>>? = null
    var canvasContext: CanvasContext? = null
    private var allRecipients: ArrayList<RecipientEntry>
    private var recipientCallback: RecipientDataCallback? = null
    private var mLastConstraint: String? = null
    private var handler = Handler()
    private var run: RecipientRunnable? = null

    init {
        allRecipients = ArrayList()
        loadCache()
        setUpCallback()
    }

    fun setRecipientCallback(recipientCallback: RecipientDataCallback?): CanvasRecipientManager {
        this.recipientCallback = recipientCallback
        return this
    }

    override fun getRecipients(): List<RecipientEntry> = allRecipients

    // Should be called off the UI thread (performFiltering)
    @Synchronized
    override fun getFilteredRecipients(constraint: String?): List<RecipientEntry> {
        if (constraint == null || TextUtils.isEmpty(constraint)) {
            return emptyList()
        }

        // When our API call returns, this method gets called again. If constraint != mConstraint, then we know the user
        // has typed in additional info, and another API call needs to be performed.
        if (constraint != mLastConstraint) fetchAdditionalRecipients(constraint)

        mLastConstraint = constraint
        val results: MutableList<RecipientEntry> = ArrayList()
        for (entry in allRecipients) {
            if (entry.name.contains(constraint, ignoreCase = true) && entry.isInCourseOrGroup(canvasContext!!.id)) {
                results.add(entry)
            }
        }
        return results
    }

    /**
     * Get a HashMap of address to RecipientEntry that contains all contact
     * information for a contact with the provided address, if one exists. This
     * may block the UI, so run it in an async task.
     */
    override fun getMatchingRecipients(recipients: ArrayList<RecipientEntry>, callback: RecipientMatchCallback) {
        val resultMap: MutableMap<String, RecipientEntry> = HashMap()
        for (entry in recipients) {
            if (allRecipients.contains(entry)) resultMap[entry.destination] = entry
        }
        callback.matchesFound(resultMap)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Recipient Fetching
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun notifyNewRecipientsAdded() {
        recipientCallback?.onNewRecipientsLoaded()
    }

    /**
     * Calls our API to query for possible recipients, with the mCurrentConstraint as the search parameter.
     * This process will "kill" any pending runnables. With a delay of 500ms.
     */
    private fun fetchAdditionalRecipients(constraint: String?) {
        if (run != null) {
            run!!.kill()
            handler.removeCallbacks(run)
        }
        run = RecipientRunnable(constraint)
        handler.postDelayed(run, RECIPIENTS_API_CALL_DELAY.toLong())
    }

    private fun setUpCallback() {
        recipientSuggestionsCallback = object : StatusCallback<List<Recipient>>() {
            override fun onResponse(response: Response<List<Recipient>>, linkHeaders: LinkHeaders, type: ApiType) {
                super.onResponse(response, linkHeaders, type)
                for (recipient in response.body()!!) {
                    // TODO : modify the recipient entry to display canvas course info. Currently displaying recipient course id instead of an "address"
                    val entry = RecipientEntry(
                        recipient.idAsLong,
                        recipient.name,
                        recipient.pronouns,
                        recipient.stringId,
                        "",
                        recipient.avatarURL,
                        recipient.userCount,
                        recipient.itemCount,
                        true,
                        recipient.commonCourses?.keys,
                        recipient.commonGroups?.keys
                    )
                    if (!allRecipients.contains(entry)) {
                        allRecipients.add(entry)
                    } else {
                        //replace the entry in case it was updated recently
                        allRecipients[allRecipients.indexOf(entry)] = entry
                    }
                }
                notifyNewRecipientsAdded()
            }
        }
    }

    inner class RecipientRunnable internal constructor(private val constraint: String?) : Runnable {
        private var isKilled = false
        override fun run() {
            if (!isKilled && null != constraint && !TextUtils.isEmpty(constraint) && canvasContext != null) {
                searchAllRecipients(false, constraint, canvasContext!!.contextId, recipientSuggestionsCallback!!)
            }
        }

        fun kill() {
            isKilled = true
        }

    }

    /***
     * Allow Glide to handle our image caching.
     */
    override fun populatePhotoBytesAsync(entry: RecipientEntry, callback: RecipientPhotoCallback?) {
        var photoThumbnailUri: Uri? = null
        if (entry.avatarUrl != null) {
            photoThumbnailUri = Uri.parse(entry.avatarUrl)
        }
        if (photoThumbnailUri != null) {
            if (isDefaultImage(photoThumbnailUri.toString())) {
                generateDefaultImage(entry, callback)
            } else {
                downloadImageWithGlide(entry, callback)
            }
        } else callback?.onPhotoBytesAsyncLoadFailed()
    }

    private fun downloadImageWithGlide(entry: RecipientEntry, callback: RecipientPhotoCallback?) {
        try {
            Glide.with(ContextKeeper.appContext).asBitmap().load(entry.avatarUrl).into(
                object : BaseTarget<Bitmap>() {
                    override fun onResourceReady(target: Bitmap, transition: Transition<in Bitmap>?) {
                        val stream = ByteArrayOutputStream()
                        target.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        entry.photoBytes = stream.toByteArray()
                        callback?.onPhotoBytesAsynchronouslyPopulated()
                    }

                    override fun getSize(sizeReadyCallback: SizeReadyCallback) {
                        sizeReadyCallback.onSizeReady(100, 100)
                    }

                    override fun removeCallback(sizeReadyCallback: SizeReadyCallback) {}
                }
            )
        } catch (e: Exception) {
            callback?.onPhotoBytesAsyncLoadFailed()
        }
    }

    /**
     * Helper to create generate a default avatar bitmap given a recipientEntry.
     */
    private fun generateDefaultImage(recipient: RecipientEntry, callback: RecipientPhotoCallback?) {
        try {
            val context = ContextKeeper.appContext
            val image = getInitialsAvatarBitMap(context, recipient.name)
            // Convert the generated bitmap to a byte array
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 75, stream)
            recipient.photoBytes = stream.toByteArray()
            callback?.onPhotoBytesAsynchronouslyPopulated()
        } catch (e: Exception) {
            callback?.onPhotoBytesAsyncLoadFailed()
        }
    }

    //TODO : Better way to determine if a user has an avatar set. (needs api work)
    private fun isDefaultImage(avatarUrl: String?): Boolean {
        return avatarUrl != null && (avatarUrl.contains(Const.PROFILE_URL) || avatarUrl.contains(
            com.instructure.loginapi.login.util.Const.noPictureURL
        ))
    }

    private fun loadCache() {
        ReadCacheData().execute(RECIPIENTS_CACHE)
    }

    fun clearCache() {
        FileUtils.deleteFile(ContextKeeper.appContext, RECIPIENTS_CACHE)
        allRecipients = ArrayList()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ReadCacheData : AsyncTask<String, Void, Serializable>() {
        private var path: String? = null
        override fun doInBackground(vararg params: String): Serializable? {
            path = params[0]
            try {
                return FileUtils.fileToSerializable(ContextKeeper.appContext, path)
            } catch (E: Exception) {
                e("NO CACHE: $path")
            }
            return null
        }

        @Suppress("UNCHECKED_CAST")
        override fun onPostExecute(serializable: Serializable?) {
            super.onPostExecute(serializable)
            if (serializable is ArrayList<*>) {
                allRecipients = try {
                    serializable as ArrayList<RecipientEntry>
                } catch (exception: ClassCastException) {
                    Log.d(TAG, "Unable to read cache file")
                    FileUtils.deleteFile(ContextKeeper.appContext, RECIPIENTS_CACHE)
                    ArrayList()
                }
            }
        }
    }

    companion object {
        private const val TAG = "CanvasRecipientManager"
        private const val RECIPIENTS_API_CALL_DELAY = 500
        const val RECIPIENTS_CACHE = "Recipients_Cache"
        private var instance: CanvasRecipientManager? = null
        fun getInstance(): CanvasRecipientManager {
            if (instance == null) instance = CanvasRecipientManager()
            return instance!!
        }
    }
}
