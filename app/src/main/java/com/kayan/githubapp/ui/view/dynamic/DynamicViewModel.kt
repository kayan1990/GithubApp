package com.kayan.githubapp.ui.view.dynamic

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
import com.kayan.githubapp.model.bean.User
import com.kayan.githubapp.model.paging.DynamicPagingSource
import com.kayan.githubapp.model.ui.EventUIModel
import com.kayan.githubapp.service.UserService
import com.kayan.githubapp.ui.common.BaseAction
import com.kayan.githubapp.ui.common.BaseEvent
import com.kayan.githubapp.ui.common.BaseViewModel
import com.kayan.githubapp.util.datastore.DataKey
import com.kayan.githubapp.util.datastore.DataStoreUtils
import com.kayan.githubapp.util.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class DynamicViewModel @Inject constructor(
    private val userService: UserService
) : BaseViewModel() {

    protected open val isGetUserEvent: Boolean = false

    protected val userInfo: User? = DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson()

    var viewStates by mutableStateOf(DynamicViewState())
        private set

    fun dispatch(action: DynamicViewAction) {
        when (action) {
            is DynamicViewAction.ShowMsg -> {
                _viewEvents.trySend(BaseEvent.ShowMsg(action.msg))
            }

            is DynamicViewAction.ClickItem -> {
                _viewEvents.trySend(DynamicViewEvent.Goto("${Route.REPO_DETAIL}/${action.eventUIModel.repositoryName}/${action.eventUIModel.owner}"))
            }

            is DynamicViewAction.SetData -> setData(action.userName)
            is DynamicViewAction.TopOrRefresh -> topOrRefresh()
        }
    }

    private fun setData(userName: String) {
        if (isInit) return
        viewModelScope.launch(exception) {
            val dynamicFlow =
                Pager(
                    PagingConfig(
                        pageSize = AppConfig.PAGE_SIZE,
                        initialLoadSize = AppConfig.PAGE_SIZE
                    )
                ) {
                    DynamicPagingSource(userService, userName, isGetUserEvent) {
                        viewStates = viewStates.copy(cacheList = null)
                        isInit = true
                    }
                }.flow.cachedIn(viewModelScope)
            viewStates = viewStates.copy(dynamicFlow = dynamicFlow)
        }
    }

    private fun topOrRefresh() {
        _viewEvents.trySend(DynamicViewEvent.TopOrRefresh)
    }
}

data class DynamicViewState(
    val dynamicFlow: Flow<PagingData<EventUIModel>>? = null,
    val cacheList: List<EventUIModel>? = null,
    val showChoosePushDialog: Boolean = false,
    val pushShaList: List<String> = listOf(),
    val pushShaDesList: List<String> = listOf()
)

sealed class DynamicViewEvent : BaseEvent() {
    object TopOrRefresh : DynamicViewEvent()
    data class Goto(val route: String) : DynamicViewEvent()
}

sealed class DynamicViewAction : BaseAction() {
    object TopOrRefresh : DynamicViewAction()
    data class ShowMsg(val msg: String) : DynamicViewAction()
    data class ClickItem(val eventUIModel: EventUIModel) : DynamicViewAction()
    data class SetData(val userName: String) : DynamicViewAction()
}