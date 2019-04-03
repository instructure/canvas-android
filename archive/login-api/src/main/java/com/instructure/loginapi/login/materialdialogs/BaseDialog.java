/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.materialdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;

/**
 * Created by Lewis on 30/08/2014.
 */
public abstract class BaseDialog extends AlertDialog {

    static enum LightColours {
        TITLE("#474747"), CONTENT("#999999"), ITEM("#999999"), BUTTON("#212121");

        final String mColour;

        private LightColours(String _colour) {
            this.mColour = _colour;
        }
    }

    static enum DarkColours {
        TITLE("#CCCCCC"), CONTENT("#999999"), ITEM("#999999"), BUTTON("#CCCCCC");

        final String mColour;

        private DarkColours(String _colour) {
            this.mColour = _colour;
        }
    }

    static enum Theme {
        LIGHT, DARK;
    }

    public static enum Alignment {
        LEFT, CENTER, RIGHT;
    }

    BaseDialog(Context _context) {
        super(_context);
    }

    static int getGravityFromAlignment(Alignment _alignment) {
        // Return corresponding gravity from our Alignment value.
        switch (_alignment) {
            case LEFT:
                return Gravity.LEFT;
            case CENTER:
                return Gravity.CENTER;
            case RIGHT:
                return Gravity.RIGHT;
            default:
                return Gravity.LEFT;
        }
    }
}
