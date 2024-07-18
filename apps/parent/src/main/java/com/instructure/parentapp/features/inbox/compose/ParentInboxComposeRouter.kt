package com.instructure.parentapp.features.inbox.compose

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.inbox.compose.InboxComposeRouter
import com.instructure.parentapp.util.navigation.Navigation
import javax.inject.Inject

class ParentInboxComposeRouter(private val activity: FragmentActivity, private val navigation: Navigation): InboxComposeRouter {

    @Inject
    lateinit var navigation: Navigation
}