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

package com.instructure.student.view;

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

	public void setDisplayedChild(int index, Animation in, Animation out)
	{
		Animation currentIn = this.getInAnimation();
		Animation currentOut = this.getOutAnimation();
		
		this.setInAnimation(in);
		this.setOutAnimation(out);
		
		super.setDisplayedChild(index);
		
		this.setInAnimation(currentIn);
		this.setOutAnimation(currentOut);
	}
	
	public void showNext(Animation in, Animation out)
	{
		Animation currentIn = this.getInAnimation();
		Animation currentOut = this.getOutAnimation();
		
		this.setInAnimation(in);
		this.setOutAnimation(out);
		
		super.showNext();
		
		this.setInAnimation(currentIn);
		this.setOutAnimation(currentOut);
	}
	
	public void showPrevious(Animation in, Animation out)
	{
		Animation currentIn = this.getInAnimation();
		Animation currentOut = this.getOutAnimation();
		
		this.setInAnimation(in);
		this.setOutAnimation(out);
		
		super.showPrevious();
		
		this.setInAnimation(currentIn);
		this.setOutAnimation(currentOut);
	}
}
