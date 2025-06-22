package com.example.gameoflive.di

import android.content.Context
import com.example.gameoflive.data.local.LocalSimulationDataSource
import com.example.gameoflive.data.local.LocalSimulationDataSourceImpl
import com.example.gameoflive.data.remote.RemoteSimulationDataSource
import com.example.gameoflive.data.remote.RemoteSimulationDataSourceStub
import com.example.gameoflive.data.repository.SimulationRepository
import com.example.gameoflive.data.repository.SimulationRepositoryImpl
import com.example.gameoflive.save.SaveManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideLocalSimulationDataSource(
        saveManager: SaveManager
    ): LocalSimulationDataSource {
        return LocalSimulationDataSourceImpl(saveManager)
    }

    @Provides
    @Singleton
    fun provideRemoteSimulationDataSource(): RemoteSimulationDataSource {
        return RemoteSimulationDataSourceStub()
    }

    @Provides
    @Singleton
    fun provideSimulationRepository(
        localDataSource: LocalSimulationDataSource,
        remoteDataSource: RemoteSimulationDataSource
    ): SimulationRepository {
        return SimulationRepositoryImpl(localDataSource, remoteDataSource)
    }
} 