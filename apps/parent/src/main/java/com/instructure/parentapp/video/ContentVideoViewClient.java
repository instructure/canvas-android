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
package com.instructure.parentapp.video;
// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import android.view.View;

/**
 *  Main callback class used by ContentVideoView.
 *
 *  This contains the superset of callbacks that must be implemented by the embedder.
 *
 *  onShowCustomView and onDestoryContentVideoView must be implemented,
 *  getVideoLoadingProgressView() is optional, and may return null if not required.
 *
 *  The implementer is responsible for displaying the Android view when
 *  {@link #onShowCustomView(View)} is called.
 */
public interface ContentVideoViewClient {
    /**
     * Called when the video view is ready to be shown. Must be implemented.
     * @param view The view to show.
     */
    public void onShowCustomView(View view);

    /**
     * Called when it's time to destroy the video view. Must be implemented.
     */
    public void onDestroyContentVideoView();

    public boolean isFullscreen();

    /**
     * Allows the embedder to replace the view indicating that the video is loading.
     * If null is returned, the default video loading view is used.
     */
    public View getVideoLoadingProgressView();

    /**
     * Allows the embedder to replace the default playback controls by returning a custom
     * implementation. If null is returned, the default controls are used.
     */
    //public ContentVideoViewControls createControls();
}
