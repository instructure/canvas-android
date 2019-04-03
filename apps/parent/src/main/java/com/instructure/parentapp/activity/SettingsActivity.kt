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
 *
 */
package com.instructure.parentapp.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandarecycler.decorations.SpacesItemDecoration
import com.instructure.pandarecycler.util.UpdatableSortedList
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.parentapp.R
import com.instructure.parentapp.adapter.SettingsRecyclerAdapter
import com.instructure.parentapp.dialogs.PairChildDialogFragment
import com.instructure.parentapp.factorys.SettingsPresenterFactory
import com.instructure.parentapp.holders.SettingsViewHolder
import com.instructure.parentapp.interfaces.AdapterToFragmentCallback
import com.instructure.parentapp.presenters.SettingsPresenter
import com.instructure.parentapp.util.ApplicationManager
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.RecyclerViewUtils
import com.instructure.parentapp.util.ViewUtils
import com.instructure.parentapp.viewinterface.SettingsView
import instructure.androidblueprint.PresenterFactory
import instructure.androidblueprint.SyncActivity
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.Job
import java.util.*
import kotlinx.android.synthetic.main.activity_settings.recyclerView as recyclerview

class SettingsActivity :
    SyncActivity<User, SettingsPresenter, SettingsView, SettingsViewHolder, SettingsRecyclerAdapter>(),
    SettingsView, PairChildDialogFragment.PairChildListener {

    private val recyclerAdapter: SettingsRecyclerAdapter by lazy {
        SettingsRecyclerAdapter(
            this@SettingsActivity,
            presenter,
            AdapterToFragmentCallback { student, _, _ ->
                startActivityForResult(
                    StudentDetailsActivity.createIntent(this@SettingsActivity, student),
                    com.instructure.parentapp.util.Const.STUDENT_DETAILS_REQUEST_CODE
                )
            })
    }

    private var addChildJob: Job? = null

    override fun getList(): UpdatableSortedList<User> = presenter.data

    override fun onCreate(savedInstanceState: Bundle?) {
        setResult(Activity.RESULT_CANCELED)
        // Make the status bar dark blue
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this@SettingsActivity, R.color.parent_colorPrimaryDark))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupViews()
        setupColor(ParentPrefs.currentColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        addChildJob?.cancel()
    }

    private fun setupColor(color: Int) {
        swipeRefreshLayout.setColorSchemeColors(color, color, color, color)
        emptyPandaView.progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun setupViews() {
        toolbar.setupAsBackButton { finish() }
        toolbar.setTitle(R.string.manageChildren)

        addChildFab.setOnClickListener {
            PairChildDialogFragment().show(supportFragmentManager, PairChildDialogFragment::class.java.simpleName)
        }
    }

    override fun pairChild(pairingCode: String) {
        // Hit dat sweet sweet API
        addChildJob = tryWeave {
            awaitApi<User> { UserManager.addObserveeWithPairingCode(ApiPrefs.user?.id!!, pairingCode, it) }
            onRefreshStarted()
            presenter.refresh(true)
            setResult(Activity.RESULT_OK)
        } catch {
            // Show off that snackbar
            Snackbar.make(settingsActivityRoot, R.string.addChildErrorMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) { } // The action won't show up unless there is a click listener ¯\_(ツ)_/¯
                    .setActionTextColor(ContextCompat.getColor(this, R.color.parentBlue))
                    .show()
        }
    }

    public override fun getAdapter(): SettingsRecyclerAdapter = recyclerAdapter


    fun addStudent(students: ArrayList<User>?) {
        if (students != null && !students.isEmpty()) {
            setResult(Activity.RESULT_OK)
            adapter.addAll(students)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == com.instructure.parentapp.util.Const.DOMAIN_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val students = data.getParcelableArrayListExtra<User>(Const.STUDENT)
            addStudent(students)
        } else if (requestCode == com.instructure.parentapp.util.Const.STUDENT_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val students = data.getParcelableArrayListExtra<User>(Const.STUDENT)
            addStudent(students)
        }
    }

    override fun airwolfDomain(): String {
        return ApiPrefs.airwolfDomain
    }

    override fun parentId(): String {
        return ApplicationManager.getParentId(this@SettingsActivity)
    }

    override fun hasStudent(hasStudent: Boolean) {
        // We used to finish the activity here. If we want to do something specific when there are no students this is the place.
    }

    //Sync

    override fun onReadySetGo(presenter: SettingsPresenter) {
        recyclerView.adapter = adapter
        getPresenter().loadData(false)
    }

    override fun getPresenterFactory(): PresenterFactory<SettingsPresenter> {
        return SettingsPresenterFactory()
    }

    override fun onPresenterPrepared(presenter: SettingsPresenter) {
        RecyclerViewUtils.buildRecyclerView(
            this@SettingsActivity, adapter,
            presenter, swipeRefreshLayout, recyclerView, emptyPandaView, getString(R.string.noStudentsView)
        )
        recyclerView.addItemDecoration(SpacesItemDecoration(this@SettingsActivity, R.dimen.med_padding))
        addSwipeToRefresh(swipeRefreshLayout)
        addPagination()
    }

    override fun getRecyclerView(): RecyclerView = recyclerview

    override fun perPageCount() = ApiPrefs.perPageCount

    override fun onRefreshStarted() {
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, recyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    companion object {

        @JvmStatic
        fun createIntent(context: Context, userName: String): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.putExtra(Const.NAME, userName)
            return intent
        }
    }
}
