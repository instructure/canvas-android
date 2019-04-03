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
import com.instructure.canvasapi.model.CommunicationChannel;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import junit.framework.Assert;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class CommunicationChannelUnitTest extends Assert{

    @Test
    public void testCommunicationChannel(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        CommunicationChannel[] communicationChannels = gson.fromJson(communicationChannelJSON, CommunicationChannel[].class);

        assertNotNull(communicationChannels);
        assertEquals(3, communicationChannels.length);

        for(CommunicationChannel communicationChannel : communicationChannels){
            assertNotNull(communicationChannel.getId());
            assertNotNull(communicationChannel.getPosition());
            assertNotNull(communicationChannel.getUserId());
            assertNotNull(communicationChannel.getWorkflowState());
            assertNotNull(communicationChannel.getAddress());
            assertNotNull(communicationChannel.getType());
        }
    }

    private static final String communicationChannelJSON = "["
            +"{\"id\":123245,"
                +"\"position\":1,"
                +"\"user_id\":123245,"
                +"\"workflow_state\":\"active\","
                +"\"address\":\"test@test.com\","
                +"\"type\":\"email\"},"
            +"{\"id\":123245,"
                +"\"position\":2,"
                +"\"user_id\":123245,"
                +"\"workflow_state\":\"active\","
                +"\"address\":\"test@test.com\","
                +"\"type\":\"email\"},"
            +"{\"id\":123245,"
                +"\"position\":3,"
                +"\"user_id\":123245,"
                +"\"workflow_state\":\"active\","
                +"\"address\":\"For All Devices\","
                +"\"type\":\"push\"}"
            +"]";
}
