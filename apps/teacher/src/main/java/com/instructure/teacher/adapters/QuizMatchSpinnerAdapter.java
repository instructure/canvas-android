/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.instructure.canvasapi2.models.QuizSubmissionMatch;
import com.instructure.teacher.R;

import java.util.ArrayList;


public class QuizMatchSpinnerAdapter extends ArrayAdapter<QuizSubmissionMatch> {

    ArrayList<QuizSubmissionMatch> matches;
    private LayoutInflater inflater;

    public QuizMatchSpinnerAdapter(Context context, int textViewResourceId,
                                   ArrayList<QuizSubmissionMatch> objects) {
        super(context, textViewResourceId, objects);
        matches = objects;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        final MatchViewHolder viewHolder;


        if(convertView == null) {
            convertView = inflater.inflate(R.layout.actionbar_course_list_spinner_dropdown, parent, false);
            viewHolder = new MatchViewHolder();
            viewHolder.title = (TextView)convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MatchViewHolder)convertView.getTag();
        }

        viewHolder.title.setText(matches.get(position).getText());
        return convertView;
    }

    private static class MatchViewHolder {
        TextView title;
    }

}
