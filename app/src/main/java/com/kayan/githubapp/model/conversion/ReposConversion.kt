package com.kayan.githubapp.model.conversion

import com.kayan.githubapp.common.utlis.CommonUtils
import com.kayan.githubapp.model.bean.Repository
import com.kayan.githubapp.model.bean.TrendingRepoModel
import com.kayan.githubapp.model.ui.ReposUIModel


/**
 * 仓库相关实体转换
 * Created by guoshuyu
 * Date: 2018-10-29
 */
object ReposConversion {

    fun trendToReposUIModel(trendModel: TrendingRepoModel): ReposUIModel {
        val reposUIModel = ReposUIModel()
        reposUIModel.hideWatchIcon = true
        reposUIModel.ownerName = trendModel.name
        reposUIModel.ownerPic = trendModel.contributors[0]
        reposUIModel.repositoryDes = trendModel.description
        reposUIModel.repositoryName = trendModel.reposName
        reposUIModel.repositoryFork = trendModel.forkCount
        reposUIModel.repositoryStar = trendModel.starCount
        reposUIModel.repositoryWatch = trendModel.meta
        reposUIModel.repositoryType = trendModel.language
        return reposUIModel
    }

    fun reposToReposUIModel(repository: Repository?): ReposUIModel {
        val reposUIModel = ReposUIModel()
        reposUIModel.hideWatchIcon = true
        reposUIModel.ownerName = repository?.owner?.login ?: ""
        reposUIModel.ownerPic = repository?.owner?.avatarUrl ?: ""
        reposUIModel.repositoryDes = repository?.description ?: ""
        reposUIModel.repositoryName = repository?.name ?: ""
        reposUIModel.repositoryFork = repository?.forksCount?.toString() ?: ""
        reposUIModel.repositoryStar = repository?.stargazersCount?.toString() ?: ""
        reposUIModel.repositoryWatch = repository?.subscribersCount?.toString() ?: ""
        reposUIModel.repositoryType = repository?.language ?: ""
        reposUIModel.repositorySize = (((repository?.size
                ?: 0) / 1024.0)).toString().substring(0, 3) + "M"
        reposUIModel.repositoryLicense = repository?.license?.name ?: ""


        val createStr = if (repository != null && repository.fork)
            "Forked from" + (repository.parent?.name ?: "")
        else
            "创建于" + CommonUtils.getDateStr(repository?.createdAt)

        reposUIModel.repositoryAction = createStr
        reposUIModel.repositoryIssue = repository?.openIssuesCount?.toString() ?: ""

        reposUIModel.repositoryTopics = repository?.topics ?: listOf()
        reposUIModel.repositoryLastUpdateTime = CommonUtils.getDateStr(repository?.pushedAt)
        reposUIModel.defaultBranch = repository?.defaultBranch
        reposUIModel.permission = repository?.permission
        return reposUIModel
    }





}