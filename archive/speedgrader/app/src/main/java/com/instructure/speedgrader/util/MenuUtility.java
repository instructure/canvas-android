/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.speedgrader.util;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import com.instructure.speedgrader.fragments.ParentFragment;

import java.util.HashSet;

public class MenuUtility {

    /*
        We usually don't want to clear the menu, we usually just want to clear out duplicates.
        This is a helper to facilitate that.
     */
    public static boolean menuContains(Context context, Menu menu, int stringResource){
        if(context == null || menu == null){
            return false;
        }

        for(int i = 0; i < menu.size(); i++){
            MenuItem menuItem = menu.getItem(i);
            if(menuItem.getTitle().equals(context.getString(stringResource))){
                return true;
            }
        }
        return false;
    }

    /*
        We usually don't want to clear the menu, we usually just want to clear out duplicates.
        This is a helper to facilitate that.
     */
    public static void removeMenuOptions(Context context, Menu menu, int ... stringResources){
        if(context == null || menu == null || stringResources == null){
            return;
        }

        //We don't want this to be n^2 time. This allows it to be linear due to constant lookups.
        HashSet<String> menuItemsToRemove = new HashSet<String>();
        for(int integer : stringResources){
            menuItemsToRemove.add(context.getResources().getString(integer));
        }

        int i = 0;
        while (i < menu.size()){
            MenuItem menuItem = menu.getItem(i);
            if(menuItemsToRemove.contains(menuItem.getTitle().toString())){
                menu.removeItem(menuItem.getItemId());
            } else{
                i++;
            }
        }
    }
}
