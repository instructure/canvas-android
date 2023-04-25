package com.instructure.interactions.bookmarks

import android.os.Parcelable
import com.instructure.canvasapi2.models.*
import com.instructure.interactions.router.RouterParams
import kotlinx.parcelize.Parcelize
import java.util.HashMap

@Parcelize
data class Bookmarker(
    var canBookmark: Boolean,
    val canvasContext: CanvasContext?,
    var url: String? = null, // Set this if the url exists and one does not need to be generated
    val getParamForBookmark : HashMap<String, String> = HashMap(),
    val getQueryParamForBookmark: HashMap<String, String> = HashMap()
) : Parcelable {

    init {
        if (canvasContext is Course || canvasContext is Group)
            getParamForBookmark[RouterParams.COURSE_ID] = canvasContext.id.toString()
        else if (canvasContext is User)
            getParamForBookmark[RouterParams.USER_ID] = canvasContext.id.toString()
    }

    fun withParam(key: String, value: String): Bookmarker {
        getParamForBookmark[key] = value
        return this
    }

    fun withParams(params: HashMap<String, String>): Bookmarker {
        params.forEach { key, value ->
            getParamForBookmark[key] = value
        }
        return this
    }

    fun withQueryParam(key: String, value: String): Bookmarker {
        getQueryParamForBookmark[key] = value
        return this
    }

    fun withQueryParams(params: HashMap<String, String>): Bookmarker {
        params.forEach { key, value ->
            getQueryParamForBookmark[key] = value
        }
        return this
    }

    fun withUrl(url: String?): Bookmarker {
        this.url = url
        return this
    }
}