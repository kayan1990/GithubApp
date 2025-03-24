
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.kayan.githubapp.common.constant.LanguageFilter
import com.kayan.githubapp.common.constant.OrderFilter
import com.kayan.githubapp.common.constant.TypeFilter
import com.kayan.githubapp.model.bean.Repository
import com.kayan.githubapp.model.bean.SearchResult
import com.kayan.githubapp.service.SearchService
import com.kayan.githubapp.ui.view.search.SearchAction
import com.kayan.githubapp.ui.view.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var searchService: SearchService
    private lateinit var viewModel: SearchViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchService = mockk()
        viewModel = SearchViewModel(searchService)
    }

    private fun mockSearchResult(): SearchResult<Repository> {
        val repo = Repository(
            id = 1,
            name = "ComposeSample",
            fullName = "JetBrains/ComposeSample",
            htmlUrl = "https://github.com/JetBrains/ComposeSample",
            description = "Sample project using Jetpack Compose",
            language = "Kotlin"
        )
        return SearchResult<Repository>().apply {
            totalCount = "1"
            incompleteResults = false
            items = arrayListOf(repo)
        }
    }

    @Test
    fun `dispatch OnSearch should update searchQuery and emit PagingData`() = runTest {
        // Mock searchService
        coEvery {
            searchService.searchRepos(
                query = any(),
                sort = any(),
                order = any(),
                page = any(),
                any()
            )
        } returns Response.success(mockSearchResult())

        // Dispatch OnSearch action
        val query = "Compose"
        viewModel.dispatch(SearchAction.OnSearch(query))

        assertEquals(query, viewModel.viewStates.searchQuery)
        assertNotNull(viewModel.viewStates.resultListFlow)

        // Verify PagingData emits
        val flow = viewModel.viewStates.resultListFlow!!
        flow.test {
            val pagingData = awaitItem()
            assertNotNull(pagingData)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dispatch OnUpdateFilter should update filters and emit PagingData`() = runTest {
        coEvery {
            searchService.searchRepos(
                query = any(),
                sort = any(),
                order = any(),
                page = any(),
                any()
            )
        } returns Response.success(mockSearchResult())

        val typeFilter = TypeFilter.BestMatch
        val orderFilter = OrderFilter.ASC
        val languageFilter = LanguageFilter.Kotlin

        viewModel.dispatch(SearchAction.OnUpdateFilter(typeFilter, orderFilter, languageFilter))

        assertEquals(typeFilter, viewModel.viewStates.typeFilter)
        assertEquals(orderFilter, viewModel.viewStates.orderFilter)
        assertEquals(languageFilter, viewModel.viewStates.languageFilter)
        assertNotNull(viewModel.viewStates.resultListFlow)

        viewModel.viewStates.resultListFlow!!.test {
            val pagingData = awaitItem()
            assertNotNull(pagingData)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearch should replace previous query`() = runTest {
        coEvery { searchService.searchRepos(any(), any(), any(), any(), any()) } returns Response.success(mockSearchResult())

        val firstQuery = "Android"
        viewModel.dispatch(SearchAction.OnSearch(firstQuery))
        assertEquals(firstQuery, viewModel.viewStates.searchQuery)

        val newQuery = "Compose"
        viewModel.dispatch(SearchAction.OnSearch(newQuery))
        assertEquals(newQuery, viewModel.viewStates.searchQuery)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
