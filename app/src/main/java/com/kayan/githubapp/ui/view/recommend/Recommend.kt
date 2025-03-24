package com.kayan.githubapp.ui.view.recommend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.kayan.githubapp.common.constant.LanguageFilter
import com.kayan.githubapp.common.route.Route
import com.kayan.githubapp.model.ui.ReposUIModel
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.BaseRefresh
import com.kayan.githubapp.ui.common.RepoItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RecommendViewModel = hiltViewModel(),
) {
    val viewState = viewModel.viewStates

    val lazyListState: LazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }

                is RecommendEvent.TopOrRefresh -> {
                    if (lazyListState.firstVisibleItemIndex == 0) {
                        // refresh
                        viewModel.dispatch(RecommendAction.RefreshData(true))
                    } else {
                        // scroll to top
                        lazyListState.animateScrollToItem(0)
                    }
                }

                is RecommendEvent.ScrollToTop -> {
                    lazyListState.animateScrollToItem(0)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RecommendAction.RefreshData(false))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterHeader(
            sinceFilter = viewState.sinceFilter,
            languageFilter = viewState.languageFilter,
            onChangeSinceFilter = { viewModel.dispatch(RecommendAction.ChangeSinceFilter(it)) },
            onChangeLanguageFilter = { viewModel.dispatch(RecommendAction.ChangeLanguage(it)) }
        )

        Box(modifier = Modifier.semantics { contentDescription = "RecommendList" }) {
            RecommendRefreshContent(
                isRefreshing = viewState.isRefreshing,
                lazyListState = lazyListState,
                dataList = viewState.dataList,
                cacheList = viewState.cacheDataList,
                navController = navController
            ) {
                viewModel.dispatch(RecommendAction.RefreshData(true))
            }
        }
    }
}

@Composable
private fun FilterHeader(
    sinceFilter: RecommendSinceFilter,
    languageFilter: LanguageFilter,
    onChangeSinceFilter: (choiceItem: RecommendSinceFilter) -> Unit,
    onChangeLanguageFilter: (choiceItem: LanguageFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        FilterDropMenu(
            title = sinceFilter.showName,
            options = RecommendSinceFilter.values(),
            onChoice = onChangeSinceFilter
        )
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )
        FilterDropMenu(
            title = languageFilter.showName,
            options = LanguageFilter.values(),
            onChoice = onChangeLanguageFilter
        )
    }
}

@Composable
private fun <T> FilterDropMenu(
    title: String,
    options: Array<T>,
    onChoice: (choiceItem: T) -> Unit
) {
    var isShow by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.clickable {
                isShow = !isShow
            }
        ) {
            Text(text = title)
            Icon(
                imageVector = if (isShow) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        DropdownMenu(expanded = isShow, onDismissRequest = { isShow = false }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = {
                        when (item) {
                            is LanguageFilter -> {
                                Text(text = item.showName)
                            }

                            is RecommendSinceFilter -> {
                                Text(text = item.showName)
                            }
                        }
                    },
                    onClick = {
                        isShow = false
                        onChoice(item)
                    },
                )
            }
        }
    }
}

@Composable
private fun RecommendRefreshContent(
    isRefreshing: Boolean,
    lazyListState: LazyListState,
    dataList: List<ReposUIModel>,
    navController: NavHostController,
    cacheList: List<ReposUIModel>? = null,
    onRefresh: () -> Unit
) {

    BaseRefresh(
        isRefresh = isRefreshing,
        itemList = dataList,
        cacheItemList = cacheList,
        lazyListState = lazyListState,
        itemUi = {
            RepoItem(it, isRefreshing, navController) {
                navController.navigate("${Route.REPO_DETAIL}/${it.repositoryName}/${it.ownerName}")
            }
        },
        onRefresh = onRefresh,
        onClickItem = {},
        headerItem = {

        }
    )
}