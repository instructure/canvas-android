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
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;

public class PassFailAdapter extends ArrayAdapter<PassFailAdapter.PassFail> {

    private LayoutInflater li;
    private boolean isUseRubric;
    private Context context;
    class ViewHolder{
        TextView sectionTitle;
        View rootView;
        ImageView expandArrow;
    }

    public enum PassFail {NO_GRADE, Pass, Fail};

    public PassFailAdapter(Context context, boolean isUseRubric) {
        super(context, R.layout.rubric_spinner_item);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isUseRubric = isUseRubric;
        this.context = context;
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

        if(PassFail.values()[position].toString().equals(PassFail.NO_GRADE.toString())){
            viewHolder.sectionTitle.setText(getContext().getString(R.string.noGrade));
        }else{
            viewHolder.sectionTitle.setText(PassFail.values()[position].toString());
        }

        if(isUseRubric){
           if(Build.VERSION.SDK_INT >= 15) {
               convertView.setBackground(getContext().getResources().getDrawable(R.drawable.card_dark));
           }else{
               convertView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.card_dark));
           }
        }
        return convertView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) { }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) { }

    @Override
    public int getCount() {
        return PassFail.values().length;
    }

    @Override
    public PassFailAdapter.PassFail getItem(int position) {
        return PassFail.values()[position];
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
    public boolean isEmpty() {
        return false;
    }

    public static PassFailAdapter.PassFail getPassFailGradeType(Context context, String grade){
        if(grade == null){ return PassFailAdapter.PassFail.NO_GRADE;}

        if(grade.equals(context.getString(R.string.complete).toLowerCase())){
            return PassFailAdapter.PassFail.Pass;
        } else if(grade.equals(context.getString(R.string.incomplete).toLowerCase())){
            return PassFailAdapter.PassFail.Fail;
        }else{
            return PassFailAdapter.PassFail.NO_GRADE;
        }
    }

    public static String getPassFailGradeString(Context context, PassFailAdapter.PassFail passFailType){
        if(passFailType.equals(PassFailAdapter.PassFail.Pass)){
            return context.getString(R.string.complete).toLowerCase();
        }else if(passFailType.equals(PassFailAdapter.PassFail.Fail)){
            return context.getString(R.string.incomplete).toLowerCase();
        }else{
            return "";
        }
    }
}