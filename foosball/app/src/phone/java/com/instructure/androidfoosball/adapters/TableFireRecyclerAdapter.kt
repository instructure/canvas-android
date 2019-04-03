/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.adapters

import android.content.Context
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.holders.TableViewHolder
import com.instructure.androidfoosball.models.Table
import java.lang.ref.WeakReference


class TableFireRecyclerAdapter(context: Context, ref: DatabaseReference) : FirebaseRecyclerAdapter<Table, TableViewHolder>(Table::class.java, R.layout.adapter_table, TableViewHolder::class.java, ref) {

    var currentPreferredTable: Int = -1
    var mContext: WeakReference<Context> = WeakReference(context)

    override fun parseSnapshot(snapshot: DataSnapshot?): Table {
        return super.parseSnapshot(snapshot).apply { id = snapshot?.key.orEmpty() }
    }

    override fun populateViewHolder(holder: TableViewHolder, table: Table, position: Int) {
        holder.bind(mContext.get(), table, adapterCallback = { pos ->
            notifyItemChanged(pos)
            if (currentPreferredTable != -1)
                notifyItemChanged(currentPreferredTable)
            currentPreferredTable = pos
        })
    }
}
