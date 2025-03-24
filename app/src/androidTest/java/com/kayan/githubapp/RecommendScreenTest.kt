package com.kayan.githubapp

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performGesture
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.swipeDown
import com.kayan.githubapp.service.RepoService
import com.kayan.githubapp.ui.view.recommend.RecommendContent
import com.kayan.githubapp.ui.view.recommend.RecommendViewModel
import com.kayan.githubapp.util.datastore.DataStoreUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalMaterial3Api::class)
class RecommendScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var repository: RepoService  //

    lateinit var viewModel: RecommendViewModel


    @Before
    fun setup() {
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<Context>()
        DataStoreUtils.init(context)
        setContent()
    }

    private fun setContent() {
        composeTestRule.activity.setContent {
            val scaffoldState = rememberBottomSheetScaffoldState()  // Real scaffoldState
            val navController = rememberNavController() // Real navController
            viewModel = hiltViewModel<RecommendViewModel>()

            RecommendContent(
                scaffoldState = scaffoldState,
                navController = navController,
                viewModel = viewModel
            )
        }
    }

    @Test
    fun recommendScreen_whenLaunched_showsFiltersAndData() {
        // Verify the filters are visible
        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
        composeTestRule.onNodeWithText("All").assertIsDisplayed()

    }

    @Test
    fun sinceFilter_whenChanged_updatesSelection() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Daily").performClick()
        composeTestRule.onNodeWithText("Weekly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly").assertIsDisplayed()

        composeTestRule.onNodeWithText("Monthly").performClick()
        composeTestRule.waitUntil {
            !viewModel.viewStates.isRefreshing
        }

        val listItems =
            composeTestRule.onAllNodesWithContentDescription("RepoItem").fetchSemanticsNodes()
        assert(listItems.isNotEmpty()) { "List should not be empty" }

    }

    @Test
    fun languageFilter_whenChanged_updatesSelection() {
        composeTestRule.onNodeWithText("All").performClick()
        composeTestRule.onNodeWithText("Kotlin").assertIsDisplayed().performClick()
        composeTestRule.waitUntil {
            !viewModel.viewStates.isRefreshing
        }
        val listItems =
            composeTestRule.onAllNodesWithContentDescription("RepoItem").fetchSemanticsNodes()
        assert(listItems.isNotEmpty()) { "List should not be empty" }
    }

    @Test
    fun recommendationList_whenPulledToRefresh_showsLoadingIndicator() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("RecommendList").performGesture { swipeDown() }
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithText("Loading……").assertIsDisplayed()
                true // 断言通过，返回 true
            } catch (e: AssertionError) {
                false // 断言失败，返回 false
            }
        }
    }

//    @Test
//    fun repositoryItem_whenClicked_navigatesToDetailScreen() {
//
//        composeTestRule.activity.setContent {
//            GithubAppTheme {
//                // A surface container using the 'background' color from the theme
//
//                    HomeNavHost {
//
//                    }
//
//            }
//        }
////        composeTestRule.onNodeWithContentDescription("RecommendList").performClick()
//        composeTestRule.waitForIdle()
//        composeTestRule.onRoot().printToLog("ComposeTest")
//        composeTestRule.waitUntil {
//            !viewModel.viewStates.isRefreshing // 等待直到 isLoading 为 false
//        }
//        composeTestRule.onAllNodesWithContentDescription("RepoItem")[0].performClick()
//
//
////
//        composeTestRule.onNodeWithText("详情").assertIsDisplayed()
//    }


    private fun findTextField(textId: Int): SemanticsNodeInteraction {
        return composeTestRule.onNode(
            hasSetTextAction() and hasText(activity.getString(textId))
        )
    }

    private fun findTextField(text: String): SemanticsNodeInteraction {
        return composeTestRule.onNode(
            hasSetTextAction() and hasText(text, substring = true)
        )
    }


}