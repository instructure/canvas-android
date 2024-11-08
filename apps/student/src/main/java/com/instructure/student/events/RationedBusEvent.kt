/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
@file:Suppress("unused")
package com.instructure.student.events

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import org.greenrobot.eventbus.EventBus

/**
 * A bus event which only allows each subscriber access to the event payload once. Designed to be
 * used as a sticky event, [RationedBusEvent] should be used in components which need to receive
 * specific events exactly once but which may not currently live in memory.
 *
 * Example: The Courses page and the All Courses page both display cached data (if available). If a
 * user opens a course from the Courses page and edits its name, both the Courses page and All
 * Courses page will need to force read from the network to avoid showing stale data. The Courses
 * page is in memory while the All Courses page is not. By sending a sticky [RationedBusEvent],
 * the Courses page can immediately refresh with new data and the All Courses page will know to
 * skip the cache the next time it is instantiated.
 *
 * @param[T] The type of payload held by this [RationedBusEvent] instance
 * @param[payload] The payload held by this [RationedBusEvent] instance
 * @param[skipId] (Optional) A subscriber ID to skip, useful when the class sending the event is
 * also subscribed to receive events of the same type but wishes to ignore this specific event.
 * Generally this ID will be the simple name of the subscribing class (e.g. [Class.getSimpleName])
 */
abstract class RationedBusEvent<out T>(protected val payload: T, skipId: String? = null) {

    /* IDs of subscribers to be skipped */
    private val skipIds = mutableSetOf<String>()

    init {
        skipId?.let { skip(it) }
    }

    /**
     * Adds a subscriberId to be skipped.
     *
     * @param[subscriberId] The subscriber ID to be skipped. Generally this ID will be the simple
     * name of the subscribing class (e.g. [Class.getSimpleName])
     */
    fun skip(subscriberId: String) {
        if (subscriberId !in skipIds) skipIds += subscriberId
    }

    /**
     * Returns the payload of this event, or null if a subscriber with the given [subscriberId]
     * has already received the payload.
     *
     * @param[subscriberId] The subscriber ID of the subscribing class. Generally this ID will be
     * the simple name of the subscribing class (e.g. [Class.getSimpleName])
     */
    fun onceOrNull(subscriberId: String): T? {
        if (subscriberId in skipIds) return null
        skipIds += subscriberId
        return payload
    }

    /**
     * Calls the provided [block] function, passing in the payload of this event, or does nothing
     * if a subscriber with the given [subscriberId] has already received the payload.
     *
     * @param[subscriberId] The subscriber ID of the subscribing class. Generally this ID will be
     * the simple name of the subscribing class (e.g. [Class.getSimpleName])
     * @param[block] A function which will receive this event's payload. This function will only be
     * called once per unique [subscriberId] per event.
     */
    fun once(subscriberId: String, block: (T) -> Unit) {
        if (subscriberId in skipIds) return
        skipIds += subscriberId
        block(payload)
    }

    /**
     * Uses the [skipIds] as a key to trigger if a payload should be returned. The opposite of the once function.
     *
     * @param[subscriberId] The subscriber ID of the subscribing class. Generally this ID will be
     * the simple name of the subscribing class (e.g. [Class.getSimpleName])
     * @param[block] A function which will receive this event's payload. This function will only be
     * called if the [subscriberId] exists in the [skipIds].
     */
    fun only(subscriberId: String, block: (T) -> Unit) {
        if (subscriberId in skipIds) block(payload) else return
    }

    /**
     * Ignores any skip ids and gives a payload.
     *
     * @param[block] A function which will receive this event's payload.
     */
    fun get(block: (T) -> Unit) {
        block(payload)
    }
}
/** Convenience function for posting this event to the EventBus */
fun RationedBusEvent<*>.postSticky() = EventBus.getDefault().postSticky(this)

/** Convenience function for posting this event to the EventBus */
fun RationedBusEvent<*>.post() = EventBus.getDefault().post(this)

/** A RationedBusEvent for a User. @see [RationedBusEvent] */
class UserUpdatedEvent(user: User, skipId: String? = null) : RationedBusEvent<User>(user, skipId)

/** A RationedBusEvent adding a new message to the MessageThreadFragment. @see [RationedBusEvent] */
class MessageAddedEvent(shouldUpdate: Boolean, skipId: String? = null) : RationedBusEvent<Boolean>(shouldUpdate, skipId)

/** A RationedBusEvent for choosing message recipients. @see [RationedBusEvent] */
class ChooseRecipientsEvent(list: List<Recipient>, skipId: String? = null) : RationedBusEvent<List<Recipient>>(list, skipId)

/** A RationedBusEvent for creating discussions. @see [RationedBusEvent] */
class DiscussionCreatedEvent(shouldUpdate: Boolean, skipId: String? = null) : RationedBusEvent<Boolean>(shouldUpdate, skipId)

/** A RationedBusEvent for updating discussions. @see [RationedBusEvent] */
class DiscussionUpdatedEvent(discussionTopicHeader: DiscussionTopicHeader, skipId: String? = null) : RationedBusEvent<DiscussionTopicHeader>(discussionTopicHeader, skipId)

/** A RationedBusEvent for Deleted DiscussionTopicHeader. @see [RationedBusEvent] */
class DiscussionTopicHeaderDeletedEvent(discussionTopicHeaderId: Long, skipId: String? = null) : RationedBusEvent<Long>(discussionTopicHeaderId, skipId)

/** A RationedBusEvent for DiscussionTopicHeader changes. @see [RationedBusEvent] */
class DiscussionTopicHeaderEvent(discussionTopicHeader: DiscussionTopicHeader, skipId: String? = null) : RationedBusEvent<DiscussionTopicHeader>(discussionTopicHeader, skipId)

/** A RationedBusEvent for updating pages. @see [RationedBusEvent] */
class PageUpdatedEvent(page: Page, skipId: String? = null) : RationedBusEvent<Page>(page, skipId)

/** A RationedBusEvent for updating modules. @see [RationedBusEvent] */
class ModuleUpdatedEvent(moduleObject: ModuleObject, skipId: String? = null) : RationedBusEvent<ModuleObject>(moduleObject, skipId)

/** A RationedBusEvent for updating the status bar color. @see [RationedBusEvent] */
class StatusBarColorChangeEvent(color: Int, skipId: String? = null) : RationedBusEvent<Int>(color, skipId)


object ShowGradesToggledEvent
object CourseColorOverlayToggledEvent
object CoreDataFinishedLoading
object ShowConfettiEvent
