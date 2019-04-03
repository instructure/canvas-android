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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.utils.ColorUtils;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.pandautils.views.AttachmentView;
import com.instructure.teacher.R;

import java.util.List;
import java.util.Locale;

public class FileUploadAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileSubmitObject> mFileSubmitObjects;
    @Nullable private OnFileEvent mCallback;

    public interface OnFileEvent {
        void onRemoveFile();
    }

    public FileUploadAdapter(Context context, List<FileSubmitObject> fileSubmitObjects) {
        mContext = context;
        mFileSubmitObjects = fileSubmitObjects;
    }

    public FileUploadAdapter(Context context, List<FileSubmitObject> fileSubmitObjects, @Nullable OnFileEvent callback) {
        mContext = context;
        mFileSubmitObjects = fileSubmitObjects;
        mCallback = callback;
    }


    @Override
    public int getCount() {
        return mFileSubmitObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileSubmitObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mFileSubmitObjects.get(position).getName().hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
                /* There is no view at this position, we create a new one.
		           In this case by inflating an xml layout */
            convertView = (LayoutInflater.from(mContext).inflate(R.layout.listview_item_row_attachedfiles, null));

            holder = new ViewHolder();
            holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            holder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            holder.remove = (ImageView) convertView.findViewById(R.id.remove_file);
            holder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            holder.uploadProgress = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            convertView.setTag(holder);
        } else {
				/* We recycle a View that already exists */
            holder = (ViewHolder) convertView.getTag();
        }

        FileSubmitObject fso = mFileSubmitObjects.get(position);

        AttachmentView.Companion.setColorAndIcon(mContext, fso.getContentType(), fso.getName(), null, holder.fileIcon);

        ColorUtils.colorIt(ThemePrefs.getBrandColor(), holder.fileIcon);

        if (fso.getCurrentState() == FileSubmitObject.STATE.UPLOADING) {
            holder.uploadProgress.setIndeterminate(true);
            holder.uploadProgress.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
            holder.remove.setVisibility(View.GONE);
        } else if (fso.getCurrentState() == FileSubmitObject.STATE.COMPLETE) {
            holder.fileIcon.setImageResource(R.drawable.vd_checkmark);
            holder.remove.setVisibility(View.GONE);
            holder.uploadProgress.setIndeterminate(false);
            holder.uploadProgress.setVisibility(View.GONE);
            holder.fileIcon.setVisibility(View.VISIBLE);
        } else if (fso.getCurrentState() == FileSubmitObject.STATE.NORMAL) {
            holder.remove.setImageResource(R.drawable.vd_utils_close);
            holder.remove.setContentDescription(mContext.getString(R.string.utils_removeAttachment));
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //they are added in the same order, so the position should be the same
                    mFileSubmitObjects.remove(position);
                    notifyDataSetChanged();
                    if(mCallback != null) {
                        mCallback.onRemoveFile();
                    }
                }
            });
        }

        holder.fileName.setText(mFileSubmitObjects.get(position).getName());
        holder.fileSize.setText(humanReadableByteCount(mFileSubmitObjects.get(position).getSize()));

        return convertView;
    }

    public void setFileState(FileSubmitObject fso, FileSubmitObject.STATE newState) {
        int index = mFileSubmitObjects.indexOf(fso);
        if (index != -1) {
            mFileSubmitObjects.get(index).setState(newState);
        }
        notifyDataSetChanged();
    }

    public void setFilesToUploading() {
        for (FileSubmitObject fso : mFileSubmitObjects) {
            fso.setState(FileSubmitObject.STATE.UPLOADING);
        }
        notifyDataSetChanged();
    }


    public void clear() {
        mFileSubmitObjects.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
        ImageView remove;
        ProgressBar uploadProgress;
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = String.valueOf(("KMGTPE").charAt(exp - 1));
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}

