package com.kayan.githubapp.ui.view.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kayan.githubapp.common.config.AppConfig
import com.kayan.githubapp.common.constant.LanguageFilter
import com.kayan.githubapp.common.constant.OrderFilter
import com.kayan.githubapp.common.constant.TypeFilter
import com.kayan.githubapp.model.paging.SearchPagingSource
import com.kayan.githubapp.service.SearchService
import com.kayan.githubapp.ui.common.BaseAction
import com.kayan.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchService: SearchService
): BaseViewModel() {
    var viewStates by mutableStateOf(SearchState())
        private set

    fun dispatch(action: SearchAction) {
        when (action) {
            is SearchAction.OnUpdateFilter -> onUpdateFilter(action.typeFilter, action.orderFilter, action.languageFilter)
            is SearchAction.OnSearch -> onSearch(action.query)
        }
    }

    private fun onUpdateFilter(typeFilter: TypeFilter, orderFilter: OrderFilter, languageFilter: LanguageFilter) {
        val resultListFlow = Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                SearchPagingSource(
                    searchService,
                    viewStates.searchQuery,
                    orderFilter.requestValue,
                    typeFilter.requestValue,
                    languageFilter.requestValue
                )
            }.flow.cachedIn(viewModelScope)

        viewStates = viewStates.copy(
            resultListFlow = resultListFlow,
            typeFilter = typeFilter,
            orderFilter = orderFilter,
            languageFilter = languageFilter
        )
    }

    private fun onSearch(query: String) {
        val resultListFlow =
            Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                SearchPagingSource(
                    searchService,
                    query,
                    viewStates.orderFilter.requestValue,
                    viewStates.typeFilter.requestValue,
                    viewStates.languageFilter.requestValue
                )
            }.flow.cachedIn(viewModelScope)

        viewStates = viewStates.copy(
            resultListFlow = resultListFlow,
            searchQuery = query
        )
    }

}

data class SearchState(
    val resultListFlow: Flow<PagingData<Any>>? = null,
    val typeFilter: TypeFilter = TypeFilter.BestMatch,
    val orderFilter: OrderFilter = OrderFilter.DESC,
    val languageFilter: LanguageFilter = LanguageFilter.All,
    val searchQuery: String = ""
)

sealed class SearchAction: BaseAction() {
    data class OnSearch(val query: String): SearchAction()
    data class OnUpdateFilter(val typeFilter: TypeFilter, val orderFilter: OrderFilter, val languageFilter: LanguageFilter): SearchAction()
}


val searchTypeFilter = TypeFilter.values().map { it.showName }

val searchOrderFilter = OrderFilter.values().map { it.showName }

val searchLanguageFilter = LanguageFilter.values().map { it.showName }
