package com.kayan.githubapp.ui.view.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kayan.githubapp.common.constant.LanguageFilter
import com.kayan.githubapp.common.constant.OrderFilter
import com.kayan.githubapp.common.constant.TypeFilter
import com.kayan.githubapp.common.route.Route
import com.kayan.githubapp.model.ui.ReposUIModel
import com.kayan.githubapp.ui.common.BaseAction
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.CheckBoxGroup
import com.kayan.githubapp.ui.common.TopBar
import com.kayan.githubapp.ui.view.list.generalRepo.GeneralRepoListContent
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navHostController: NavHostController,
    queryString: String?,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val filterDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!queryString.isNullOrBlank()) {
            viewModel.dispatch(SearchAction.OnSearch(queryString))
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = getTitle(queryString),
                actions = {
                    IconButton(
                        onClick = {
                            filterDialogState.show()
                        }
                    ) {
                        Icon(Icons.Filled.Sort, "Filter")
                    }
                }
            ) {
                navHostController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (queryString.isNullOrBlank()) {
                SearchContent(viewModel, navHostController)
            } else {
                @Suppress("UNCHECKED_CAST")
                val repoPagingItems =
                    viewState.resultListFlow?.collectAsLazyPagingItems() as LazyPagingItems<ReposUIModel>?

                GeneralRepoListContent(
                    navHostController = navHostController,
                    repoPagingItems = repoPagingItems,
                    cacheRepoList = null,
                    isInit = false,
                    onLoadError = {
                        viewModel.dispatch(BaseAction.ShowMag(it))
                    },
                    onClickItem = {
                        navHostController.navigate("${Route.REPO_DETAIL}/${it.repositoryName}/${it.ownerName}")
                    }
                )
            }

            FilterDialog(
                dialogState = filterDialogState,
                isShowLanguage = true,
                defaultType = viewState.typeFilter.ordinal,
                defaultOrder = viewState.orderFilter.ordinal,
                defaultLanguage = viewState.languageFilter.ordinal
            ) { type: Int, order: Int, language: Int ->
                viewModel.dispatch(
                    SearchAction.OnUpdateFilter(
                        TypeFilter.values()[type],
                        OrderFilter.values()[order],
                        LanguageFilter.values()[language]
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchContent(
    viewModel: SearchViewModel,
    navHostController: NavHostController
) {
    val viewState = viewModel.viewStates
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        SearchHeader(
            onSearch = {
                keyboardController?.hide()
                viewModel.dispatch(SearchAction.OnSearch(it))
            }
        )

        @Suppress("UNCHECKED_CAST")
        val repoPagingItems =
            viewState.resultListFlow?.collectAsLazyPagingItems() as LazyPagingItems<ReposUIModel>?

        GeneralRepoListContent(
            navHostController = navHostController,
            repoPagingItems = repoPagingItems,
            cacheRepoList = null,
            isInit = false,
            onLoadError = {
                viewModel.dispatch(BaseAction.ShowMag(it))
            },
            onClickItem = {
                navHostController.navigate("${Route.REPO_DETAIL}/${it.repositoryName}/${it.ownerName}")
            }
        )

    }
}


@Composable
private fun FilterDialog(
    dialogState: MaterialDialogState,
    isShowLanguage: Boolean,
    defaultType: Int = 0,
    defaultOrder: Int = 0,
    defaultLanguage: Int = 0,
    onUpdateFilter: (type: Int, order: Int, language: Int) -> Unit
) {
    var type = defaultType
    var order = defaultOrder
    var language = defaultLanguage

    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
    ) {
        val scrollState = rememberScrollState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(text = "筛选结果")
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(8f)
                    .fillMaxWidth()
                    .padding(start = 6.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "类型")

                CheckBoxGroup(
                    options = searchTypeFilter,
                    defaultCheck = defaultType,
                    onCheckChange = {
                        type = it
                    }
                )

                Text(text = "排序")

                CheckBoxGroup(
                    options = searchOrderFilter,
                    defaultCheck = defaultOrder,
                    onCheckChange = {
                        order = it
                    }
                )

                if (isShowLanguage) {
                    Text(text = "语言")

                    CheckBoxGroup(
                        options = searchLanguageFilter,
                        defaultCheck = defaultLanguage,
                        onCheckChange = {
                            language = it
                        }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .weight(0.8f)
            ) {
                TextButton(onClick = {
                    onUpdateFilter(0, 0, 0)
                    dialogState.hide()
                }) {
                    Text(text = "重置")
                }

                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                TextButton(onClick = {
                    onUpdateFilter(type, order, language)
                    dialogState.hide()
                }) {
                    Text(text = "确定")
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    onSearch: (word: String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .clickable {
                            onSearch(searchText)
                        }
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onAny = { onSearch(searchText) }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("SearchInput")
        )

    }
}

private fun getTitle(queryString: String?): String {
    return if (queryString.isNullOrBlank()) {
        "Search"
    } else {
        if (queryString.startsWith("topic:")) {
            try {
                val topicName = queryString.substring(6)
                "”$topicName“ 主题仓库"
            } catch (tr: Throwable) {
                "仓库"
            }
        } else {
            "Search： $queryString"
        }
    }
}