package com.smartcall.guard.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.smartcall.guard.domain.usecase.EvaluateCallUseCase
import com.smartcall.guard.utils.EmergencyNumberChecker
import com.smartcall.guard.utils.NumberNormalizer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class CallGuardScreeningService : CallScreeningService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CallGuardScreeningServiceEntryPoint {
        fun evaluateCallUseCase(): EvaluateCallUseCase
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: return
        val response = CallResponse.Builder()

        val normalized = NumberNormalizer.normalize(number)

        if (EmergencyNumberChecker.isEmergency(normalized)) {
            respondToCall(callDetails, response.setDisallowCall(false).build())
            return
        }

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            CallGuardScreeningServiceEntryPoint::class.java
        )
        val evaluateCallUseCase = entryPoint.evaluateCallUseCase()

        val result = try {
            evaluateCallUseCase.precompilePatterns()
            evaluateCallUseCase.executeSync(number)
        } catch (e: Exception) {
            respondToCall(callDetails, response.setDisallowCall(false).build())
            return
        }

        if (result.shouldBlock) {
            response
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(true)
                .setSkipCallLog(false)
        } else {
            response.setDisallowCall(false)
        }

        respondToCall(callDetails, response.build())

        if (result.shouldBlock) {
            evaluateCallUseCase.logBlockedCall(
                phoneNumber = number,
                reason = result.reason,
                matchedRule = result.matchedRule,
                displayLocation = result.displayLocation,
                blockReason = result.blockReason
            )
        }
    }
}
