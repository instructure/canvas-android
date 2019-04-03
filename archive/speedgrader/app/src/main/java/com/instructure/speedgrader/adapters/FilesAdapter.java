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
import com.instructure.canvasapi.model.Attachment;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import java.util.ArrayList;

public class FilesAdapter extends ArrayAdapter<Attachment> {

    private ArrayList<Attachment> attachments = new ArrayList<Attachment>();
    private LayoutInflater li;

    class ViewHolder{
        TextView sectionTitle;
        View rootView;
        ImageView expandArrow;
    }

    public FilesAdapter(Context context, int textViewResourceId,  ArrayList<Attachment> objects) {
        super(context, textViewResourceId, objects);
        this.attachments = objects;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addAll(ArrayList<Attachment> collection) {
        attachments = collection;
        notifyDataSetChanged();
    }

    @Override
    public void add(Attachment attachment) {
        attachments.add(attachment);
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

        viewHolder.sectionTitle.setText(attachments.get(position).getDisplayName());

        return convertView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) { }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) { }

    @Override
    public int getCount() {
        return attachments.size();
    }

    @Override
    public Attachment getItem(int position) {
        return attachments.get(position);
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