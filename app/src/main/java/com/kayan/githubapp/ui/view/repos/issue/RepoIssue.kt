package com.kayan.githubapp.ui.view.repos.issue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kayan.githubapp.model.bean.RepoPermission
import com.kayan.githubapp.model.ui.IssueUIModel
import com.kayan.githubapp.ui.common.AvatarContent
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.BaseRefreshPaging
import com.kayan.githubapp.ui.common.comPlaceholder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ReposIssueContent(
    userName: String,
    reposName: String,
    repoPermission: RepoPermission?,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RepoIssueViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }

                is RepoIssueEvent.GoTo -> {
//                    navController.navigate(it.path)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RepoIssueAction.SetDate(userName, reposName, repoPermission))
    }

    Column {

        val issueList = viewState.issueFlow?.collectAsLazyPagingItems()

        RefreshContent(
            navController = navController,
            pagingItems = issueList,
            cacheList = viewState.cacheIssueList,
            isInit = viewModel.isInit,
            onLoadError = {
                viewModel.dispatch(RepoIssueAction.ShowMsg(it))
            },
            onClickItem = {
                viewModel.dispatch(RepoIssueAction.GoIssueDetail(userName, reposName, issueNumber = it.issueNum, repoPermission?.admin ?: true))
            }
        )
    }

}

@Composable
private fun RefreshContent(
    navController: NavHostController,
    pagingItems: LazyPagingItems<IssueUIModel>?,
    cacheList: List<IssueUIModel>?,
    isInit: Boolean,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (commitUIModel: IssueUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    if (pagingItems?.itemCount == 0 && isInit && cacheList.isNullOrEmpty()) {
        return
    }

    BaseRefreshPaging(
        pagingItems = pagingItems,
        cacheItems = cacheList,
        itemUi = {data, isRefresh ->
            Column(modifier = Modifier.padding(8.dp).semantics { contentDescription = "RepoIssueItem" }) {
                ReposIssueItem(
                    data, isRefresh, navController
                ) {
                    onClickItem(data)
                }
            }
        },
        onLoadError = onLoadError,
        onClickItem = {},
        headerItem = headerItem,
        onRefresh = onRefresh
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReposIssueItem(
    issueUIModel: IssueUIModel,
    isRefresh: Boolean,
    navController: NavHostController,
    onClickItem: () -> Unit
) {
    Card(onClick = onClickItem) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarContent(
                        data = issueUIModel.image,
                        size = DpSize(50.dp, 50.dp),
                        userName = issueUIModel.username,
                        navHostController = navController,
                        isRefresh = isRefresh
                    )

                    Text(
                        text = issueUIModel.username,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 6.dp).comPlaceholder(isRefresh)
                    )

                }

                Text(text = issueUIModel.time, modifier = Modifier.comPlaceholder(isRefresh))
            }

            Column(modifier = Modifier
                .padding(start = 50.dp)
                .padding(4.dp)) {
                Text(text = issueUIModel.action, modifier = Modifier.comPlaceholder(isRefresh))

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        val color = if (issueUIModel.status == "closed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = "Issue status", tint = color, modifier = Modifier.comPlaceholder(isRefresh))
                        Text(text = issueUIModel.status, color = color, modifier = Modifier.comPlaceholder(isRefresh))
                        Text(text = "#${issueUIModel.issueNum}", modifier = Modifier.padding(start = 2.dp).comPlaceholder(isRefresh))
                    }

                    Row {
                        Icon(imageVector = Icons.Filled.ChatBubble, contentDescription = "comment", modifier = Modifier.comPlaceholder(isRefresh))
                        Text(text = issueUIModel.comment, modifier = Modifier.comPlaceholder(isRefresh))
                    }
                }
            }
        }
    }
}