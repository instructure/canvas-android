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

package com.instructure.teacher.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.instructure.pandautils.utils.ActivityResult
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.RequestCodes.CAMERA_PIC_REQUEST
import com.instructure.pandautils.utils.RequestCodes.PICK_FILE_FROM_DEVICE
import com.instructure.pandautils.utils.RequestCodes.PICK_IMAGE_GALLERY
import com.instructure.pandautils.utils.postSticky

abstract class BaseAppCompatActivity : AppCompatActivity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_PIC_REQUEST ||
            requestCode == PICK_FILE_FROM_DEVICE ||
            requestCode == PICK_IMAGE_GALLERY) {
            //File Dialog Fragment will not be notified of onActivityResult(), alert manually
            OnActivityResults(ActivityResult(requestCode, resultCode, data), null).postSticky()
        }
    }
}
