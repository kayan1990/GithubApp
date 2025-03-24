package com.kayan.githubapp.ui.view.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.kayan.githubapp.common.utlis.CommonUtils
import com.kayan.githubapp.common.utlis.compareVersion
import com.kayan.githubapp.common.utlis.getVersionName
import com.kayan.githubapp.common.utlis.imgCachePath
import com.kayan.githubapp.model.bean.Issue
import com.kayan.githubapp.service.IssueService
import com.kayan.githubapp.service.RepoService
import com.kayan.githubapp.ui.common.BaseAction
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.BaseViewModel
import com.kayan.githubapp.util.datastore.DataStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val issueService: IssueService,
    private val repoService: RepoService
) : BaseViewModel() {

    var viewStates by mutableStateOf(MainViewState())
        private set

    fun dispatch(action: MainViewAction) {
        when (action) {
            is MainViewAction.ScrollTo -> scrollTo(action.pager)
            is MainViewAction.ChangeGesturesEnabled -> changeGesturesEnabled(action.enable)
            is MainViewAction.PostFeedBack -> postFeedBack(action.content)
            is MainViewAction.Logout -> logout()
            is MainViewAction.CheckUpdate -> checkUpdate(action.showTip, action.forceRequest, action.context)
            is MainViewAction.ClearCache -> clearCache(action.context)
        }
    }

    private fun checkUpdate(showTip: Boolean, forceRequest: Boolean, context: Context) {
        if (isInit && !forceRequest) return

        isInit = true

        viewModelScope.launch(exception) {
            val response = repoService.getReleasesNotHtml(true, "equationl", "githubAppByCompose", 1)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(BaseEvent.ShowMsg("检查失败：返回为空"))
                }
                else {
                    if (body.size > 0) {
                        val item = body[0]
                        val versionName = item.name
                        versionName?.apply {
                            val currentName = context.getVersionName()
                            if (currentName.compareVersion(versionName)) {
                                // 有更新
                                _viewEvents.trySend(MainViewEvent.HadUpdate(item.body ?: "新版本"))
                            }
                            else {
                                if (showTip) _viewEvents.trySend(BaseEvent.ShowMsg("已经是最新版本"))
                            }
                        }
                    } else {
                        if (showTip) _viewEvents.trySend(BaseEvent.ShowMsg("已经是最新版本"))
                    }
                }
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("检查失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun changeGesturesEnabled(enable: Boolean) {
        viewStates = viewStates.copy(gesturesEnabled = enable)
    }

    private fun scrollTo(pager: MainPager) {
        viewStates = viewStates.copy(
            currentPage = pager,
            title = "GithubApp",
        )
    }

    private fun postFeedBack(content: String) {
        val issue = Issue()
        issue.title = "FeedBack From App"
        issue.body = content

        viewModelScope.launch(exception) {
            val response = issueService.createIssue("equationl", "githubAppByCompose", issue)
            if (response.isSuccessful) {
                _viewEvents.trySend(BaseEvent.ShowMsg("已发送"))
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("发送失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun logout() {
        runBlocking {
            DataStoreUtils.clearSync()
            CommonUtils.clearCookies()
        }
    }

    private fun clearCache(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                imgCachePath(context).deleteRecursively()
                _viewEvents.trySend(BaseEvent.ShowMsg("清除缓存成功！"))
            }
        }
    }
}

data class MainViewState(
    val title: String = "GithubApp",
    val currentPage: MainPager = MainPager.HOME_RECOMMEND,
    val gesturesEnabled: Boolean = true
)

sealed class MainViewEvent: BaseEvent() {
    data class Goto(val route: String): MainViewEvent()
    data class HadUpdate(val Content: String): MainViewEvent()
}

sealed class MainViewAction: BaseAction() {
    object Logout: MainViewAction()
    data class ClearCache(val context: Context): MainViewAction()
    data class ScrollTo(val pager: MainPager): MainViewAction()
    data class ChangeGesturesEnabled(val enable: Boolean): MainViewAction()
    data class PostFeedBack(val content: String): MainViewAction()
    data class CheckUpdate(val showTip: Boolean, val forceRequest: Boolean, val context: Context): MainViewAction()
}

enum class MainPager {
    HOME_RECOMMEND,
    HOME_MY
}