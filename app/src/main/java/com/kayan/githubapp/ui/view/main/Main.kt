package com.kayan.githubapp.ui.view.main

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Recommend
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.kayan.githubapp.common.route.Route
import com.kayan.githubapp.common.utlis.browse
import com.kayan.githubapp.model.bean.User
import com.kayan.githubapp.ui.common.BaseAction
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.HomeTopBar
import com.kayan.githubapp.ui.view.dynamic.DynamicViewModel
import com.kayan.githubapp.ui.view.my.MyContent
import com.kayan.githubapp.ui.view.person.PersonAction
import com.kayan.githubapp.ui.view.person.PersonViewModel
import com.kayan.githubapp.ui.view.recommend.RecommendAction
import com.kayan.githubapp.ui.view.recommend.RecommendContent
import com.kayan.githubapp.ui.view.recommend.RecommendViewModel
import com.kayan.githubapp.util.datastore.DataKey
import com.kayan.githubapp.util.datastore.DataStoreUtils
import com.kayan.githubapp.util.fromJson
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    onFinish: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    dynamicViewModel: DynamicViewModel = hiltViewModel(),
    recommendViewModel: RecommendViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewState = mainViewModel.viewStates
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val userInfo: User? = remember { DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson() }
    var updateContent by remember { mutableStateOf("") }
    val updateDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    LaunchedEffect(Unit) {
        mainViewModel.viewEvents.collect {
            when (it) {
                is MainViewEvent.Goto -> {
                    navController.navigate(it.route)
                }

                is BaseEvent.ShowMsg -> {
                    launch {
                        drawerState.close()
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }

                is MainViewEvent.HadUpdate -> {
                    updateContent = it.Content
                    updateDialogState.show()
                }
            }
        }
    }

    // 监听 pager 变化
    LaunchedEffect(pagerState) {
        withContext(Dispatchers.IO) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                mainViewModel.dispatch(MainViewAction.ScrollTo(MainPager.values()[page]))
            }
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.dispatch(
            MainViewAction.CheckUpdate(
                showTip = false,
                forceRequest = false,
                context = context
            )
        )
    }

    var lastClickTime = remember { 0L }
    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch {
                drawerState.close()
            }
        } else {
            if (System.currentTimeMillis() - lastClickTime > 2000) {
                lastClickTime = System.currentTimeMillis()
                mainViewModel.dispatch(BaseAction.ShowMag("再按一次退出"))
            } else {
                onFinish()
            }
        }
    }


    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                HomeTopBar(
                    title = viewState.title,
                    navigationIcon = Icons.Outlined.Menu,
                    actions = {
                        IconButton(onClick = { navController.navigate(Route.SEARCH) }) {
                            Icon(Icons.Outlined.Search, "搜索")
                        }
                    },
                    onBack = {
                        coroutineScope.launch {
                            if (drawerState.isOpen) drawerState.close() else drawerState.open()
                        }
                    }
                )
            }
        },
        bottomBar = {
            BottomBar(
                viewState,
                onScrollTo = {
                    coroutineScope.launch {
                        if (it == viewState.currentPage) { // 点击的是当前页面的按钮，回到顶部或刷新
                            when (it) {
                                MainPager.HOME_RECOMMEND -> {
                                    recommendViewModel.dispatch(RecommendAction.TopOrRefresh)
                                }

                                MainPager.HOME_MY -> {
                                    personViewModel.dispatch(PersonAction.TopOrRefresh)
                                }
                            }
                        } else { // 点击的不是当前页面的按钮，跳转到点击的页面
                            pagerState.animateScrollToPage(it.ordinal)
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(it)
        ) {
            MainContent(
                pagerState,
                drawerState,
                navController,
                scaffoldState,
                viewState.gesturesEnabled,
                dynamicViewModel,
                recommendViewModel
            ) { enable ->
                mainViewModel.dispatch(MainViewAction.ChangeGesturesEnabled(enable))
            }
        }

        UpdateDialog(context = context, dialogState = updateDialogState, content = updateContent)
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MainContent(
    pagerState: PagerState,
    drawerState: DrawerState,
    navController: NavHostController,
    scaffoldState: BottomSheetScaffoldState,
    gesturesEnabled: Boolean,
    dynamicViewModel: DynamicViewModel = hiltViewModel(),
    recommendViewModel: RecommendViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel(),
    onChangeGesturesEnabled: (enable: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        beyondBoundsPageCount = 2,
        userScrollEnabled = gesturesEnabled && pagerState.currentPage != 0,
        modifier = Modifier.draggable(
            state = rememberDraggableState {
                if (drawerState.isClosed && !drawerState.isAnimationRunning) {
                    if (it >= 5f) {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    } else if (it < -5f && pagerState.canScrollForward && !pagerState.isScrollInProgress) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                }
            },
            orientation = Orientation.Horizontal,
            enabled = pagerState.currentPage == 0
        )
    ) { page ->
        when (page) {
            0 -> RecommendContent(scaffoldState, navController, recommendViewModel)
            1 -> MyContent(scaffoldState, navController, personViewModel) {
                onChangeGesturesEnabled(it)
            }
        }
    }
}


@Composable
private fun UpdateDialog(
    context: Context,
    dialogState: MaterialDialogState,
    content: String
) {
    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
        buttons = {
            positiveButton(
                text = "更新",
                onClick = {
                    context.browse("https://github.com/equationl/githubAppByCompose/releases")
                }
            )
            negativeButton(text = "取消")
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {

            Text(text = "发现新版本", style = MaterialTheme.typography.titleLarge)

            Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))

            Text(
                text = content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .verticalScroll(
                        rememberScrollState()
                    )
            )
        }
    }
}




@Composable
private fun BottomBar(
    viewState: MainViewState,
    onScrollTo: (to: MainPager) -> Unit
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        BottomAppBar {

            BottomItem(
                isSelected = viewState.currentPage == MainPager.HOME_RECOMMEND,
                title = "Trending",
                iconUnselect = Icons.Outlined.Recommend,
                iconSelect = Icons.Filled.Recommend,
                onClick = { onScrollTo(MainPager.HOME_RECOMMEND) }
            )

            Spacer(Modifier.weight(1f, true))

            BottomItem(
                isSelected = viewState.currentPage == MainPager.HOME_MY,
                title = "Me",
                iconUnselect = Icons.Outlined.Person,
                iconSelect = Icons.Filled.Person,
                onClick = { onScrollTo(MainPager.HOME_MY) }
            )

        }
    }
}

@Composable
fun RowScope.BottomItem(
    isSelected: Boolean,
    title: String,
    iconUnselect: ImageVector,
    iconSelect: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .weight(1f)
    ) {
        Icon(
            if (isSelected) iconSelect else iconUnselect,
            title,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
        Text(
            title,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}
