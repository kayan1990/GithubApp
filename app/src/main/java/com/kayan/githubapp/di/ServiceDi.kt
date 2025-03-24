package com.kayan.githubapp.di

import com.kayan.githubapp.common.net.RetrofitFactory
import com.kayan.githubapp.service.IssueService
import com.kayan.githubapp.service.LoginService
import com.kayan.githubapp.service.NotificationService
import com.kayan.githubapp.service.RepoService
import com.kayan.githubapp.service.SearchService
import com.kayan.githubapp.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

//这里使用了SingletonComponent，因此 NetworkModule 绑定到 Application 的整个生命周期
@Module
@InstallIn(SingletonComponent::class)
object ServiceDi {

    @Singleton
    @Provides
    fun provideOkHttpClient() = RetrofitFactory.getOkhttpClient()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = RetrofitFactory.getRetrofit(okHttpClient)

    @Singleton
    @Provides
    fun provideRepoService(retrofit: Retrofit): RepoService  = retrofit.create(RepoService::class.java)

    @Singleton
    @Provides
    fun provideUserService(retrofit: Retrofit): UserService = retrofit.create(UserService::class.java)


    @Singleton
    @Provides
    fun provideIssueService(retrofit: Retrofit): IssueService = retrofit.create(IssueService::class.java)

    @Singleton
    @Provides
    fun provideLoginService(retrofit: Retrofit): LoginService = retrofit.create(LoginService::class.java)

    @Singleton
    @Provides
    fun provideNotificationService(retrofit: Retrofit): NotificationService = retrofit.create(NotificationService::class.java)

    @Singleton
    @Provides
    fun provideSearchService(retrofit: Retrofit): SearchService = retrofit.create(SearchService::class.java)
}