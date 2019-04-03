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

import com.google.gson.Gson;
import com.instructure.canvasapi.model.KalturaConfig;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import retrofit.Callback;
import retrofit.http.GET;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class KalturaUnitTest extends Assert {

    @Test
    public void test1(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        KalturaConfig kalturaConfig = gson.fromJson(kalturaConfigJSON, KalturaConfig.class);

        assertNotNull(kalturaConfig);
        assertTrue(kalturaConfig.isEnabled());
        assertNotNull(kalturaConfig.getDomain().equals("www.instructuremedia.com"));
        assertTrue(kalturaConfig.getPartner_id() == 101);
    }


    //Kaltura Config
    //@GET("/services/kaltura")
    //void getKalturaConfigaration(Callback<KalturaConfig> callback);
    final String kalturaConfigJSON = "{\n" +
            "\"enabled\": true,\n" +
            "\"domain\": \"www.instructuremedia.com\",\n" +
            "\"resource_domain\": \"www.instructuremedia.com\",\n" +
            "\"rtmp_domain\": \"rtmp.instructuremedia.com\",\n" +
            "\"partner_id\": \"101\"\n" +
            "}";
}
