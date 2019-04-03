/*
 * Copyright (c) 2012 Antony Tran
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.antonyt.infiniteviewpager;

import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * A PagerAdapter that wraps around another PagerAdapter to handle paging
 * wrap-around.
 * 
 */
public class InfinitePagerAdapter extends PagerAdapter {
	private PagerAdapter adapter;

	public InfinitePagerAdapter(PagerAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public int getCount() {
		// warning: scrolling to very high values (1,000,000+) results in
		// strange drawing behaviour
		return Integer.MAX_VALUE;
	}

	/**
	 * @return the {@link #getCount()} result of the wrapped adapter
	 */
	public int getRealCount() {
		return adapter.getCount();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		int virtualPosition = position % getRealCount();
		// only expose virtual position to the inner adapter
		return adapter.instantiateItem(container, virtualPosition);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		int virtualPosition = (position) % getRealCount();
		// only expose virtual position to the inner adapter
		adapter.destroyItem(container, virtualPosition, object);
	}

	/*
	 * Delegate rest of methods directly to the inner adapter.
	 */

	@Override
	public void finishUpdate(ViewGroup container) {
		adapter.finishUpdate(container);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return adapter.isViewFromObject(view, object);
	}

	@Override
	public void restoreState(Parcelable bundle, ClassLoader classLoader) {
		adapter.restoreState(bundle, classLoader);
	}

	@Override
	public Parcelable saveState() {
		return adapter.saveState();
	}

	@Override
	public void startUpdate(ViewGroup container) {
		adapter.startUpdate(container);
	}
}
