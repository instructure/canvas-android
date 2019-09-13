package com.instructure.student.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class ShardE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.NONE, TestCategory.E2E, true)
    fun testShardE2E() {
        // TODO: Test against institutions across multiple shards, for courses/assignments/etc... that have cross shard ids
        //  (i.e., 12345~1234 instead of 1234500000000001234)
    }
}