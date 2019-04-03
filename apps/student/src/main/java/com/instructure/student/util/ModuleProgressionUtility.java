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

package com.instructure.student.util;

import android.content.Context;
import com.instructure.student.fragment.CourseModuleProgressionFragment;
import com.instructure.canvasapi2.models.ModuleItem;
import com.instructure.canvasapi2.models.ModuleObject;

import java.util.ArrayList;

public class ModuleProgressionUtility {

    public static class ModuleHelper {
        public ArrayList<ArrayList<ModuleItem>> strippedModuleItems;
        public int newGroupPosition;
        public int newChildPosition;
    }

    public static ModuleHelper prepareModulesForCourseProgression(Context context, long moduleItemId, ArrayList<ModuleObject> modules, ArrayList<ArrayList<ModuleItem>>moduleItems){

        ModuleHelper moduleHelper = new ModuleHelper();

        //we want to give CourseModuleProgressionFragment an arrayList without SubHeaders and ExternalTool moduleItems. We currently don't display
        //them and there isn't a good way to just skip over them during the progression.  This makes it easier to keep track of which item
        //we're on in the progression without having to do lots of checks/math to account for skipping over subheaders and external tools.

        //remove all the subHeaders and external tools items from the children list. We won't display them in module progression
        ArrayList<ArrayList<ModuleItem>> headerlessItems = new ArrayList<ArrayList<ModuleItem>>();
        for (int i = 0; i < modules.size(); i++) {
            headerlessItems.add(new ArrayList<ModuleItem>());
            for (int k = 0; k < moduleItems.get(i).size(); k++) {
                if (CourseModuleProgressionFragment.Companion.shouldAddModuleItem(context, moduleItems.get(i).get(k))) {
                    headerlessItems.get(i).add(moduleItems.get(i).get(k));
                }
            }
        }
        //at this point it's possible that we removed some subheaders or external tools from the arrayList, which could throw off the groupPosition and childPosition. This
        //will get the new, correct group and child position of the module item
        int newGroupPos = 0;
        int newChildPos = 0;
        for (int i = 0; i < headerlessItems.size(); i++) {
            for (int k = 0; k < headerlessItems.get(i).size(); k++) {
                if (moduleItemId == (headerlessItems.get(i).get(k)).getId()) {
                    newGroupPos = i;
                    newChildPos = k;
                    break;
                }
            }
        }

        moduleHelper.strippedModuleItems = headerlessItems;
        moduleHelper.newGroupPosition = newGroupPos;
        moduleHelper.newChildPosition = newChildPos;

        return moduleHelper;
    }
}
