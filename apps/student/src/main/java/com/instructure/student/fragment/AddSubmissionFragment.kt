/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.ExternalToolManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.activities.NotoriousMediaUploadPicker
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.mobius.common.ui.SubmissionService
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.Analytics
import com.instructure.student.util.VisibilityAnimator
import kotlinx.android.synthetic.main.fragment_add_submission.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Response
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class AddSubmissionFragment : ParentFragment(), Bookmarkable {

    // Passed In Assignment and course
    private var assignment: Assignment by ParcelableArg(key = ASSIGNMENT)
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    // Assignment Permissions **** These names must match their counterparts in Const (used for Bundle keys)
    private var isOnlineTextAllowed: Boolean by BooleanArg()
    private var isUrlEntryAllowed: Boolean by BooleanArg()
    private var isFileUploadAllowed: Boolean by BooleanArg()
    private var isMediaUploadAllowed: Boolean by BooleanArg()

    private var isFileUploadCanceled = false

    private lateinit var canvasCallbackSubmission: StatusCallback<Submission>
    private lateinit var ltiToolCallback: StatusCallback<List<LTITool>>

    private var arcLTITool: LTITool? = null

    private var delayLoadJob: Job? = null
    private var delayPostJob: Job? = null

    /*
	 * Useful for 2 reasons.
	 * 1.) The WebView won't load without a scheme
	 * 2.) The API automatically puts http or https at the front if it's not there anyways.
	 */
    private val httpURLSubmission: String
        get() {
            val url = onlineURL.text.toString()
            return if (!url.startsWith("http://") && !url.startsWith("https://"))
                "http://" + url
            else
                url
        }

    private val mArcSubmissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val extras = intent?.extras
            val url = extras?.getString(Const.URL)
            arcEntryContainer!!.visibility = View.VISIBLE
            arcEntry!!.setText(url)
        }
    }

    //region Lifecycle Callbacks
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpCallbacks()
        setupViews()
        setupListeners()

        // If file entry is allowed check to see if the account has arc installed
        if (isFileUploadAllowed) {
            ExternalToolManager.getExternalToolsForCanvasContext(canvasContext, ltiToolCallback, true)
        }

        // If only file is allowed, move to the next page (or go back if we are coming from file uploads).
        if (!isOnlineTextAllowed && !isUrlEntryAllowed) {
            if (isMediaUploadAllowed && !isFileUploadAllowed) {
                val intent = NotoriousMediaUploadPicker.createIntentForAssignmentSubmission(requireContext(), assignment)
                startActivityForResult(intent, RequestCodes.NOTORIOUS_REQUEST)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = layoutInflater.inflate(R.layout.fragment_add_submission, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Styling
        ViewStyler.themeButton(submitTextEntry)
        ViewStyler.themeButton(submitURLEntry)
        ViewStyler.themeButton(submitArcEntry)

        ViewStyler.themeEditText(requireContext(), textEntry, ThemePrefs.brandColor)
        ViewStyler.themeEditText(requireContext(), onlineURL, ThemePrefs.brandColor)
        ViewStyler.themeEditText(requireContext(), arcEntry, ThemePrefs.brandColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        delayPostJob?.cancel()
        delayLoadJob?.cancel()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mArcSubmissionReceiver, IntentFilter(Const.ARC_SUBMISSION))
    }

    override fun onResume() {
        super.onResume()
        if (dataLossResume(textEntry, Const.DATA_LOSS_ADD_SUBMISSION)) {
            if (isOnlineTextAllowed) {
                textEntryHeader.performClick()
            }
        }

        if (dataLossResume(onlineURL, Const.DATA_LOSS_ADD_SUBMISSION_URL)) {
            if (isUrlEntryAllowed) {
                onlineURLHeader.performClick()
            }
        }
        dataLossAddTextWatcher(textEntry, Const.DATA_LOSS_ADD_SUBMISSION)
        dataLossAddTextWatcher(onlineURL, Const.DATA_LOSS_ADD_SUBMISSION_URL)
    }

    override fun onPause() {
        dataLossPause(textEntry, Const.DATA_LOSS_ADD_SUBMISSION)
        dataLossPause(onlineURL, Const.DATA_LOSS_ADD_SUBMISSION_URL)
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mArcSubmissionReceiver)
    }
    //endregion

    //region Setup
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupViews() {
        webView.settings.javaScriptEnabled = true

        // Hide arc until we can figure out if arc is installed for the course
        arcSubmission.setGone()

        // Hide text if it's not allowed.
        textEntryHeader.setVisible(isOnlineTextAllowed)

        // Hide url if it's not allowed.
        onlineURLHeader.setVisible(isUrlEntryAllowed)

        // Hide file if it's not allowed.
        fileUpload.setVisible(isFileUploadAllowed)

        // Hide media submission UI if media recording is not allowed
        mediaSubmission.setVisible(isMediaUploadAllowed)

        // If only text is allowed, open the tab.
        if (isOnlineTextAllowed && !isUrlEntryAllowed && !isFileUploadAllowed) {
            VisibilityAnimator.animateVisible(AnimationUtils.loadAnimation(requireActivity(),
                    R.anim.slow_push_left_in), textEntryContainer)
        }

        // If only url is allowed, open the tab.
        if (!isOnlineTextAllowed && isUrlEntryAllowed && !isFileUploadAllowed) {
            VisibilityAnimator.animateVisible(AnimationUtils.loadAnimation(requireActivity(),
                    R.anim.slow_push_left_in), urlEntryContainer)
        }

        setupWebview()
    }

    private fun setupWebview() {
        // Give it a default url
        webView.loadUrl("")

        // Start off by hiding webview box
        webView.setGone()

        // Fit to width
        // Configure the webview
        val settings = webView.settings
        settings.builtInZoomControls = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        // Open all urls with our webview.
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, finishedURL: String) {
                // Once a page has loaded, stop the spinner.
                if (!isAdded) return
                delayLoadJob = weave {
                    var url = finishedURL
                    scrollView.scrollTo(0, urlEntryContainer.top)

                    // Do my best to not interrupt their typing.
                    if (onlineURL.text.toString().endsWith("/")) {
                        if (!url.endsWith("/")) {
                            url += "/"
                        }
                    } else {
                        if (url.endsWith("/")) {
                            url = url.substring(0, url.length - 1)
                        }
                    }

                    // We only want to set the text to the url if it's a valid url. If you put an invalid url (www.goog for example)
                    // the webview redirects and eventually returns some html that then is put into the onlineURL editText
                    if (Patterns.WEB_URL.matcher(url).matches()) {
                        onlineURL.setText(url)
                        onlineURL.setSelection(onlineURL.text?.length ?: 0)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        fileUpload.setOnClickListener {
            UploadFilesDialog.show(fragmentManager, UploadFilesDialog.createAssignmentBundle(null, canvasContext as Course, assignment)) { _ -> }
        }

        onlineURLHeader.setOnClickListener {
            if (urlEntryContainer.visibility == View.VISIBLE) {
                VisibilityAnimator.animateGone(AnimationUtils.loadAnimation(requireActivity(),
                        R.anim.slow_push_right_out), urlEntryContainer!!)
            } else {
                VisibilityAnimator.animateVisible(AnimationUtils.loadAnimation(requireActivity(),
                        R.anim.slow_push_left_in), urlEntryContainer!!)
            }
        }

        textEntryHeader.setOnClickListener {
            if (textEntryContainer.visibility == View.VISIBLE) {
                VisibilityAnimator.animateGone(AnimationUtils.loadAnimation(requireActivity(),
                        R.anim.slow_push_right_out), textEntryContainer!!)
            } else {
                VisibilityAnimator.animateVisible(AnimationUtils.loadAnimation(requireActivity(),
                        R.anim.slow_push_left_in), textEntryContainer!!)
            }
        }

        submitTextEntry.setOnClickListener { tryToSubmitText() }

        // Because it's not single line, we have to handle the enter button.
        textEntry.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                tryToSubmitText()

                // Hide keyboard
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                return@OnEditorActionListener true
            }
            false
        })

        submitURLEntry.setOnClickListener { tryToSubmitURL() }

        // Because it's not single line, we have to handle the enter button.
        onlineURL.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                tryToSubmitURL()

                // Hide keyboard
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                return@OnEditorActionListener true
            }
            false
        })

        onlineURL.onTextChanged { text ->
            if(text.isBlank()) dataLossDeleteStoredData(Const.DATA_LOSS_ADD_SUBMISSION_URL)

            delayPostJob?.cancel()
            delayPostJob = weave {
                delay(1500)
                var originalURL = webView.url ?: ""

                // Strip off ending / characters
                if (originalURL.endsWith("/")) {
                    originalURL = originalURL.substring(0, originalURL.length - 1)
                }

                var currentURL = httpURLSubmission
                if (currentURL.endsWith("/")) {
                    currentURL = currentURL.substring(0, currentURL.length - 1)
                }

                // If it's empty clear the view
                if (text.trim { it <= ' ' }.isEmpty()) {
                    webView.setGone()
                    webView.loadUrl("")
                } else if (originalURL != currentURL) {
                    // If it's already loaded, don't do it
                    webView.visibility = View.VISIBLE
                    webView.loadUrl(httpURLSubmission)
                }
            }
        }

        mediaSubmission.setOnClickListener {
            val intent = NotoriousMediaUploadPicker.createIntentForAssignmentSubmission(requireContext(), assignment)
            startActivityForResult(intent, RequestCodes.NOTORIOUS_REQUEST)
        }

        arcSubmission.setOnClickListener {
            val url = String.format(Locale.getDefault(), "%s/%s/external_tools/%d/resource_selection?launch_type=homework_submission&assignment_id=%d", ApiPrefs.fullDomain, canvasContext.toAPIString(), arcLTITool!!.id, assignment.id)
            RouteMatcher.route(requireActivity(), ArcWebviewFragment.makeRoute(canvasContext, url, arcLTITool!!.name!!, true))
        }

        submitArcEntry.setOnClickListener { tryToSubmitArc() }
    }

    private fun setUpCallbacks() {
        canvasCallbackSubmission = object : StatusCallback<Submission>() {
            override fun onResponse(response: Response<Submission>, linkHeaders: LinkHeaders, type: ApiType) {
                if (!isAdded) {
                    return
                }

                val result = response.body()
                result?.let {
                    if (it.body != null || it.url != null) {
                        toast(R.string.successPostingSubmission, Toast.LENGTH_LONG)
                        // Clear text fields because they are saved
                        textEntry.setText("")
                        onlineURL.setText("")
                        // Send broadcast so list is updated.
                        EventBus.getDefault().post(FileUploadEvent(FileUploadNotification(null, ArrayList())))
                        val navigation = navigation
                        navigation?.popCurrentFragment()
                    }
                } ?: toast(R.string.errorPostingSubmission, Toast.LENGTH_LONG)
            }
        }

        ltiToolCallback = object : StatusCallback<List<LTITool>>() {
            override fun onResponse(response: Response<List<LTITool>>, linkHeaders: LinkHeaders, type: ApiType) {
                response.body()?.let {
                    for (ltiTool in it) {
                        val url = ltiTool.url
                        if (url != null && url.contains("instructuremedia.com/lti/launch")) {
                            arcSubmission.setVisible()
                            arcLTITool = ltiTool
                            break
                        }
                    }
                    // Check to see if we should automatically show the file upload dialog
                    showFileUploadDialog()
                }
            }

            override fun onFail(call: Call<List<LTITool>>?, error: Throwable, response: Response<*>?) {
                // Check to see if we should automatically show the file upload dialog
                // we don't want to show it if this failed due to there being no cache
                if (response != null && response.code() != 504) {
                    showFileUploadDialog()
                }
            }
        }
    }
    //endregion

    //region Parent Fragment Overrides
    //  This gets called by the activity in onBackPressed().
    //  Call super so that we can check if there is unsaved data.
    override fun handleBackPressed(): Boolean =
            if (urlEntryContainer.visibility == View.VISIBLE && webView.canGoBack()) {
                webView!!.goBack()
                true
            } else {
                super.handleBackPressed()
            }

    //endregion

    //region Fragment Interaction Interface Overrides
    override fun title(): String = getString(R.string.assignmentTabSubmission)

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }
    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    //region Fragment Overrides
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RequestCodes.NOTORIOUS_REQUEST) {
            // When its a Notorious request, just dismiss the fragment, and the user can look at the notification to see progress.
            requireActivity().onBackPressed()
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == RequestCodes.NOTORIOUS_REQUEST) {
            isFileUploadCanceled = true
        }
    }
    //endregion

    //region Bus Events
    @Suppress("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FileUploadEvent) {
        event.once(FileUploadNotification::class.java.simpleName) {
            EventBus.getDefault().post(event) // Repost for SubmissionDetailsFragment
            if (navigation != null) navigation!!.popCurrentFragment()
        }
    }

    //endregion

    //region Submit Submission Data
    private fun tryToSubmitText() {
        // Get the text, replace all line breaks with <br/> tags so they are preserved when displayed in a webview
        var textToSubmit = textEntry.text.toString().replace("\\n".toRegex(), "<br/>")
        try {
            textToSubmit = URLEncoder.encode(textToSubmit, "UTF-8")
        } catch (e: UnsupportedEncodingException) {}
        if (textToSubmit.trim { it <= ' ' }.isEmpty()) {
            // It's an empty submission
            toast(R.string.blankSubmission, Toast.LENGTH_LONG)
        } else {
            // Log to GA
            Analytics.trackButtonPressed(requireActivity(), "Submit Text Assignment", null)

            SubmissionManager.postTextSubmission(canvasContext as Course, assignment.id, textToSubmit, canvasCallbackSubmission)
            dataLossDeleteStoredData(Const.DATA_LOSS_ADD_SUBMISSION)
        }
    }

    private fun tryToSubmitURL() {
        val urlToSubmit = onlineURL!!.text.toString()
        if (urlToSubmit.trim { it <= ' ' }.isEmpty()) {
            // It's an empty submission
            toast(R.string.blankSubmission, Toast.LENGTH_LONG)
        } else {
            // Log to GA
            Analytics.trackButtonPressed(requireActivity(), "Submit URL Assignment", null)

            SubmissionManager.postUrlSubmission(canvasContext as Course, assignment.id, urlToSubmit, false, canvasCallbackSubmission)
            dataLossDeleteStoredData(Const.DATA_LOSS_ADD_SUBMISSION_URL)
        }
    }

    private fun tryToSubmitArc() {
        val urlToSubmit = arcEntry.text.toString()
        if (urlToSubmit.trim { it <= ' ' }.isEmpty()) {
            // It's an empty submission
            toast(R.string.blankSubmission, Toast.LENGTH_LONG)
        } else {
            // Log to GA
            Analytics.trackButtonPressed(requireActivity(), "Submit Arc Assignment", null)

            SubmissionManager.postUrlSubmission(canvasContext as Course, assignment.id, urlToSubmit, true, canvasCallbackSubmission)
            dataLossDeleteStoredData(Const.DATA_LOSS_ADD_SUBMISSION_URL)
        }
    }

    //endregion

    //region Helpers
    private fun showFileUploadDialog() {
        if (!isMediaUploadAllowed && isFileUploadAllowed && arcLTITool == null) {
            if (isFileUploadCanceled) {
                requireActivity().onBackPressed()
            } else {
                isFileUploadCanceled = true
                UploadFilesDialog.show(fragmentManager, UploadFilesDialog.createAssignmentBundle(null, canvasContext as Course, assignment)) { event ->
                    if (event == UploadFilesDialog.EVENT_ON_UPLOAD_BEGIN) {
                        if (navigation != null) navigation!!.popCurrentFragment()
                    }
                }
            }
        }
    }
    //endregion

    companion object {
        const val ASSIGNMENT = "assignment"

        @JvmStatic
        fun makeRoute(course: Course, assignment: Assignment, textEntryAllowed: Boolean, urlEntryAllowed: Boolean, fileEntryAllowed: Boolean, mediaUploadAllowed: Boolean): Route {
            val bundle = course.makeBundle().apply {
                putParcelable(Const.ASSIGNMENT, assignment)
                putBoolean(Const.TEXT_ALLOWED, textEntryAllowed)
                putBoolean(Const.URL_ALLOWED, urlEntryAllowed)
                putBoolean(Const.FILE_ALLOWED, fileEntryAllowed)
                putBoolean(Const.MEDIA_UPLOAD_ALLOWED, mediaUploadAllowed)
            }

            return Route(AddSubmissionFragment::class.java, course, bundle)
        }

        fun newInstance(route: Route): AddSubmissionFragment? {
            return if (validRoute(route)) {
                AddSubmissionFragment().apply {
                    arguments = route.canvasContext!!.makeBundle(route.arguments)
                }
            } else null
        }

        @JvmStatic
        private fun validRoute(route: Route) = with(route.arguments) {
            route.canvasContext?.isCourse == true &&
                    containsKey(Const.ASSIGNMENT) &&
                    containsKey(Const.TEXT_ALLOWED) &&
                    containsKey(Const.URL_ALLOWED) &&
                    containsKey(Const.FILE_ALLOWED) &&
                    containsKey(Const.MEDIA_UPLOAD_ALLOWED)
        }
    }
}
