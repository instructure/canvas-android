/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package instructure.instrumentationtests.robo;

import android.content.Context;
import android.support.annotation.NonNull;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import instructure.instrumentationtests.BuildConfig;

@RunWith(RoboTestRunner.class)
@Config(constants = BuildConfig.class, sdk = RoboTestRunner.DEFAULT_SDK)
public abstract class RoboTestCase extends TestCase {

    private RoboAppManager mApplication;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    protected @NonNull RoboAppManager application() {
        if (mApplication != null) {
            return mApplication;
        }
        mApplication = (RoboAppManager) RuntimeEnvironment.application;
        return mApplication;
    }

    protected @NonNull Context context() {
        return application().getApplicationContext();
    }
}
