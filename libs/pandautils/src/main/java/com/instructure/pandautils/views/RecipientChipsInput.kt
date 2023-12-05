/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.pandautils.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.instructure.canvasapi2.managers.RecipientManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.AdapterRecipientSearchResultBinding
import com.instructure.pandautils.databinding.ViewRecipientChipsInputBinding
import com.instructure.pandautils.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class RecipientChipsInput @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewRecipientChipsInputBinding

    var onRecipientsChanged: ((List<Recipient>) -> Unit)? = null
    var canvasContext: CanvasContext? = null
    val recipients: List<Recipient> get() = binding.chipGroup.children<RecipientChip>().map { it.recipient }

    private val searchResults: MutableList<Recipient> = mutableListOf()
    private var searchJob: Job? = null
    private var lastQuery: String = ""

    private val searchAdapter: ArrayAdapter<Recipient> = object : ArrayAdapter<Recipient>(context, 0, searchResults) {
        private val filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val result = FilterResults()
                result.values = searchResults
                result.count = searchResults.size
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val user = getItem(position)!!
            val view = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.adapter_recipient_search_result, parent, false)
            val itemBinding = AdapterRecipientSearchResultBinding.bind(view)
            ProfileUtils.loadAvatarForUser(itemBinding.recipientAvatar, user.name, user.avatarURL)
            itemBinding.recipientName.text = Pronouns.span(user.name, user.pronouns)
            return view
        }

        override fun getFilter(): Filter = filter
    }

    init {
        binding = ViewRecipientChipsInputBinding.inflate(LayoutInflater.from(context), this, true)
        if (isInEditMode) {
            addRecipients(
                listOf(
                    Recipient(stringId = "1", name = "Recipient 1", userCount = 1),
                    Recipient(stringId = "2", name = "Recipient 2", userCount = 1),
                    Recipient(stringId = "3", name = "Recipient 3", pronouns = "Pro/Noun", userCount = 1)
                )
            )
        }
        setupSearchAdapter()
    }

    fun addRecipient(recipient: Recipient) = addRecipients(listOf(recipient))

    fun addRecipients(recipients: List<Recipient>) {
        recipients.forEach {
            val chip = RecipientChip(context, it) { updateAndNotify() }
            binding.chipGroup.addView(chip)
        }
        updateAndNotify()
    }

    private fun updateAndNotify() {
        with(recipients) {
            binding.chipGroup.setVisible(isNotEmpty())
            onRecipientsChanged?.invoke(this)
        }
    }

    fun removeRecipient(recipientId: String) {
        binding.chipGroup.children<RecipientChip>()
            .find { it.recipient.stringId == recipientId }
            ?.let {
                binding.chipGroup.removeView(it)
                updateAndNotify()
            }
    }

    fun clearRecipients() {
        binding.chipGroup.removeAllViews()
        updateAndNotify()
    }

    private fun setupSearchAdapter() = with(binding) {
        searchField.onTextChanged { performSearch(it) }
        searchAdapter.setNotifyOnChange(true)
        searchField.setAdapter(searchAdapter)
        searchField.setOnItemClickListener { _, _, position, _ ->
            searchAdapter.getItem(position)?.let { addRecipient(it) }
            searchField.setText("")
        }
    }

    private fun performSearch(newText: String) {
        searchJob?.cancel()
        searchJob = weave {
            val query = newText.trim().takeIf { it.length >= 2 }.orEmpty()
            lastQuery = query
            if (query.isBlank()) {
                searchResults.clear()
                searchAdapter.notifyDataSetChanged()
            } else {
                delay(400)
                try {
                    val recipients: List<Recipient> = awaitApi {
                        RecipientManager.searchAllRecipients(false, query, canvasContext!!.contextId, it)
                    }
                    searchResults.clear()
                    searchResults.addAll(recipients - this@RecipientChipsInput.recipients)
                    searchAdapter.notifyDataSetChanged()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}

@SuppressLint("ViewConstructor")
class RecipientChip(context: Context, val recipient: Recipient, onRemoved: (String?) -> Unit) : Chip(context) {

    init {
        text = Pronouns.span(recipient.name, recipient.pronouns)
        chipIcon = ColorDrawable(Color.TRANSPARENT) // Placeholder while avatar loads
        isCloseIconVisible = true
        setOnCloseIconClickListener {
            (parent as? ChipGroup)?.removeView(this)
            onRemoved(recipient.stringId)
        }
        weave { loadAvatar() }
    }

    private suspend fun loadAvatar() {
        val placeholder = withContext(Dispatchers.IO) {
            ProfileUtils.getInitialsAvatarDrawable(context, recipient.name.orEmpty())
        }
        Glide.with(this)
            .load(recipient.avatarURL.takeUnless { ProfileUtils.shouldLoadAltAvatarImage(it) })
            .placeholder(placeholder)
            .circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    chipIcon = placeholder
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    source: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    chipIcon = resource
                    return true
                }
            })
            .preload()
    }
}
