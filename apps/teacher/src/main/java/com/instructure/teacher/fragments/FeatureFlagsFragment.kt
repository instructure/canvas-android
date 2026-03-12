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
package com.instructure.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.utils.FeatureFlagPref
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterFeatureFlagBinding
import com.instructure.teacher.databinding.FragmentFeatureFlagsBinding
import com.instructure.teacher.utils.FeatureFlags

class FeatureFlagsFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentFeatureFlagsBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feature_flags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        binding.recyclerView.adapter = FeatureFlagAdapter()

        binding.toolbar.applyTopSystemBarInsets()

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        if (!isTablet) binding.toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }
}

private class FeatureFlagAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val flags = FeatureFlags.delegates.filterIsInstance<FeatureFlagPref>()

    private lateinit var binding: AdapterFeatureFlagBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = AdapterFeatureFlagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return object : RecyclerView.ViewHolder(binding.root) {}
    }

    override fun getItemCount(): Int = flags.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val flag = flags[position]
        with(binding) {
            featureSwitch.setOnCheckedChangeListener(null)
            featureSwitch.text = flag.description
            featureSwitch.isChecked = flag.getValue(FeatureFlags, flag.property)
            featureSwitch.setOnCheckedChangeListener { _, isChecked ->
                flag.setValue(FeatureFlags, flag.property, isChecked)
            }
        }
    }
}
