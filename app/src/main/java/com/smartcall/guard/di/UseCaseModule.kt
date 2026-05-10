package com.smartcall.guard.di

import android.content.ContentResolver
import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

}
