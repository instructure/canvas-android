package com.instructure.pandautils.domain.usecase.enrollment

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class HandleCourseInvitationUseCaseTest {

    private val enrollmentRepository: EnrollmentRepository = mockk(relaxed = true)
    private lateinit var useCase: HandleCourseInvitationUseCase

    @Before
    fun setup() {
        useCase = HandleCourseInvitationUseCase(enrollmentRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute accepts invitation successfully`() = runTest {
        coEvery {
            enrollmentRepository.handleInvitation(any(), any(), any())
        } returns DataResult.Success(Unit)

        useCase(
            HandleCourseInvitationParams(
                courseId = 100L,
                enrollmentId = 1L,
                accept = true
            )
        )

        coVerify {
            enrollmentRepository.handleInvitation(
                courseId = 100L,
                enrollmentId = 1L,
                accept = true
            )
        }
    }

    @Test
    fun `execute declines invitation successfully`() = runTest {
        coEvery {
            enrollmentRepository.handleInvitation(any(), any(), any())
        } returns DataResult.Success(Unit)

        useCase(
            HandleCourseInvitationParams(
                courseId = 200L,
                enrollmentId = 2L,
                accept = false
            )
        )

        coVerify {
            enrollmentRepository.handleInvitation(
                courseId = 200L,
                enrollmentId = 2L,
                accept = false
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when repository fails`() = runTest {
        coEvery {
            enrollmentRepository.handleInvitation(any(), any(), any())
        } returns DataResult.Fail()

        useCase(
            HandleCourseInvitationParams(
                courseId = 100L,
                enrollmentId = 1L,
                accept = true
            )
        )
    }
}