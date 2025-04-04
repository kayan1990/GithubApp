package com.kayan.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kayan.githubapp.common.net.PageInfo
import com.kayan.githubapp.model.conversion.EventConversion
import com.kayan.githubapp.model.ui.EventUIModel
import com.kayan.githubapp.service.NotificationService
import com.kayan.githubapp.util.fromJson
import retrofit2.HttpException

class NotifyPagingSource(
    private val notifyServer: NotificationService,
    private val all: Boolean?,
    private val participating: Boolean?
): PagingSource<Int, EventUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EventUIModel> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response =
                if (all == null || participating == null) notifyServer.getNotificationUnRead(true, nextPageNumber)
                else notifyServer.getNotification(true, all, participating, nextPageNumber)

            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            val uiEventModel = response.body()?.map { EventConversion.notificationToEventUIModel(it) }

            return LoadResult.Page(
                data = uiEventModel ?: listOf(),
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, EventUIModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}