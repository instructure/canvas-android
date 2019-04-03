/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */
package com.instructure.androidfoosball.views

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.IncomingData
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.Commentator
import com.instructure.androidfoosball.utils.dp
import com.instructure.androidfoosball.utils.setAvatar
import kotlinx.android.synthetic.tablet.dialog_table_requested.*
import org.jetbrains.anko.sdk21.listeners.onClick
import java.text.SimpleDateFormat
import java.util.*

class TableRequestedDialog : DialogFragment() {

    private val dateFormat = SimpleDateFormat("MMMM d 'at' h:mmaa", Locale.getDefault())

    lateinit var user: User
    lateinit var date: Date

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.dialog_table_requested, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        Glide.with(context).load("https://upload.wikimedia.org/wikipedia/commons/8/8c/Blaulicht.gif").apply {
            into(sirenOne)
            into(sirenTwo)
        }
        okayButton.onClick { dismiss() }
        userAvatar.setAvatar(user, context.resources.getDimension(R.dimen.avatar_size_small).toInt())
        userInfo.text = getString(R.string.tableRequestUserInfo, user.name, dateFormat.format(date))
    }

    override fun onResume() {
        super.onResume()
        dialog?.apply { window.setLayout(920f.dp().toInt(), ViewGroup.LayoutParams.WRAP_CONTENT) }
        App.commentator.announce(Commentator.Sfx.POLICE_SIREN.name + getString(R.string.tableRequestAnnouncement))
    }

    companion object {

        fun showIfNecessary(dataRef: DatabaseReference, snapshot: DataSnapshot?, fm: FragmentManager) {
            with (snapshot?.getValue(IncomingData::class.java) ?: return) {
                if (tableRequestUserId.isEmpty() || tableRequestTime.isEmpty()) return
                val realmUser = App.realm.where(User::class.java).equalTo("id", tableRequestUserId).findFirst()
                        ?: User(name = "Unknown User")
                val time = tableRequestTime.toLongOrNull() ?: return
                val dialog = TableRequestedDialog().apply {
                    user = realmUser
                    date = Date(time)
                }
                if (fm.findFragmentByTag(TableRequestedDialog::class.java.simpleName) == null) {
                    dialog.show(fm, TableRequestedDialog::class.java.simpleName)
                }
                dataRef.setValue(IncomingData())
            }
        }

    }

}
