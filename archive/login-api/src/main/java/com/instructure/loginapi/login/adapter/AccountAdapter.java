/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.instructure.canvasapi.model.AccountDomain;
import com.instructure.loginapi.login.R;

import java.util.ArrayList;
import java.util.Locale;

public class AccountAdapter extends BaseAdapter implements Filterable {

    private LayoutInflater layoutInflater;
    private Activity activity;
    private ArrayList<AccountDomain> locationAccounts = new ArrayList<>();
    private ArrayList<AccountDomain> originalAccounts = new ArrayList<>();
    private ArrayList<AccountDomain> displayAccounts = new ArrayList<>();
    private Filter filter;

    public AccountAdapter(Activity activity, ArrayList<AccountDomain> originalAccounts, ArrayList<AccountDomain> locationAccounts) {
        this.activity = activity;
        this.layoutInflater = activity.getLayoutInflater();
        this.originalAccounts = originalAccounts;
        this.locationAccounts = locationAccounts;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public int getCount() {
        if(displayAccounts == null) {
            return 0;
        }
        return displayAccounts.size();
    }

    @Override
    public Object getItem(int position) {
        return displayAccounts.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.accounts_adapter_item, null);

            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        final AccountDomain account = displayAccounts.get(position);
        final Double distance = account.getDistance();

        viewHolder.name.setText(account.getName());

        if(distance == null) {
            viewHolder.distance.setText("");
            viewHolder.distance.setVisibility(View.GONE);
        } else {
            //distance is in meters
            //We calculate this in miles because the USA is dumb and still uses miles
            //At the time of this most of our customers are in the USA
            double distanceInMiles = (distance* 0.000621371192237334);
            double distanceInKm = distance / 1000;
            String distanceText = "";
            if(distanceInMiles < 1) {
                distanceText = activity.getString(R.string.lessThanOne);
            } else if(distanceInMiles - 1 < .1) {
                //we're 1 mile away
                distanceText = activity.getString(R.string.oneMile);
            } else {
                distanceText = String.format("%.1f", distanceInMiles) + " " + activity.getString(R.string.miles) + ", " + String.format("%.1f", distanceInKm) + " " + activity.getString(R.string.kilometers);
            }
            viewHolder.distance.setText(distanceText);
            viewHolder.distance.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter =  new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    final Locale locale = Locale.getDefault();
                    final String toMatch = constraint.toString().toLowerCase(locale);
                    final FilterResults filterResults = new FilterResults();

                    if(!TextUtils.isEmpty(toMatch)) {
                        ArrayList<AccountDomain> accountContains = new ArrayList<>();
                        for(AccountDomain account : originalAccounts) {

                            if(account.getName().toLowerCase(locale).contains(toMatch) ||
                                    toMatch.contains(account.getName().toLowerCase(locale)) ||
                                    account.getDomain().toLowerCase(locale).contains(toMatch) ||
                                    toMatch.contains(account.getDomain().toLowerCase(locale))) {
                                accountContains.add(account);
                            }
                        }

                        filterResults.count = accountContains.size();
                        filterResults.values = accountContains;

                    } else {
                        filterResults.count = locationAccounts.size();
                        filterResults.values = locationAccounts;
                    }

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    displayAccounts = (ArrayList<AccountDomain>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }

    public static class ViewHolder {
        TextView name;
        TextView distance;
    }
}
