package com.kayan.githubapp.ui.view.issue

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kayan.githubapp.common.route.Route
import com.kayan.githubapp.common.utlis.copy
import com.kayan.githubapp.model.ui.IssueUIModel
import com.kayan.githubapp.ui.common.AvatarContent
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.BaseRefreshPaging
import com.kayan.githubapp.ui.common.MoreMenu
import com.kayan.githubapp.ui.common.TopBar
import com.kayan.githubapp.ui.common.comPlaceholder
import com.kayan.githubapp.ui.theme.getRichTextStyle
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.markdown.MarkdownParseOptions
import com.halilibo.richtext.ui.material3.Material3RichText
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    userName: String,
    repoName: String,
    issueNumber: Int,
    hasAdminPermission: Boolean,
    navController: NavHostController,
    viewModel: IssueViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val commentList = viewState.issueCommentFlow?.collectAsLazyPagingItems()
    val commentMenuDialogState: MaterialDialogState = rememberMaterialDialogState(false)
    val editDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    var editIssue: IssueUIModel? by remember { mutableStateOf(null) }

    var isShowHomeIcon by remember { mutableStateOf(false) }

    val navBackStackEntry = navController.currentBackStack.collectAsState().value

    isShowHomeIcon = navBackStackEntry.getOrNull(navBackStackEntry.lastIndex - 1)?.destination?.route?.contains(Route.REPO_DETAIL) == false

    LaunchedEffect(commentList) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }

                is IssueEvent.Refresh -> {
                    commentList?.refresh()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(IssueAction.LoadData(userName, repoName, issueNumber))
    }

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }

            TopBar(
                title = if (viewState.cacheIssueInfo == null) viewState.issueInfo.action else viewState.cacheIssueInfo.action,
                actions = {
                    MoreMenu(
                        isShow = isShowDropMenu,
                        onDismissRequest = { isShowDropMenu = false },
                        onClick = {
                            viewModel.dispatch(IssueAction.ClickMoreMenu(context, it, userName, repoName, issueNumber))
                        }
                    )
                    // 如果是从仓库详情页跳转过来的则不显示返回仓库主页按钮
                    if (isShowHomeIcon) {
                        IconButton(onClick = {
                            navController.navigate("${Route.REPO_DETAIL}/${repoName}/${userName}")
                        }) {
                            Icon(imageVector = Icons.Filled.Home , contentDescription = "Repo Home")
                        }
                    }

                    IconButton(onClick = { isShowDropMenu = !isShowDropMenu}) {
                        Icon(Icons.Outlined.MoreHoriz, "More")
                    }
                }
            ) {
                navController.popBackStack()
            }
        },
        bottomBar = {
            BottomBar(
                issueUIModel = viewState.cacheIssueInfo ?: viewState.issueInfo,
                hasAdminPermission = hasAdminPermission,
                loginUser = viewState.loginUser,
                onChangeIssueStatus = { viewModel.dispatch(IssueAction.OnChangeIssueStatus(it)) },
                onChangeIssueLockStatus = { viewModel.dispatch(IssueAction.OnChangeIssueLockStatus(it)) },
                onClickAddComment = {
                    editIssue = null
                    editDialogState.show()
                },
                onClickEditIssue = {
                    editIssue = it
                    editDialogState.show()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) {
        var clickCommentItem: IssueUIModel? by remember { mutableStateOf(null) }

        CommentEditMenuDialog(
            commentItem = clickCommentItem,
            viewModel = viewModel,
            dialogState = commentMenuDialogState
        ) { commentItem ->
            editIssue = commentItem
            editDialogState.show()
        }

        EditDialog(
            issueUIModel = editIssue,
            editDialogState
        ) { tittle: String, content: String, operate: EditIssueDialogOperate ->
            when (operate) {
                EditIssueDialogOperate.AddComment -> { viewModel.dispatch(IssueAction.AddComment(content)) }
                EditIssueDialogOperate.EditComment -> { viewModel.dispatch(IssueAction.EditComment(content, clickCommentItem?.status ?: "")) }
                EditIssueDialogOperate.EditIssue -> { viewModel.dispatch(IssueAction.EditIssue(tittle, content)) }
            }
        }

        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(it)
        ) {
            IssueContent(
                commentList,
                viewState.cacheCommentList,
                viewState.cacheIssueInfo ?: viewState.issueInfo,
                navController,
                viewModel.isInit && viewState.cacheCommentList.isNullOrEmpty(),
                onError = {msg ->
                    viewModel.dispatch(IssueAction.ShowMag(msg))
                },
                onClickComment = { issueUuiModel ->
                    clickCommentItem = issueUuiModel
                    commentMenuDialogState.show()
                })
        }
    }

}

@Composable
private fun EditDialog(
    issueUIModel: IssueUIModel? = null,
    dialogState: MaterialDialogState,
    onPostDate: (tittle: String, content: String, from: EditIssueDialogOperate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    if (issueUIModel != null) {
        if (issueUIModel.isComment) {
            title = ""
            content = issueUIModel.action
        }
        else {
            title = issueUIModel.action
            content = issueUIModel.content
        }
    }
    else {
        title = ""
        content = ""
    }

    val titleText: String
    val operate: EditIssueDialogOperate

    if (title.isEmpty()) {
        if (issueUIModel?.isComment == true) {
            titleText = "编辑回复"
            operate = EditIssueDialogOperate.EditComment
        }
        else {
            titleText = "回复"
            operate = EditIssueDialogOperate.AddComment
        }
    }
    else {
        titleText = "编辑"
        operate = EditIssueDialogOperate.EditIssue
    }

    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {

                Text(text = titleText)
            }

            if (title.isNotEmpty()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it},
                    label = {
                        Text(text = "Title")
                    },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = {
                    Text(text = "body（support markdown）")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextButton(onClick = { dialogState.hide() }) {
                    Text(text = "Cancel")
                }

                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))

                TextButton(onClick = {
                    dialogState.hide()

                    onPostDate(title, content, operate)
                }) {
                    Text(text = "OK")
                }
            }
        }
    }
}

@Composable
private fun CommentEditMenuDialog(
    commentItem: IssueUIModel?,
    viewModel: IssueViewModel,
    dialogState: MaterialDialogState,
    onEditComment: (commentItem: IssueUIModel) -> Unit
) {
    val context = LocalContext.current

    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = {
                dialogState.hide()
                if (commentItem != null) {
                    context.copy(commentItem.action)
                    viewModel.dispatch(IssueAction.ShowMag("已复制"))
                }
            }) {
                Text(text = "复制")
            }
            TextButton(onClick = {
                dialogState.hide()
                if (commentItem != null) {
                    onEditComment(commentItem)
                }
            }) {
                Text(text = "编辑")
            }
            TextButton(onClick = {
                dialogState.hide()
                if (commentItem != null) {
                    viewModel.dispatch(IssueAction.DelComment(commentItem))
                }
            }) {
                Text(text = "删除")
            }
        }
    }
}

@Composable
private fun IssueContent(
    commentList: LazyPagingItems<IssueUIModel>?,
    cacheCommentList: List<IssueUIModel>?,
    issueInfo: IssueUIModel,
    navController: NavHostController,
    isInit: Boolean,
    onError: (msg: String) -> Unit,
    onClickComment: (issueUIModel: IssueUIModel) -> Unit
) {
    // FIXME 这里返回后的位置不对
    if (commentList?.itemCount == 0 && isInit) {
        return
    }

    BaseRefreshPaging(
        pagingItems = commentList,
        cacheItems = cacheCommentList,
        itemUi = { data, isRefresh ->
            Column(modifier = Modifier.padding(8.dp)) {
                CommentItem(
                    data,
                    isRefresh,
                    navController
                ) {
                    onClickComment(it)
                }
            }
        },
        onLoadError = onError,
        onClickItem = {},
        headerItem = {
            item(key = "Header") {
                Header(issueUIModel = issueInfo, navController = navController)
            }
        },
        emptyItem = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "没有回复哦")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentItem(
    issueUIModel: IssueUIModel,
    isRefresh: Boolean,
    navController: NavHostController,
    onClickComment: (issueUIModel: IssueUIModel) -> Unit
) {
    Card(
        onClick = {
            onClickComment(issueUIModel)
        }
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarContent(
                        data = issueUIModel.image,
                        navHostController = navController,
                        userName = issueUIModel.username,
                        isRefresh = isRefresh
                    )

                    Text(text = issueUIModel.username, modifier = Modifier
                        .padding(start = 4.dp)
                        .comPlaceholder(isRefresh))
                }

                Text(text = issueUIModel.time, modifier = Modifier.comPlaceholder(isRefresh))
            }

            Material3RichText(modifier = Modifier
                .padding(start = 30.dp)
                .comPlaceholder(isRefresh),
                style = getRichTextStyle()
            ) {
                Markdown(
                    content = issueUIModel.action,
                    markdownParseOptions = MarkdownParseOptions.Default.copy(autolink = false),
                    onImgClicked = {
                        navController.navigate("${Route.IMAGE_PREVIEW}/${Uri.encode(it)}")
                    }
                )
            }
        }
    }

}

@Composable
private fun Header(
    issueUIModel: IssueUIModel,
    navController: NavHostController
) {
    Card {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarContent(
                    data = issueUIModel.image,
                    size = DpSize(90.dp, 90.dp),
                    navHostController = navController,
                    userName = issueUIModel.username
                )

                Column(modifier = Modifier.padding(start = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = issueUIModel.username,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row {
                                val color = if (issueUIModel.status == "closed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary

                                Icon(imageVector = Icons.Outlined.Info, contentDescription = "Issue status", tint = color)
                                Text(text = issueUIModel.status, color = color)
                                Text(text = "#${issueUIModel.issueNum}", modifier = Modifier.padding(start = 4.dp))
                                Icon(imageVector = Icons.Filled.ChatBubble, contentDescription = "comment", modifier = Modifier.padding(start = 4.dp))
                                Text(text = issueUIModel.comment)
                            }
                        }

                        Text(text = issueUIModel.time)
                    }

                    Text(text = issueUIModel.action, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Material3RichText(style = getRichTextStyle()) {
                    Markdown(
                        content = issueUIModel.content,
                        markdownParseOptions = MarkdownParseOptions.Default.copy(autolink = false),
                        onImgClicked = {
                            navController.navigate("${Route.IMAGE_PREVIEW}/${Uri.encode(it)}")
                        }
                    )
                }
            }

        }
    }
}

@Composable
private fun BottomBar(
    issueUIModel: IssueUIModel,
    hasAdminPermission: Boolean,
    loginUser: String?,
    onChangeIssueStatus: (status: String) -> Unit,
    onChangeIssueLockStatus: (isLocked: Boolean) -> Unit,
    onClickEditIssue: (issueUiModel: IssueUIModel) -> Unit,
    onClickAddComment: () -> Unit
) {
    Divider(modifier = Modifier.fillMaxWidth())

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        if (hasAdminPermission) {
            TextButton(onClick = {
                onChangeIssueLockStatus(!issueUIModel.locked)
            }) {
                if (issueUIModel.locked) {
                    Text(text = "解锁")
                }
                else {
                    Text(text = "锁定")
                }
            }
        }

        // 如果是登录用户创建的 issue 也能编辑和关闭
        if (hasAdminPermission || (loginUser != null && loginUser == issueUIModel.username)) {
            TextButton(onClick = {
                onChangeIssueStatus(if (issueUIModel.status == "open") "closed" else "open")
            }) {
                if (issueUIModel.status == "open") {
                    Text(text = "关闭")
                }
                else {
                    Text(text = "打开")
                }
            }

            TextButton(onClick = { onClickEditIssue(issueUIModel) }) {
                Text(text = "编辑")
            }
        }

        TextButton(onClick = onClickAddComment) {
            Text(text = "回复")
        }
    }
}