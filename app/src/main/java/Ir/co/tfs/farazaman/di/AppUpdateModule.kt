package Ir.co.tfs.farazaman.di

import Ir.co.tfs.farazaman.data.update.AndroidApkFileValidator
import Ir.co.tfs.farazaman.data.update.ApkFileValidator
import Ir.co.tfs.farazaman.data.update.AppUpdateService
import Ir.co.tfs.farazaman.data.update.OkHttpAppUpdateService
import Ir.co.tfs.farazaman.util.NetworkTimeouts
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppUpdateClient

@Module
@InstallIn(SingletonComponent::class)
abstract class AppUpdateBindingsModule {
    @Binds
    @Singleton
    abstract fun bindAppUpdateService(implementation: OkHttpAppUpdateService): AppUpdateService

    @Binds
    @Singleton
    abstract fun bindApkFileValidator(implementation: AndroidApkFileValidator): ApkFileValidator
}

@Module
@InstallIn(SingletonComponent::class)
object AppUpdateNetworkModule {
    @Provides
    @Singleton
    @AppUpdateClient
    fun provideAppUpdateClient(): OkHttpClient {
        // This public endpoint must never receive the municipality bearer token.
        return OkHttpClient.Builder()
            .connectTimeout(NetworkTimeouts.UPDATE_CONNECT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkTimeouts.UPDATE_READ_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkTimeouts.UPDATE_WRITE_SECONDS, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }
}
