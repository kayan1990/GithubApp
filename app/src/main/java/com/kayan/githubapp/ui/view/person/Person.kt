package com.kayan.githubapp.ui.view.person

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.kayan.githubapp.common.route.Route
import com.kayan.githubapp.common.utlis.CommonUtils
import com.kayan.githubapp.model.bean.User
import com.kayan.githubapp.ui.common.AvatarContent
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.LinkText
import com.kayan.githubapp.ui.common.MoreMenu
import com.kayan.githubapp.ui.common.TopBar
import com.kayan.githubapp.ui.view.dynamic.DynamicViewAction
import com.kayan.githubapp.ui.view.dynamic.DynamicViewEvent
import com.kayan.githubapp.ui.view.dynamic.EventRefreshContent
import com.kayan.githubapp.ui.view.list.GeneralListEnum
import com.kayan.githubapp.util.datastore.DataKey
import com.kayan.githubapp.util.datastore.DataStoreUtils
import com.kayan.githubapp.util.fromJson
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
    userName: String,
    navController: NavHostController,
    viewModel: PersonViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }

            TopBar(
                title = userName,
                actions = {
                    IconButton(onClick = { isShowDropMenu = !isShowDropMenu }) {
                        Icon(Icons.Outlined.MoreHoriz, "More")
                    }

                    MoreMenu(
                        isShow = isShowDropMenu,
                        onDismissRequest = { isShowDropMenu = false },
                        onClick = {
                            viewModel.dispatch(PersonAction.ClickMoreMenu(context, it, userName))
                        }
                    )
                }
            ) {
                navController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        },
        floatingActionButton = {
            val focusState = viewModel.personViewState.isFollow
            if (focusState != IsFollow.NotNeed) {
                FloatingActionButton(onClick = { viewModel.dispatch(PersonAction.ChangeFollowState) }) {
                    Text(text = if (focusState == IsFollow.Followed) "取关" else "关注")
                }
            }
        }
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(it)
        ) {
            PersonContent(
                scaffoldState = scaffoldState,
                navController = navController,
                userName = userName
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    userName: String? = null,
    onEnablePagerScroll: ((enable: Boolean) -> Unit)? = null,
    viewModel: PersonViewModel = hiltViewModel()
) {
    val userInfo: User? = remember { DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson() }

    LaunchedEffect(Unit) {
        if (userName != null) {
            viewModel.dispatch(PersonAction.GetUser(userName))
            viewModel.dispatch(DynamicViewAction.SetData(userName))
        } else {
            viewModel.dispatch(DynamicViewAction.SetData((userInfo?.login) ?: ""))
        }

        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }

                is DynamicViewEvent.Goto -> {
                    navController.navigate(it.route)
                }
            }
        }
    }

    when (viewModel.personViewState.user.type) {
        null -> { // 数据还没初始化
            PersonHeader(
                user = User(),
                viewModel = viewModel,
                navController = navController,
                isLoginUser = false,
                withChartMap = false
            )
        }

        else -> { // 当前是一个个人用户，显示用户动态
            PersonDynamic(
                navController = navController,
                viewModel = viewModel,
                isLoginUser = (userName == null || userInfo?.login == userName),
                onEnablePagerScroll = onEnablePagerScroll
            )
        }
    }
}

@Composable
private fun PersonDynamic(
    navController: NavHostController,
    viewModel: PersonViewModel,
    isLoginUser: Boolean,
    onEnablePagerScroll: ((enable: Boolean) -> Unit)?
) {
    val personViewState = viewModel.personViewState
    val viewState = viewModel.viewStates
    val lazyListState: LazyListState = rememberLazyListState()


    val dynamicList = viewState.dynamicFlow?.collectAsLazyPagingItems()

    LaunchedEffect(dynamicList) {
        viewModel.viewEvents.collect {
            when (it) {
                is PersonEvent.TopOrRefresh -> {
                    if (lazyListState.firstVisibleItemIndex == 0) {
                        // refresh
                        dynamicList?.refresh()
                    } else {
                        // scroll to top
                        lazyListState.animateScrollToItem(0)
                    }
                }
            }
        }
    }

    if (dynamicList?.itemCount == 0 && viewModel.isInit && viewState.cacheList.isNullOrEmpty()) {
        return
    }

    var isScrollEnable by remember { mutableStateOf(true) }

    EventRefreshContent(
        navHostController = navController,
        eventPagingItems = dynamicList,
        cacheList = viewState.cacheList,
        isScrollEnable = isScrollEnable,
        lazyListState = lazyListState,
        onLoadError = {
            viewModel.dispatch(DynamicViewAction.ShowMsg(it))
        },
        onClickItem = {
            viewModel.dispatch(DynamicViewAction.ClickItem(it))
        },
        headerItem = {
            item(key = "header") {
                PersonHeader(
                    user = personViewState.user,
                    viewModel = viewModel,
                    navController = navController,
                    isLoginUser = isLoginUser,
                    onEnablePagerScroll = {
                        isScrollEnable = it
                        onEnablePagerScroll?.invoke(it)
                    },
                )
            }
        },
        onRefresh = {
            viewModel.dispatch(PersonAction.GetUser(personViewState.user.login ?: ""))
        }
    )
}

@Composable
fun PersonHeader(
    user: User,
    viewModel: PersonViewModel,
    navController: NavHostController,
    isLoginUser: Boolean,
    withChartMap: Boolean = true,
    onEnablePagerScroll: ((enable: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val logoutDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    Column {
        Card(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarContent(
                    data = user.avatarUrl ?: "",
                    size = DpSize(50.dp, 50.dp),
                    onClick = {
                        navController.navigate("${Route.IMAGE_PREVIEW}/${Uri.encode(user.avatarUrl)}")
                    }
                )

                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.login ?: "加载中",
                            fontSize = 23.sp
                        )

                        if (isLoginUser) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .clickable {
                                        navController.navigate(Route.NOTIFY)
                                    }
                            )
                        }
                    }

                    user.name?.let {
                        Text(text = it)
                    }

                    IconText(imageVector = Icons.Filled.CorporateFare, text = user.company ?: "")
                    IconText(imageVector = Icons.Filled.Place, text = user.location ?: "")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.blog?.isNotEmpty() == true) {
                    Icon(imageVector = Icons.Filled.Link, contentDescription = null)
                    LinkText(text = user.blog ?: "") {
                        user.blog?.let {
                            var url = it
                            if (!url.startsWith("http://") || !url.startsWith("https://")) {
                                url = "http://$url"
                            }
                            val uri = Uri.parse(url)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                    }
                }
            }

            Text(text = user.bio ?: "")

            Text(text = CommonUtils.getDateStr(user.createdAt))


            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                UserStatsItem(
                    title = "Repositories",
                    value = user.publicRepos.toString(),
                    onClick = {
                        navController.navigate("${Route.REPO_LIST}/null/${user.login}/${GeneralListEnum.UserRepository.name}")
                    }
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                UserStatsItem(
                    title = "Followers",
                    value = user.followers.toString()
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                UserStatsItem(
                    title = "Following",
                    value = user.following.toString(),
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                UserStatsItem(
                    title = "Stars",
                    value = user.starRepos.toString(),
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                UserStatsItem(
                    title = "Honors",
                    value = user.honorRepos.toString(),
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                UserStatsItem(
                    title = "Logout",
                    value = "",
                    onClick = {
                        logoutDialogState.show()
                    }
                )
            }

        }

        LogoutDialog(dialogState = logoutDialogState) {
            viewModel.dispatch(PersonAction.Logout)

        }
    }
}


@Composable
private fun LogoutDialog(
    dialogState: MaterialDialogState,
    onConfirm: () -> Unit
) {
    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {

            Text(text = "确定要退出吗？", modifier = Modifier.padding(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextButton(onClick = { dialogState.hide() }) {
                    Text(text = "取消")
                }

                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                TextButton(onClick = {
                    dialogState.hide()

                    onConfirm()
                }) {
                    Text(text = "确定")
                }
            }
        }
    }
}

@Composable
fun UserStatsItem(title: String, value: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = value)
    }
}

@Composable
private fun IconText(imageVector: ImageVector, text: String, hideWhenTextBlank: Boolean = true) {
    if (!hideWhenTextBlank || text.isNotEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = imageVector, contentDescription = null)
            Text(text = text)
        }
    }
}

@Composable
private fun VerticalText(topText: String, bottomText: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = topText)
        Text(text = bottomText)
    }
}