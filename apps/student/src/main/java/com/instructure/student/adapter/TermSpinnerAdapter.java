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

package com.instructure.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.instructure.student.R;
import com.instructure.canvasapi2.models.GradingPeriod;

import java.util.List;

public class TermSpinnerAdapter extends ArrayAdapter<GradingPeriod> {

    private List<GradingPeriod> mGradingPeriods;
    private LayoutInflater mInflater;
    private boolean mIsLoading;

    public TermSpinnerAdapter(Context context, int resource, List<GradingPeriod> objects) {
        super(context, resource, objects);
        mGradingPeriods = objects;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TermSpinnerViewHolder holder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.term_spinner_view, parent, false);

            holder = new TermSpinnerViewHolder();
            holder.periodName = (TextView)convertView.findViewById(R.id.periodName);
            holder.dropDown = (ImageView)convertView.findViewById(R.id.dropDownArrow);
            holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
            convertView.setTag(holder);
        } else {
            holder = ((TermSpinnerViewHolder)convertView.getTag());
        }
        if(mIsLoading){
            holder.dropDown.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.VISIBLE);
        } else {
            holder.dropDown.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }

        if(mGradingPeriods.get(position) != null) {
            holder.periodName.setText(mGradingPeriods.get(position).getTitle());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TermDropDownViewHolder holder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_row_grading_period, parent, false);

            holder = new TermDropDownViewHolder();
            holder.periodName = (TextView)convertView.findViewById(R.id.periodName);
            convertView.setTag(holder);
        } else {
            holder = ((TermDropDownViewHolder)convertView.getTag());
        }

        if(mGradingPeriods.get(position) != null) {
            holder.periodName.setText(mGradingPeriods.get(position).getTitle());
        }
        return convertView;
    }

    public int getPositionForId(long id){
        for(int i = 0; i < getCount(); i++){
            if(getItem(i).getId() == id){
                return i;
            }
        }
        return -1;
    }

    private static class TermSpinnerViewHolder {
        TextView periodName;
        ImageView dropDown;
        ProgressBar progressBar;
    }

    private static class TermDropDownViewHolder {
        TextView periodName;
    }

    public void setIsLoading(boolean isLoading){
        mIsLoading = isLoading;
    }

}
