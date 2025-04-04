package com.kayan.githubapp.ui.view.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayan.githubapp.BuildConfig
import com.kayan.githubapp.common.route.Route
import com.kayan.githubapp.common.utlis.CommonUtils
import com.kayan.githubapp.service.LoginService
import com.kayan.githubapp.service.RepoService
import com.kayan.githubapp.service.UserService
import com.kayan.githubapp.util.datastore.DataKey
import com.kayan.githubapp.util.datastore.DataStoreUtils
import com.kayan.githubapp.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "el, LOVM"

@HiltViewModel
class LoginOauthViewModel @Inject constructor(
    private val loginService: LoginService,
    private val userService: UserService,
    private val repoService: RepoService
) : ViewModel() {

    var viewStates by mutableStateOf(LoginOAuthState())
        private set

    private val _viewEvents = Channel<LoginOauthViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: LoginOauthViewAction) {
        when (action) {
            is LoginOauthViewAction.RequestToken -> requestToken(action.code)
            is LoginOauthViewAction.WebViewLoadError -> webViewLoadError(action.message)
        }
    }

    private fun requestToken(code: String) {
        val exception = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "requestToken: ", throwable)
            webViewLoadError("错误："+throwable.message)
        }

        if (viewStates.isRequestToken) return

        viewModelScope.launch(exception) {
            viewStates = viewStates.copy(isRequestToken = true)
            val response = loginService.authorizationsCode(
                client_id = BuildConfig.CLIENT_ID,
                client_secret = BuildConfig.CLIENT_SECRET,
                code = code
            )

            if (response.isSuccessful) {
                val tokenBody = response.body()
                if (tokenBody == null) {
                    webViewLoadError("获取 token 失败：返回数据为空！")
                }
                else {
                    DataStoreUtils.saveSyncStringData(DataKey.LoginAccessToken, tokenBody.access_token ?: "")
                    getUserInfo()
                    _viewEvents.send(LoginOauthViewEvent.Goto(Route.MAIN))
                }
            }
            else {
                val result = kotlin.runCatching {
                    webViewLoadError("获取 token 失败："+response.errorBody()?.string())
                }
                if (result.isFailure) {
                    webViewLoadError("获取 token 失败：获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}")
                }
            }
        }
    }

    private suspend fun getUserInfo() {
        val response = userService.getPersonInfo(true)
        if (response.isSuccessful) {
            val user = response.body()
            if (user == null) {
                throw Exception("获取账号信息失败：response is null")
            }
            else {
                // 获取 star 信息后保存
                val newUser = CommonUtils.updateStar(user, repoService)
                DataStoreUtils.saveSyncStringData(DataKey.UserInfo, newUser.toJson())
            }
        }
        else {
            throw Exception("获取账号信息失败：${response.errorBody()?.string()}")
        }
    }

    private fun webViewLoadError(message: String) {
        viewModelScope.launch {
            _viewEvents.send(LoginOauthViewEvent.ShowMessage(message))
            //delay(3000)
            //_viewEvents.send(LoginOauthViewEvent.Goto(Route.LOGIN))
        }
    }
}

data class LoginOAuthState(
    val isRequestToken: Boolean = false
)

sealed class LoginOauthViewEvent {
    data class Goto(val route: String):LoginOauthViewEvent()
    data class ShowMessage(val message: String) :LoginOauthViewEvent()
}

sealed class LoginOauthViewAction {
    data class RequestToken(val code: String): LoginOauthViewAction()
    data class WebViewLoadError(val message: String): LoginOauthViewAction()
}