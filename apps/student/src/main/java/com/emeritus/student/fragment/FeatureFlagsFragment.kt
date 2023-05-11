/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.utils.FeatureFlagPref
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton
import com.emeritus.student.R
import com.emeritus.student.util.FeatureFlagPrefs
import kotlinx.android.synthetic.main.adapter_feature_flag.view.*
import kotlinx.android.synthetic.main.fragment_feature_flags.*

class FeatureFlagsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feature_flags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        recyclerView.adapter = FeatureFlagAdapter()
    }

    private fun setupToolbar() {
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }
}

private class FeatureFlagAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val flags = FeatureFlagPrefs.delegates.filterIsInstance<FeatureFlagPref>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.adapter_feature_flag, parent, false)
        return object : RecyclerView.ViewHolder(layout) {}
    }

    override fun getItemCount(): Int = flags.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val flag = flags[position]
        with (holder.itemView) {
            featureSwitch.setOnCheckedChangeListener(null)
            featureSwitch.text = flag.description
            featureSwitch.isChecked = flag.getValue(FeatureFlagPrefs, flag.property)
            featureSwitch.setOnCheckedChangeListener { _, isChecked ->
                flag.setValue(FeatureFlagPrefs, flag.property, isChecked)
            }
        }
    }
}
