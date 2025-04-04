package com.kayan.githubapp.ui.view.list.generalRepo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kayan.githubapp.common.config.AppConfig
import com.kayan.githubapp.model.paging.RepoPagingSource
import com.kayan.githubapp.model.ui.ReposUIModel
import com.kayan.githubapp.service.RepoService
import com.kayan.githubapp.ui.common.BaseAction
import com.kayan.githubapp.ui.common.BaseViewModel
import com.kayan.githubapp.ui.view.list.GeneralListEnum
import com.kayan.githubapp.ui.view.list.GeneralRepoListSort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralRepoListViewModel @Inject constructor(
    private val repoService: RepoService,
): BaseViewModel() {
    var viewStates by mutableStateOf(GeneralRepoListState())
        private set

    fun dispatch(action: GeneralRepoListAction) {
        when (action) {
            is GeneralRepoListAction.SetData -> setData(action.userName, action.repoName, action.requestType, action.sort, action.forceRequest)
        }
    }

    private fun setData(userName: String, repoName: String, requestType: GeneralListEnum, sort: GeneralRepoListSort?, forceRequest: Boolean) {
        if (isInit && !forceRequest) return

        viewModelScope.launch(exception) {
//            loadDataFromCache(userName, repoName, requestType, sort)

            val repoListFlow =
                Pager(
                    PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
                ) {
                    RepoPagingSource(repoService, userName, repoName, sort, requestType) {
                        viewStates = viewStates.copy(cacheRepoList = null)
                        isInit = true
                    }
                }.flow.cachedIn(viewModelScope)
            viewStates = viewStates.copy(repoListFlow = repoListFlow, sort = sort)
        }
    }

//    private suspend fun loadDataFromCache(userName: String, repoName: String, requestType: GeneralListEnum, sort: GeneralRepoListSort?) {
//        when (requestType) {
//            GeneralListEnum.UserRepository -> {
//                val cacheData = dataBase.cacheDB().queryUserRepos(userName, sort?.requestValue ?: "")
//                if (!cacheData.isNullOrEmpty()) {
//                    val body = cacheData[0].data?.fromJson<List<Repository>>()
//                    if (body != null) {
//                        Log.i("el", "refreshData: 使用缓存数据")
//                        viewStates = viewStates.copy(cacheRepoList = body.map { ReposConversion.reposToReposUIModel(it) })
//                    }
//                }
//            }
//            GeneralListEnum.UserStar -> {
//                val cacheData = dataBase.cacheDB().queryUserStared(userName, sort?.requestValue ?: "")
//                if (!cacheData.isNullOrEmpty()) {
//                    val body = cacheData[0].data?.fromJson<List<Repository>>()
//                    if (body != null) {
//                        Log.i("el", "refreshData: 使用缓存数据")
//                        viewStates = viewStates.copy(cacheRepoList = body.map { ReposConversion.reposToReposUIModel(it) })
//                    }
//                }
//            }
//            GeneralListEnum.RepositoryForkUser -> {
//                val cacheData = dataBase.cacheDB().queryRepositoryFork("$userName/$repoName")
//                if (!cacheData.isNullOrEmpty()) {
//                    val body = cacheData[0].data?.fromJson<List<Repository>>()
//                    if (body != null) {
//                        Log.i("el", "refreshData: 使用缓存数据")
//                        viewStates = viewStates.copy(cacheRepoList = body.map { ReposConversion.reposToReposUIModel(it) })
//                    }
//                }
//            }
//            else -> {  }
//        }
//    }
}

data class GeneralRepoListState(
    val repoListFlow: Flow<PagingData<ReposUIModel>>? = null,
    val cacheRepoList: List<ReposUIModel>? = null,
    val sort: GeneralRepoListSort? = null
)

sealed class GeneralRepoListAction: BaseAction() {
    data class SetData(val userName: String, val repoName: String, val requestType: GeneralListEnum, val sort: GeneralRepoListSort?, val forceRequest: Boolean): GeneralRepoListAction()
}