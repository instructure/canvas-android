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
package com.instructure.teacher.adapters

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.SpinnerAdapter

// Sourced from from http://stackoverflow.com/questions/867518/how-to-make-an-android-spinner-with-initial-text-select-one

/**
 * Decorator Adapter to allow a Spinner to show a 'Nothing Selected...' initially displayed instead of
 * the first choice in the Adapter.
 */
class NothingSelectedSpinnerAdapter constructor(
    private val adapter: SpinnerAdapter,
    private val nothingSelectedLayout: Int,
    private val nothingSelectedDropdownLayout: Int = -1,
    private val context: Context?
) : SpinnerAdapter, ListAdapter {
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // This provides the View for the Selected Item in the Spinner, not
        // the dropdown (unless dropdownView is not set).
        return if (position == 0) {
            getNothingSelectedView(parent)
        } else {
            adapter.getView(position - EXTRA, null, parent)
        }
    }

    /**
     * View to show in Spinner with Nothing Selected
     * Override this to do something dynamic... e.g. "37 Options Found"
     * @param parent
     * @return
     */
    private fun getNothingSelectedView(parent: ViewGroup?): View {
        return layoutInflater.inflate(nothingSelectedLayout, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Android BUG! http://code.google.com/p/android/issues/detail?id=17128 -
        // Spinner does not support multiple view types
        return if (position == 0) {
            if (nothingSelectedDropdownLayout == -1) View(context) else getNothingSelectedDropdownView(parent)
        } else {
            adapter.getDropDownView(position - EXTRA, null, parent)
        }
    }

    /**
     * Override this to do something dynamic... For example, "Pick your favorite of these 37".
     * @param parent
     * @return
     */
    private fun getNothingSelectedDropdownView(parent: ViewGroup?): View {
        return layoutInflater.inflate(nothingSelectedDropdownLayout, parent, false)
    }

    override fun getCount(): Int {
        val count = adapter.count
        return if (count == 0) 0 else count + EXTRA
    }

    override fun getItem(position: Int): Any? {
        return if (position == 0) null else adapter.getItem(position - EXTRA)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return if (position >= EXTRA) adapter.getItemId(
            position - EXTRA
        ) else (position - EXTRA).toLong()
    }

    override fun hasStableIds(): Boolean {
        return adapter.hasStableIds()
    }

    override fun isEmpty(): Boolean {
        return adapter.isEmpty
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        adapter.registerDataSetObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        adapter.unregisterDataSetObserver(observer)
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return if (adapter is CanvasContextSpinnerAdapter) {
            // allow for the group separator to be disabled
            position != 0 && adapter.isEnabled(position - 1) || position == 0
        } else {
            position != 0 // Don't allow the 'nothing selected'
        }
    }

    companion object {
        private const val EXTRA = 1
    }

}
