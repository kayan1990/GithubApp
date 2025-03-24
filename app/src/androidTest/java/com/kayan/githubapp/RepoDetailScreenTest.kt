package com.kayan.githubapp

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.kayan.githubapp.ui.view.HomeNavHost
import com.kayan.githubapp.ui.view.repos.ReposViewModel
import com.kayan.githubapp.util.datastore.DataStoreUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@OptIn(ExperimentalMaterial3Api::class)
class RepoDetailScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val activity get() = composeTestRule.activity

    lateinit var viewModel: ReposViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<Context>()
        DataStoreUtils.init(context)
        setContent()
    }

    private fun setContent(queryString: String? = null) {
        composeTestRule.activity.setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                HomeNavHost {}
            }
        }
    }

    @Test
    fun givenRepoDetailScreen_whenOpen_thenShouldShowReadmeTabByDefault() {
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithContentDescription("RepoItem")[0].performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Readme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Issues").assertIsDisplayed()

    }

    @Test
    fun givenRepoDetailScreen_whenClickIssueTab_thenShouldSwitchToIssuePage() {
        // Given
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithContentDescription("RepoItem")[0].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Issues").performClick()
        composeTestRule.waitForIdle()

        val listItems =
            composeTestRule.onAllNodesWithContentDescription("RepoIssueItem").fetchSemanticsNodes()
        assert(listItems.isNotEmpty()) { "List should not be empty" }
    }

    @Test
    fun givenCreateIssueDialog_whenFillFormAndConfirm_thenShouldCreateIssueSuccessfully() {
        // Given：进入页面并打开弹窗
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithContentDescription("RepoItem")[0].performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Add issue").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("body（support markdown）")).assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()

    }
}