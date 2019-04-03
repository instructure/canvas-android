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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.loginapi.login.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DomainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int ITEM = 0, FOOTER = 1;

    public interface DomainEvents {
        void onDomainClick(AccountDomain account);
        void onHelpClick();
    }

    private List<AccountDomain> mOriginalAccounts = new ArrayList<>();
    private List<AccountDomain> mDisplayAccounts = new ArrayList<>();
    private Filter mFilter;
    private DomainEvents mCallback;

    public DomainAdapter(DomainEvents callback) {
        mCallback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ITEM) {
            return new DomainAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_account, parent, false));
        } else {
            return new DomainAdapter.FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_account_footer, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            AccountDomain account = mDisplayAccounts.get(position);
            if (account.getName() == null || account.getName().length() == 0) {
                viewHolder.schoolDomain.setText(account.getDomain());
            } else {
                viewHolder.schoolDomain.setText(account.getName());
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onDomainClick(mDisplayAccounts.get(holder.getAdapterPosition()));
                    }
                }
            });
        } else if(holder instanceof FooterViewHolder) {
            FooterViewHolder viewHolder = (FooterViewHolder) holder;
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCallback != null) {
                        mCallback.onHelpClick();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDisplayAccounts.size();
    }

    public int getTotalItemCount() {
        return mOriginalAccounts.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(getItemCount() == 1 || (position != 0 && position == getItemCount() - 1)) {
            return FOOTER;
        }
        return ITEM;
    }

    @Override
    public Filter getFilter() {
        if(mFilter == null) {
            mFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    final Locale locale = Locale.getDefault();
                    final String toMatch = constraint.toString().toLowerCase(locale);
                    final FilterResults filterResults = new FilterResults();

                    if(!TextUtils.isEmpty(toMatch)) {
                        ArrayList<AccountDomain> accountContains = new ArrayList<>();
                        for(AccountDomain account : mOriginalAccounts) {
                            if(account.getName().toLowerCase(locale).contains(toMatch) || toMatch.contains(account.getName().toLowerCase(locale)) ||
                                account.getDomain().toLowerCase(locale).contains(toMatch) || toMatch.contains(account.getDomain().toLowerCase(locale))) {

                                accountContains.add(account);
                            }
                        }

                        filterResults.count = accountContains.size();
                        filterResults.values = accountContains;
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    mDisplayAccounts = (ArrayList<AccountDomain>) results.values;
                    if(mDisplayAccounts == null) {
                        mDisplayAccounts = new ArrayList<>();
                    }

                    if(constraint != null && constraint.length() >= 3) {
                        //Help Footer
                        mDisplayAccounts.add(new AccountDomain());
                    }

                    notifyDataSetChanged();
                }
            };
        }
        return mFilter;
    }

    public void setItems(List<AccountDomain> originalAccounts) {
        mOriginalAccounts = originalAccounts;
        if(mDisplayAccounts != null) {
            mDisplayAccounts.clear();
        }
        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView schoolDomain;
        ViewHolder(View itemView) {
            super(itemView);
            schoolDomain = (TextView) itemView.findViewById(R.id.schoolDomain);
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
