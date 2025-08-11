package com.example.myentrance

import app.cash.turbine.test
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.ProfileRepository
import com.example.myentrance.presentation.viewmodel.ProfileViewModel
import com.example.myentrance.domain.entities.Result
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var profileRepository: ProfileRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ProfileViewModel

    private val testUser = User(
        id = "test_id",
        name = "Test User",
        phoneNumber = "+79991234567",
        description = "Test description",
        apartmentNumber = "123",
        buildingId = "Building A",
        avatarUrl = "https://example.com/avatar.jpg"
    )

    @Before
    fun setup() {
        profileRepository = mockk()
        authRepository = mockk()

        every { authRepository.getCurrentUser() } returns testUser

        viewModel = ProfileViewModel(profileRepository, authRepository)
    }

    @Test
    fun `initial state loads user from authRepository`() = runTest {
        assertEquals(testUser, viewModel.user.value)
    }

    @Test
    fun `updateUser success emits success result and updates user`() = runTest {
        // Arrange
        val updatedUser = testUser.copy(name = "Updated Name")
        coEvery { profileRepository.updateUser(updatedUser) } returns Result.Success(Unit)
        coEvery { authRepository.saveCurrentUser(updatedUser) } just Runs

        // Act & Assert
        viewModel.updateResult.test {
            viewModel.updateUser(updatedUser)

            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(updatedUser, viewModel.user.value)
        }
    }

    @Test
    fun `logout emits logout event successfully`() = runTest {
        // Arrange
        coEvery { authRepository.logout() } just Runs

        // Act & Assert
        viewModel.logoutEvent.test {
            viewModel.logout()
            awaitItem() // Ожидаем emission события
        }
    }
}
