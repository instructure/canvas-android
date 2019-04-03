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
package com.instructure.loginapi.login.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.snicker.SnickerDoodle;

import java.util.ArrayList;

public class SnickerDoodleAdapter extends RecyclerView.Adapter<SnickerDoodleAdapter.ViewHolder> {

    private ArrayList<SnickerDoodle> mSnickerDoodles;
    private SnickerCallback mCallback;

    public interface SnickerCallback {
        void onClick(SnickerDoodle snickerDoodle);
    }

    public SnickerDoodleAdapter(ArrayList<SnickerDoodle> snickerDoodles, SnickerCallback callback) {
        mSnickerDoodles = snickerDoodles;
        mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_snicker_doodle, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SnickerDoodle snickerDoodle = mSnickerDoodles.get(position);
        holder.title.setText(snickerDoodle.title);
        holder.subtitle.setText(snickerDoodle.subtitle);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onClick(mSnickerDoodles.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSnickerDoodles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title, subtitle;
        public ViewHolder(View v){
            super(v);
            title = (TextView)v.findViewById(R.id.title);
            subtitle = (TextView)v.findViewById(R.id.subtitle);
        }
    }
}
