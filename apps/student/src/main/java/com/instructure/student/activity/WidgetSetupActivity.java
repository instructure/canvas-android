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

package com.instructure.student.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.CompoundButton;

import com.instructure.student.R;
import com.instructure.student.util.StudentPrefs;
import com.instructure.student.widget.WidgetUpdater;

public class WidgetSetupActivity extends AppCompatActivity {

    public static final String WIDGET_BACKGROUND_COLOR_KEY = "widgetBackgroundColorKey";
    public static final String WIDGET_BACKGROUND_COLOR_LIGHT = "widgetBackgroundColorLight";
    public static final String WIDGET_BACKGROUND_COLOR_DARK = "widgetBackgroundColorDark";
    public static final String WIDGET_BACKGROUND_PREFIX = "widgetBackground__";
    public static final String WIDGET_DETAILS_PREFIX = "widgetDetails__";

    protected CardView mCardDark, mCardLight;
    protected AppCompatCheckBox mCheckBox;

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the result canceled so if the user decides not to setup the widget it does not get added
        setResult(Activity.RESULT_CANCELED);
        setContentView(R.layout.activity_widget_setup);

        mCardDark = findViewById(R.id.cardDark);
        mCardLight = findViewById(R.id.cardLight);
        mCardDark.setOnClickListener(mCardClickListener);
        mCardLight.setOnClickListener(mCardClickListener);

        mCheckBox = findViewById(R.id.check_box);
        mCheckBox.setOnCheckedChangeListener(mWidgetDetailsCheckChangeListener);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            //get the widget id we are adding/updating
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        //If our appWidgetId is bad or does not exist we exit the activity
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    private View.OnClickListener mCardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            switch (v.getId()) {
                case R.id.cardDark: {
                    resultValue.putExtra(WIDGET_BACKGROUND_COLOR_KEY, WIDGET_BACKGROUND_COLOR_DARK);
                    StudentPrefs.INSTANCE.putString(WIDGET_BACKGROUND_PREFIX + appWidgetId, WIDGET_BACKGROUND_COLOR_DARK);
                    break;
                }
                case R.id.cardLight: {
                    resultValue.putExtra(WIDGET_BACKGROUND_COLOR_KEY, WIDGET_BACKGROUND_COLOR_LIGHT);
                    StudentPrefs.INSTANCE.putString(WIDGET_BACKGROUND_PREFIX + appWidgetId, WIDGET_BACKGROUND_COLOR_LIGHT);
                    break;
                }
            }
            setResult(RESULT_OK, resultValue);
            finish();

            WidgetUpdater.updateWidgets();
        }
    };

    private CompoundButton.OnCheckedChangeListener mWidgetDetailsCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            StudentPrefs.INSTANCE.putBoolean(WIDGET_DETAILS_PREFIX + appWidgetId, isChecked);
        }
    };
}
