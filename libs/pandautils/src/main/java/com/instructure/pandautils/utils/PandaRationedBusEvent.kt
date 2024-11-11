package com.instructure.pandautils.utils

import android.content.Intent
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject

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
abstract class PandaRationedBusEvent<out T>(protected val payload: T, skipId: String? = null) {

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

fun PandaRationedBusEvent<*>.remove() = org.greenrobot.eventbus.EventBus.getDefault().removeStickyEvent(this)

/** Convenience function for posting this event to the EventBus */
fun PandaRationedBusEvent<*>.postSticky() = org.greenrobot.eventbus.EventBus.getDefault().postSticky(this)

/** Convenience function for posting this event to the EventBus */
fun PandaRationedBusEvent<*>.post() = org.greenrobot.eventbus.EventBus.getDefault().post(this)

class FilesSelected(fileSubmitObjects: List<FileSubmitObject>) : PandaRationedBusEvent<List<FileSubmitObject>>(fileSubmitObjects)
class QuizFileUploadStarted(quizData: Pair<Long, Int>) : PandaRationedBusEvent<Pair<Long, Int>>(quizData)

class ActivityResult(val requestCode: Int, val resultCode: Int, val data: Intent?)
class OnActivityResults(results: ActivityResult, skipId: String? = null) : PandaRationedBusEvent<ActivityResult>(results, skipId)

class FileUploadNotification(val intent: Intent?, val attachments: List<Attachment>)
class FileUploadEvent(fileNotification: FileUploadNotification) : PandaRationedBusEvent<FileUploadNotification>(fileNotification)
class DiscussionEntryEvent(entryId: Long, val topLevelReplyPosted: Boolean = false) : PandaRationedBusEvent<Long>(entryId)

class OnBackStackChangedEvent(clazz: Class<*>?) : PandaRationedBusEvent<Class<*>?>(clazz)

class FileFolderDeletedEvent(val deletedFileFolder: FileFolder, skipId: String? = null) : PandaRationedBusEvent<FileFolder>(deletedFileFolder, skipId)
class FileFolderUpdatedEvent(val updatedFileFolder: FileFolder, skipId: String? = null) : PandaRationedBusEvent<FileFolder>(updatedFileFolder, skipId)

class ConversationUpdatedEvent(conversation: Conversation, skipId: String? = null) : PandaRationedBusEvent<Conversation>(conversation, skipId)
