/*
 * Copyright (c) 2013 by Roomorama Inc.
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

package com.roomorama.caldroid;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.caldroid.R;

/**
 * DateGridFragment contains only 1 gridview with 7 columns to display all the
 * dates within a month.
 * <p/>
 * Client must supply gridAdapter and onItemClickListener before the fragment is
 * attached to avoid complex crash due to fragment life cycles.
 */
public class DateGridFragment extends Fragment {
    private GridView gridView;
    private CaldroidGridAdapter gridAdapter;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private int gridViewRes = 0;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public CaldroidGridAdapter getGridAdapter() {
        return gridAdapter;
    }

    public void setGridAdapter(CaldroidGridAdapter gridAdapter) {
        this.gridAdapter = gridAdapter;
    }

    public GridView getGridView() {
        return gridView;
    }

    public void setGridViewRes(int gridViewRes) {
        this.gridViewRes = gridViewRes;
    }

    private void setupGridView() {
        // Client normally needs to provide the adapter and onItemClickListener
        // before the fragment is attached to avoid complex crash due to
        // fragment life cycles
        if (gridAdapter != null) {
            gridView.setAdapter(gridAdapter);
        }

        if (onItemClickListener != null) {
            gridView.setOnItemClickListener(onItemClickListener);
        }
        if (onItemLongClickListener != null) {
            gridView.setOnItemLongClickListener(onItemLongClickListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // If gridViewRes is not valid, use default fragment layout
        if (gridViewRes == 0) {
            gridViewRes = R.layout.date_grid_fragment;
        }

        if (gridView == null) {
            gridView = (GridView) inflater.inflate(gridViewRes, container, false);
            setupGridView();
        } else {
            ViewGroup parent = (ViewGroup) gridView.getParent();
            if (parent != null) {
                parent.removeView(gridView);
            }
        }

        return gridView;
    }

}
