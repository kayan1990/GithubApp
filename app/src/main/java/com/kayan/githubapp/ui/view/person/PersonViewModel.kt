package com.kayan.githubapp.ui.view.person

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.kayan.githubapp.common.utlis.CommonUtils
import com.kayan.githubapp.common.utlis.browse
import com.kayan.githubapp.common.utlis.copy
import com.kayan.githubapp.common.utlis.share
import com.kayan.githubapp.model.bean.User
import com.kayan.githubapp.service.RepoService
import com.kayan.githubapp.service.UserService
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.view.dynamic.DynamicViewModel
import com.kayan.githubapp.util.datastore.DataKey
import com.kayan.githubapp.util.datastore.DataStoreUtils
import com.kayan.githubapp.util.fromJson
import com.kayan.githubapp.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val repoService: RepoService,
    private val userService: UserService,
): DynamicViewModel(userService) {
    override val isGetUserEvent = true

    var personViewState by mutableStateOf(PersonViewState(userInfo ?: User()))
        private set

    fun dispatch(action: PersonAction) {
        when (action) {
            is PersonAction.GetUser -> getUser(action.user)
            is PersonAction.ClickMoreMenu -> clickMoreMenu(action.context, action.pos, action.user)
            is PersonAction.ChangeFollowState -> changeFollowState()
            is PersonAction.Logout -> logout()
            PersonAction.TopOrRefresh -> topOrRefresh()
        }
    }

    private fun clickMoreMenu(context: Context, pos: Int, user: String) {
        val realUrl = CommonUtils.getUserHtmlUrl(user)
        when (pos) {
            0 -> { // 在浏览器中打开
                context.browse(realUrl)
            }
            1 -> { // 复制链接
                context.copy(realUrl)
                _viewEvents.trySend(BaseEvent.ShowMsg("已复制"))
            }
            2 -> { // 分享
                context.share(realUrl)
            }
        }
    }

    private fun getUser(user: String) {
        viewModelScope.launch(exception) {
            val saveUserInfo: User? = DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson()

            // 不清除 user.type 避免重组整个 UI （person 使用 user.type 来判断应该显示哪个 UI 界面）
            personViewState = personViewState.copy(user = User().apply { type = personViewState.user.type })

            val isLoginUser = saveUserInfo?.login == user // 是否是当前登录用户

            val response = if (isLoginUser) userService.getPersonInfo(true) else userService.getUser(true, user)
            if (response.isSuccessful) {
                response.body()?.let {
                    personViewState = personViewState.copy(user = it) // 先刷新UI

                    // 获取 follow 状态
                    personViewState = if (!isLoginUser) {
                        if (userService.checkFollowing(user).isSuccessful) { // 已 follow
                            personViewState.copy(isFollow = IsFollow.Followed)
                        } else { // 未 follow
                            personViewState.copy(isFollow = IsFollow.Unfollow)
                        }
                    } else { // 就是自己
                        personViewState.copy(isFollow = IsFollow.NotNeed)
                    }

                    // 获取 Star 等额外数据
                    val newUser = CommonUtils.updateStar(it, repoService)
                    if (isLoginUser) { // 登录用户则保存一下
                        DataStoreUtils.saveSyncStringData(DataKey.UserInfo, newUser.toJson())
                    }
                    personViewState = personViewState.copy(user = newUser)
                }
            }
            else {
                val errorText = response.errorBody()?.string()
                _viewEvents.trySend(BaseEvent.ShowMsg(errorText ?: "获取用户信息失败"))
            }
        }
    }

    private fun changeFollowState() {
        viewModelScope.launch(exception) {
            val isFollow = personViewState.isFollow != IsFollow.Followed

            val response = if (isFollow) userService.followUser(personViewState.user.login ?: "") else userService.unfollowUser(personViewState.user.login ?: "")

            val text = if (isFollow) "关注" else "取关"

            if (response.isSuccessful) {
                personViewState = personViewState.copy(isFollow = if (isFollow) IsFollow.Followed else IsFollow.Unfollow)
                _viewEvents.trySend(BaseEvent.ShowMsg("$text 成功"))
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("$text 失败, ${response.code()}"))
            }
        }
    }

    private fun topOrRefresh() {
        _viewEvents.trySend(PersonEvent.TopOrRefresh)
    }

    private fun logout() {
        runBlocking {
            DataStoreUtils.clearSync()
            CommonUtils.clearCookies()
        }
    }
}

data class PersonViewState(
    val user: User,
    val isFollow: IsFollow = IsFollow.NotNeed
)

sealed class PersonAction {
    data class GetUser(val user: String) : PersonAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val user: String): PersonAction()
    object ChangeFollowState: PersonAction()
    object TopOrRefresh: PersonAction()
    object Logout: PersonAction()
}

sealed class PersonEvent: BaseEvent() {
    object TopOrRefresh: PersonEvent()
}

enum class IsFollow {
    NotNeed,
    Followed,
    Unfollow
}