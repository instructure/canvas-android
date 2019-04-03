package com.instructure.student.factory

import com.instructure.student.presenters.ViewPdfFragmentPresenter
import instructure.androidblueprint.PresenterFactory

class ViewPdfFragmentPresenterFactory(val pdfUrl : String) : PresenterFactory<ViewPdfFragmentPresenter> {
    override fun create() = ViewPdfFragmentPresenter(pdfUrl)
}
