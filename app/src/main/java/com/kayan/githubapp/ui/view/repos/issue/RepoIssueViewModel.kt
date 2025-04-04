package com.kayan.githubapp.ui.view.repos.issue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kayan.githubapp.common.config.AppConfig
import com.kayan.githubapp.common.route.Route
import com.kayan.githubapp.model.bean.RepoPermission
import com.kayan.githubapp.model.paging.RepoIssuePagingSource
import com.kayan.githubapp.model.ui.IssueUIModel
import com.kayan.githubapp.service.IssueService
import com.kayan.githubapp.service.SearchService
import com.kayan.githubapp.ui.common.BaseAction
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoIssueViewModel @Inject constructor(
    private val issueService: IssueService,
    private val searchService: SearchService,
): BaseViewModel() {

    private var queryParameter = QueryParameter()

    var viewStates by mutableStateOf(RepoIssueState())
        private set

    fun dispatch(action: RepoIssueAction) {
        when (action) {
            is RepoIssueAction.SetDate -> setData(userName = action.userName, repoName = action.repoName, repoPermission = action.repoPermission)
            is RepoIssueAction.ChangeState -> changeState(action.newState)
            is RepoIssueAction.Search -> search(action.q)

            is RepoIssueAction.ShowMsg -> {
                _viewEvents.trySend(BaseEvent.ShowMsg(action.msg))
            }

            is RepoIssueAction.GoIssueDetail -> {
                _viewEvents.trySend(RepoIssueEvent.GoTo(
                    "${Route.ISSUE_DETAIL}/${action.repoName}/${action.userName}/${action.issueNumber}/${action.hasPermission}"
                ))
            }
        }
    }


    private fun setData(userName: String, repoName: String, repoPermission: RepoPermission?) {
        if (isInit) return

        viewModelScope.launch {
            viewStates = viewStates.copy(repoPermission = repoPermission)

            queryParameter = queryParameter.copy(userName = userName, repoName = repoName)

            val newFlow = Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                RepoIssuePagingSource(issueService, searchService, queryParameter) {
                    viewStates = viewStates.copy(cacheIssueList = null)
                    isInit = true
                }
            }.flow.cachedIn(viewModelScope)

            viewStates = viewStates.copy(issueFlow = newFlow)

        }
    }

    private fun changeState(newState: IssueState) {
        viewModelScope.launch {
            queryParameter = queryParameter.copy(state = newState)

            viewStates = viewStates.copy(currentState = newState)

            val newFlow = Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                RepoIssuePagingSource(issueService, searchService, queryParameter) {
                    viewStates = viewStates.copy(cacheIssueList = null)
                }
            }.flow.cachedIn(viewModelScope)

            viewStates = viewStates.copy(issueFlow = newFlow)
        }
    }

    private fun search(search: String) {
        viewModelScope.launch {
            queryParameter = queryParameter.copy(queryString = search)

            val newFlow = Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                RepoIssuePagingSource(issueService, searchService, queryParameter) {
                    viewStates = viewStates.copy(cacheIssueList = null)
                }
            }.flow.cachedIn(viewModelScope)

            viewStates = viewStates.copy(issueFlow = newFlow)
        }
    }
}

data class RepoIssueState(
    val issueFlow: Flow<PagingData<IssueUIModel>>? = null,
    val cacheIssueList: List<IssueUIModel>? = null,
    val currentState: IssueState = IssueState.All,
    val repoPermission: RepoPermission? = null
)

sealed class RepoIssueAction: BaseAction() {
    data class ChangeState(val newState: IssueState): RepoIssueAction()
    data class Search(val q: String): RepoIssueAction()
    data class SetDate(val userName: String, val repoName: String, val repoPermission: RepoPermission?): RepoIssueAction()
    data class ShowMsg(val msg: String): RepoIssueAction()
    data class GoIssueDetail(val userName: String, val repoName: String, val issueNumber: Int, val hasPermission: Boolean): RepoIssueAction()
}

sealed class RepoIssueEvent: BaseEvent() {
    data class GoTo(val path: String): RepoIssueEvent()
}

data class QueryParameter(
    val state: IssueState = IssueState.All,
    val queryString: String = "",
    val userName: String = "",
    val repoName: String = ""
)

enum class IssueState(val originalName: String, val showName: String) {
    All("all", "全部"),
    Open("open", "打开"),
    Close("closed", "关闭")
}