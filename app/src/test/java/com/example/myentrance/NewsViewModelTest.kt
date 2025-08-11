package com.example.myentrance

import android.net.Uri
import app.cash.turbine.test
import com.example.myentrance.domain.entities.News
import com.example.myentrance.domain.entities.NewsWithUser
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.NewsRepository
import com.example.myentrance.presentation.viewmodel.NewsViewModel
import com.example.myentrance.domain.entities.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class NewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var newsRepository: NewsRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: NewsViewModel

    private val testUser = User(
        id = "test_user_id",
        name = "Test User",
        phoneNumber = "+79991234567"
    )

    private val testNewsWithUser = listOf(
        NewsWithUser(
            news = News(
                id = "news1",
                text = "Тестовая новость 1",
                imageUrl = "https://example.com/image1.jpg",
                createdAt = System.currentTimeMillis(),
                userId = "test_user_id",
                userName = "Test User"
            ),
            userName = "Test User",
            userAvatarUrl = "https://example.com/avatar.jpg"
        ),
        NewsWithUser(
            news = News(
                id = "news2",
                text = "Тестовая новость 2",
                imageUrl = null,
                createdAt = System.currentTimeMillis() - 1000,
                userId = "test_user_id",
                userName = "Test User"
            ),
            userName = "Test User",
            userAvatarUrl = null
        )
    )

    @Before
    fun setup() {
        newsRepository = mockk()
        authRepository = mockk()

        every { authRepository.getCurrentUser() } returns testUser

        viewModel = NewsViewModel(newsRepository, authRepository)
    }

    @Test
    fun `loadNews updates newsFeed with repository data`() = runTest {
        // Arrange
        coEvery { newsRepository.getNewsFeedWithUser() } returns testNewsWithUser

        // Act
        viewModel.loadNews()

        // Assert
        assertEquals(testNewsWithUser, viewModel.newsFeed.value)
        coVerify { newsRepository.getNewsFeedWithUser() }
    }

    @Test
    fun `addNews success emits success result and reloads news`() = runTest {
        // Arrange
        val testImageUri = mockk<Uri>()
        val newsText = "Новая тестовая новость"

        coEvery { newsRepository.addNews(any(), testImageUri) } returns Result.Success(Unit)
        coEvery { newsRepository.getNewsFeedWithUser() } returns testNewsWithUser

        // Act & Assert
        viewModel.addNewsResult.test {
            viewModel.addNews(newsText, testImageUri)

            val result = awaitItem()
            assertTrue(result is Result.Success)

            // Проверяем, что новости перезагрузились
            assertEquals(testNewsWithUser, viewModel.newsFeed.value)
        }
    }

    @Test
    fun `addNews without user does nothing`() = runTest {
        // Arrange
        every { authRepository.getCurrentUser() } returns null

        // Act
        viewModel.addNews("Test news", null)

        // Assert
        coVerify(exactly = 0) { newsRepository.addNews(any(), any()) }
    }

    @Test
    fun `addNews failure emits failure result without reloading`() = runTest {
        // Arrange
        val exception = Exception("Network error")
        coEvery { newsRepository.addNews(any(), any()) } returns Result.Failure(exception)

        // Act & Assert
        viewModel.addNewsResult.test {
            viewModel.addNews("Test news", null)

            val result = awaitItem()
            assertTrue(result is Result.Failure)
            assertEquals(exception, (result as Result.Failure).exception)
        }
    }
}
