package com.smartcall.guard.di

import com.smartcall.guard.domain.usecase.EvaluateCallUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    // EvaluateCallUseCase is already provided via UseCaseModule
    // CallGuardScreeningService uses EntryPointAccessors for injection
}
