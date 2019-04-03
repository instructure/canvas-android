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
 *
 */
@file:Suppress("unused")

package com.instructure.android.buildtools.transform

import javassist.ClassPool
import javassist.CtClass
import javassist.CtNewMethod

class MasqueradeUITransformer(private val startingClass: String) : ClassTransformer() {

    override val transformName = "MasqueradeUITransformer"

    private lateinit var activityClass: CtClass
    private lateinit var dialogFragmentClass: CtClass
    private lateinit var compatDialogFragmentClass: CtClass

    override fun onClassPoolReady(classPool: ClassPool) {
        activityClass = classPool["android.app.Activity"]
        dialogFragmentClass = classPool["android.app.DialogFragment"]
        compatDialogFragmentClass = classPool["androidx.fragment.app.DialogFragment"]
        classPool.importPackage("android.os.Bundle")
        classPool.importPackage("com.instructure.loginapi.login.util.MasqueradeUI")
    }

    override fun createFilter() = NameContains("instructure") +
            SubclassesAny(activityClass, dialogFragmentClass, compatDialogFragmentClass)

    override fun transform(cc: CtClass, classPool: ClassPool): Boolean {
        when {
            cc.subclassOf(activityClass) -> cc.transformActivity()
            cc.subclassOf(dialogFragmentClass) -> cc.transformDialogFragment()
            cc.subclassOf(compatDialogFragmentClass) -> cc.transformDialogFragment()
            else -> throw UnsupportedOperationException("Transforming classes of type ${cc.superclass.name} is unsupported.")
        }
        return true
    }

    /**
     * Transforms this class to show the Masquerade UI whenever the user is masquerading.
     * This only works for subclasses of android.app.Activity.
     */
    private fun CtClass.transformActivity() {
        val method = declaredMethods.find { it.name == "onPostCreate" }
        if (method != null) {
            removeMethod(method)
            method.insertBefore("com.instructure.loginapi.login.util.MasqueradeUI.showMasqueradeNotification(this, $startingClass);")
            addMethod(method)
        } else {
            val newMethod = CtNewMethod.make(
                    """
                protected void onPostCreate(android.os.Bundle savedInstanceState) {
                    super.onPostCreate(savedInstanceState);
                    com.instructure.loginapi.login.util.MasqueradeUI.showMasqueradeNotification(this, $startingClass);
                }
                """.trimIndent(), this)
            addMethod(newMethod)
        }
    }

    /**
     * Transforms this class to show the Masquerade UI whenever the user is masquerading.
     * This only works for subclasses of android.app.DialogFragment and androidx.fragment.app.DialogFragment.
     */
    private fun CtClass.transformDialogFragment() {
        val method = declaredMethods.find { it.name == "onStart" }
        if (method != null) {
            removeMethod(method)
            method.insertBefore("com.instructure.loginapi.login.util.MasqueradeUI.showMasqueradeNotification(this, $startingClass);")
            addMethod(method)
        } else {
            val newMethod = CtNewMethod.make(
                    """
                protected void onStart() {
                    super.onStart();
                    com.instructure.loginapi.login.util.MasqueradeUI.showMasqueradeNotification(this, $startingClass);
                }
                """.trimIndent(), this)
            addMethod(newMethod)
        }
    }

}
