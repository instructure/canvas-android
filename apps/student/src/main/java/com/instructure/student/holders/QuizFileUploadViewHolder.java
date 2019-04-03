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

package com.instructure.student.holders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.instructure.student.R;
import com.instructure.pandautils.views.CanvasWebView;


public class QuizFileUploadViewHolder extends RecyclerView.ViewHolder {

    public CanvasWebView question;
    public TextView questionNumber;
    public View divider;
    public Button uploadFileButton;
    public TextView fileName;
    public ImageView fileIcon;
    public ProgressBar progressBar;
    public Button remove;
    public FloatingActionButton flag;

    public QuizFileUploadViewHolder(View itemView) {
        super(itemView);
        questionNumber = (TextView) itemView.findViewById(R.id.question_number);
        question = (CanvasWebView) itemView.findViewById(R.id.quiz_question);
        flag = (FloatingActionButton) itemView.findViewById(R.id.flag_question);
        divider = itemView.findViewById(R.id.divider);
        uploadFileButton = (Button)itemView.findViewById(R.id.file_upload_button);
        fileName = (TextView) itemView.findViewById(R.id.file_name);
        fileIcon = (ImageView) itemView.findViewById(R.id.file_icon);
        remove = (Button) itemView.findViewById(R.id.remove_file);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
    }

    public static int adapterResId() { return R.layout.quiz_file_upload; }
}
