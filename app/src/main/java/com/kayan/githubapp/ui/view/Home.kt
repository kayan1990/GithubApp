package com.kayan.githubapp.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kayan.githubapp.common.route.Route
import com.kayan.githubapp.common.route.RouteParams
import com.kayan.githubapp.ui.view.image.ImageScreen
import com.kayan.githubapp.ui.view.login.LoginScreen
import com.kayan.githubapp.ui.view.login.OAuthLoginScreen
import com.kayan.githubapp.ui.view.main.MainScreen
import com.kayan.githubapp.ui.view.notify.NotifyScreen
import com.kayan.githubapp.ui.view.person.PersonScreen
import com.kayan.githubapp.ui.view.repos.RepoDetailScreen
import com.kayan.githubapp.ui.view.search.SearchScreen
import com.kayan.githubapp.ui.view.welcome.WelcomeScreen


@Composable
fun HomeNavHost(
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController, Route.WELCOME) {

        // 欢迎页
        composable(Route.WELCOME) {
            WelcomeScreen(navHostController = navController)
        }

        // 登录
        composable(Route.LOGIN) {
            Column(Modifier.systemBarsPadding()) {
                LoginScreen(navHostController = navController)
            }
        }

        // OAuth 登录
        composable(Route.OAuthLogin) {
            Column(Modifier.systemBarsPadding()) {
                OAuthLoginScreen(navHostController = navController)
            }
        }

        // 主页
        composable(Route.MAIN) {
            MainScreen(navController, onFinish)
        }

        // 通知页
        composable(Route.NOTIFY) {
            Column(Modifier.systemBarsPadding()) {
                NotifyScreen(navHostController = navController)
            }
        }

        // 搜索页
        composable("${Route.SEARCH}?${RouteParams.PAR_SEARCH_QUERY}={${RouteParams.PAR_SEARCH_QUERY}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_SEARCH_QUERY) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            val query = it.arguments?.getString(RouteParams.PAR_SEARCH_QUERY)

            Column(Modifier.systemBarsPadding()) {
                SearchScreen(navHostController = navController, queryString = query)
            }
        }

        // 仓库详情页
        composable("${Route.REPO_DETAIL}/{${RouteParams.PAR_REPO_PATH}}/{${RouteParams.PAR_REPO_OWNER}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_OWNER) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH)
            val repoOwner = argument.getString(RouteParams.PAR_REPO_OWNER)

            Column(Modifier.systemBarsPadding()) {
                RepoDetailScreen(navController, repoName = repoPath, repoOwner = repoOwner)
            }
        }

        // 用户详情页
        composable("${Route.PERSON_DETAIL}/{${RouteParams.PAR_USER_NAME}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_USER_NAME) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val userName = argument.getString(RouteParams.PAR_USER_NAME)

            Column(Modifier.systemBarsPadding()) {
                PersonScreen(userName = userName ?: "", navController = navController)
            }
        }

        // 图像预览页
        composable("${Route.IMAGE_PREVIEW}/{${RouteParams.PAR_IMAGE_URL}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_IMAGE_URL) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val imageUrl = argument.getString(RouteParams.PAR_IMAGE_URL)

            ImageScreen(image = imageUrl ?: "", navController = navController)
        }


    }
}