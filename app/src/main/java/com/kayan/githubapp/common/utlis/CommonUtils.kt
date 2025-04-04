package com.kayan.githubapp.common.utlis

import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.compose.ui.graphics.Color
import com.kayan.githubapp.common.config.AppConfig
import com.kayan.githubapp.common.constant.LocalCache
import com.kayan.githubapp.common.net.PageInfo
import com.kayan.githubapp.model.bean.User
import com.kayan.githubapp.service.RepoService
import com.kayan.githubapp.util.Utils.toHexString
import com.kayan.githubapp.util.fromJson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


/**
 * 通用工具类
 */
object CommonUtils {

    private const val MILLIS_LIMIT = 1000.0

    private const val SECONDS_LIMIT = 60 * MILLIS_LIMIT

    private const val MINUTES_LIMIT = 60 * SECONDS_LIMIT

    private const val HOURS_LIMIT = 24 * MINUTES_LIMIT

    private const val DAYS_LIMIT = 30 * HOURS_LIMIT


    fun getDateStr(date: Date?): String {
        if (date?.toString() == null) {
            return ""
        } else if (date.toString().length < 10) {
            return date.toString()
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date).substring(0, 10)
    }

    /**
     * 获取时间格式化
     */
    fun getNewsTimeStr(date: Date?): String {
        if (date == null) {
            return ""
        }
        val subTime = Date().time - date.time
        return when {
            subTime < MILLIS_LIMIT -> "刚刚"
            subTime < SECONDS_LIMIT -> (subTime / MILLIS_LIMIT).roundToInt().toString() + " " + "秒前"
            subTime < MINUTES_LIMIT -> (subTime / SECONDS_LIMIT).roundToInt().toString() + " " + "分钟前"
            subTime < HOURS_LIMIT -> (subTime / MINUTES_LIMIT).roundToInt().toString() + " " + "小时前"
            subTime < DAYS_LIMIT -> (subTime / HOURS_LIMIT).roundToInt().toString() + " " + "天前"
            else -> getDateStr(date)
        }
    }


    fun getReposHtmlUrl(userName: String, reposName: String): String =
            AppConfig.GITHUB_BASE_URL + userName + "/" + reposName

    fun getIssueHtmlUrl(userName: String, reposName: String, number: String): String =
            AppConfig.GITHUB_BASE_URL + userName + "/" + reposName + "/issues/" + number

    fun getUserHtmlUrl(userName: String) =
            AppConfig.GITHUB_BASE_URL + userName

    fun getFileHtmlUrl(userName: String, reposName: String, path: String, branch: String = "master"): String =
            AppConfig.GITHUB_BASE_URL + userName + "/" + reposName + "/blob/" + branch + if (path.startsWith("/") || path.isBlank()) path else "/$path"

    fun getCommitHtmlUrl(userName: String, reposName: String, sha: String): String =
            AppConfig.GITHUB_BASE_URL + userName + "/" + reposName + "/commit/" + sha

    fun getReleaseHtmlUrl(userName: String, reposName: String): String =
        AppConfig.GITHUB_BASE_URL + userName + "/" + reposName + "/releases/"

    fun getFileType(path: String): FileType {
        val fileExtension = File(path).extension.lowercase()

        if (fileExtension in FileType.Img.type) return FileType.Img
        if (fileExtension in FileType.Md.type) return FileType.Md

        return FileType.Other
    }

    /**
     * 获取用户贡献图 URL
     * */
    fun getUserChartAddress(name: String, color: Color): String {
        return "${AppConfig.GRAPHIC_HOST}${color.toHexString}/$name"
    }

    /**
     *  获取用户的 star 数量，并更新到 user 中
     * */
    suspend fun updateStar(user: User, repoService: RepoService): User {
        val newUser = user.copy()
        val startResponse = repoService.getStarredRepos(true, user.login ?: "", 1, "updated", 1)
        val honorResponse = repoService.getUserRepository100StatusDao(true, user.login ?: "", 1)
        val starCount = startResponse.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1
        if (starCount != -1) {
            newUser.starRepos = starCount
        }

        if (honorResponse.isSuccessful) {
            val list = honorResponse.body()
            LocalCache.UserHonorCacheList = list
            var count = 0
            list?.forEach {
                count += it.watchersCount
            }
            newUser.honorRepos = count
        }

        return newUser
    }

    fun clearCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().flush()
    }


    enum class FileType(
        val type: List<String>
    ) {
        Img(listOf("png", "jpg", "jpeg", "gif", "svg")),
        Md(listOf("md")),
        Other(listOf())
    }
}