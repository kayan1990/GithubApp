package com.kayan.githubapp.ui.view.my

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.kayan.githubapp.model.bean.User
import com.kayan.githubapp.ui.view.person.PersonContent
import com.kayan.githubapp.ui.view.person.PersonViewModel
import com.kayan.githubapp.util.datastore.DataKey
import com.kayan.githubapp.util.datastore.DataStoreUtils
import com.kayan.githubapp.util.fromJson
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: PersonViewModel = hiltViewModel(),
    onEnablePagerScroll: (enable: Boolean) -> Unit
) {
    val userInfoJson by DataStoreUtils.readStringFlow(DataKey.UserInfo, "")
        .collectAsState(initial = "")

    val token by DataStoreUtils.readStringFlow(DataKey.LoginAccessToken, "")
        .collectAsState(initial = "")

    val userInfo = remember(userInfoJson) { userInfoJson.fromJson<User>() }

    if (token.isBlank() || userInfo == null) {
        LoginContent(navController = navController)
    } else {
        PersonContent(
            scaffoldState = scaffoldState,
            navController = navController,
            userName = null, // 设置为 null 时表示使用当前登录用户信息
            onEnablePagerScroll = onEnablePagerScroll,
            viewModel = viewModel
        )
    }
}