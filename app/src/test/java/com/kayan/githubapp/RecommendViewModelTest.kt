
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kayan.githubapp.common.constant.LanguageFilter
import com.kayan.githubapp.model.bean.TrendingRepoModel
import com.kayan.githubapp.model.ui.ReposUIModel
import com.kayan.githubapp.service.RepoService
import com.kayan.githubapp.ui.view.recommend.RecommendAction
import com.kayan.githubapp.ui.view.recommend.RecommendEvent
import com.kayan.githubapp.ui.view.recommend.RecommendSinceFilter
import com.kayan.githubapp.ui.view.recommend.RecommendViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: RecommendViewModel
    private val repoService: RepoService = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RecommendViewModel(repoService)
    }

    @Test
    fun `test refreshData success`() = runTest {
        val mockApiData = listOf(TrendingRepoModel().apply {
            name = "Repo1"
            contributors = listOf("User1", "User2")
        })

        val expectedUIModel = listOf(ReposUIModel().apply {
            ownerName = "Repo1"
            ownerPic = "User1"
        })

        coEvery { repoService.getTrendDataAPI(any(), any(), any(), any()) } returns Response.success(mockApiData)

        viewModel.dispatch(RecommendAction.RefreshData(true))
        advanceUntilIdle()

        assertEquals(expectedUIModel.first().ownerName, viewModel.viewStates.dataList.first().ownerName )
        assertFalse(viewModel.viewStates.isRefreshing)
    }

    @Test
    fun `test refreshData failure`() = runTest {
        coEvery { repoService.getTrendDataAPI(any(), any(), any(), any()) } returns Response.error(500, "Server Error".toResponseBody())

        viewModel.dispatch(RecommendAction.RefreshData(true))
        advanceUntilIdle()

        assertFalse(viewModel.viewStates.isRefreshing)
        assertTrue(viewModel.viewStates.dataList.isEmpty())
    }

    @Test
    fun `test changeLanguage triggers refresh`() = runTest {
        val mockApiData = listOf(TrendingRepoModel().apply {
            name = "Repo1"
            contributors = listOf("User1", "User2")
        })

        val expectedUIModel = listOf(ReposUIModel().apply {
            ownerName = "Repo1"
            ownerPic = "User1"
        })

        coEvery { repoService.getTrendDataAPI(any(), any(), any(), any()) } returns Response.success(mockApiData)
        val languageFilter = LanguageFilter.Java
        viewModel.dispatch(RecommendAction.ChangeLanguage(languageFilter))
        advanceUntilIdle()

        assertEquals(languageFilter, viewModel.viewStates.languageFilter)
    }

    @Test
    fun `test changeSinceFilter triggers refresh`() = runTest {
        val mockApiData = listOf(TrendingRepoModel().apply {
            name = "Repo1"
            contributors = listOf("User1", "User2")
        })

        val expectedUIModel = listOf(ReposUIModel().apply {
            ownerName = "Repo1"
            ownerPic = "User1"
        })

        coEvery { repoService.getTrendDataAPI(any(), any(), any(), any()) } returns Response.success(mockApiData)
        val sinceFilter = RecommendSinceFilter.Weekly
        viewModel.dispatch(RecommendAction.ChangeSinceFilter(sinceFilter))
        advanceUntilIdle()

        assertEquals(sinceFilter, viewModel.viewStates.sinceFilter)
    }

    @Test
    fun `test topOrRefresh event triggered`() = runTest {
        viewModel.dispatch(RecommendAction.TopOrRefresh)
        val event = viewModel.viewEvents.first()
        assertTrue(event is RecommendEvent.TopOrRefresh)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
