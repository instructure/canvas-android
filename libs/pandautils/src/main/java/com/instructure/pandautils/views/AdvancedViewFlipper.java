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

package com.instructure.pandautils.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.ViewFlipper;


public class AdvancedViewFlipper extends ViewFlipper {

    public AdvancedViewFlipper(Context context) {
        super(context);
    }


    public AdvancedViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDisplayedChild(int index, Animation in, Animation out) {
        Animation currentIn = this.getInAnimation();
        Animation currentOut = this.getOutAnimation();

        this.setInAnimation(in);
        this.setOutAnimation(out);

        super.setDisplayedChild(index);

        this.setInAnimation(currentIn);
        this.setOutAnimation(currentOut);
    }

    public void showNext(Animation in, Animation out) {
        Animation currentIn = this.getInAnimation();
        Animation currentOut = this.getOutAnimation();

        this.setInAnimation(in);
        this.setOutAnimation(out);

        super.showNext();

        this.setInAnimation(currentIn);
        this.setOutAnimation(currentOut);
    }

    public void showPrevious(Animation in, Animation out) {
        Animation currentIn = this.getInAnimation();
        Animation currentOut = this.getOutAnimation();

        this.setInAnimation(in);
        this.setOutAnimation(out);

        super.showPrevious();

        this.setInAnimation(currentIn);
        this.setOutAnimation(currentOut);
    }
}