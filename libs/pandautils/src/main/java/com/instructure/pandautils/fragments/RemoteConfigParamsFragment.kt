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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.onTextChanged
import com.instructure.pandautils.utils.setupAsBackButton
import kotlinx.android.synthetic.main.fragment_remote_config_params.*

class RemoteConfigParamsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remote_config_params, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = RemoteConfigParamAdapter()
        toolbar.setupAsBackButton(this)
    }
}

private class RemoteConfigParamAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val params = RemoteConfigParam.values()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.adapter_remote_config_param, parent, false)
        return object : RecyclerView.ViewHolder(layout) {}
    }

    override fun getItemCount(): Int = params.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val param = params[position]
        with (holder.itemView) {
            val nameLabel = findViewById<TextView>(R.id.param_name)
            val valueEditText = findViewById<EditText>(R.id.param_value)
            nameLabel.text = param.rc_name + ": "
            valueEditText.text.insert(0, RemoteConfigPrefs.getString(param.rc_name, param.safeValueAsString))
            valueEditText.onTextChanged { newText ->
                RemoteConfigPrefs.putString(param.rc_name, newText)
            }
        }
    }

}