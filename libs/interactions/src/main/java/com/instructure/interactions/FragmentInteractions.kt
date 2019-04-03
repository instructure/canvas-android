package com.instructure.interactions

import androidx.fragment.app.Fragment

interface FragmentInteractions {

    val navigation: Navigation?

    fun title(): String
    fun applyTheme()
    fun getFragment(): Fragment?
}
