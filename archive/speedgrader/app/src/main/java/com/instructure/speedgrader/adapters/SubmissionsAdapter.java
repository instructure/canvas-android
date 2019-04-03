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

package com.instructure.speedgrader.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import java.util.ArrayList;

public class SubmissionsAdapter extends ArrayAdapter<Submission> {

    private ArrayList<Submission> objects = new ArrayList<Submission>();
    private LayoutInflater li;

    class ViewHolder{
        TextView sectionTitle;
        View rootView;
        ImageView expandArrow;
    }

    public SubmissionsAdapter(Context context, int textViewResourceId, ArrayList<Submission> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void add(Submission submission) {
        objects.add(submission);
        notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent,false);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent, boolean isDropDown) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = li.inflate(R.layout.rubric_spinner_item, parent, false);
            viewHolder.rootView = convertView.findViewById(R.id.rootView);
            viewHolder.sectionTitle = (TextView) convertView.findViewById(R.id.itemTitle);
            viewHolder.expandArrow = (ImageView) convertView.findViewById(R.id.expandArrow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(isDropDown){
            viewHolder.rootView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            viewHolder.expandArrow.setVisibility(View.GONE);
        }else{
            Drawable d = CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_arrow_down_fill, getContext().getResources().getColor(R.color.lightGray));
            viewHolder.expandArrow.setImageDrawable(d);
        }
        if(getItem(position).getSubmitDate() != null) {
            viewHolder.sectionTitle.setText(
                    DateHelpers.getFormattedDate(getContext(), objects.get(position).getSubmitDate()) + " " + DateHelpers.getFormattedTime(getContext(),objects.get(position).getSubmitDate())

                            + " (" + String.valueOf(objects.get(position).getScore()) + ")");
        }else{
            viewHolder.sectionTitle.setText(getContext().getString(R.string.noSubmissionForAssignment));
        }
        return convertView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) { }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) { }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Submission getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
