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
package com.instructure.pandautils.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.AdapterRemoteConfigParamBinding
import com.instructure.pandautils.databinding.FragmentRemoteConfigParamsBinding
import com.instructure.pandautils.utils.ToolbarSetupBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RemoteConfigParamsFragment : BaseCanvasFragment() {

    @Inject
    lateinit var toolbarSetupBehavior: ToolbarSetupBehavior

    private val binding by viewBinding(FragmentRemoteConfigParamsBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remote_config_params, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        toolbarSetupBehavior.setupToolbar(toolbar)
        recyclerView.adapter = RemoteConfigParamAdapter()
    }
}

private class RemoteConfigParamAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val params = RemoteConfigParam.values()

    // Remove the TextWatcher from the view's EditText when the ViewHolder is recycled
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        val watcher = holder.itemView.findViewById<EditText>(R.id.param_value)?.tag as? TextWatcher
        if(watcher != null) {
            holder.itemView.findViewById<EditText>(R.id.param_value).removeTextChangedListener(watcher)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.adapter_remote_config_param, parent, false)
        return object : RecyclerView.ViewHolder(layout) {}
    }

    override fun getItemCount(): Int = params.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val param = params[position]
        with (holder.itemView) {
            val paramValue = findViewById<EditText>(R.id.param_value)
            val paramName = findViewById<TextView>(R.id.param_name)
            paramName.text = param.rc_name + ": "
            paramValue.setText(RemoteConfigPrefs.getString(param.rc_name, param.safeValueAsString))
            // Save the TextWatcher in the "tag" field so that we can reference it later when we remove
            // the TextWatcher/listener from param_value when this ViewHolder is recycled.
            paramValue.tag = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    Log.d("RemoteConfigPrefs", "afterTextChanged: ${param.rc_name} = ${s.toString()}")
                    RemoteConfigPrefs.putString(param.rc_name, s.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            }
            paramValue.addTextChangedListener(paramValue.tag as TextWatcher)
        }
    }
}