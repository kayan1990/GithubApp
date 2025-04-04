package com.kayan.githubapp.ui.view.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kayan.githubapp.common.route.Route

@Composable
fun WelcomeScreen(
    navHostController: NavHostController
) {
    LaunchedEffect(Unit) {
        navHostController.navigate(Route.MAIN) {
            popUpTo(0)
        }
    }

    WelcomeContent()
}

@Composable
private fun WelcomeContent() {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "GithubAPP", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = "Loading...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}