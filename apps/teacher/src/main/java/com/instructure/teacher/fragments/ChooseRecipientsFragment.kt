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
package com.instructure.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX_RECIPIENTS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.applyHorizontalSystemBarInsets
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.teacher.R
import com.instructure.teacher.adapters.ChooseMessageRecipientRecyclerAdapter
import com.instructure.teacher.databinding.FragmentChooseRecipientsBinding
import com.instructure.teacher.databinding.RecyclerSwipeRefreshLayoutBinding
import com.instructure.teacher.events.ChooseMessageEvent
import com.instructure.teacher.factory.ChooseRecipientsPresenterFactory
import com.instructure.teacher.holders.RecipientViewHolder
import com.instructure.teacher.interfaces.RecipientAdapterCallback
import com.instructure.teacher.presenters.ChooseRecipientsPresenter
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.ChooseRecipientsView
import org.greenrobot.eventbus.EventBus
import java.util.*

@ScreenView(SCREEN_VIEW_INBOX_RECIPIENTS)
class ChooseRecipientsFragment : BaseSyncFragment<Recipient, ChooseRecipientsPresenter, ChooseRecipientsView, RecipientViewHolder, ChooseMessageRecipientRecyclerAdapter>(), ChooseRecipientsView {

    private val binding by viewBinding(FragmentChooseRecipientsBinding::bind)

    private lateinit var swipeRefreshLayoutContainerBinding: RecyclerSwipeRefreshLayoutBinding

    private var mRecyclerAdapter: ChooseMessageRecipientRecyclerAdapter? = null

    private var mRecyclerView: RecyclerView? = null

    private val mAdapterToFragmentCallback = object : RecipientAdapterCallback {

        override fun onRowClicked(recipient: Recipient, position: Int, isCheckbox: Boolean) {
            when (recipient.recipientType) {
                Recipient.Type.Group -> {
                    // If it's a group, make sure there are actually users in that group
                    if (recipient.userCount > 0) {
                        if (isCheckbox) {
                            presenter.addOrRemoveRecipient(recipient)
                            adapter.notifyItemChanged(position)
                        } else {
                            // Filter down to the group
                            if (presenter.isRecipientSelected(recipient)) {
                                showToast(R.string.entire_group_selected)
                            } else {
                                presenter.setContextRecipient(recipient)
                            }
                        }
                    } else {
                        showToast(R.string.no_users_in_group)
                    }
                }

                Recipient.Type.Metagroup -> {
                    // Always go to a metagroup - Canvas won't let you send a message to an entire metagroup
                    presenter.setContextRecipient(recipient)
                }

                Recipient.Type.Person -> {
                    // Select and deselect individuals
                    presenter.addOrRemoveRecipient(recipient)
                    adapter.notifyItemChanged(position)
                }

                else -> throw RuntimeException("Recipient type was null") // Type was null for some reason
            }
        }

        override fun isRecipientSelected(recipient: Recipient): Boolean = presenter.isRecipientSelected(recipient)

        override fun isRecipientCurrentUser(recipient: Recipient): Boolean = isSelfSelected(recipient.stringId!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutResId(), container, false)

        setupToolbar(view)
        view.findViewById<TextView>(R.id.menuDone).setTextColor(ThemePrefs.textButtonColor)

        view.findViewById<Toolbar>(R.id.toolbar).applyTopSystemBarInsets()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayoutContainerBinding = RecyclerSwipeRefreshLayoutBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
        view.applyHorizontalSystemBarInsets()

        mRecyclerView?.let { recyclerView ->
            ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(bottom = systemBars.bottom)
                insets
            }
        }
    }

    override fun layoutResId(): Int = R.layout.fragment_choose_recipients

    override fun onCreateView(view: View) {}

    private fun setupToolbar(view: View) {
        with(view.findViewById<Toolbar>(R.id.toolbar)) {
            // Set 'close' button
            setupBackButton(this@ChooseRecipientsFragment)

            // Set titles
            setTitle(R.string.select_recipients)

            // Set up menu
            inflateMenu(R.menu.menu_done_text)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menuDone -> {
                        // Send the recipient list back to the message
                        EventBus.getDefault().postSticky(ChooseMessageEvent(presenter.recipients, null))

                        // Clear the backstack because we want to go back to the message, not necessarily the previous screen
                        presenter.clearBackStack()
                        requireActivity().onBackPressed()
                    }
                }
                false
            }

            // Apply toolbar theme
            ViewStyler.themeToolbarLight(requireActivity(), this)
        }
    }

    override fun onReadySetGo(presenter: ChooseRecipientsPresenter) {
        presenter.loadData(false)
        arguments?.getParcelableArrayList<Recipient>(RECIPIENT_LIST)?.let {
            presenter.addAlreadySelectedRecipients(it)
        }
    }

    override fun getPresenterFactory() = ChooseRecipientsPresenterFactory(nonNullArgs.getString(CONTEXT_ID)!!)

    override fun onPresenterPrepared(presenter: ChooseRecipientsPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(requireActivity().window.decorView.rootView,
                requireContext(), adapter, presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.no_items_to_display_short))
    }

    override fun createAdapter(): ChooseMessageRecipientRecyclerAdapter {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = ChooseMessageRecipientRecyclerAdapter(requireContext(), presenter, mAdapterToFragmentCallback)
        }
        return mRecyclerAdapter!!
    }

    override val recyclerView: RecyclerView? = mRecyclerView

    override fun onRefreshFinished() {
        swipeRefreshLayoutContainerBinding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onRefreshStarted() {
        swipeRefreshLayoutContainerBinding.emptyPandaView.setLoading()
    }

    override fun withPagination(): Boolean = true

    override fun checkIfEmpty() = with(swipeRefreshLayoutContainerBinding) {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    private fun isSelfSelected(stringId: String): Boolean {
        try {
            if (stringId.toLong() == ApiPrefs.user!!.id) {
                return true
            }
        } catch (ignore: NumberFormatException) { }

        return false
    }

    override fun onHandleBackPressed(): Boolean = presenter.popBackStack()

    override fun perPageCount(): Int = ApiPrefs.perPageCount

    companion object {
        private const val RECIPIENT_LIST = "recipient_list"
        private const val CONTEXT_ID = "context_id"

        fun createBundle(canvasContext: CanvasContext, addedRecipients: ArrayList<Recipient>): Bundle =
                Bundle().apply {
                    putString(CONTEXT_ID, canvasContext.contextId)
                    putParcelableArrayList(RECIPIENT_LIST, addedRecipients)
                }

        fun newInstance(bundle: Bundle): ChooseRecipientsFragment =
                ChooseRecipientsFragment().apply {
                    arguments = bundle
                }
    }
}


