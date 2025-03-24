package com.kayan.githubapp.ui.view.list.generalRepo

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import com.kayan.githubapp.model.ui.ReposUIModel
import com.kayan.githubapp.ui.common.BaseRefreshPaging
import com.kayan.githubapp.ui.common.RepoItem
import com.kayan.githubapp.ui.view.list.GeneralListEnum
import com.kayan.githubapp.ui.view.list.GeneralRepoListSort

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun GeneralRepoListContent(
    navHostController: NavHostController,
    repoPagingItems: LazyPagingItems<ReposUIModel>?,
    cacheRepoList: List<ReposUIModel>?,
    isInit: Boolean,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (eventUiModel: ReposUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    if (repoPagingItems?.itemCount == 0 && isInit && cacheRepoList.isNullOrEmpty()) {
        return
    }

    BaseRefreshPaging(
        pagingItems = repoPagingItems,
        cacheItems = cacheRepoList,
        itemUi = { data, isRefresh ->
            RepoItem(data, isRefresh, navHostController) {
                onClickItem(data)
            }
        },
        onLoadError = onLoadError,
        onClickItem = {},
        headerItem = headerItem,
        onRefresh = onRefresh
    )
}

@Composable
private fun SortMenu(
    isShow: Boolean,
    onDismissRequest: () -> Unit,
    requestType: GeneralListEnum,
    onClick: (item: GeneralRepoListSort) -> Unit
) {
    val options = remember {
        mutableListOf<GeneralRepoListSort>().apply {
            when (requestType) {
                GeneralListEnum.UserRepository -> {
                    add(GeneralRepoListSort.Push)
                    add(GeneralRepoListSort.Create)
                    add(GeneralRepoListSort.Name)
                }
                GeneralListEnum.UserStar -> {
                    add(GeneralRepoListSort.Stars)
                    add(GeneralRepoListSort.RecentlyStar)
                    add(GeneralRepoListSort.Update)
                }
                else -> {}
            }
        }
    }

    DropdownMenu(expanded = isShow, onDismissRequest = onDismissRequest) {
        Text(text = "排序方式：", Modifier.padding(start = 2.dp))
        options.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(text = item.showName)
                },
                onClick = {
                    onDismissRequest()
                    onClick(item)
                },
            )
        }
    }
}