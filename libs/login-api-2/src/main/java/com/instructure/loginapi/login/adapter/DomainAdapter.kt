/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.loginapi.login.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.loginapi.login.R
import java.util.*

class DomainAdapter(private val callback: DomainEvents) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    interface DomainEvents {
        fun onDomainClick(account: AccountDomain)
        fun onHelpClick()
    }

    private var originalAccounts: List<AccountDomain> = ArrayList()
    private var displayAccounts: MutableList<AccountDomain> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM -> DomainHolder(inflater.inflate(R.layout.adapter_account, parent, false))
            else -> FooterViewHolder(inflater.inflate(R.layout.adapter_account_footer, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DomainHolder) {
            val domain = displayAccounts[position]
            holder.schoolDomain.text = domain.name.validOrNull() ?: domain.domain
            holder.itemView.setOnClickListener { callback.onDomainClick(domain) }
        } else if (holder is FooterViewHolder) {
            holder.itemView.setOnClickListener { callback.onHelpClick() }
        }
    }

    override fun getItemCount(): Int = displayAccounts.size

    override fun getItemViewType(position: Int): Int {
        return if (itemCount == 1 || position != 0 && position == itemCount - 1) FOOTER else ITEM
    }

    override fun getFilter() = domainFilter

    private val domainFilter: Filter = object : Filter() {
        private fun matches(a: String, b: String) = a.contains(b, ignoreCase = true) || b.contains(a, ignoreCase = true)

        override fun performFiltering(constraint: CharSequence): FilterResults {
            val toMatch = constraint.toString().validOrNull() ?: return FilterResults()
            val matches = originalAccounts.filter { matches(it.name!!, toMatch) || matches(it.domain!!, toMatch) }
            return FilterResults().apply {
                count = matches.size
                values = matches.toMutableList()
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            displayAccounts = results.values as? MutableList<AccountDomain> ?: mutableListOf()
            if (constraint.length >= 3) displayAccounts.add(AccountDomain()) // Help Footer
            notifyDataSetChanged()
        }
    }

    fun setItems(originalAccounts: List<AccountDomain>) {
        this.originalAccounts = originalAccounts
        displayAccounts.clear()
        notifyDataSetChanged()
    }

    private class DomainHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var schoolDomain: TextView = itemView.findViewById(R.id.schoolDomain)
    }

    private class FooterViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val ITEM = 0
        private const val FOOTER = 1
    }
}
