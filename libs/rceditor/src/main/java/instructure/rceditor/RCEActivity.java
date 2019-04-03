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
package instructure.rceditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import static instructure.rceditor.RCEConst.BUTTON_COLOR;
import static instructure.rceditor.RCEConst.HTML_ACCESSIBILITY_TITLE;
import static instructure.rceditor.RCEConst.HTML_CONTENT;
import static instructure.rceditor.RCEConst.HTML_TITLE;
import static instructure.rceditor.RCEConst.THEME_COLOR;

public class RCEActivity extends AppCompatActivity implements RCEFragment.RCEFragmentCallbacks {

    private RCEFragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.rce_activity_layout);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof RCEFragment) {
            mFragment = (RCEFragment) fragment;
            mFragment.loadArguments(
                    getIntent().getStringExtra(HTML_CONTENT),
                    getIntent().getStringExtra(HTML_TITLE),
                    getIntent().getStringExtra(HTML_ACCESSIBILITY_TITLE),
                    getIntent().getIntExtra(THEME_COLOR, Color.BLACK),
                    getIntent().getIntExtra(BUTTON_COLOR, Color.BLACK)
            );
        }
        super.onAttachFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        mFragment.showExitDialog();
    }

    @Override
    public void onResult(int activityResult, @Nullable Intent data) {
        if (activityResult == RESULT_OK && data != null) {
            setResult(activityResult, data);
            finish();
        } else {
            setResult(activityResult);
            finish();
        }
    }

    public static Intent createIntent(
            Context context,
            String html,
            String title,
            String accessibilityTitle,
            @ColorInt int themeColor,
            @ColorInt int buttonColor) {
        Intent intent = new Intent(context, RCEActivity.class);
        intent.putExtra(HTML_CONTENT, html);
        intent.putExtra(HTML_TITLE, title);
        intent.putExtra(HTML_ACCESSIBILITY_TITLE, accessibilityTitle);
        intent.putExtra(THEME_COLOR, themeColor);
        intent.putExtra(BUTTON_COLOR, buttonColor);
        return intent;
    }
}
