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
package com.instructure.annotations

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.instructure.annotations.AnnotationDialogs.AnnotationCommentDialog
import com.instructure.annotations.AnnotationDialogs.AnnotationErrorDialog
import com.instructure.annotations.AnnotationDialogs.FreeTextDialog
import com.instructure.annotations.FileCaching.DocumentListenerSimpleDelegate
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.models.ApiValues
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.extractCanvaDocsDomain
import com.instructure.canvasapi2.utils.extractSessionId
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.ProgressiveCanvasLoadingView
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.AnnotationFlags
import com.pspdfkit.annotations.AnnotationProvider
import com.pspdfkit.annotations.AnnotationType
import com.pspdfkit.annotations.appearance.AssetAppearanceStreamGenerator
import com.pspdfkit.annotations.configuration.*
import com.pspdfkit.annotations.stamps.CustomStampAppearanceStreamGenerator
import com.pspdfkit.annotations.stamps.StampPickerItem
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.listeners.DocumentListener
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.inspector.PropertyInspectorCoordinatorLayout
import com.pspdfkit.ui.inspector.annotation.AnnotationCreationInspectorController
import com.pspdfkit.ui.inspector.annotation.AnnotationEditingInspectorController
import com.pspdfkit.ui.inspector.annotation.DefaultAnnotationCreationInspectorController
import com.pspdfkit.ui.inspector.annotation.DefaultAnnotationEditingInspectorController
import com.pspdfkit.ui.inspector.views.BorderStylePreset
import com.pspdfkit.ui.note.AnnotationNoteHinter
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.controller.AnnotationEditingController
import com.pspdfkit.ui.special_mode.controller.AnnotationSelectionController
import com.pspdfkit.ui.special_mode.controller.AnnotationTool
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.toolbar.*
import com.pspdfkit.ui.toolbar.grouping.MenuItemGroupingRule
import kotlinx.coroutines.Job
import okhttp3.Response
import java.io.File
import java.util.*

@SuppressLint("ViewConstructor")
abstract class PdfSubmissionView(context: Context, private val studentAnnotationView: Boolean = false, private val courseId: Long) : FrameLayout(context), AnnotationManager.OnAnnotationCreationModeChangeListener, AnnotationManager.OnAnnotationEditingModeChangeListener {

    protected lateinit var docSession: DocSession
    protected lateinit var apiValues: ApiValues
    protected val commentRepliesHashMap: HashMap<String, ArrayList<CanvaDocAnnotation>> = HashMap()
    protected var pdfFragment: PdfFragment? = null
    protected var noteHinter: AnnotationNoteHinter? = null
    protected val supportFragmentManager: FragmentManager = (context as AppCompatActivity).supportFragmentManager

    private val annotationCreationList = mutableListOf(AnnotationTool.INK, AnnotationTool.HIGHLIGHT, AnnotationTool.STRIKEOUT, AnnotationTool.SQUARE, AnnotationTool.STAMP, AnnotationTool.FREETEXT, AnnotationTool.ERASER, AnnotationTool.NOTE)
    private val annotationEditList = mutableListOf(
        AnnotationType.INK,
        AnnotationType.HIGHLIGHT,
        AnnotationType.STRIKEOUT,
        AnnotationType.SQUARE,
        AnnotationType.STAMP,
        AnnotationType.FREETEXT,
        AnnotationType.NONE // Wee need this to enable the eraser
    )

    private val pdfConfiguration: PdfConfiguration = PdfConfiguration.Builder()
            .scrollDirection(PageScrollDirection.VERTICAL)
            .enabledAnnotationTools(annotationCreationList)
            .editableAnnotationTypes(annotationEditList)
            .setAnnotationInspectorEnabled(true)
            .layoutMode(PageLayoutMode.SINGLE)
            .textSelectionEnabled(false)
            .disableCopyPaste()
            .build()

    private val annotationCreationToolbar = AnnotationCreationToolbar(context)
    private val annotationEditingToolbar = AnnotationEditingToolbar(context)
    private var annotationEditingInspectorController: AnnotationEditingInspectorController? = null
    private var annotationCreationInspectorController: AnnotationCreationInspectorController? = null
    private var fileJob: Job? = null
    private var createAnnotationJob: Job? = null
    private var updateAnnotationJob: Job? = null
    private var deleteAnnotationJob: Job? = null
    private var pdfContentJob: Job? = null
    private var annotationsJob: Job? = null
    private var sendCommentJob: Job? = null
    private var currentAnnotationModeTool: AnnotationTool? = null
    private var currentAnnotationModeType: AnnotationType? = null
    private var isUpdatingWithNoNetwork = false
    private var stampRaceFlag = true

    @get:ColorRes
    abstract val progressColor: Int
    abstract val annotationToolbarLayout: ToolbarCoordinatorLayout
    abstract val inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout
    abstract val commentsButton: ImageView
    abstract val loadingContainer: FrameLayout
    abstract val progressBar: ProgressiveCanvasLoadingView

    abstract fun setFragment(fragment: Fragment)
    abstract fun removeContentFragment()
    abstract fun showNoInternetDialog()
    abstract fun disableViewPager()
    abstract fun enableViewPager()
    abstract fun setIsCurrentlyAnnotating(boolean: Boolean)
    abstract fun showAnnotationComments(commentList: ArrayList<CanvaDocAnnotation>, headAnnotationId: String, docSession: DocSession, apiValues: ApiValues)
    abstract fun showFileError()

    open fun logOnAnnotationSelectedAnalytics() {}

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initializeSubmissionView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterPdfFragmentListeners()
        removeContentFragment()
        pdfFragment = null
        createAnnotationJob?.cancel()
        updateAnnotationJob?.cancel()
        deleteAnnotationJob?.cancel()
        annotationsJob?.cancel()
        pdfContentJob?.cancel()
        fileJob?.cancel()
    }

    protected fun unregisterPdfFragmentListeners() {
        pdfFragment?.removeOnAnnotationCreationModeChangeListener(this)
        pdfFragment?.removeOnAnnotationEditingModeChangeListener(this)
        pdfFragment?.document?.annotationProvider?.removeOnAnnotationUpdatedListener(annotationUpdateListener)
        pdfFragment?.removeOnAnnotationSelectedListener(annotationSelectedListener)
        pdfFragment?.removeOnAnnotationDeselectedListener(annotationDeselectedListener)
    }

    /**
     * THIS HAS TO BE CALLED
     */
    @Throws(UninitializedPropertyAccessException::class)
    protected fun initializeSubmissionView() {
        annotationEditingInspectorController = DefaultAnnotationEditingInspectorController(context, inspectorCoordinatorLayout)
        annotationCreationInspectorController = DefaultAnnotationCreationInspectorController(context, inspectorCoordinatorLayout)

        annotationToolbarLayout.setOnContextualToolbarLifecycleListener(object : ToolbarCoordinatorLayout.OnContextualToolbarLifecycleListener {
            override fun onDisplayContextualToolbar(p0: ContextualToolbar<*>) {}
            override fun onRemoveContextualToolbar(p0: ContextualToolbar<*>) {}

            override fun onPrepareContextualToolbar(toolbar: ContextualToolbar<*>) {
                toolbar.layoutParams = ToolbarCoordinatorLayout.LayoutParams(
                        ToolbarCoordinatorLayout.LayoutParams.Position.TOP, EnumSet.of(ToolbarCoordinatorLayout.LayoutParams.Position.TOP)
                )

                if (toolbar is AnnotationCreationToolbar) {
                    setUpGrabAnnotationTool(toolbar)
                }
            }
        })

        annotationCreationToolbar.closeButton.setGone()
        annotationCreationToolbar.setMenuItemGroupingRule(AnnotationCreationGroupingRule(context))

        annotationEditingToolbar.setMenuItemGroupingRule(object : MenuItemGroupingRule {
            override fun groupMenuItems(items: MutableList<ContextualToolbarMenuItem>, i: Int) = configureEditMenuItemGrouping(items)
            override fun areGeneratedGroupItemsSelectable() = true
        })

        annotationEditingToolbar.setOnMenuItemClickListener { _, contextualToolbarMenuItem ->
            if (contextualToolbarMenuItem.title == context.getString(com.pspdfkit.R.string.pspdf__edit) &&
                    currentAnnotationModeType == AnnotationType.FREETEXT) {

                val dialog = FreeTextDialog.getInstance(supportFragmentManager, true, pdfFragment?.selectedAnnotations?.get(0)?.contents
                        ?: "", freeTextDialogCallback)
                dialog.show(supportFragmentManager, FreeTextDialog::class.java.simpleName)

                return@setOnMenuItemClickListener true
            }

            if (contextualToolbarMenuItem.title == context.getString(com.pspdfkit.R.string.pspdf__delete)) {
                val annotation = pdfFragment?.selectedAnnotations?.get(0)
                // Remove the annotation
                if (annotation != null) {
                    pdfFragment?.document?.annotationProvider?.removeAnnotationFromPage(annotation)
                    pdfFragment?.notifyAnnotationHasChanged(annotation)
                    pdfFragment?.clearSelectedAnnotations()
                    pdfFragment?.exitCurrentlyActiveMode()
                    pdfFragment?.enterAnnotationCreationMode()
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }

        configureCommentView(commentsButton)
    }

    private fun setUpGrabAnnotationTool(toolbar: AnnotationCreationToolbar) {
        val menuItems = toolbar.menuItems

        val grabItem = ContextualToolbarMenuItem.createSingleItem(
            context,
            R.id.grab_annotation,
            ContextCompat.getDrawable(context, R.drawable.ic_grab)!!,
            context.getString(R.string.grabAnnotationTool),
            context.getColor(R.color.white),
            context.getColor(R.color.white),
            ContextualToolbarMenuItem.Position.START,
            true
        )

        if (currentAnnotationModeTool == null || currentAnnotationModeTool == AnnotationTool.NONE) {
            Handler(Looper.getMainLooper()).post { grabItem.isSelected = true }
        }

        menuItems.add(grabItem)
        toolbar.menuItems = menuItems

        toolbar.setOnMenuItemClickListener(
            ContextualToolbar.OnMenuItemClickListener { contextualToolbar, menuItem ->
                return@OnMenuItemClickListener (
                    if (menuItem.id == R.id.grab_annotation) {
                        if (!menuItem.isSelected) {
                            contextualToolbar.menuItems.forEach { it.isSelected = false }
                            pdfFragment?.exitCurrentlyActiveMode()
                            pdfFragment?.enterAnnotationCreationMode(AnnotationTool.NONE)
                            menuItem.isSelected = true
                        }
                        true
                    } else {
                        if (menuItem.isSelected) {
                            val grab = contextualToolbar.menuItems.find { it.id == R.id.grab_annotation }
                            Handler(Looper.getMainLooper()).postDelayed ({ grab?.isSelected = true }, 50)
                        }
                        false
                    })
            }
        )
    }

    open fun configureCommentView(commentsButton: ImageView) {
        //we want to offset the comment button by the height of the action bar
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        val typedArray = context.obtainStyledAttributes(typedValue.resourceId, intArrayOf(android.R.attr.actionBarSize))
        val actionBarDp = typedArray.getDimensionPixelSize(0, -1)
        typedArray.recycle()

        val marginDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics)
        val layoutParams = commentsButton.layoutParams as LayoutParams
        layoutParams.gravity = Gravity.END or Gravity.TOP
        layoutParams.topMargin = marginDp.toInt() + actionBarDp
        layoutParams.rightMargin = marginDp.toInt()

        commentsButton.onClick {
            openComments()
        }
    }

    protected fun openComments() {
        // Get current annotation in both forms
        if (pdfFragment?.selectedAnnotations.isNullOrEmpty()) {
            toast(R.string.noAnnotationSelected)
            return
        }
        val currentPdfAnnotation = pdfFragment?.selectedAnnotations?.get(0)
        val currentAnnotation = currentPdfAnnotation?.convertPDFAnnotationToCanvaDoc(docSession.documentId)
        // Assuming neither is null, continue
        if (currentPdfAnnotation != null && currentAnnotation != null) {
            // If the contents of the current annotation are empty we want to prompt them to add a comment
            if (commentRepliesHashMap[currentAnnotation.annotationId] == null || commentRepliesHashMap[currentAnnotation.annotationId]?.isEmpty() == true) {
                // No comments for this annotation, show a dialog for the user to add some if they want
                AnnotationCommentDialog.getInstance(supportFragmentManager, "", context.getString(R.string.addAnnotationComment)) { _, text ->
                    setIsCurrentlyAnnotating(true) //don't want the sliding panel getting in the way
                    // Create new comment reply for this annotation.
                    if (text.isValid()) {
                        createCommentAnnotation(currentAnnotation.annotationId, currentAnnotation.page, text)
                        // Add contents to the current annotation so we can add an indicator
                        if(currentAnnotation.annotationType != CanvaDocAnnotation.AnnotationType.TEXT && currentAnnotation.annotationType != CanvaDocAnnotation.AnnotationType.FREE_TEXT) {
                            currentPdfAnnotation.contents = "comment"
                            noteHinter?.notifyDrawablesChanged()
                            pdfFragment?.notifyAnnotationHasChanged(currentPdfAnnotation)
                        }
                    }
                }.show(supportFragmentManager, AnnotationCommentDialog::class.java.simpleName)
            } else {
                // Otherwise, show the comment list fragment
                commentRepliesHashMap[currentAnnotation.annotationId]?.let {
                    if (it.isNotEmpty()) {
                        showAnnotationComments(it, currentAnnotation.annotationId, docSession, apiValues)
                    }
                }
            }
        }
    }

    open fun setupPSPDFKit(uri: Uri) {
        val newPdfFragment = PdfFragment.newInstance(uri, pdfConfiguration)
        setFragment(newPdfFragment)
        pdfFragment = newPdfFragment
        pdfFragment?.addOnAnnotationCreationModeChangeListener(this)
        pdfFragment?.addOnAnnotationEditingModeChangeListener(this)

        noteHinter = AnnotationNoteHinter(context)
        pdfFragment?.addDrawableProvider(noteHinter!!)

        if (docSession.annotationMetadata?.canWrite() == true) {
            // push the pdf viewing screen under the toolbar
            pdfFragment?.setInsets(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, context.resources.displayMetrics).toInt(), 0, 0)
        }

        attachDocListener()
    }

    protected fun handlePdfContent(url: String) {
        pdfContentJob = tryWeave {
            if (url.contains("canvadoc")) {
                val redirectUrl = getCanvaDocsRedirect(url, domain = ApiPrefs.overrideDomains[courseId])
                //extract the domain for API use
                if (redirectUrl.isNotEmpty()) {
                    docSession = awaitApi { CanvaDocsManager.getCanvaDoc(redirectUrl, it) }
                    docSession.let {
                        val canvaDocsDomain = extractCanvaDocsDomain(redirectUrl)
                        val pdfUrl = canvaDocsDomain + it.annotationUrls.pdfDownload
                        apiValues = ApiValues(it.documentId, pdfUrl, extractSessionId(pdfUrl), canvaDocsDomain)
                    }

                    load(apiValues.pdfUrl) { setupPSPDFKit(it) }
                } else {
                    toast(R.string.errorOccurred)
                }
            } else {
                //keep things working if they don't have canvadocs
                load(url) { setupPSPDFKit(it) }
            }
        } catch {
            // Show error
            toast(R.string.errorOccurred)
            it.printStackTrace()
        }
    }

    open fun attachDocListener() {
        pdfFragment?.addDocumentListener(documentListener)
    }

    fun updateAnnotations() {
        if (pdfFragment?.document != null) {
            loadAnnotations()
        }
    }

    private val documentListener = object : DocumentListener by DocumentListenerSimpleDelegate() {
        override fun onDocumentLoaded(pdfDocument: PdfDocument) {
            setupPdfAnnotationDefaults()
            loadCustomAppearanceGenerators()

            docSession.rotations?.let { rotations ->
                pdfFragment?.document?.let {
                    handlePageRotation(it, rotations)
                }
            }

            pdfFragment?.enterAnnotationCreationMode()
            if (docSession.annotationMetadata?.canRead() != true) return
            loadAnnotations()
        }
    }

    private fun loadAnnotations() {
        annotationsJob = tryWeave {
            // Snag them annotations with the session id
            val annotations = awaitApi { CanvaDocsManager.getAnnotations(apiValues.sessionId, apiValues.canvaDocsDomain, it) }
            // We don't want to trigger the annotation events here, so unregister and re-register after
            pdfFragment?.document?.annotationProvider?.removeOnAnnotationUpdatedListener(annotationUpdateListener)

            // Grab all the annotations and sort them by type (descending).
            // This will result in all of the comments being iterated over first as the COMMENT_REPLY type is last in the AnnotationType enum.
            val sortedAnnotationList = annotations.data.sortedByDescending { it.annotationType }
            for (item in sortedAnnotationList) {
                if (item.annotationType == CanvaDocAnnotation.AnnotationType.COMMENT_REPLY) {
                    // Grab the annotation comments and store them to be displayed later when user selects annotation
                    if (commentRepliesHashMap.containsKey(item.inReplyTo)) {
                        commentRepliesHashMap[item.inReplyTo]?.add(item)
                    } else {
                        commentRepliesHashMap[item.inReplyTo!!] = arrayListOf(item)
                    }
                } else {
                    // We don't want to add deleted annotations to the view
                    if (!item.deleted) {
                        val annotation = item.convertCanvaDocAnnotationToPDF(this@PdfSubmissionView.context)
                        if (annotation != null) {
                            // If the user doesn't have at least write permissions we need to lock down all annotations
                            if (docSession.annotationMetadata?.canWrite() == false || item.userId != docSession.annotationMetadata?.userId) {
                                annotation.flags = EnumSet.of(AnnotationFlags.LOCKED, AnnotationFlags.LOCKEDCONTENTS)
                                if (annotation.type != AnnotationType.FREETEXT) {
                                    annotation.flags.add(AnnotationFlags.NOZOOM)
                                }
                            }

                            if(commentRepliesHashMap.containsKey(annotation.name)
                                && (item.annotationType != CanvaDocAnnotation.AnnotationType.TEXT && item.annotationType != CanvaDocAnnotation.AnnotationType.FREE_TEXT)) {
                                annotation.contents = "comment"
                            }

                            pdfFragment?.document?.annotationProvider?.addAnnotationToPage(annotation)
                            pdfFragment?.notifyAnnotationHasChanged(annotation)
                        }
                    }
                }

            }

            noteHinter?.notifyDrawablesChanged()
            pdfFragment?.document?.annotationProvider?.addOnAnnotationUpdatedListener(annotationUpdateListener)
            pdfFragment?.addOnAnnotationSelectedListener(annotationSelectedListener)
            pdfFragment?.addOnAnnotationDeselectedListener(annotationDeselectedListener)
        } catch {
            // Show error
            toast(R.string.annotationErrorOccurred)
            it.printStackTrace()
        }
    }

    private fun handlePageRotation(pdfDocument: PdfDocument, rotationMap: HashMap<String, Int>) {
        // Removing the listener prevents an infinite loop with onDocumentLoaded, which is triggered
        // by the calls to setRotationOffset()
        pdfFragment?.removeDocumentListener(documentListener)

        rotationMap.forEach { pageRotation ->
            pageRotation.key.toIntOrNull()?.let { pageIndex ->
                pdfDocument.setRotationOffset(calculateRotationOffset(pdfDocument.getPageRotation(pageIndex), pageRotation.value), pageIndex)
            }
        }
    }

    protected fun load(url: String?, onFinished: (Uri) -> Unit) {
        fileJob?.cancel()
        fileJob = tryWeave {
            progressBar.isIndeterminate = true
            progressBar.setColor(ContextCompat.getColor(this@PdfSubmissionView.context, R.color.textDark))
            val teacherYellow = ContextCompat.getColor(this@PdfSubmissionView.context, progressColor)

            val jitterThreshold = 300L
            val showLoadingRunner = Runnable {
                loadingContainer.setVisible()
                progressBar.announceForAccessibility(getContext().getString(R.string.loading))
            }
            val startTime = System.currentTimeMillis()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(showLoadingRunner, jitterThreshold)

            // If we don't have a url we'll display an error
            val tempFile: File? = com.instructure.annotations.FileCaching.FileCache.awaitFileDownload(url!!) {
                onUI {
                    progressBar.setColor(teacherYellow)
                    progressBar.setProgress(it)
                }
            }

            if (tempFile != null) {
                progressBar.isIndeterminate = true
                onFinished(Uri.fromFile(tempFile))
            } else {
                showFileError()
            }

            val passedTime = System.currentTimeMillis() - startTime
            val hideLoadingRunner = Runnable { loadingContainer.setGone() }
            when {
                passedTime < jitterThreshold -> {
                    handler.removeCallbacks(showLoadingRunner); hideLoadingRunner.run()
                }
                passedTime < jitterThreshold * 2 -> handler.postDelayed(hideLoadingRunner, (jitterThreshold * 2) - passedTime)
                else -> hideLoadingRunner.run()
            }
        } catch {
            // Show error
            toast(R.string.annotationErrorOccurred)
            it.printStackTrace()
        }
    }

    private val annotationUpdateListener = object : AnnotationProvider.OnAnnotationUpdatedListener {
        override fun onAnnotationCreated(annotation: Annotation) {
            if (!annotation.isAttached || annotationNetworkCheck(annotation)) return

            // If it's a freetext and it's empty that means that they haven't had a chance to fill it out
            if ((annotation.type == AnnotationType.FREETEXT) && annotation.contents.isNullOrEmpty()) {
                return
            }

            // If its a stamp we need to modify its rect to match the webs stamp size
            if (annotation.type == AnnotationType.STAMP) {
                annotation.transformStamp()
                // Transforming the stamp causes an update call and a race condition, often allowing for two annotations
                // to be created. This race flag will prevent the update from happening.
                stampRaceFlag = false
            }

            createNewAnnotation(annotation)
        }

        override fun onAnnotationUpdated(annotation: Annotation) {
            if (!annotation.isAttached || annotationNetworkCheck(annotation)) return

            if (!annotation.flags.contains(AnnotationFlags.LOCKED) && annotation.isModified && annotation.name.isValid()) {
                //we only want to update the annotation if it isn't Locked and IS modified
                updateAnnotation(annotation)
            }
        }

        override fun onAnnotationRemoved(annotation: Annotation) {
            if (annotationNetworkCheck(annotation)) return

            //removed annotation
            if (annotation.name.isValid()) {
                deleteAnnotation(annotation)
            }
        }

        override fun onAnnotationZOrderChanged(p0: Int, p1: MutableList<Annotation>, p2: MutableList<Annotation>) {}
    }

    private fun annotationNetworkCheck(annotation: Annotation): Boolean {
        if (!APIHelper.hasNetworkConnection()) {
            if (isUpdatingWithNoNetwork) {
                isUpdatingWithNoNetwork = false
                return true
            } else {
                isUpdatingWithNoNetwork = true
                if (annotation.isAttached) {
                    pdfFragment?.clearSelectedAnnotations()
                    pdfFragment?.document?.annotationProvider?.removeAnnotationFromPage(annotation)
                    pdfFragment?.notifyAnnotationHasChanged(annotation)
                }
                showNoInternetDialog()
            }
        }
        return false
    }

    private val annotationSelectedListener = object : AnnotationManager.OnAnnotationSelectedListener {
        override fun onAnnotationSelected(annotation: Annotation, isCreated: Boolean) {
            logOnAnnotationSelectedAnalytics()
        }

        override fun onPrepareAnnotationSelection(p0: AnnotationSelectionController, annotation: Annotation, isCreated: Boolean): Boolean {
            if (APIHelper.hasNetworkConnection()) {
                if (annotation.type == AnnotationType.FREETEXT && annotation.contents.isNullOrEmpty()) {
                    //this is a new free text annotation, and needs to be selected to be created
                    if (supportFragmentManager.findFragmentByTag(FreeTextDialog::class.java.simpleName) == null) {
                        val dialog = FreeTextDialog.getInstance(supportFragmentManager, false, "", freeTextDialogCallback)
                        dialog.show(supportFragmentManager, FreeTextDialog::class.java.simpleName)
                    }
                } else if (annotation.type == AnnotationType.FREETEXT) {
                    setIsCurrentlyAnnotating(true)
                }

                if (annotation.type != AnnotationType.FREETEXT && annotation.name.isValid() && (!studentAnnotationView || hasComments(annotation))) {
                    // if the annotation is an existing annotation (has an ID) and is NOT freetext
                    // we want to display the button to view/make comments
                    commentsButton.setVisible()
                }
            }
            return true
        }
    }

    private fun hasComments(annotation: Annotation): Boolean {
        val currentAnnotation = annotation.convertPDFAnnotationToCanvaDoc(docSession.documentId)
        return currentAnnotation != null
            && commentRepliesHashMap[currentAnnotation.annotationId] != null
            && commentRepliesHashMap[currentAnnotation.annotationId]?.isNotEmpty() == true
    }

    private val annotationDeselectedListener = AnnotationManager.OnAnnotationDeselectedListener { _, _ ->
        commentsButton.setGone()
    }

    //region Annotation Manipulation
    fun createNewAnnotation(annotation: Annotation) {
        if (annotation.type == AnnotationType.FREETEXT) {
            annotation.flags.remove(AnnotationFlags.NOZOOM)
        }

        if (docSession.annotationMetadata?.canWrite() != true) return

        // This is a new annotation; Post it
        post {
            commentsButton.isEnabled = false
        }

        createAnnotationJob = tryWeave {
            val canvaDocAnnotation = annotation.convertPDFAnnotationToCanvaDoc(apiValues.documentId)
            if (canvaDocAnnotation != null) {
                val newAnnotation = awaitApi { CanvaDocsManager.putAnnotation(apiValues.sessionId, generateAnnotationId(), canvaDocAnnotation, apiValues.canvaDocsDomain, it) }

                // Edit the annotation with the appropriate id
                annotation.name = newAnnotation.annotationId
                pdfFragment?.document?.annotationProvider?.removeOnAnnotationUpdatedListener(annotationUpdateListener)
                pdfFragment?.notifyAnnotationHasChanged(annotation)
                pdfFragment?.document?.annotationProvider?.addOnAnnotationUpdatedListener(annotationUpdateListener)
                commentsButton.isEnabled = true
                if (annotation.type == AnnotationType.STAMP) {
                    commentsButton.setVisible()
                    openComments()
                    // Now that the stamp has been created, re-enable the flag for updates
                    stampRaceFlag = true
                }
            }
        } catch {
            // Show general error, make more specific in the future?
            toast(R.string.errorOccurred)
            it.printStackTrace()
            commentsButton.isEnabled = true

            // Just in case something went wrong, re-enable stamp updates
            if (annotation.type == AnnotationType.STAMP) stampRaceFlag = true
        }
    }

    private fun updateAnnotation(annotation: Annotation) {
        if (annotation.type == AnnotationType.FREETEXT) {
            annotation.flags.remove(AnnotationFlags.NOZOOM)
        }
        if (docSession.annotationMetadata?.canWrite() != true) return

        // Don't want to update if we just created a stamp.
        if(annotation.type == AnnotationType.STAMP && !stampRaceFlag) return
        // Annotation modified; Update it
        updateAnnotationJob = tryWeave {
            val canvaDocAnnotation = annotation.convertPDFAnnotationToCanvaDoc(apiValues.documentId)
            if (canvaDocAnnotation != null && !annotation.name.isNullOrEmpty()) {
                awaitApi<CanvaDocAnnotation> { CanvaDocsManager.putAnnotation(apiValues.sessionId, annotation.name!!, canvaDocAnnotation, apiValues.canvaDocsDomain, it) }
            }
        } catch {
            if (it is StatusCallbackError) {
                val rawResponse: Response? = it.response?.raw()
                if (rawResponse?.code == 404) {
                    // Not found; Annotation has been deleted and no longer exists.
                    val dialog = AnnotationErrorDialog.getInstance(supportFragmentManager) {
                        // Delete annotation after user clicks OK on dialog
                        pdfFragment?.clearSelectedAnnotations()
                        pdfFragment?.document?.annotationProvider?.removeAnnotationFromPage(annotation)
                        pdfFragment?.notifyAnnotationHasChanged(annotation)
                    }
                    dialog.show(supportFragmentManager, AnnotationErrorDialog::class.java.simpleName)
                }
            }

            // Show general error, make more specific in the future?
            toast(R.string.errorOccurred)

            it.printStackTrace()
        }
    }

    private fun deleteAnnotation(annotation: Annotation) {
        // Annotation deleted; DELETE
        deleteAnnotationJob = tryWeave {
            // If it is not found, don't hit the server (it will fail)
            if (!annotation.name.isNullOrEmpty())
                awaitApi { CanvaDocsManager.deleteAnnotation(apiValues.sessionId, annotation.name!!, apiValues.canvaDocsDomain, it) }
            noteHinter?.notifyDrawablesChanged()
        } catch {
            // Show general error, make more specific in the future?
            toast(R.string.errorOccurred)
            it.printStackTrace()
        }
    }

    private fun createCommentAnnotation(inReplyToId: String, page: Int, comment: String?) {
        // Annotation modified; Update it
        commentsButton.isEnabled = false

        sendCommentJob = tryWeave {
            val newCommentReply = awaitApi {
                CanvaDocsManager.putAnnotation(apiValues.sessionId, generateAnnotationId(), createCommentReplyAnnotation(comment
                        ?: "", inReplyToId, apiValues.documentId, ApiPrefs.user?.id.toString(), page), apiValues.canvaDocsDomain, it)
            }

            // The put request doesn't return this property, so we need to set it to true
            newCommentReply.isEditable = true
            commentRepliesHashMap[inReplyToId] = arrayListOf(newCommentReply)
            commentsButton.isEnabled = true
        } catch {
            // Show general error, make more specific in the future?
            toast(R.string.errorOccurred)
            it.printStackTrace()
            commentsButton.isEnabled = true
        }
    }

    //region annotation listeners
    override fun onEnterAnnotationCreationMode(controller: AnnotationCreationController) {
        // We never want to show annotation toolbars if the user doesn't have permission to write
        if (docSession.annotationMetadata?.canWrite() != true) return
        //we only want to disable the viewpager if they are actively annotating
        if (controller.activeAnnotationTool != AnnotationTool.NONE) {
            disableViewPager()
        } else {
            val grabItem = annotationCreationToolbar.menuItems.find { it.id == R.id.grab_annotation }
            grabItem?.isSelected = true
        }

        currentAnnotationModeTool = controller.activeAnnotationTool

        annotationCreationInspectorController?.bindAnnotationCreationController(controller)
        annotationCreationToolbar.bindController(controller)
        annotationToolbarLayout.displayContextualToolbar(annotationCreationToolbar, true)
    }

    override fun onExitAnnotationCreationMode(p0: AnnotationCreationController) {

        enableViewPager()
        annotationToolbarLayout.removeContextualToolbar(true)
        annotationCreationToolbar.unbindController()
        annotationCreationInspectorController?.unbindAnnotationCreationController()

        currentAnnotationModeTool = AnnotationTool.NONE
    }

    override fun onEnterAnnotationEditingMode(controller: AnnotationEditingController) {

        // We never want to show annotation toolbars if the user doesn't have permission to write
        if (docSession.annotationMetadata?.canWrite() == false) return
        currentAnnotationModeType = controller.currentSingleSelectedAnnotation?.type
        //we only want to disable the viewpager if they are actively annotating
        if (controller.currentSingleSelectedAnnotation != null) disableViewPager()
        annotationEditingToolbar.bindController(controller)
        annotationEditingInspectorController?.bindAnnotationEditingController(controller)
        annotationToolbarLayout.displayContextualToolbar(annotationEditingToolbar, true)
    }

    override fun onExitAnnotationEditingMode(controller: AnnotationEditingController) {

        enableViewPager()
        annotationToolbarLayout.removeContextualToolbar(true)
        annotationEditingToolbar.unbindController()
        annotationEditingInspectorController?.unbindAnnotationEditingController()

        currentAnnotationModeType = AnnotationType.NONE

        //send them back to creating annotations
        pdfFragment?.enterAnnotationCreationMode()
    }

    override fun onChangeAnnotationEditingMode(controller: AnnotationEditingController) {
        currentAnnotationModeType = controller.currentSingleSelectedAnnotation?.type

        //we only want to disable the viewpager if they are actively annotating
        if (controller.currentSingleSelectedAnnotation != null) disableViewPager()
        else enableViewPager()
    }

    override fun onChangeAnnotationCreationMode(controller: AnnotationCreationController) {
        //we only want to disable the viewpager if they are actively annotating
        if (controller.activeAnnotationTool != AnnotationTool.NONE) disableViewPager()
        else enableViewPager()

        //we want to make sure that the keyboard doesn't mess up the view if they are using these annotations
        setIsCurrentlyAnnotating(controller.activeAnnotationTool != AnnotationTool.NONE)

        currentAnnotationModeTool = controller.activeAnnotationTool!!
    }

    private fun setupPdfAnnotationDefaults() {
        pdfFragment?.annotationConfiguration?.put(
            AnnotationType.INK,
            InkAnnotationConfiguration.builder(context)
                .setAvailableColors(context.resources.getIntArray(R.array.standardAnnotationColors).toMutableList())
                .setCustomColorPickerEnabled(false)
                .setSupportedProperties(EnumSet.of(AnnotationProperty.COLOR))
                .setDefaultColor(ContextCompat.getColor(context, R.color.blueAnnotation))
                .setDefaultThickness(2f)
                .setZIndexEditingEnabled(false)
                .build()

        )
        pdfFragment?.annotationConfiguration?.put(
            AnnotationType.SQUARE,
            ShapeAnnotationConfiguration.builder(context, AnnotationTool.SQUARE)
                .setAvailableColors(context.resources.getIntArray(R.array.standardAnnotationColors).toMutableList())
                .setCustomColorPickerEnabled(false)
                .setSupportedProperties(EnumSet.of(AnnotationProperty.COLOR))
                .setDefaultColor(ContextCompat.getColor(context, R.color.blueAnnotation))
                .setDefaultThickness(2f)
                .setDefaultAlpha(1f)
                .setDefaultFillColor(ContextCompat.getColor(context, R.color.transparent))
                .setDefaultBorderStylePreset(BorderStylePreset.SOLID)
                .setZIndexEditingEnabled(false)
                .build()
        )
        pdfFragment?.annotationConfiguration?.put(
            AnnotationType.HIGHLIGHT,
            MarkupAnnotationConfiguration.builder(context, AnnotationTool.HIGHLIGHT)
                .setAvailableColors(context.resources.getIntArray(R.array.highlightAnnotationColors).toMutableList())
                .disableProperty(AnnotationProperty.ANNOTATION_ALPHA)
                .setDefaultColor(ContextCompat.getColor(context, R.color.yellowHighlightAnnotation))
                .setZIndexEditingEnabled(false)
                .build()
        )
        pdfFragment?.annotationConfiguration?.put(
            AnnotationType.STRIKEOUT,
            MarkupAnnotationConfiguration.builder(context, AnnotationTool.STRIKEOUT)
                .setAvailableColors(context.resources.getIntArray(R.array.standardAnnotationColors).toMutableList())
                .setSupportedProperties(EnumSet.of(AnnotationProperty.COLOR))
                .setDefaultColor(ContextCompat.getColor(context, R.color.redAnnotation))
                .setZIndexEditingEnabled(false)
                .build()
        )
        pdfFragment?.annotationConfiguration?.put(
            AnnotationType.FREETEXT,
            FreeTextAnnotationConfiguration.builder(context)
                .setSupportedProperties(EnumSet.of(AnnotationProperty.COLOR))
                .setAvailableColors(context.resources.getIntArray(R.array.standardAnnotationColors).toMutableList())
                .setDefaultColor(ContextCompat.getColor(context, R.color.darkGrayAnnotation))
                .setDefaultTextSize(smallFont)
                .setDefaultFillColor(Color.TRANSPARENT)
                .setCustomColorPickerEnabled(false)
                .setHorizontalResizingEnabled(false)
                .setVerticalResizingEnabled(false)
                .setZIndexEditingEnabled(false)
                .build()
        )
        pdfFragment?.annotationConfiguration?.put(
            AnnotationType.STAMP,
            StampAnnotationConfiguration.builder(context)
                .setAvailableStampPickerItems(getAppearanceStreams())
                .setSupportedProperties(EnumSet.noneOf(AnnotationProperty::class.java))
                .setZIndexEditingEnabled(false)
                .build()
        )
        pdfFragment?.annotationConfiguration?.put(
            AnnotationTool.ERASER,
            EraserToolConfiguration.builder()
                .setDefaultThickness(5f)
                .setForceDefaults(true)
                .build()
        )
    }


    // region Stamp Appearance Streams
    private fun getAppearanceStreams(): MutableList<StampPickerItem> {
        val stamps = ArrayList<StampPickerItem>()

        // Create appearance stream generators with a PDF containing vector logo.
        val black = AssetAppearanceStreamGenerator(blackStampFile)
        val blue = AssetAppearanceStreamGenerator(blueStampFile)
        val brown = AssetAppearanceStreamGenerator(brownStampFile)
        val green = AssetAppearanceStreamGenerator(greenStampFile)
        val navy = AssetAppearanceStreamGenerator(navyStampFile)
        val orange = AssetAppearanceStreamGenerator(orangeStampFile)
        val pink = AssetAppearanceStreamGenerator(pinkStampFile)
        val purple = AssetAppearanceStreamGenerator(purpleStampFile)
        val red = AssetAppearanceStreamGenerator(redStampFile)
        val yellow = AssetAppearanceStreamGenerator(yellowStampFile)

        // Create picker items with custom subject and custom appearance stream generator set.
        stamps.add(StampPickerItem.fromTitle(context, blackStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(black)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, blueStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(blue)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, brownStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(brown)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, greenStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(green)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, navyStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(navy)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, orangeStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(orange)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, pinkStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(pink)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, purpleStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(purple)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, redStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(red)
                .build())

        stamps.add(StampPickerItem.fromTitle(context, yellowStampSubject)
                .withSize(18.66f, 26.66f)
                .withAppearanceStreamGenerator(yellow)
                .build())

        return stamps
    }

    private fun loadCustomAppearanceGenerators() {
        // Register custom stamp appearance stream generator as a global appearance stream generator.
        val customStampAppearanceStreamGenerator = CustomStampAppearanceStreamGenerator()
        pdfFragment?.document?.annotationProvider?.addAppearanceStreamGenerator(customStampAppearanceStreamGenerator)

        // Create appearance stream generators with a PDF containing vector logo.
        val black = AssetAppearanceStreamGenerator(blackStampFile)
        val blue = AssetAppearanceStreamGenerator(blueStampFile)
        val brown = AssetAppearanceStreamGenerator(brownStampFile)
        val green = AssetAppearanceStreamGenerator(greenStampFile)
        val navy = AssetAppearanceStreamGenerator(navyStampFile)
        val orange = AssetAppearanceStreamGenerator(orangeStampFile)
        val pink = AssetAppearanceStreamGenerator(pinkStampFile)
        val purple = AssetAppearanceStreamGenerator(purpleStampFile)
        val red = AssetAppearanceStreamGenerator(redStampFile)
        val yellow = AssetAppearanceStreamGenerator(yellowStampFile)

        // Register created appearance stream generator for the custom subject.
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(blackStampSubject, black)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(blueStampSubject, blue)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(brownStampSubject, brown)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(greenStampSubject, green)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(navyStampSubject, navy)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(orangeStampSubject, orange)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(pinkStampSubject, pink)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(purpleStampSubject, purple)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(redStampSubject, red)
        customStampAppearanceStreamGenerator.addAppearanceStreamGenerator(yellowStampSubject, yellow)
    }
    //endregion

    open fun configureEditMenuItemGrouping(toolbarMenuItems: MutableList<ContextualToolbarMenuItem>): MutableList<ContextualToolbarMenuItem> {
        //if current tool == freeText add edit button
        //There are 7 items total, and always need to leave room for the color, it has to show.
        //First we need to get all of the items and store them in variables for readability.... rip
        var delete: ContextualToolbarMenuItem? = null
        var color: ContextualToolbarMenuItem? = null

        val edit: ContextualToolbarMenuItem? = if (currentAnnotationModeType == AnnotationType.FREETEXT) {
            ContextualToolbarMenuItem.createSingleItem(context, View.generateViewId(),
                    ContextCompat.getDrawable(context, com.pspdfkit.R.drawable.pspdf__ic_edit)!!,
                    context.getString(com.pspdfkit.R.string.pspdf__edit), -1, -1,
                    ContextualToolbarMenuItem.Position.END, false)
        } else null

        for (item in toolbarMenuItems) {
            when (item.title) {
                context.getString(com.pspdfkit.R.string.pspdf__edit_menu_color) -> {
                    color = item
                }
                context.getString(com.pspdfkit.R.string.pspdf__delete) -> {
                    delete = item
                }
            }
        }

        var list = mutableListOf<ContextualToolbarMenuItem>()
        //check to make sure we have all of our items

        // If the user has read/write/manage we want to let them delete (and only delete) non-authored annotations
        val annotation = pdfFragment?.selectedAnnotations?.get(0)
        if (docSession.annotationMetadata?.canManage() == true && annotation?.flags?.contains(AnnotationFlags.LOCKED) == true) {
            // We need to only return a list with the delete menu item
            delete = ContextualToolbarMenuItem.createSingleItem(context, View.generateViewId(),
                    ContextCompat.getDrawable(context, R.drawable.ic_trash)!!,
                    context.getString(com.pspdfkit.R.string.pspdf__delete), -1, -1,
                    ContextualToolbarMenuItem.Position.END, false)
            list.add(delete)
        } else {
            if (color != null && delete != null) {
                if (edit != null)
                    list.add(edit)
                list.add(color)
                list.add(delete)
            } else {
                // If we don't have all items, just return the default that we have
                list = toolbarMenuItems
            }
        }


        return list
    }

    val freeTextDialogCallback = object : (Boolean, Boolean, String) -> Unit {
        override fun invoke(cancelled: Boolean, isEditing: Boolean, text: String) {
            if (isEditing && cancelled) return

            val annotation = if ((pdfFragment?.selectedAnnotations?.size ?: 0) > 0) pdfFragment?.selectedAnnotations?.get(0)
                    ?: return else return
            if ((cancelled && annotation.contents.isNullOrEmpty()) || text.isEmpty()) {
                // Remove the annotation
                pdfFragment?.document?.annotationProvider?.removeAnnotationFromPage(annotation)
                pdfFragment?.notifyAnnotationHasChanged(annotation)
                pdfFragment?.clearSelectedAnnotations()
                pdfFragment?.enterAnnotationCreationMode()
                return
            }

            // Updating the annotation's contents triggers an update call which creates the new annotation.
            annotation.contents = text

            // We need to update the UI so pspdfkit knows how to handle this.
            pdfFragment?.exitCurrentlyActiveMode()
            pdfFragment?.enterAnnotationCreationMode()
        }
    }
}
