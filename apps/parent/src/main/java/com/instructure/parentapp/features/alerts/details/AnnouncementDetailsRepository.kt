package com.instructure.parentapp.features.alerts.details

import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasModel
import com.instructure.canvasapi2.utils.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AnnouncementDetailsRepository @Inject constructor(
    private val announcementApi: AnnouncementAPI.AnnouncementInterface,
    private val courseApi: CourseAPI.CoursesInterface
) {
    fun getCourseAnnouncement(
        courseId: Long,
        announcementId: Long,
        forceNetwork: Boolean
    ): Flow<DataResult<CanvasModel<*>>> {
        return flow {
            emit(DataResult.Loading())
            try {
                val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
                val announcementResponse =
                    announcementApi.getCourseAnnouncement(courseId, announcementId, restParams)
                emit(DataResult.Loading(announcementResponse))

                val courseResponse = courseApi.getCourseDetails(courseId, restParams)
                emit(DataResult.Success(courseResponse))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                emit(DataResult.fromException(e))
            }
        }
    }
}