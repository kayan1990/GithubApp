package com.kayan.githubapp

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.kayan.githubapp.common.constant.LanguageFilter
import com.kayan.githubapp.common.constant.OrderFilter
import com.kayan.githubapp.common.constant.TypeFilter
import com.kayan.githubapp.ui.view.search.SearchAction
import com.kayan.githubapp.ui.view.search.SearchScreen
import com.kayan.githubapp.ui.view.search.SearchViewModel
import com.kayan.githubapp.util.datastore.DataStoreUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@OptIn(ExperimentalMaterial3Api::class)
class SearchScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val activity get() = composeTestRule.activity

    lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<Context>()
        DataStoreUtils.init(context)
//        setContent()
    }

    private fun setContent(queryString: String? = null) {
        composeTestRule.activity.setContent {
            val scaffoldState = rememberBottomSheetScaffoldState()  // Real scaffoldState
            val navController = rememberNavController() // Real navController
            viewModel = hiltViewModel<SearchViewModel>()

            SearchScreen(
                navHostController = navController,
                queryString = queryString,
                viewModel = viewModel
            )
        }
    }

    @Test
    fun givenNoQuery_whenScreenLoads_showsSearchInput() {
        setContent("Compose")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Search", substring = true).assertIsDisplayed()
    }

    @Test
    fun sinceSearchQuery_whenUserTypesAndSearches_updatesSearchResult() {
        setContent()

        val inputText = "Android"
        composeTestRule.onNode(hasTestTag("SearchInput")).performTextInput(inputText)

        composeTestRule.onNodeWithContentDescription("Search").performClick()

        composeTestRule.runOnIdle {
            assertEquals(inputText, viewModel.viewStates.searchQuery)
            assertNotNull(viewModel.viewStates.resultListFlow)
        }
    }


    @Test
    fun sinceFilter_whenChanged_updatesSelection() {
        setContent()

        // 打开筛选弹窗
        composeTestRule.onNodeWithContentDescription("Filter").performClick()

        // 模拟用户切换 TypeFilter（这里直接发出action）
        composeTestRule.runOnIdle {
            viewModel.dispatch( SearchAction.OnUpdateFilter(
                    typeFilter = TypeFilter.Star,
                    orderFilter = OrderFilter.ASC,
                    languageFilter = LanguageFilter.Kotlin
                )
            )
        }

        // 校验ViewModel的过滤器状态被更新
        composeTestRule.runOnIdle {
            assertEquals(TypeFilter.Star, viewModel.viewStates.typeFilter)
            assertEquals(OrderFilter.ASC, viewModel.viewStates.orderFilter)
            assertEquals(LanguageFilter.Kotlin, viewModel.viewStates.languageFilter)
            assertNotNull(viewModel.viewStates.resultListFlow)
        }
    }


}