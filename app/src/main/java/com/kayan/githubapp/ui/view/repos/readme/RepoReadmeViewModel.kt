package com.kayan.githubapp.ui.view.repos.readme

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayan.githubapp.common.utlis.browse
import com.kayan.githubapp.common.utlis.formatReadme
import com.kayan.githubapp.service.RepoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoReadmeViewModel @Inject constructor(
    private val repoService: RepoService
) : ViewModel() {
    var viewStates by mutableStateOf(RepoReadmeState())
        private set

    private val _viewEvents = Channel<RepoReadMeEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("RVM", "Request Error: ", throwable)
            _viewEvents.send(RepoReadMeEvent.ShowMsg("错误：" + throwable.message))
        }
    }

    fun dispatch(action: RepoReadMeAction) {
        when (action) {
            is RepoReadMeAction.GetReadmeContent -> getReadmeContent(
                action.repoName,
                action.ownerName,
                action.branch
            )

            is RepoReadMeAction.OnClickLink -> onClickLink(action.context, action.link)
        }
    }

    private fun onClickLink(context: Context, link: String) {
        //TODO 这里点击链接时应该判断一下，如果是 GitHub 链接就在本地使用 API 打开而不是直接使用浏览器打开
        // 包括仓库、 issue、 release、仓库文件 等

        println("click link $link")
        if (!context.browse(link)) {
            viewModelScope.launch {
                _viewEvents.send(RepoReadMeEvent.ShowMsg("打开失败： $link"))
            }
        }
        return

    }

    private fun getReadmeContent(repoName: String, ownerName: String, branch: String?) {
        viewModelScope.launch(exception) {
            val response = repoService.getReadmeHtml(true, ownerName, repoName, branch = branch)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(RepoReadMeEvent.ShowMsg("body is null!"))
                } else {
                    // val fullPath = body.downloadUrl?.substring(0, body.downloadUrl.lastIndexOf('/')) ?: ""
                    var fullPath = "https://raw.githubusercontent.com/$ownerName/$repoName"
                    if (!branch.isNullOrBlank()) {
                        fullPath += "/$branch"
                    }

                    val mdContent = body.formatReadme(fullPath)

                    viewStates = viewStates.copy(
                        readmeContent = mdContent
                    )
                }
            } else {
                if (response.code() == 404) {
                    viewStates = viewStates.copy(
                        readmeContent = "该仓库没有 README"
                    )
                } else {
                    _viewEvents.trySend(
                        RepoReadMeEvent.ShowMsg(
                            "获取失败：${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            }
        }
    }
}

data class RepoReadmeState(
    val readmeContent: String = "Loading..."
)

sealed class RepoReadMeAction {
    data class GetReadmeContent(val repoName: String, val ownerName: String, val branch: String?) :
        RepoReadMeAction()

    data class OnClickLink(val context: Context, val link: String) : RepoReadMeAction()
}

sealed class RepoReadMeEvent {
    data class ShowMsg(val msg: String) : RepoReadMeEvent()
}