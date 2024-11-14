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
package com.instructure.student.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.managers.BookmarkManager
import com.instructure.canvasapi2.models.Bookmark
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.pandautils.analytics.SCREEN_VIEW_BOOKMARK_CREATION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.blueprint.BaseCanvasAppCompatDialogFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isCourseOrGroup
import com.instructure.student.R
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.Analytics
import com.instructure.student.util.CacheControlFlags
import kotlinx.coroutines.Job

@ScreenView(SCREEN_VIEW_BOOKMARK_CREATION)
class BookmarkCreationDialog : BaseCanvasAppCompatDialogFragment() {
    private var bookmarkJob: Job? = null
    private var bookmarkEditText: AppCompatEditText? = null

    init {
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = View.inflate(ContextThemeWrapper(activity, 0), R.layout.dialog_bookmark, null)
        setupViews(view)
        builder.setView(view)
        builder.setTitle(R.string.addBookmark)
        builder.setCancelable(true)
        builder.setPositiveButton(R.string.save, null)
        builder.setNegativeButton(android.R.string.cancel, null)
        val buttonColor = arguments?.getParcelable<CanvasContext>(BOOKMARK_CANVAS_CONTEXT)?.color ?: ThemePrefs.brandColor
        val dialog = builder.create()
        dialog.setOnShowListener { _ ->
            val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.setTextColor(buttonColor)
            positiveButton.setOnClickListener {
                closeKeyboard()
                saveBookmark()
            }
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(buttonColor)
        }
        return dialog
    }

    private fun setupViews(view: View) {
        bookmarkEditText = view.findViewById(R.id.bookmarkEditText)
        bookmarkEditText?.let {
            ViewStyler.themeEditText(
                requireContext(), it,
                arguments?.getParcelable<CanvasContext>(BOOKMARK_CANVAS_CONTEXT)?.color ?: ThemePrefs.brandColor
            )
            it.setText(arguments?.getString(BOOKMARK_LABEL, "").orEmpty())
            it.setSelection(it.text?.length ?: 0)
        }
    }

    private fun saveBookmark() {
        val label = bookmarkEditText!!.text.toString()
        if(label.isBlank()) {
            Toast.makeText(activity, R.string.bookmarkTitleRequired, Toast.LENGTH_SHORT).show()
            return
        }

        bookmarkJob = tryWeave {
            awaitApi<Bookmark> { BookmarkManager.createBookmark(Bookmark(name = label, url = arguments?.getString(BOOKMARK_URL), position = 0), it) }
            Analytics.trackBookmarkCreated(activity)
            Toast.makeText(activity, R.string.bookmarkAddedSuccess, Toast.LENGTH_SHORT).show()
            CacheControlFlags.forceRefreshBookmarks = true
            dismiss()
        } catch {
            Toast.makeText(context, R.string.bookmarkAddedFailure, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    // TODO: Move this to an extension
    private fun closeKeyboard() {
        // Close the keyboard
        val inputManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        if(bookmarkEditText != null) {
            inputManager?.hideSoftInputFromWindow(bookmarkEditText!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun onDestroyView() {
        val dialog = dialog
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) dialog.setDismissMessage(null)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        bookmarkJob?.cancel()
    }


    companion object {
        private const val BOOKMARK_CANVAS_CONTEXT = "bookmarkCanvasContext"
        private const val BOOKMARK_URL = "bookmarkUrl"
        private const val BOOKMARK_LABEL = "bookmarkLabel"

        private fun newInstance(canvasContext: CanvasContext, bookmarkUrl: String, label: String? = ""): BookmarkCreationDialog {
            val dialog = BookmarkCreationDialog()
            val args = Bundle()
            args.putParcelable(BOOKMARK_CANVAS_CONTEXT, canvasContext)
            args.putString(BOOKMARK_URL, bookmarkUrl)
            args.putString(BOOKMARK_LABEL, label)
            dialog.arguments = args
            return dialog
        }

        fun <F> newInstance(context: Activity, topFragment: F?, peakingFragment: F?): BookmarkCreationDialog? where F : Fragment {
            if(topFragment is Bookmarkable && peakingFragment is Bookmarkable && topFragment.bookmark.canBookmark && peakingFragment.bookmark.canBookmark) {
                val bookmark = topFragment.bookmark
                if(bookmark.canvasContext?.isCourseOrGroup == true) {

                    var bookmarkUrl = RouteMatcher.generateUrl(bookmark.url, bookmark.getQueryParamForBookmark)
                    if(bookmarkUrl.isNullOrBlank()) {
                        bookmarkUrl = RouteMatcher.generateUrl(
                                bookmark.canvasContext!!.type,
                                peakingFragment::class.java,
                                topFragment::class.java,
                                bookmark.getParamForBookmark,
                                bookmark.getQueryParamForBookmark)
                    }

                    Analytics.trackBookmarkSelected(context, peakingFragment::class.java.simpleName + " " + topFragment::class.java.simpleName)

                    if(bookmarkUrl != null) {
                        Analytics.trackButtonPressed(context, "Add bookmark to fragment", null)
                        return newInstance(bookmark.canvasContext!!, bookmarkUrl, null)
                    }
                }
            } else if(topFragment is Bookmarkable && topFragment.bookmark.canBookmark) {
                val bookmark = topFragment.bookmark
                if (bookmark.canvasContext?.isCourseOrGroup == true) {
                    var bookmarkUrl = RouteMatcher.generateUrl(bookmark.url, bookmark.getQueryParamForBookmark)
                    if(bookmarkUrl.isNullOrBlank()) {
                        bookmarkUrl = RouteMatcher.generateUrl(bookmark.canvasContext!!.type, topFragment::class.java, topFragment.bookmark.getParamForBookmark)
                    }

                    Analytics.trackBookmarkSelected(context, topFragment::class.java.simpleName)

                    if(bookmarkUrl != null) {
                        Analytics.trackButtonPressed(context, "Add bookmark to fragment", null)
                        return newInstance(bookmark.canvasContext!!, bookmarkUrl, null)
                    }
                }
            }

            return null
        }
    }
}
