package com.smartcall.guard.di

import android.content.ContentResolver
import com.smartcall.guard.data.repository.BlockLogRepository
import com.smartcall.guard.data.repository.LocationRepository
import com.smartcall.guard.data.repository.RuleRepository
import com.smartcall.guard.data.repository.SegmentRepository
import com.smartcall.guard.data.repository.SettingsRepository
import com.smartcall.guard.domain.usecase.EvaluateCallUseCase
import com.smartcall.guard.domain.usecase.ImportContactsUseCase
import com.smartcall.guard.domain.usecase.ImportExportUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideEvaluateCallUseCase(
        ruleRepository: RuleRepository,
        settingsRepository: SettingsRepository,
        segmentRepository: SegmentRepository,
        locationRepository: LocationRepository,
        blockLogRepository: BlockLogRepository
    ): EvaluateCallUseCase = EvaluateCallUseCase(
        ruleRepository,
        settingsRepository,
        segmentRepository,
        locationRepository,
        blockLogRepository
    )

    @Provides
    @Singleton
    fun provideImportContactsUseCase(
        contentResolver: ContentResolver,
        ruleRepository: RuleRepository
    ): ImportContactsUseCase = ImportContactsUseCase(contentResolver, ruleRepository)

    @Provides
    @Singleton
    fun provideImportExportUseCase(
        ruleRepository: RuleRepository
    ): ImportExportUseCase = ImportExportUseCase(ruleRepository)
}
