/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.android.buildtools.transform

import javassist.ClassPool
import javassist.CtClass

class ScreenViewTransformer(private val appName: String) : ClassTransformer() {

    override val transformName: String = "ScreenViewTransformer"

    private lateinit var activityClass: CtClass
    private lateinit var fragmentClass: CtClass

    private val emptyOnResume =
            """
        protected void onResume() {
            super.onResume();
        }
        """

    private val emptyOnViewCreated =
            """
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
        """

    private val emptyOnAttach =
            """
        public void onAttach(Context context) {
            super.onAttach(context);
        }
            """

    override fun onClassPoolReady(classPool: ClassPool) {
        activityClass = classPool["android.app.Activity"]
        fragmentClass = classPool["androidx.fragment.app.Fragment"]
        classPool.importPackage("com.instructure.canvasapi2.utils.Analytics")
        classPool.importPackage("android.view.View")
        classPool.importPackage("android.content.Context")
    }

    override fun createFilter(): ClassFilter = NameContains("instructure") +
            HasAnnotation("com.instructure.pandautils.analytics.ScreenView") +
            SubclassesAny(activityClass, fragmentClass)

    override fun transform(cc: CtClass, classPool: ClassPool): Boolean {
        when {
            cc.subclassOf(activityClass) -> cc.transformActivity()
            cc.subclassOf(fragmentClass) -> cc.transformFragment()
            else -> throw UnsupportedOperationException("Transforming classes of type ${cc.superclass.name} is unsupported.")
        }
        return true
    }

    private fun CtClass.transformActivity() {
        val annotation = getAnnotation("com.instructure.pandautils.analytics.ScreenView")
        val screenName = annotation?.getString("screenName") ?: simpleName
        val fullEvent = "${appName}_screen_view_${screenName}"

        // Add logic to onResume()
        val onResumeContent = logEventLogic(fullEvent)
        addOrUpdateDeclaredMethod("onResume", onResumeContent, emptyOnResume)
    }

    private fun CtClass.transformFragment() {
        val annotation = getAnnotation("com.instructure.pandautils.analytics.ScreenView")
        val screenName = annotation?.getString("screenName") ?: simpleName
        val fullEvent = "${appName}_screen_view_${screenName}"

        if (appName == "student") {
            val onViewCreatedContent =
                    """
            if (isAdded() && isVisible() && getUserVisibleHint()) {
                ${logEventLogic(fullEvent)}
            }
            """
            addOrUpdateDeclaredMethod("onViewCreated", onViewCreatedContent, emptyOnViewCreated)
        } else {
            val onAttach = logEventLogic(fullEvent)
            addOrUpdateDeclaredMethod("onAttach", onAttach, emptyOnAttach)
        }
    }

    private fun logEventLogic(fullEvent: String) =
            """
        Analytics.INSTANCE.logEvent("$fullEvent");
        """
}