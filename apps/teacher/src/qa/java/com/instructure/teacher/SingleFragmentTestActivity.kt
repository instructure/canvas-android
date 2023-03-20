/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.teacher.databinding.ActivitySingleFragmentTestBinding

class SingleFragmentTestActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivitySingleFragmentTestBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.container.id, fragment, fragment.javaClass.simpleName)
        transaction.commitAllowingStateLoss()
    }

    fun loadView(view: View, width: Int, height: Int) {
        val params = FrameLayout.LayoutParams(width, height)
        binding.container.addView(view, params)
    }
}
