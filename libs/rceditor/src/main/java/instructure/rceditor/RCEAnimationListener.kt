/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package instructure.rceditor

import android.animation.Animator
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class RCEAnimationListener : Animator.AnimatorListener {
    open fun onAnimationBegin(animation: Animator) {}
    open fun onAnimationFinish(animation: Animator) {}
    override fun onAnimationCancel(animation: Animator) {}
    override fun onAnimationRepeat(animation: Animator) {}
    override fun onAnimationStart(animation: Animator) = onAnimationBegin(animation)
    override fun onAnimationEnd(animation: Animator) = onAnimationFinish(animation)
}
