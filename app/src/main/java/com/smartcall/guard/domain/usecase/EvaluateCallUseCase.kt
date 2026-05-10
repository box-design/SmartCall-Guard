package com.smartcall.guard.domain.usecase

import com.smartcall.guard.data.entity.BlockReason
import com.smartcall.guard.data.entity.LocationRule
import com.smartcall.guard.data.entity.RuleType
import com.smartcall.guard.data.repository.BlockLogRepository
import com.smartcall.guard.data.repository.LocationRepository
import com.smartcall.guard.data.repository.RuleRepository
import com.smartcall.guard.data.repository.SegmentRepository
import com.smartcall.guard.data.repository.SettingsRepository
import com.smartcall.guard.utils.EmergencyNumberChecker
import com.smartcall.guard.utils.LocationHelper
import com.smartcall.guard.utils.NumberNormalizer
import com.smartcall.guard.utils.PatternCompiler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class EvaluateResult(
    val shouldBlock: Boolean,
    val reason: String = "",
    val blockReason: BlockReason? = null,
    val matchedRule: String? = null,
    val displayLocation: String? = null
)

@Singleton
class EvaluateCallUseCase @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val settingsRepository: SettingsRepository,
    private val segmentRepository: SegmentRepository,
    private val locationRepository: LocationRepository,
    private val blockLogRepository: BlockLogRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var compiledPatterns: List<PatternCompiler.CompiledPattern> = emptyList()

    fun precompilePatterns() {
        val allBlacklists = ruleRepository.getActiveBlacklistsSync()
        compiledPatterns = PatternCompiler.compile(allBlacklists)
    }

    fun executeSync(phoneNumber: String): EvaluateResult {
        val startTime = System.currentTimeMillis()
        val normalized = NumberNormalizer.normalize(phoneNumber)

        if (EmergencyNumberChecker.isEmergency(normalized)) {
            return EvaluateResult(shouldBlock = false, reason = "emergency")
        }

        fun checkTimeout(): Boolean {
            return System.currentTimeMillis() - startTime > 100
        }

        val settings = settingsRepository.getSettingsSync() ?: return EvaluateResult(shouldBlock = false)

        if (settings.whitelistContacts) {
        }

        val whitelists = ruleRepository.getActiveWhitelistsSync()
        for (wl in whitelists) {
            if (checkTimeout()) return EvaluateResult(shouldBlock = false)
            if (isRuleMatch(wl, normalized)) {
                return EvaluateResult(shouldBlock = false, reason = "whitelist")
            }
        }

        val blacklists = ruleRepository.getActiveBlacklistsSync()
        val regexBlacklists = blacklists.filter { it.type == RuleType.BLACKLIST_REGEX }
        val nonRegexBlacklists = blacklists.filter { it.type != RuleType.BLACKLIST_REGEX }

        for (bl in nonRegexBlacklists) {
            if (checkTimeout()) return EvaluateResult(shouldBlock = false)
            if (isRuleMatch(bl, normalized)) {
                return EvaluateResult(
                    shouldBlock = true,
                    reason = bl.note ?: "blacklist: ${bl.value}",
                    blockReason = BlockReason.BLACKLIST,
                    matchedRule = bl.value
                )
            }
        }

        val matched = PatternCompiler.match(compiledPatterns, normalized)
        if (matched != null) {
            return EvaluateResult(
                shouldBlock = true,
                reason = "regex: ${matched.originalValue}",
                blockReason = BlockReason.BLACKLIST,
                matchedRule = matched.originalValue
            )
        }

        if (checkTimeout()) return EvaluateResult(shouldBlock = false)
        if (settings.locationRule != LocationRule.OFF) {
            val segment = try {
                segmentRepository.lookup(normalized)
            } catch (e: Exception) { null }

            if (segment != null) {
                val displayLocation = "${segment.province}${segment.city}"
                
                when (settings.locationRule) {
                    LocationRule.SAME_CITY -> {
                        val currentCity = locationRepository.getCurrentCitySync()
                        if (currentCity != null && !LocationHelper.isSameCity(segment.city, currentCity)) {
                            return EvaluateResult(
                                shouldBlock = true,
                                reason = "location_mismatch:city",
                                blockReason = BlockReason.LOCATION_MISMATCH,
                                displayLocation = displayLocation
                            )
                        }
                    }
                    LocationRule.SAME_PROVINCE -> {
                        val currentProvince = locationRepository.getCurrentProvinceSync()
                        if (currentProvince != null && !LocationHelper.isSameProvince(segment.province, currentProvince)) {
                            return EvaluateResult(
                                shouldBlock = true,
                                reason = "location_mismatch:province",
                                blockReason = BlockReason.LOCATION_MISMATCH,
                                displayLocation = displayLocation
                            )
                        }
                    }
                    LocationRule.BLOCK_OVERSEAS -> {
                        if (segment.province.contains("海外") || segment.province.contains("国外")) {
                            return EvaluateResult(
                                shouldBlock = true,
                                reason = "overseas",
                                blockReason = BlockReason.LOCATION_MISMATCH,
                                displayLocation = displayLocation
                            )
                        }
                    }
                    LocationRule.RADIUS -> {
                        val currentProvince = locationRepository.getCurrentProvinceSync()
                        if (currentProvince != null && !LocationHelper.isSameProvince(segment.province, currentProvince)) {
                            return EvaluateResult(
                                shouldBlock = true,
                                reason = "radius_mismatch",
                                blockReason = BlockReason.LOCATION_MISMATCH,
                                displayLocation = displayLocation
                            )
                        }
                    }
                    LocationRule.OFF -> {}
                }
            } else {
                if (settings.blockUnknownSegment) {
                    return EvaluateResult(
                        shouldBlock = true,
                        reason = "unknown_segment",
                        blockReason = BlockReason.UNKNOWN_SEGMENT
                    )
                }
            }
        }

        if (checkTimeout()) return EvaluateResult(shouldBlock = false)
        return EvaluateResult(shouldBlock = false)
    }

    private fun isRuleMatch(rule: com.smartcall.guard.data.entity.RuleEntity, normalized: String): Boolean {
        return when (rule.type) {
            RuleType.BLACKLIST_EXACT, RuleType.WHITELIST -> {
                val ruleNormalized = NumberNormalizer.normalize(rule.value)
                ruleNormalized == normalized
            }
            RuleType.BLACKLIST_PREFIX -> {
                val ruleNormalized = NumberNormalizer.normalize(rule.value)
                normalized.startsWith(ruleNormalized)
            }
            else -> false
        }
    }

    fun logBlockedCall(phoneNumber: String, reason: String, matchedRule: String?, displayLocation: String?, blockReason: BlockReason?) {
        scope.launch {
            try {
                val log = com.smartcall.guard.data.entity.BlockLogEntity(
                    phoneNumber = phoneNumber,
                    normalizedNumber = NumberNormalizer.normalize(phoneNumber),
                    displayLocation = displayLocation,
                    matchedRule = matchedRule ?: reason,
                    blockReason = blockReason ?: BlockReason.BLACKLIST
                )
                blockLogRepository.insertLogSync(log)
            } catch (_: Exception) {}
        }
    }
}
