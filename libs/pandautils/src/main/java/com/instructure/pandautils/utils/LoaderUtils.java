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

package com.instructure.pandautils.utils;

import android.os.Bundle;

import com.instructure.pandautils.utils.Const;
import androidx.loader.app.LoaderManager;

public class LoaderUtils {


    /**
     * Helper utility to save loader bundles during onSaveInstanceState
     * @param outState - reference to a fragments outstate bundle
     * @param loaderBundle - reference to a loaders "data" bundle to be stored into outstate
     */
    public static void saveLoaderBundle(Bundle outState, Bundle loaderBundle){
        saveLoaderBundle(outState, loaderBundle, Const.LOADER_BUNDLE);
    }

    /**
     * overloaded version for unique loader ID
     * @param bundleId unique ID for bundle saveState
     */
    public static void saveLoaderBundle(Bundle outState, Bundle loaderBundle, String bundleId){
        if(loaderBundle != null && outState != null){
            outState.putBundle(bundleId, loaderBundle);
        }
    }


    /**
     * Helper utility to restore loader during fragment creation
     * @param loaderManager - loader manager from fragment
     * @param saveState - save state from fragment
     * @param callback - reference to the fragment that extends loaderCallbacks
     * @param loaderId - identifier for loader to restart
     * @param <G> - generic reference to callback
     */
    public static <G extends LoaderManager.LoaderCallbacks> void restoreLoaderFromBundle(LoaderManager loaderManager, Bundle saveState, G callback, int loaderId) {
        restoreLoaderFromBundle(loaderManager, saveState, callback, loaderId, Const.LOADER_BUNDLE);
    }


    /**
     * Overloaded version for unique loader ID, default is Const.LOADER_BUNDLE
     * @param bundleId - unique ID for bundle saveState
     */
    public static <G extends LoaderManager.LoaderCallbacks> void restoreLoaderFromBundle(LoaderManager loaderManager, Bundle saveState, G callback, int loaderId, String bundleId) {
        if (loaderManager != null && saveState != null && saveState.getBundle(bundleId) != null) {
            //This will restart any loaders previously started with this ID
            loaderManager.initLoader(loaderId, saveState.getBundle(bundleId), callback);
        }
    }


    /**
     * Helper utility to start new loader
     * @param loaderManager - loader manager from fragment
     * @param loaderBundle - arguments for loader
     * @param callback - reference to the fragment that extends loaderCallbacks
     * @param loaderId - identifier for loader to restart
     * @param <G> - generic reference to callback
     */
    public static <G extends LoaderManager.LoaderCallbacks> void restartLoaderWithBundle(LoaderManager loaderManager, Bundle loaderBundle, G callback, int loaderId){
        loaderManager.restartLoader(loaderId, loaderBundle, callback).forceLoad();
    }
}
