package com.example.gameoflive.di

import com.example.gameoflive.data.repository.SimulationRepository
import com.example.gameoflive.domain.usecase.LoadSimulationUseCase
import com.example.gameoflive.domain.usecase.SaveSimulationUseCase
import com.example.gameoflive.domain.usecase.SpawnOrganismUseCase
import com.example.gameoflive.domain.usecase.TickSimulationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideSaveSimulationUseCase(
        repository: SimulationRepository
    ): SaveSimulationUseCase {
        return SaveSimulationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLoadSimulationUseCase(
        repository: SimulationRepository
    ): LoadSimulationUseCase {
        return LoadSimulationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideTickSimulationUseCase(): TickSimulationUseCase {
        return TickSimulationUseCase()
    }

    @Provides
    @Singleton
    fun provideSpawnOrganismUseCase(): SpawnOrganismUseCase {
        return SpawnOrganismUseCase()
    }
} 