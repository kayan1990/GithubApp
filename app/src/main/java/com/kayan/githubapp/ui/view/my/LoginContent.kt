package com.kayan.githubapp.ui.view.my

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kayan.githubapp.common.route.Route

@Composable
fun LoginContent(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to GitHub",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Sign in to access your profile",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        Button(
            onClick = { navController.navigate(Route.LOGIN) },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Sign In")
        }
    }
} 