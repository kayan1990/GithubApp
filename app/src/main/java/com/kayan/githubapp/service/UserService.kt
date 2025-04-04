package com.kayan.githubapp.service

import com.kayan.githubapp.common.config.AppConfig
import com.kayan.githubapp.model.bean.Event
import com.kayan.githubapp.model.bean.User
import com.kayan.githubapp.model.bean.UserInfoRequestModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*


interface UserService {

    @GET("user")
    suspend fun getPersonInfo(
            @Header("forceNetWork") forceNetWork: Boolean
    ):Response<User>

    @GET("users/{user}")
    suspend fun getUser(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String
    ):Response<User>

    @PATCH("user")
    suspend fun saveUserInfo(
            @Body body: UserInfoRequestModel
    ):Response<User>


    @GET("user/following/{user}")
    suspend fun checkFollowing(
            @Path("user") user: String
    ):Response<ResponseBody>

    /**
     * Check if one user follows another
     */
    @GET("users/{user}/following/{targetUser}")
    fun checkFollowing(
            @Path("user") user: String,
            @Path("targetUser") targetUser: String
    ):Response<ResponseBody>

    @PUT("user/following/{user}")
    suspend fun followUser(
            @Path("user") user: String
    ):Response<ResponseBody>

    @DELETE("user/following/{user}")
    suspend fun unfollowUser(
            @Path("user") user: String
    ):Response<ResponseBody>

    @GET("users/{user}/followers")
    suspend fun getFollowers(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<User>>

    @GET("users/{user}/following")
    suspend fun getFollowing(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<User>>

    /**
     * List events performed by a user
     */
    @GET("users/{user}/events")
    suspend fun getUserEvents(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Event>>

    /**
     * List events that a user has received
     */
    @GET("users/{user}/received_events")
    suspend fun getNewsEvent(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Event>>

    @GET("orgs/{org}/members")
    suspend fun getOrgMembers(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("org") org: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<User>>

    @GET("users/{user}/orgs")
    fun getUserOrgs(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String
    ):Response<ArrayList<User>>


}
